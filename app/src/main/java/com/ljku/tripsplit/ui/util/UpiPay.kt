package com.ljku.tripsplit.ui.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

object UpiPay {
    fun copy(context: Context, text: String, label: String = "UPI") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    fun pay(context: Context, upiId: String, name: String, amountPaise: Long, note: String) {
        val rupees = String.format(Locale.US, "%.2f", amountPaise / 100.0)
        val uri = Uri.parse(
            "upi://pay?pa=${Uri.encode(upiId)}" +
                "&pn=${Uri.encode(name)}" +
                "&am=$rupees" +
                "&cu=INR" +
                "&tn=${Uri.encode(note)}"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            copy(context, upiId, "UPI ID")
            Toast.makeText(context, "No UPI app found. UPI ID copied.", Toast.LENGTH_LONG).show()
        }
    }
}
