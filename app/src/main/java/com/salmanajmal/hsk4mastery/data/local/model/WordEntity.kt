package com.salmanajmal.hsk4mastery.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: String,

    @ColumnInfo(name = "wordId")
    val wordId: Int?,

    @ColumnInfo(name = "hanzi")
    val hanzi: String,

    @ColumnInfo(name = "pinyin")
    val pinyin: String,

    @ColumnInfo(name = "meaning")
    val meaning: String,

    // Stored JSON string of the full object from the seed, mirrors Expo's fullData
    @ColumnInfo(name = "fullData")
    val fullData: String,
)
