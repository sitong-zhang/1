package com.kyant.backdrop.catalog

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.catalog.destinations.ButtonsContent
import com.kyant.backdrop.catalog.destinations.CardsContent
import com.kyant.backdrop.catalog.destinations.ControlCenterContent
import com.kyant.backdrop.catalog.destinations.HomeContent
import com.kyant.backdrop.catalog.destinations.PlaygroundContent
import com.kyant.backdrop.catalog.destinations.SegmentedContent
import com.kyant.backdrop.catalog.destinations.SliderContent
import com.kyant.backdrop.catalog.destinations.SwitchContent
import com.kyant.backdrop.catalog.destinations.TabBarContent
import com.kyant.backdrop.catalog.utils.BackHandler

@Composable
fun MainContent() {
    val isLightTheme = !isSystemInDarkTheme()

    CompositionLocalProvider(
        LocalIndication provides ripple(color = if (isLightTheme) Color.Black else Color.White)
    ) {
        var destination by rememberSaveable { mutableStateOf(CatalogDestination.Home) }

        when (destination) {
            CatalogDestination.Home -> HomeContent(onNavigate = { destination = it })

            CatalogDestination.Buttons -> ButtonsContent()
            CatalogDestination.Switch -> SwitchContent()
            CatalogDestination.Slider -> SliderContent()
            CatalogDestination.Cards -> CardsContent()
            CatalogDestination.Segmented -> SegmentedContent()
            CatalogDestination.TabBar -> TabBarContent()
            CatalogDestination.ControlCenter -> ControlCenterContent()
            CatalogDestination.Playground -> PlaygroundContent()
        }

        BackHandler(destination != CatalogDestination.Home) {
            destination = CatalogDestination.Home
        }
    }
}