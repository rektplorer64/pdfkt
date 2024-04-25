package com.rizzi.composepdf

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.rizzi.bouquet.compose.state.HorizontalPdfReaderState
import com.rizzi.bouquet.compose.state.PdfReaderState
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.compose.state.VerticalPdfReaderState
import com.rizzi.bouquet.loader.DocumentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BouquetViewModel : ViewModel() {
    private val mStateFlow = MutableStateFlow<PdfReaderState?>(null)
    val stateFlow: StateFlow<PdfReaderState?> = mStateFlow

    val switchState = mutableStateOf(false)

    fun openResource(documentResource: DocumentResource) {
        mStateFlow.tryEmit(
            if (switchState.value) {
                HorizontalPdfReaderState(DocumentRequest(documentResource), true)
            } else {
                VerticalPdfReaderState(DocumentRequest(documentResource), true)
            }
        )
    }

    fun clearResource() {
        mStateFlow.tryEmit(null)
    }
}
