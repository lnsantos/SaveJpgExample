package com.lnsantos.testdocumento.savedfile.out

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.lnsantos.testdocumento.savedfile.`in`.ICreateContent

class CreateBitmapByView : ICreateContent<View, Bitmap> {
    override suspend fun create(input: View): Bitmap {
        val width = input.width
        val height = input.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        input.draw(Canvas(bitmap))

        return bitmap
    }
}
