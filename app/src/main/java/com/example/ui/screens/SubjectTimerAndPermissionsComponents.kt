package com.example.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.notifications.AlarmCategory
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary

@Composable
fun PermissionsStatusAndQuickVerifyCard(
    onTestNotificationNow: (AlarmCategory, String) -> Unit
) {
    val context = LocalContext.current

    var notificationsGranted by remember {
        mutableStateOf(isNotificationPermissionGranted(context))
    }
    var exactAlarmsGranted by remember {
        mutableStateOf(isExactAlarmPermissionGranted(context))
    }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted || isNotificationPermissionGranted(context)
        feedbackMessage = if (notificationsGranted) {
            "✓ تم منح إذن الإشعارات بنجاح!"
        } else {
            "يرجى السماح بالإشعارات من إعدادات التطبيق"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("permissions_status_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (notificationsGranted && exactAlarmsGranted) EmeraldPrimary else GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "أذونات الإشعارات والمنبه",
                        style = MaterialTheme.typography.titleSmall,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                TextButton(
                    onClick = {
                        notificationsGranted = isNotificationPermissionGranted(context)
                        exactAlarmsGranted = isExactAlarmPermissionGranted(context)
                        onTestNotificationNow(
                            AlarmCategory.STUDY,
                            "مذاكرة Data Structures"
                        )
                        feedbackMessage = "تم الاختبار"
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "تجربة الإشعار",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Notification Permission Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (notificationsGranted) EmeraldPrimary.copy(alpha = 0.12f) else CoralAlert.copy(alpha = 0.14f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsGranted) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                notificationsGranted = isNotificationPermissionGranted(context)
                                feedbackMessage = "✓ إذن الإشعارات (POST_NOTIFICATIONS) مفعّل وجاهز"
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (notificationsGranted) EmeraldPrimary else CoralAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (notificationsGranted) "الإشعارات: مفعّل ✓" else "تفعيل الإشعارات",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (notificationsGranted) EmeraldPrimary else CoralAlert,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Exact Alarm Permission Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (exactAlarmsGranted) EmeraldPrimary.copy(alpha = 0.12f) else GoldAccent.copy(alpha = 0.16f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            exactAlarmsGranted = isExactAlarmPermissionGranted(context)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmsGranted) {
                                runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    )
                                }
                            } else {
                                feedbackMessage = "✓ المنبهات الدقيقة (EXACT_ALARM) مفعّلة بالثانية"
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AlarmOn,
                            contentDescription = null,
                            tint = if (exactAlarmsGranted) EmeraldPrimary else GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (exactAlarmsGranted) "المنبه الدقيق: مفعّل ✓" else "ضبط المنبه الدقيق",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (exactAlarmsGranted) EmeraldPrimary else Color(0xFFB45309),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (!feedbackMessage.isNullOrBlank()) {
                Text(
                    text = feedbackMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun isNotificationPermissionGranted(context: Context): Boolean {
    val runtimeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    return runtimeGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
}

private fun isExactAlarmPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        am?.canScheduleExactAlarms() ?: true
    } else {
        true
    }
}

@Composable
fun CustomSubjectTimerDialog(
    initialSubject: String,
    availableSubjects: List<String>,
    subjectCustomTimers: Map<String, Int>,
    onDismiss: () -> Unit,
    onSaveCustomTimer: (subjectTitle: String, customMinutes: Int, startImmediately: Boolean) -> Unit
) {
    val distinctSubjects = remember(availableSubjects, subjectCustomTimers, initialSubject) {
        (listOf(initialSubject) + availableSubjects + subjectCustomTimers.keys)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    var selectedSubject by remember { mutableStateOf(initialSubject.ifBlank { distinctSubjects.firstOrNull() ?: "Algorithm Analysis" }) }
    val currentSavedCustom = subjectCustomTimers[selectedSubject] ?: 60
    var customMinutesText by remember(selectedSubject, currentSavedCustom) {
        mutableStateOf(currentSavedCustom.toString())
    }

    val presetMinutes = listOf(15, 25, 30, 45, 60, 75, 90, 120, 150, 180)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = PurplePrimary)
                Text(
                    text = "تايمر المادة",
                    style = MaterialTheme.typography.titleMedium,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "المادة:",
                    style = MaterialTheme.typography.labelLarge,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    distinctSubjects.forEach { subj ->
                        val isSelected = selectedSubject == subj
                        val customMinsForSubj = subjectCustomTimers[subj] ?: 60
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSubject = subj
                                customMinutesText = (subjectCustomTimers[subj] ?: 60).toString()
                            },
                            label = {
                                Text("$subj (مخصص: ${customMinsForSubj}د | أساسي: 90د)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = selectedSubject,
                    onValueChange = { selectedSubject = it },
                    label = { Text("اسم المادة أو الجلسة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "المدة (دقيقة):",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetMinutes.forEach { mins ->
                        val isChosen = customMinutesText.toIntOrNull() == mins
                        FilterChip(
                            selected = isChosen,
                            onClick = { customMinutesText = mins.toString() },
                            label = {
                                Text("$mins د")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = customMinutesText,
                    onValueChange = { customMinutesText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("المدة (دقيقة)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_subject_timer_minutes_input")
                )

                // Quick Summary of Both Timers for the Subject
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PurplePrimary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "المادة: $selectedSubject",
                            style = MaterialTheme.typography.labelLarge,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• التايمر الأساسي الثابت: 90 دقيقة (ساعة ونصف)\n• التايمر المخصص لهذه المادة: ${customMinutesText.toIntOrNull() ?: 60} دقيقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        val mins = customMinutesText.toIntOrNull()?.coerceIn(1, 600) ?: 60
                        onSaveCustomTimer(selectedSubject, mins, true)
                        onDismiss()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_and_start_custom_timer_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ وبدء (${customMinutesText.ifBlank { "60" }} د)")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            val mins = customMinutesText.toIntOrNull()?.coerceIn(1, 600) ?: 60
                            onSaveCustomTimer(selectedSubject, mins, false)
                            onDismiss()
                        },
                        shape = CircleShape
                    ) {
                        Text("حفظ")
                    }

                    TextButton(onClick = onDismiss) {
                        Text("إغلاق")
                    }
                }
            }
        }
    )
}
