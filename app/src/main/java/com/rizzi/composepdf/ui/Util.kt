package com.rizzi.composepdf.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import com.rizzi.composepdf.ui.theme.ComposePDFTheme

fun ComponentActivity.setThemedContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit
) {
    setContent(parent = parent) {
        ComposePDFTheme {
            content()
        }
    }
}