package com.lnsantos.testdocumento

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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


class MainActivity : AppCompatActivity(), SavedFiledContent.Callback {

    private val button by lazy { findViewById<Button>(R.id.btn) }
    private val content by lazy { findViewById<LinearLayout>(R.id.content) }

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
        }
    }

    override suspend fun onSuccessSavedFile(uri: Uri): Unit = withContext(Dispatchers.Main) {

        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        AlertDialog.Builder(this@MainActivity).apply {
            setTitle("Salvo com sucesso")
            setMessage("Deseja acessar o arquivo, armazenado ?")
            setPositiveButton("Sim") { dialog, _ ->

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/*"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                startActivity(intent)
                dialog.dismiss()
            }
            setNegativeButton("Não", null)
            show()
        }
    }

    override suspend fun onFailedSavedFile(): Unit = withContext(Dispatchers.Main) {
        Toast.makeText(
            this@MainActivity,
            "Falha ao tenta salvar o arquivo, tente novamente!",
            Toast.LENGTH_LONG
        ).show()
    }
}