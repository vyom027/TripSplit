package com.ljku.tripsplit.data

import androidx.room.TypeConverter
import com.ljku.tripsplit.domain.PaidShare
import com.ljku.tripsplit.domain.SplitShare
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromPaidShareList(value: List<PaidShare>): String = json.encodeToString(value)

    @TypeConverter
    fun toPaidShareList(value: String): List<PaidShare> = json.decodeFromString(value)

    @TypeConverter
    fun fromSplitShareList(value: List<SplitShare>): String = json.encodeToString(value)

    @TypeConverter
    fun toSplitShareList(value: String): List<SplitShare> = json.decodeFromString(value)

    @TypeConverter
    fun fromLongMap(value: Map<String, Long>): String = json.encodeToString(value)

    @TypeConverter
    fun toLongMap(value: String): Map<String, Long> =
        if (value.isBlank()) emptyMap() else json.decodeFromString(value)
}
