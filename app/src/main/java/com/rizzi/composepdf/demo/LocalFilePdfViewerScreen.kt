package com.rizzi.composepdf.demo

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.compose.state.LazyListPdfReaderState
import com.rizzi.bouquet.compose.state.rememberLazyListPdfReaderState
import com.rizzi.bouquet.loader.DocumentRequest
import com.rizzi.composepdf.R
import com.rizzi.composepdf.ui.component.OrientationToggle
import com.rizzi.composepdf.ui.component.LazyPdfListViewer
import kotlinx.coroutines.launch

@Composable
fun LocalFilePdfViewerScreen(
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var fileUriString by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) {
        it ?: return@rememberLauncherForActivityResult run {
            Toast.makeText(context, "Cancelled picking a PDF file", Toast.LENGTH_SHORT).show()
        }

        fileUriString = it.toString()
    }

    val state: LazyListPdfReaderState = rememberLazyListPdfReaderState(
        request = DocumentRequest(DocumentResource.Local(fileUriString?.toUri()))
    )

    var orientation by rememberSaveable {
        mutableStateOf(Orientation.Vertical)
    }

    LaunchedEffect(state.status) {
        println("[X] Status: ${state.status}")
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            Surface {
                Column {
                    TopAppBar(
                        title = { Text("Local File") },
                        navigationIcon = {
                            IconButton(onClick = onBackPress) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            OrientationToggle(
                                value = orientation,
                                onValueChange = {
                                    orientation = it
                                }
                            )
                        }
                    )

                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = fileUriString ?: "",
                            onValueChange = {},
                            label = { Text("Local File URI") },
                            placeholder = {
                                Text("Pick a file")
                            },
                            maxLines = 1,
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { picker.launch(arrayOf("application/pdf")) }) {
                                    Icon(
                                        Icons.Default.Create,
                                        contentDescription = "Pick a file"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        },
    ) { pv ->
        LazyPdfListViewer(
            modifier = Modifier.padding(pv),
            state = state,
            orientation = orientation,
            onError = {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = it.message ?: "Error"
                    )
                }
            }
        )
    }
}