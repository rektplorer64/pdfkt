package com.rizzi.composepdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rizzi.composepdf.demo.Screens
import com.rizzi.composepdf.demo.includeDemoRoutes
import com.rizzi.composepdf.ui.setThemedContent
import java.io.File


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        setThemedContent {

            val navController = rememberNavController()

            NavHost(
                startDestination = Screens.Home,
                navController = navController,
            ) {
                includeDemoRoutes(navController = navController)
            }
        }
    }

    private fun shareFile(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${this.packageName}.fileprovider",
            file
        )
        val intent = ShareCompat.IntentBuilder.from(this)
            .setType("application/pdf")
            .setStream(uri)
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }
}

enum class MenuItem {
    Base64,
    RemoteUrl,
    LocalFile,
}

