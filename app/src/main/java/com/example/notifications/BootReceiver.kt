package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        @Volatile
        private var lastBootHandledAt: Long = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        ) {
            val now = System.currentTimeMillis()
            if (now - lastBootHandledAt < 5_000L) {
                return
            }
            lastBootHandledAt = now

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    NotificationAndAlarmHelper.scheduleAllAlarms(
                        context = context.applicationContext,
                        reason = "BOOT_OR_SYSTEM_EVENT_$action",
                        forceReschedule = true
                    )
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
