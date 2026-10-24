package ma.bosaj.updateradar.helper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ma.bosaj.updateradar.MainActivity
import ma.bosaj.updateradar.R
import ma.bosaj.updateradar.data.RadarRepository
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.util.StoreLinks

/** One step of a run: open this store page and press its update button if there is one. */
data class HelperTask(
    /** null = Play Store's Updates overview, where "Update all" is pressed. */
    val pkg: String?,
    val label: String,
    /** The store that shows this app's page (already routed: Galaxy Store or Play Store). */
    val store: Store,
)

data class HelperProgress(
    val running: Boolean = false,
    val total: Int = 0,
    val done: Int = 0,
    val current: String = "",
    val updated: Int = 0,
    val upToDate: Int = 0,
    val unknown: Int = 0,
    val playUpdateAll: Boolean = false,
    /** Set when a run ends, so the app can show the summary once. */
    val finishedAt: Long = 0L,
    val cancelled: Boolean = false,
)

/**
 * One-tap updates. A deterministic helper that, during a run the user starts, opens each store page
 * and presses the store's own Update (or "Update all") button. It reacts only to Play Store and
 * Galaxy Store windows (see res/xml/store_helper_service.xml), ignores every event when no run is
 * active, reads only button labels, and has no internet permission to send anything with.
 */
class StoreHelperService : AccessibilityService() {

    companion object {
        /** The bound service, or null while the user has not turned it on. */
        @Volatile
        var instance: StoreHelperService? = null
            private set

        private val _progress = MutableStateFlow(HelperProgress())
        val progress: StateFlow<HelperProgress> = _progress.asStateFlow()

        /** How long to wait for a store page before moving on. */
        private const val TASK_TIMEOUT_MS = 12_000L

        /** Pause after pressing Update, so the store registers the tap before the next page opens. */
        private const val AFTER_TAP_MS = 1_400L

        fun isEnabled(context: Context): Boolean {
            val am = context.getSystemService(AccessibilityManager::class.java) ?: return false
            val me = ComponentName(context, StoreHelperService::class.java)
            return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { it.resolveInfo?.serviceInfo?.let { s -> ComponentName(s.packageName, s.name) } == me }
        }

        /** Android's screen for turning the helper on or off. */
        fun settingsIntents(context: Context): List<Intent> = listOf(
            Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS")
                .putExtra(Intent.EXTRA_COMPONENT_NAME, ComponentName(context, StoreHelperService::class.java).flattenToString()),
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
        ).map { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        fun acknowledge() = _progress.update { it.copy(finishedAt = 0L) }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var tasks: List<HelperTask> = emptyList()
    private var index = 0
    private var acted = false
    private val currentApps = mutableSetOf<String>()
    private var overlay: TextView? = null
    private var overlayRoot: View? = null

    private val inspectRunnable = Runnable { inspect() }
    private val timeoutRunnable = Runnable { next(Outcome.UNKNOWN) }

    private enum class Outcome { UPDATED, CURRENT, UNKNOWN }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (_progress.value.running) finish(cancelled = true)
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        handler.removeCallbacksAndMessages(null)
        removeOverlay()
        super.onDestroy()
    }

    override fun onInterrupt() = Unit

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Outside a run every event is ignored: nothing is read.
        if (!_progress.value.running || acted) return
        if (event?.packageName?.toString() != expectedPackage()) return
        schedule(450)
    }

    /** Starts a run. Returns false if one is already running. */
    fun start(list: List<HelperTask>): Boolean {
        if (_progress.value.running || list.isEmpty()) return false
        tasks = list
        index = 0
        currentApps.clear()
        _progress.value = HelperProgress(running = true, total = list.size)
        showOverlay()
        open()
        return true
    }

    fun stop() {
        if (_progress.value.running) finish(cancelled = true)
    }

    private fun expectedPackage(): String? = when (tasks.getOrNull(index)?.store) {
        Store.GALAXY -> StorePage.GALAXY
        null -> null
        else -> StorePage.PLAY
    }

