package com.rizzi.composepdf.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rizzi.composepdf.MenuItem

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("pdf-kt demo")
                }
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .padding(pv)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MenuItem.entries.forEach {
                ListItem(
                    modifier = Modifier.clickable {
                        navController.navigate(
                            when (it) {
                                MenuItem.Base64 -> Screens.Base64()
                                MenuItem.RemoteUrl -> Screens.RemoteUrl()
                                MenuItem.LocalFile -> Screens.LocalFile()
                            }
                        )
                    }.fillMaxWidth(),
                    text = {
                        Text(
                            when (it) {
                                MenuItem.Base64 -> "PDF from Base64 String"
                                MenuItem.RemoteUrl -> "PDF from a HTTP URL"
                                MenuItem.LocalFile -> "PDF from a Local file"
                            },
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    },
                )
                Divider()
            }
        }
    }
}