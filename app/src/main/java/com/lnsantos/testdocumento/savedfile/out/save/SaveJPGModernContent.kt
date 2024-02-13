package com.lnsantos.testdocumento.savedfile.out.save

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Downloads.getContentUri
import android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.lnsantos.testdocumento.savedfile.`in`.ICreateContent
import com.lnsantos.testdocumento.savedfile.`in`.SavedFiledContent
import com.lnsantos.testdocumento.savedfile.`in`.SavedFiledContent.Companion.MIME_TYPE_IMAGE_PNG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

class SaveJPGModernContent(
    createBitmap: ICreateContent<View, Bitmap>
) : SavedFiledContent, ICreateContent<View, Bitmap> by createBitmap {

    override val versionSupported: Int = Build.VERSION_CODES.Q
    override var filename: String = "ticket_${Date().time}.jpg"

    private fun createSettingFile() = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE_IMAGE_PNG)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createdUri(context: Context): Uri? {
        val referenceUriDownload = getContentUri(VOLUME_EXTERNAL_PRIMARY)
        return context.contentResolver.insert(
            referenceUriDownload,
            createSettingFile()
        )
    }

    override fun createFile(): File {
        throw NotImplementedError("this not implemented this class")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun save(
        view: View,
        context: Context,
        callback: SavedFiledContent.Callback
    ) = withContext(Dispatchers.IO) {
        try {
            val bitmap = create(view)
            val uri = createdUri(context) ?: run {
                callback.onFailedSavedFile()
                return@withContext
            }
            val modeWriteBytes = "w"

            val output = context.contentResolver.openOutputStream(uri, modeWriteBytes)

            if (!bitmap.compress(JPEG, 100, output!!)) {

                output.flush()
                output.close()

                callback.onFailedSavedFile()
                return@withContext
            }

            output.flush()
            output.close()

            callback.onSuccessSavedFile(uri)
        } catch (e: Throwable) {
            Log.d("ErrorFind", e.stackTraceToString())
            callback.onFailedSavedFile()
        }
    }
}