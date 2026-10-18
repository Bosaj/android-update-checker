package ma.bosaj.updateradar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ma.bosaj.updateradar.data.AppChange
import ma.bosaj.updateradar.data.InstalledApp
import ma.bosaj.updateradar.data.Radar
import ma.bosaj.updateradar.data.RadarRepository
import ma.bosaj.updateradar.data.RadarSettings
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.work.Reminders

enum class Screen { HOME, REVIEW, SETTINGS }
enum class Tab { APPS, UPDATES }

data class UiState(
    val loading: Boolean = true,
    val apps: List<InstalledApp> = emptyList(),
    val feed: List<AppChange> = emptyList(),
    val filter: Radar.Filter = Radar.Filter(),
    val sort: Radar.Sort = Radar.Sort.STALEST,
    val tab: Tab = Tab.APPS,
    val screen: Screen = Screen.HOME,
    val selected: InstalledApp? = null,
    val galaxyInstalled: Boolean = false,
    val lastChanges: Int = 0,
    val reviewQueue: List<String> = emptyList(),
    val reviewIndex: Int = 0,
    val reviewDone: Set<String> = emptySet(),
)

class RadarViewModel(app: Application) : AndroidViewModel(app) {
    val repo = RadarRepository(app)
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()
    val settings: StateFlow<RadarSettings> = repo.settings.flow.stateIn(viewModelScope, SharingStarted.Eagerly, RadarSettings())

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        val r = repo.scan()
        _ui.update {
            it.copy(
                loading = false,
                apps = r.apps,
                feed = repo.feed(),
                galaxyInstalled = repo.galaxyStoreInstalled(),
                lastChanges = r.newChanges.size,
                selected = it.selected?.let { s -> r.apps.firstOrNull { a -> a.packageName == s.packageName } },
            )
        }
        Reminders.schedule(getApplication(), repo.settings.current())
    }

    fun query(q: String) = _ui.update { it.copy(filter = it.filter.copy(query = q)) }
    fun store(s: Store?) = _ui.update { it.copy(filter = it.filter.copy(store = s)) }
    fun onlyStale(on: Boolean) = _ui.update { it.copy(filter = it.filter.copy(onlyStale = on)) }
    fun sort(s: Radar.Sort) = _ui.update { it.copy(sort = s) }
    fun tab(t: Tab) = _ui.update { it.copy(tab = t) }
    fun select(a: InstalledApp?) = _ui.update { it.copy(selected = a) }
    fun go(s: Screen) = _ui.update { it.copy(screen = s) }

    fun ignore(pkg: String) = viewModelScope.launch { repo.settings.toggleIgnored(pkg) }

    fun clearFeed() {
        repo.clearFeed()
        _ui.update { it.copy(feed = emptyList(), lastChanges = 0) }
    }

    /** Start reviewing the stale apps, stalest first. */
    fun startReview() {
        val s = settings.value
        val now = System.currentTimeMillis()
        val queue = Radar.apply(_ui.value.apps, Radar.Filter(onlyStale = true), Radar.Sort.STALEST, now, s.agingAfterDays, s.staleAfterDays, s.ignored)
            .map { it.packageName }
        _ui.update { it.copy(screen = Screen.REVIEW, reviewQueue = queue, reviewIndex = 0, reviewDone = emptySet()) }
    }

    fun reviewNext(markDone: Boolean) = _ui.update {
        val pkg = it.reviewQueue.getOrNull(it.reviewIndex)
        it.copy(
            reviewIndex = (it.reviewIndex + 1).coerceAtMost(it.reviewQueue.size),
            reviewDone = if (markDone && pkg != null) it.reviewDone + pkg else it.reviewDone,
        )
    }

    fun setThresholds(aging: Int, stale: Int) = viewModelScope.launch { repo.settings.setThresholds(aging, stale) }

    fun setReminders(on: Boolean) = viewModelScope.launch {
        repo.settings.setReminders(on)
        Reminders.schedule(getApplication(), repo.settings.current())
    }

    fun setEvery(days: Int) = viewModelScope.launch {
        repo.settings.setEvery(days)
        Reminders.schedule(getApplication(), repo.settings.current())
    }

    fun setOnlyCharging(on: Boolean) = viewModelScope.launch {
        repo.settings.setOnlyCharging(on)
        Reminders.schedule(getApplication(), repo.settings.current())
    }

    fun setIncludeSystem(on: Boolean) = viewModelScope.launch {
        repo.settings.setIncludeSystem(on)
        refresh()
    }

    fun finishOnboarding() = viewModelScope.launch { repo.settings.setOnboarded() }
}
