package com.rizzi.bouquet.loader

import android.os.ParcelFileDescriptor
import com.rizzi.bouquet.DocumentResource
import java.io.File

data class DocumentRequest(val type: DocumentResource)
sealed class DocumentResult {
    abstract val request: DocumentRequest

    class Success(
        override val request: DocumentRequest,
        val file: File?,
        val fileDescriptor: ParcelFileDescriptor
    ) : DocumentResult()

    class Error(
        override val request: DocumentRequest,
        val throwable: Throwable
    ) : DocumentResult()
}