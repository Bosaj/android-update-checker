package ma.bosaj.updateradar

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ma.bosaj.updateradar.ui.RadarViewModel
import ma.bosaj.updateradar.ui.Screen
import ma.bosaj.updateradar.ui.screens.AppSheet
import ma.bosaj.updateradar.ui.screens.HomeScreen
import ma.bosaj.updateradar.ui.screens.OnboardingScreen
import ma.bosaj.updateradar.ui.screens.ReviewScreen
import ma.bosaj.updateradar.ui.screens.SettingsScreen
import ma.bosaj.updateradar.ui.theme.UpdateRadarTheme

class MainActivity : AppCompatActivity() {
    private val vm: RadarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Hold the splash until settings are read, so onboarding never flashes for returning users.
        splash.setKeepOnScreenCondition { !vm.settings.value.loaded }
        setContent {
            UpdateRadarTheme {
                val ui by vm.ui.collectAsStateWithLifecycle()
                val s by vm.settings.collectAsStateWithLifecycle()
                val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.finishOnboarding() }

                // Coming back from a store page: rescan so updated apps leave the stale list.
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { if (!ui.loading) vm.refresh() }

                if (!s.loaded) return@UpdateRadarTheme
                if (!s.onboarded) {
                    OnboardingScreen(ui.apps) {
                        if (Build.VERSION.SDK_INT >= 33) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else vm.finishOnboarding()
                    }
                    return@UpdateRadarTheme
                }

                BackHandler(enabled = ui.screen != Screen.HOME) { vm.go(Screen.HOME) }
                when (ui.screen) {
                    Screen.HOME -> HomeScreen(vm, ui, s)
                    Screen.REVIEW -> ReviewScreen(vm, ui)
                    Screen.SETTINGS -> SettingsScreen(vm, s, ui.galaxyInstalled, ui.usageAccess)
                }
                ui.selected?.let { AppSheet(it, s, ui.galaxyInstalled, ui.usageAccess, vm) }
            }
        }
    }
}
