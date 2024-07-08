package com.rizzi.composepdf.ui.component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OrientationToggle(
    value: Orientation,
    onValueChange: (Orientation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier,
        onClick = {
            onValueChange(
                when (value) {
                    Orientation.Vertical -> Orientation.Horizontal
                    Orientation.Horizontal -> Orientation.Vertical
                }
            )
        }
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Checkbox(
                checked = value == Orientation.Horizontal,
                onCheckedChange = {
                    onValueChange(
                        when (value) {
                            Orientation.Vertical -> Orientation.Horizontal
                            Orientation.Horizontal -> Orientation.Vertical
                        }
                    )
                },
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text("Horizontal")
    }
}