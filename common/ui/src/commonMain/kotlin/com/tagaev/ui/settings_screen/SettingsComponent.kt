package com.tagaev.ui.settings_screen

import com.arkivanov.decompose.ComponentContext
import com.tagaev.core.ui.ThemeController
import com.tagaev.core.ui.ThemeMode
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ISettingsComponent {
    val themeMode: StateFlow<ThemeMode>
    fun onSelectTheme(mode: ThemeMode)
    fun onSelectSystem() = onSelectTheme(ThemeMode.System)
    fun onSelectLight() = onSelectTheme(ThemeMode.Light)
    fun onSelectDark() = onSelectTheme(ThemeMode.Dark)

    fun onWriteToDeveloper()
    fun back()
}

class SettingsComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val onWriteToDeveloperAction: () -> Unit,
) : ISettingsComponent, ComponentContext by componentContext, KoinComponent {

    private val themeController: ThemeController by inject()
    override val themeMode: StateFlow<ThemeMode> = themeController.mode

    override fun onWriteToDeveloper() {
        onWriteToDeveloperAction()
    }

    override fun onSelectTheme(mode: ThemeMode) {
        themeController.set(mode)
    }

    override fun back() = onBack()
}