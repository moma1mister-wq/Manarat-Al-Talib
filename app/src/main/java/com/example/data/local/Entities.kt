package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val username: String,
    val passwordPlainOrHash: String,
    val fullName: String,
    val sectionNumber: Int = 1, // 1 or 2
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "schedule_items")
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dayOfWeek: String, // السبت, الأحد, الاثنين, الثلاثاء, الأربعاء, الخميس, الجمعة
    val startTime: String, // e.g. "08:00"
    val endTime: String,   // e.g. "10:00"
    val location: String,  // مدرج 1, معمل 1, معمل 2, أونلاين, مذاكرة منزلية
    val sectionFilter: Int = 0, // 0 = All sections, 1 = Section 1, 2 = Section 2
    val itemType: String = "LECTURE", // LECTURE, LAB, ONLINE, STUDY_TASK
    val recurrence: String = "WEEKLY", // WEEKLY, DAILY, ONCE
    val timerMinutes: Int = 90,
    val customToneUri: String = "", // Empty = default study_tone.mp3
    val alarmEnabled: Boolean = true,
    val isCoreCurriculum: Boolean = true
)

@Entity(tableName = "session_ratings")
data class SessionRatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String = "",
    val subjectTitle: String,
    val stars: Int, // 1..5
    val isCompleted: Boolean, // أنجزت / لم أنجز
    val note: String,
    val durationMinutes: Int = 90,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordType: String, // "HISN_PDF" or "PRICE_LIST"
    val title: String,
    val category: String,
    val contentOrDescription: String,
    val priceEgp: Double = 0.0,
    val unitOrReference: String = "",
    val targetCount: Int = 3,
    val pdfPageNumber: Int = 1
)

@Entity(tableName = "adhkar_alarms")
data class AdhkarAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val alarmTime: String, // "05:30"
    val isEnabled: Boolean = true,
    val toneName: String = "adhkar_tone.mp3",
    val isFixedCore: Boolean = true
)
