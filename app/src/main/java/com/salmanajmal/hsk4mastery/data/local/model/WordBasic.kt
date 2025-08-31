package com.salmanajmal.hsk4mastery.data.local.model

import androidx.room.ColumnInfo

// POJO projection for lightweight word list queries
data class WordBasic(
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
    @ColumnInfo(name = "status")
    val status: String? = null,
    @ColumnInfo(name = "comfortLevel")
    val comfortLevel: Int? = null,
)