    private fun open() {
        val t = tasks.getOrNull(index) ?: return finish(cancelled = false)
        acted = false
        _progress.update { it.copy(done = index, current = t.label) }
        updateOverlay()
        val opened = if (t.pkg == null) StoreLinks.openUpdatesPage(this, Store.PLAY)
        else StoreLinks.launchFirst(this, StoreLinks.pageIntents(t.pkg, t.store).map { it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) })
        if (!opened) return next(Outcome.UNKNOWN)
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, TASK_TIMEOUT_MS)
        schedule(1_200)
    }

    private fun schedule(delay: Long) {
        handler.removeCallbacks(inspectRunnable)
        handler.postDelayed(inspectRunnable, delay)
    }

    private fun inspect() {
        if (!_progress.value.running || acted) return
        val root = rootInActiveWindow
        if (root == null || root.packageName?.toString() != expectedPackage()) return schedule(600)
        val infos = ArrayList<AccessibilityNodeInfo>()
        collect(root, infos, depth = 0)
        val nodes = infos.map { PageNode(it.text?.toString() ?: it.contentDescription?.toString(), it.viewIdResourceName) }
        val task = tasks[index]
        when (val state = StorePage.classify(nodes, wantUpdateAll = task.pkg == null)) {
            PageState.UPDATE_ALL, PageState.UPDATE -> {
                val i = StorePage.target(nodes, state)
                if (i >= 0 && press(infos[i])) {
                    acted = true
                    if (task.pkg == null) _progress.update { it.copy(playUpdateAll = true) }
                    handler.postDelayed({ next(Outcome.UPDATED) }, AFTER_TAP_MS)
                } else {
                    schedule(700)
                }
            }
            PageState.CURRENT -> next(Outcome.CURRENT)
            PageState.LOADING -> schedule(700)
        }
    }

    private fun collect(n: AccessibilityNodeInfo, out: MutableList<AccessibilityNodeInfo>, depth: Int) {
        if (depth > 40 || out.size > 600) return
        out += n
        for (i in 0 until n.childCount) n.getChild(i)?.let { collect(it, out, depth + 1) }
    }

    /** Clicks the nearest clickable node, or taps the centre of the label if no node accepts a click. */
    private fun press(node: AccessibilityNodeInfo): Boolean {
        var n: AccessibilityNodeInfo? = node
        while (n != null) {
            if (n.isClickable && n.isEnabled) return n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            n = n.parent
        }
        val r = Rect().also(node::getBoundsInScreen)
        if (r.isEmpty) return false
        val path = Path().apply { moveTo(r.exactCenterX(), r.exactCenterY()) }
        return dispatchGesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 60)).build(), null, null)
    }

    private fun next(outcome: Outcome) {
        if (!_progress.value.running) return
        handler.removeCallbacks(timeoutRunnable)
        handler.removeCallbacks(inspectRunnable)
        val t = tasks.getOrNull(index)
        if (outcome == Outcome.CURRENT) t?.pkg?.let(currentApps::add)
        _progress.update {
            when (outcome) {
                Outcome.UPDATED -> if (t?.pkg == null) it else it.copy(updated = it.updated + 1)
                Outcome.CURRENT -> it.copy(upToDate = it.upToDate + 1)
                Outcome.UNKNOWN -> it.copy(unknown = it.unknown + 1)
            }
        }
        index++
        if (index >= tasks.size) finish(cancelled = false) else handler.postDelayed({ open() }, 250)
    }

    private fun finish(cancelled: Boolean) {
        handler.removeCallbacksAndMessages(null)
        val verified = currentApps.toSet()
        // Remember which apps the store showed as up to date, so they leave the stale list.
        Thread { runCatching { RadarRepository(applicationContext).markCurrent(verified) } }.start()
        _progress.update { it.copy(running = false, done = index.coerceAtMost(tasks.size), finishedAt = System.currentTimeMillis(), cancelled = cancelled) }
        removeOverlay()
        runCatching {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
        }
    }

    // ---- floating progress pill with Stop ----

    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun showOverlay() {
        if (overlayRoot != null) return
        val wm = getSystemService(WindowManager::class.java) ?: return
        val label = TextView(this).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        val stop = TextView(this).apply {
            text = getString(R.string.stop)
            setTextColor(Color.parseColor("#00201A"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(dp(14), dp(6), dp(14), dp(6))
            background = GradientDrawable().apply { cornerRadius = dp(20).toFloat(); setColor(Color.parseColor("#3BE3C4")) }
            setOnClickListener { stop() }
            contentDescription = getString(R.string.stop)
        }
        val pill = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(8), dp(8), dp(8))
            background = GradientDrawable().apply { cornerRadius = dp(28).toFloat(); setColor(Color.parseColor("#E6101828")) }
            addView(label)
            addView(View(this@StoreHelperService), LinearLayout.LayoutParams(dp(12), 1))
            addView(stop)
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = dp(96)
        }
        runCatching { wm.addView(pill, lp) }.onSuccess {
            overlayRoot = pill
            overlay = label
        }
    }

    private fun updateOverlay() {
        val p = _progress.value
        overlay?.text = getString(R.string.helper_overlay, (p.done + 1).coerceAtMost(p.total), p.total)
    }

    private fun removeOverlay() {
        val v = overlayRoot ?: return
        runCatching { getSystemService(WindowManager::class.java)?.removeView(v) }
        overlayRoot = null
        overlay = null
    }
}
