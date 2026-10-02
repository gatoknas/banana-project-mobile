package org.banana.project.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.banana.project.ui.screens.ProductCatalogScreen as ProductCatalogComposable

class ProductCatalogScreen : Screen {

    @Composable
    override fun Content() {
        ProductCatalogComposable()
    }
}
