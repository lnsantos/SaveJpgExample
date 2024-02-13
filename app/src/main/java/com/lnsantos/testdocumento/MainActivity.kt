package com.lnsantos.testdocumento

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lnsantos.testdocumento.savedfile.`in`.SavedFiledContent
import com.lnsantos.testdocumento.savedfile.out.save.SaveJPGDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date


class MainActivity : AppCompatActivity(), SavedFiledContent.Callback {

    private val button by lazy { findViewById<Button>(R.id.btn) }
    private val content by lazy { findViewById<LinearLayout>(R.id.content) }

    private val pdf = PdfDocument()

    /**Dimension For A4 Size Paper (1 inch = 72 points)**/
    private val PDF_PAGE_WIDTH = 595 //8.26 Inch
    private val PDF_PAGE_HEIGHT = 842 //11.69 Inch

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                GlobalScope.launch {
                    SaveJPGDecision.build().save(
                        content,
                        this@MainActivity,
                        this@MainActivity
                    )
                }
            } else {
                Toast.makeText(
                    this,
                    "Permissão negada pelo usuario",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this,
                    WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                    requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
                    return@setOnClickListener
                }
            }

            GlobalScope.launch {
                delay(1000)
                SaveJPGDecision.build().save(
                    content,
                    this@MainActivity,
                    this@MainActivity
                )
            }

//            return@setOnClickListener
//            val width = content.width
//            val height = content.height
//
//            val bitmapTemp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            val canvasTemp = Canvas(bitmapTemp)
//
//            content.draw(canvasTemp)
//
//            // val downloadFileRef = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//
//            val uriStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//            else
//                Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
//
//            val filename: String = "foto${Date().time}.jpg"
//            val file = File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//                filename
//            )
//
//            val outputStream = file.outputStream()
//
//            val MIME_TYPE_IMAGE_PNG = "image/png"
//
//            val result = bitmapTemp.compress(
//                Bitmap.CompressFormat.JPEG,
//                100,
//                outputStream
//            )
//
//
//            if (result) {
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    val contentValues = ContentValues().apply {
//                        put(MediaStore.Images.Media.DISPLAY_NAME, "foto${Date().time}.jpg")
//                        put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE_IMAGE_PNG)
//                    }
//
//                    val uri = contentResolver.insert(uriStorage, contentValues)
//
//                    if (uri != null) {
//                        contentResolver.openOutputStream(uri, "w")?.run {
//                            use {
//                                file.inputStream().copyTo(it)
//                            }
//                        }
//                    }
//
//                } else {
//                    // createPdf()
//                    MediaStore.Images.Media.insertImage(
//                        contentResolver,
//                        bitmapTemp,
//                        "teste",
//                        "teste"
//                    )
////                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
////                        addCategory(Intent.CATEGORY_OPENABLE)
////                        type = "application/jpg"
////                        putExtra(Intent.EXTRA_TITLE, "foto${Date().time}.jpg")
////
////                        // Optionally, specify a URI for the directory that should be opened in
////                        // the system file picker before your app creates the document.
////                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, file)
////                    }
////                    startActivityForResult(intent, 1)
////                    file.outputStream().run {
////                        use {
////                            file.inputStream().copyTo(it)
////                        }
////                    }
//
//                    outputStream.use {
//
//                    }
//                }
//            }
//
//            outputStream.flush()
//            outputStream.close()
        }
    }

    private fun createPdf(): File {

        val doc = PdfDocument.PageInfo.Builder(
            PDF_PAGE_WIDTH,
            PDF_PAGE_HEIGHT,
            1
        )

        val page = pdf.startPage(doc.create())

        content.draw(page.canvas)

        pdf.finishPage(page)
        val downloadFileRef = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val filename: String = "comprovante_${Date().time}.pdf"
        val file = File(downloadFileRef, filename)
        pdf.writeTo(file.outputStream())
        pdf.close()

        return file
    }

    override suspend fun onSuccessSavedFile(uri: Uri): Unit = withContext(Dispatchers.Main) {

        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Salvo com sucesso")
        builder.setMessage("Deseja acessar o arquivo, armazenado ?")
        builder.setPositiveButton("Sim") { dialog, which ->
            val intent = Intent(
                Intent.ACTION_VIEW
            ).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/*"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
            dialog.dismiss()
        }
        builder.setNegativeButton("Não", null)
        builder.show()
    }

    override suspend fun onFailedSavedFile(): Unit = withContext(Dispatchers.Main) {
        Toast.makeText(
            this@MainActivity,
            "Falha ao tenta salvar o arquivo",
            Toast.LENGTH_LONG
        ).show()
    }
}