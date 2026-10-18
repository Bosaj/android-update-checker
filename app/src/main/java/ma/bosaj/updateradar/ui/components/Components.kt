package ma.bosaj.updateradar.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ma.bosaj.updateradar.R
import ma.bosaj.updateradar.data.Freshness
import ma.bosaj.updateradar.data.InstalledApp
import ma.bosaj.updateradar.data.Radar
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.ui.theme.LocalRadar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun freshColor(f: Freshness): Color = when (f) {
    Freshness.FRESH -> LocalRadar.current.fresh
    Freshness.AGING -> LocalRadar.current.aging
    Freshness.STALE -> LocalRadar.current.stale
}

/**
 * Animated radar: rings, a sweeping beam and one blip per app. Distance from the centre grows with
 * the days since the last update, so stale apps sit on the outer ring.
 */
@Composable
fun RadarScope(
    apps: List<InstalledApp>,
    now: Long,
    aging: Int,
    stale: Int,
    modifier: Modifier = Modifier,
    center: @Composable () -> Unit,
) {
    val radar = LocalRadar.current
    val transition = rememberInfiniteTransition(label = "sweep")
    val angle by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart), label = "angle")
    val blips = remember(apps, now, aging, stale) {
        apps.map { a ->
            val days = Radar.daysSince(a.lastUpdate, now).toFloat()
            val r = (0.22f + 0.74f * min(days / (stale * 2f), 1f))
            val theta = ((a.packageName.hashCode().toLong() and 0xffff) / 65535f) * 360f
            Triple(r, theta, Radar.freshness(a, now, aging, stale))
        }
    }
    val colors = blips.map { freshColor(it.third) }
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            for (i in 1..4) drawCircle(radar.grid, radius * i / 4f, c, style = Stroke(1.2.dp.toPx()))
            drawLine(radar.grid, Offset(c.x - radius, c.y), Offset(c.x + radius, c.y), 1.dp.toPx())
            drawLine(radar.grid, Offset(c.x, c.y - radius), Offset(c.x, c.y + radius), 1.dp.toPx())
            rotate(angle, c) {
                drawCircle(
                    Brush.sweepGradient(0f to Color.Transparent, 0.82f to Color.Transparent, 1f to radar.sweep.copy(alpha = 0.45f), center = c),
                    radius, c,
                )
                drawLine(radar.sweep.copy(alpha = 0.9f), c, Offset(c.x + radius, c.y), 2.dp.toPx())
            }
            blips.forEachIndexed { i, (r, theta, _) ->
                val rad = Math.toRadians(theta.toDouble())
                val p = Offset(c.x + (cos(rad) * r * radius).toFloat(), c.y + (sin(rad) * r * radius).toFloat())
                // Blips glow brighter right after the beam passes over them.
                val since = ((angle - theta) % 360f + 360f) % 360f
                val glow = 1f - (since / 360f)
                drawCircle(colors[i].copy(alpha = 0.18f + 0.35f * glow), 7.dp.toPx(), p)
                drawCircle(colors[i].copy(alpha = 0.55f + 0.45f * glow), 3.2.dp.toPx(), p)
            }
        }
        center()
    }
}

@Composable
fun AppIcon(pkg: String, size: Dp = 44.dp, loader: (String) -> android.graphics.drawable.Drawable?) {
    val px = with(androidx.compose.ui.platform.LocalDensity.current) { size.roundToPx() }
    val bmp by produceState<ImageBitmap?>(null, pkg) {
        value = withContext(Dispatchers.IO) { loader(pkg)?.toBitmap(px, px)?.asImageBitmap() }
    }
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 4))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        bmp?.let { Image(it, contentDescription = null, modifier = Modifier.size(size)) }
    }
}

@Composable
fun storeName(store: Store): String = stringResource(
    when (store) {
        Store.PLAY -> R.string.store_play
        Store.GALAXY -> R.string.store_galaxy
        Store.AMAZON -> R.string.store_amazon
        Store.HUAWEI -> R.string.store_huawei
        Store.FDROID -> R.string.store_fdroid
        Store.OTHER_STORE -> R.string.store_other
        Store.SIDELOADED -> R.string.store_sideloaded
        Store.PREINSTALLED -> R.string.store_preinstalled
    },
)

@Composable
fun StoreChip(store: Store) {
    val color = when (store) {
        Store.PLAY -> Color(0xFF34A853)
        Store.GALAXY -> Color(0xFF6E8CFF)
        Store.SIDELOADED -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f), contentColor = color) {
        Text(storeName(store), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
fun daysAgo(days: Long): String = when (days) {
    0L -> stringResource(R.string.today)
    else -> pluralStringResource(R.plurals.days_ago, days.toInt(), days.toInt())
}

@Composable
fun FreshDot(f: Freshness, modifier: Modifier = Modifier) {
    val label = stringResource(
        when (f) {
            Freshness.FRESH -> R.string.fresh
            Freshness.AGING -> R.string.aging
            Freshness.STALE -> R.string.stale
        },
    )
    Box(modifier.size(10.dp).clip(CircleShape).background(freshColor(f)).semantics { contentDescription = label })
}

@Composable
fun Stat(value: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
