package com.ljku.tripsplit.ui.util

import android.content.Context
import android.content.Intent

fun shareText(context: Context, text: String, title: String = "Share trip summary") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
