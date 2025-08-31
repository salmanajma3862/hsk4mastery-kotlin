package com.salmanajmal.hsk4mastery.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_word_progress")
data class UserWordProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "word_id")
    val wordId: String,

    @ColumnInfo(name = "status")
    val status: String?,

    @ColumnInfo(name = "srsLevel")
    val srsLevel: Int?,

    // epoch millis
    @ColumnInfo(name = "nextReviewAt")
    val nextReviewAt: Long?,

    @ColumnInfo(name = "comfortLevel")
    val comfortLevel: Int?,

    @ColumnInfo(name = "reviewCount")
    val reviewCount: Int?,

    @ColumnInfo(name = "timesCorrect")
    val timesCorrect: Int?,

    @ColumnInfo(name = "timesIncorrect")
    val timesIncorrect: Int?,

    // epoch millis
    @ColumnInfo(name = "firstSeenAt")
    val firstSeenAt: Long?,

    // 0/1 boolean flag represented as INTEGER in SQLite
    @ColumnInfo(name = "isStruggling")
    val isStruggling: Int?,
)
