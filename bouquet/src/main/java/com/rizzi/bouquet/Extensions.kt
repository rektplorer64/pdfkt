package com.rizzi.bouquet

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

internal suspend fun Context.cacheBase64AsPdfFile(
    base64: String,
    cacheFileName: String,
) = cacheBase64AsPdfFile(
    Base64.decode(base64, Base64.DEFAULT),
    cacheFileName
)

internal suspend fun Context.cacheBase64AsPdfFile(
    base64: ByteArray,
    cacheFileName: String
): File {
    val file = File(cacheDir, cacheFileName)
    with(FileOutputStream(file, false)) {
        withContext(Dispatchers.IO) {
            write(base64)
            flush()
            close()
        }
    }
    return file
}

internal suspend fun Context.uriToFile(
    uri: Uri,
    cacheFileName: String = generateFileName()
): File  {
    val file = File(cacheDir, cacheFileName)
    withContext(Dispatchers.IO) {
        val fileOutputStream = FileOutputStream(file, false)
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.readBytes()?.let {
            fileOutputStream.write(it)
        }
        fileOutputStream.flush()
        fileOutputStream.close()
        inputStream?.close()
    }
    return file
}

internal fun generateFileName(name: String = Date().time.toString()): String {
    return "$name.pdf"
}


