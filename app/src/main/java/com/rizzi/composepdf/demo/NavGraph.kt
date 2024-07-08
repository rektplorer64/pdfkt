package com.rizzi.composepdf.demo

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.compose.state.rememberLazyListPdfReaderState
import com.rizzi.bouquet.loader.DocumentRequest
import com.rizzi.composepdf.R
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

object Screens {
    @Serializable
    data object Home
    @Serializable
    data class Base64(val orientation: Orientation = Orientation.Vertical)
    @Serializable
    data class RemoteUrl(val orientation: Orientation = Orientation.Vertical)
    @Serializable
    data class LocalFile(val orientation: Orientation = Orientation.Vertical)
}

val typeMap by lazy {
    mapOf(
        typeOf<Orientation>() to NavType.EnumType(Orientation::class.java)
    )
}

fun NavGraphBuilder.includeDemoRoutes(navController: NavController) {
    composable<Screens.Home> {
        HomeScreen(navController = navController)
    }

    composable<Screens.Base64>(typeMap = typeMap) {
        val arguments = it.toRoute<Screens.Base64>()
        Base64PdfViewerScreen(onBackPress = navController::navigateUp)
    }

    composable<Screens.LocalFile>(typeMap = typeMap) {
        val arguments = it.toRoute<Screens.LocalFile>()

        LocalFilePdfViewerScreen(onBackPress = navController::navigateUp)
    }

    composable<Screens.RemoteUrl>(typeMap = typeMap) {
        val arguments = it.toRoute<Screens.RemoteUrl>()

        RemoteUrlPdfViewerScreen(
            onBackPress = navController::navigateUp
        )
    }
}