package com.tagaev.ui.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.tagaev.core.ui.AppTheme
import com.tagaev.core.ui.ThemeController
import com.tagaev.ui.main_weather_screen.MainWeatherScreen
import com.tagaev.ui.settings_screen.SettingsScreen
//import com.tagaev.core.ThemeController
import compose.icons.FeatherIcons
import compose.icons.feathericons.Home
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Sun
import compose.icons.feathericons.Sunrise
import org.koin.compose.koinInject

@Composable
fun AppRoot(root: IRootComponent) {
    val stack by root.childStack.subscribeAsState()   // State<ChildStack<..., ...>>
    val activeChild = stack.active.instance
    val themeController = koinInject<ThemeController>()
    AppTheme(controller = themeController) {
        Scaffold(
            bottomBar = {
                AppBottomNavBar(
                    activeChild = activeChild,
                    onMainWeather = { if (activeChild !is IRootComponent.Child.MainWeather) root.openMainWeather() },
                    onSettings = { if (activeChild !is IRootComponent.Child.Settings) root.openSettings() },
                )
            }
        ) { padding ->
            Children(
                stack = root.childStack,
                animation = stackAnimation(fade()),
                modifier = Modifier.padding(padding)
            ) { created ->
                when (val c = created.instance) {
                    is IRootComponent.Child.MainWeather -> MainWeatherScreen(c.component)
                    is IRootComponent.Child.Settings -> SettingsScreen(c.component)
                }
            }
        }
    }
}

@Composable
fun AppBottomNavBar(
    activeChild: IRootComponent.Child,
    onMainWeather: () -> Unit,
    onSettings: () -> Unit
) {
    NavigationBar(Modifier) {
        NavigationBarItem(
            selected = activeChild is IRootComponent.Child.MainWeather,
            onClick = onMainWeather,
            icon = { Icon(FeatherIcons.Sun, null) },
            label = { Text("Прогноз погоды") }
        )

        NavigationBarItem(
            selected = activeChild is IRootComponent.Child.Settings,
            onClick = onSettings,
            icon = { Icon(FeatherIcons.Settings, null) },
            label = { Text("Настройки") }
        )
    }
}