package com.salmanajmal.hsk4mastery.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? = value?.let { if (it) 1 else 0 }

    @TypeConverter
    fun toBoolean(value: Int?): Boolean? = value?.let { it != 0 }
}
