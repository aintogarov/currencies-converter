package com.aintogarov.currencyconverter.data.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rates")
data class RateDB(
    @PrimaryKey val currency: String,
    @ColumnInfo val value: String
)