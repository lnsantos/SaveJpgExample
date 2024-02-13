package com.lnsantos.testdocumento.savedfile.out.save

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.media.ExifInterface
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.lnsantos.testdocumento.savedfile.`in`.ICreateContent
import com.lnsantos.testdocumento.savedfile.`in`.SavedFiledContent
import com.lnsantos.testdocumento.savedfile.`in`.SavedFiledContent.Companion.MAX_QUALITY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.Date

class SaveJPGLegacyContent(
    createBitmap: ICreateContent<View, Bitmap>
) : SavedFiledContent, ICreateContent<View, Bitmap> by createBitmap{

    override val versionSupported: Int = Build.VERSION_CODES.LOLLIPOP
    override var filename: String = "legacy_${Date().time}.jpg"

    override fun createFile(): File {
        return File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), filename)
    }

    override suspend fun save(
        view: View,
        context: Context,
        callback: SavedFiledContent.Callback
    )= withContext(Dispatchers.IO) {
        try {
            val file = createFile()
            val bitmap = create(view)

            if(!file.createNewFile()) {
                callback.onFailedSavedFile()
                return@withContext
            }

            val fileOutput = FileOutputStream(file)

            if(!bitmap.compress(JPEG, MAX_QUALITY, fileOutput)) {
                callback.onFailedSavedFile()
                return@withContext
            }

            fileOutput.flush()
            fileOutput.close()

            callback.onSuccessSavedFile(file.toUri())
        } catch (e: Throwable) {
            Log.d("ErrorFind", e.stackTraceToString())
            callback.onFailedSavedFile()
        }
    }
}
