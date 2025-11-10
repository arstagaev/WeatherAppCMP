package com.tagaev.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import org.koin.core.component.KoinComponent
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.tagaev.data.local.AppSettings
import com.tagaev.ui.main_weather_screen.IMainWeatherComponent
import com.tagaev.ui.main_weather_screen.MainWeatherComponent
import com.tagaev.ui.settings_screen.ISettingsComponent
import com.tagaev.ui.settings_screen.SettingsComponent
import org.koin.core.component.inject

interface IRootComponent {
    val childStack: Value<ChildStack<Config, Child>>


    fun openMainWeather()
    fun openSettings()
    fun back()

    sealed interface Config {
        data object MainWeather : Config
        data object Settings : Config
    }

    sealed interface Child {
        data class MainWeather(val component: IMainWeatherComponent) : Child
        data class Settings(val component: ISettingsComponent) : Child
    }



}

class DefaultRootComponent(
    componentContext: ComponentContext
) : IRootComponent, ComponentContext by componentContext, KoinComponent {

    private val appSettings: AppSettings by inject()
    private val nav = StackNavigation<IRootComponent.Config>()

//    init {
//    }

    override val childStack = childStack(
            source = nav,
            serializer = null,
            initialConfiguration = IRootComponent.Config.MainWeather,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(cfg: IRootComponent.Config, ctx: ComponentContext): IRootComponent.Child =
        when (cfg) {

            is IRootComponent.Config.MainWeather ->
                IRootComponent.Child.MainWeather(
                    MainWeatherComponent(
                        componentContext = ctx,
                        appSettings = appSettings
                    )
                )

            is IRootComponent.Config.Settings ->
                IRootComponent.Child.Settings(
                    SettingsComponent(
                    componentContext = ctx,
                    onWriteToDeveloperAction = {  },
                    onBack = { nav.pop() }
                ))
        }

    override fun openMainWeather() = nav.bringToFront(IRootComponent.Config.MainWeather)
    override fun openSettings() = nav.bringToFront(IRootComponent.Config.Settings)
    override fun back() = nav.pop()
}