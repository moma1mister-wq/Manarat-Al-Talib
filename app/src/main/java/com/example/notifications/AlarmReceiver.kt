package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STOP_ALARM_SOUND = "com.example.notifications.ACTION_STOP_ALARM_SOUND"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP_ALARM_SOUND) {
            val notifId = intent.getIntExtra("ALARM_ID", -1)
            NotificationAndAlarmHelper.stopAlertSound(context, notifId)
            return
        }

        val alarmId = intent.getIntExtra("ALARM_ID", 1001)
        val title = intent.getStringExtra("TITLE") ?: "تنبيه"
        val message = intent.getStringExtra("MESSAGE") ?: ""
        val catName = intent.getStringExtra("CATEGORY") ?: AlarmCategory.STUDY.name
        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: ""
        val customToneUri = intent.getStringExtra("CUSTOM_TONE_URI") ?: ""
        val recurrenceType = intent.getStringExtra("RECURRENCE_TYPE") ?: "ONCE"
        val time24 = intent.getStringExtra("TIME_24") ?: ""
        val dayOfWeekAr = intent.getStringExtra("DAY_OF_WEEK_AR") ?: ""

        val category = runCatching { AlarmCategory.valueOf(catName) }.getOrDefault(AlarmCategory.STUDY)
        val formattedTimeBody = when {
            time24.isNotBlank() -> com.example.ui.components.Time12hFormatter.format24To12Ar(time24)
            message.isNotBlank() -> com.example.ui.components.Time12hFormatter.replaceAll24hTimesWith12hAr(message)
            else -> ""
        }

        // Single unified trigger protected by Section Guard + 60-second Dedupe Guard
        val didFire = NotificationAndAlarmHelper.triggerImmediateAlertAndNotification(
            context = context,
            notificationId = alarmId,
            title = title,
            message = formattedTimeBody,
            category = category,
            subjectName = subjectName,
            customToneUri = customToneUri
        )

        if (!didFire) return

        // Reschedule next occurrence if this is a daily or weekly recurring alarm, or disable if ONCE
        if (time24.isNotBlank()) {
            when (recurrenceType.uppercase()) {
                "DAILY" -> {
                    NotificationAndAlarmHelper.scheduleDailyExactAlarm(
                        context = context,
                        requestCode = alarmId,
                        time24 = time24,
                        title = title,
                        message = "",
                        category = category,
                        subjectName = subjectName,
                        customToneUri = customToneUri
                    )
                }
                "WEEKLY" -> {
                    NotificationAndAlarmHelper.scheduleWeeklyExactAlarm(
                        context = context,
                        requestCode = alarmId,
                        dayOfWeekAr = dayOfWeekAr,
                        time24 = time24,
                        title = title,
                        message = "",
                        category = category,
                        subjectName = subjectName,
                        customToneUri = customToneUri
                    )
                }
                "ONCE" -> {
                    if (alarmId in 3000..3999) {
                        val scheduleItemId = alarmId - 3000
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            runCatching {
                                val dao = com.example.data.local.AppDatabase.getInstance(context.applicationContext).appDao()
                                val item = dao.getAllScheduleItemsOnce().firstOrNull { it.id == scheduleItemId }
                                if (item != null && item.recurrence.equals("ONCE", ignoreCase = true)) {
                                    dao.insertScheduleItem(item.copy(alarmEnabled = false))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Schedule Post-Notification Rating Followup for any scheduled activity (Study, Exercise, Adhkar, Prayer)
        if (category != AlarmCategory.RATING_FOLLOWUP &&
            alarmId != NotificationAndAlarmHelper.TEST_NOTIFICATION_ID &&
            alarmId < 50000
        ) {
            val prefs = context.getSharedPreferences("study_organizer_prefs", Context.MODE_PRIVATE)
            val delayMinutes = prefs.getInt("rating_delay_minutes", 15).coerceAtLeast(1)
            val followupTime = System.currentTimeMillis() + (delayMinutes * 60_000L)
            val cleanSubj = NotificationAndAlarmHelper.cleanSubjectName(subjectName.ifBlank { title })
            val followupRequestCode = 50000 + (alarmId % 9000)
            NotificationAndAlarmHelper.scheduleExactAtMillis(
                context = context,
                requestCode = followupRequestCode,
                triggerAtMillis = followupTime,
                title = "تقييم: $cleanSubj",
                message = "",
                category = AlarmCategory.RATING_FOLLOWUP,
                subjectName = cleanSubj
            )
        }
    }
}
