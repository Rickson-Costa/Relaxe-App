package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breathing_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val wasCrisis: Boolean,
    val feltBetter: Boolean,
    val durationSeconds: Int
)
