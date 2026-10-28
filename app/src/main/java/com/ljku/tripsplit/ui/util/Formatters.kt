package com.ljku.tripsplit.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

object Formatters {
    private val inLocale = Locale("en", "IN")

    private val wholeRupees: NumberFormat = NumberFormat.getCurrencyInstance(inLocale).apply {
        currency = Currency.getInstance("INR")
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val paiseRupees: NumberFormat = NumberFormat.getCurrencyInstance(inLocale).apply {
        currency = Currency.getInstance("INR")
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    val currency: NumberFormat = wholeRupees

    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dateTimeFormatter: SimpleDateFormat =
        SimpleDateFormat("MMM dd, yyyy · h:mm a", Locale.getDefault())
    private val timeFormatter: SimpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun money(cents: Long): String {
        val rupees = Math.round(cents / 100.0)
        return wholeRupees.format(rupees)
    }

    fun date(millis: Long): String = dateFormatter.format(Date(millis))
    fun time(millis: Long): String = timeFormatter.format(Date(millis))
    fun dateTime(millis: Long): String = dateTimeFormatter.format(Date(millis))
}
