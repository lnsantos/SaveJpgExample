package com.lnsantos.testdocumento.savedfile.out.save

import android.os.Build
import com.lnsantos.testdocumento.savedfile.`in`.SavedFiledContent
import com.lnsantos.testdocumento.savedfile.out.CreateBitmapByView

internal typealias RuleStrategy = (SavedFiledContent) -> Boolean

class SaveJPGDecision {
    companion object {
        private val createBitmap = CreateBitmapByView()
        private val strategy = arrayOf<SavedFiledContent>(
            SaveJPGModernContent(createBitmap),
            SaveJPGLegacyContent(createBitmap)
        )

        fun build(): SavedFiledContent {
            val rule : RuleStrategy = { Build.VERSION.SDK_INT >= it.versionSupported }
            return strategy.firstOrNull(rule) ?: strategy.last()
        }
    }
}
