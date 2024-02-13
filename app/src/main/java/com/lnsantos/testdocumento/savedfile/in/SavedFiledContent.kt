package com.lnsantos.testdocumento.savedfile.`in`

import android.content.Context
import android.net.Uri
import android.view.View
import java.io.File

interface SavedFiledContent {
    val versionSupported: Int
    var filename: String
    fun createFile() : File

    suspend fun save(
        view: View,
        context: Context,
        callback: Callback
    )

    companion object {
        const val MIME_TYPE_IMAGE_PNG = "image/png"
        const val MAX_QUALITY = 100
    }

    interface Callback {
        suspend fun onSuccessSavedFile(uri: Uri)
        suspend fun onFailedSavedFile()
    }
}