package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * محوّل ومنسّق الوقت بنظام 12 ساعة (ص / م) مع تخزين داخلي بنظام 24 ساعة (HH:mm)
 * لضمان دقة الجدولة 100%:
 * - 12:00 ص = 00:00 (منتصف الليل)
 * - 12:00 م = 12:00 (الظهر)
 */
object Time12hFormatter {

    data class Time12hParts(
        val hour12: Int,   // 1..12
        val minute: Int,   // 0..59
        val isPm: Boolean  // false = ص (AM), true = م (PM)
    ) {
        val periodAr: String get() = if (isPm) "م" else "ص"
        fun formatAr(): String = String.format(Locale.US, "%d:%02d %s", hour12, minute, periodAr)
        fun to24Hour(): String = to24HourString(hour12, minute, isPm)
    }

    /**
     * يحول الوقت من صيغة 24 ساعة (مثل "08:00" أو "14:30" أو "00:00" أو "12:00")
     * إلى أجزاء 12 ساعة (hour12 in 1..12, minute in 0..59, isPm).
     * إذا كان النص بالفعل بصيغة 12 ساعة (مثل "8:00 ص" أو "2:30 م") يحلله بدقة أيضاً.
     */
    fun parseTo12hParts(rawTime: String, defaultHour24: Int = 8, defaultMinute: Int = 0): Time12hParts {
        val trimmed = rawTime.trim()
        if (trimmed.isEmpty()) {
            return from24Hour(defaultHour24, defaultMinute)
        }

        val hasPmMarker = trimmed.contains("م") || trimmed.uppercase(Locale.US).contains("PM")
        val hasAmMarker = trimmed.contains("ص") || trimmed.uppercase(Locale.US).contains("AM")

        val cleanDigits = trimmed
            .replace("صباحاً", "")
            .replace("صباحا", "")
            .replace("مساءً", "")
            .replace("مساء", "")
            .replace("ص", "")
            .replace("م", "")
            .replace("AM", "", ignoreCase = true)
            .replace("PM", "", ignoreCase = true)
            .trim()

        val parts = cleanDigits.split(":")
        val parsedHour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: defaultHour24
        val parsedMin = parts.getOrNull(1)?.trim()?.take(2)?.toIntOrNull()?.coerceIn(0, 59) ?: defaultMinute

        return if (hasPmMarker || hasAmMarker) {
            val h12 = when {
                parsedHour <= 0 -> 12
                parsedHour > 12 -> ((parsedHour - 1) % 12) + 1
                else -> parsedHour
            }
            Time12hParts(hour12 = h12, minute = parsedMin, isPm = hasPmMarker)
        } else {
            from24Hour(parsedHour.coerceIn(0, 23), parsedMin)
        }
    }

    fun from24Hour(hour24: Int, minute: Int): Time12hParts {
        val h = ((hour24 % 24) + 24) % 24
        val m = minute.coerceIn(0, 59)
        val isPm = h >= 12
        val hour12 = when {
            h == 0 -> 12   // 00:00 -> 12:00 ص (منتصف الليل)
            h == 12 -> 12  // 12:00 -> 12:00 م (الظهر)
            h > 12 -> h - 12
            else -> h
        }
        return Time12hParts(hour12 = hour12, minute = m, isPm = isPm)
    }

    /**
     * يحول من نظام 12 ساعة (hour12: 1..12, minute: 0..59, isPm: Boolean)
     * إلى صيغة 24 ساعة القياسية "HH:mm":
     * - 12:00 ص (isPm = false) -> "00:00"
     * - 12:00 م (isPm = true)  -> "12:00"
     */
    fun to24HourString(hour12: Int, minute: Int, isPm: Boolean): String {
        val safeHour12 = hour12.coerceIn(1, 12)
        val safeMin = minute.coerceIn(0, 59)
        val hour24 = if (!isPm) {
            if (safeHour12 == 12) 0 else safeHour12
        } else {
            if (safeHour12 == 12) 12 else safeHour12 + 12
        }
        return String.format(Locale.US, "%02d:%02d", hour24, safeMin)
    }

    /**
     * يحول أي نص وقت (مثل "08:00" أو "14:30" أو "00:00" أو "12:00")
     * إلى صيغة العرض المطلوبة بنظام 12 ساعة: "8:00 ص" أو "2:30 م" أو "12:00 ص" أو "12:00 م".
     */
    fun format24To12Ar(time24: String): String {
        val trimmed = time24.trim()
        if (trimmed.isEmpty()) return ""
        // إذا كان النص يحتوي على نطاق وقت مثل "08:00 - 10:00"
        if (trimmed.contains("-")) {
            val segments = trimmed.split("-")
            if (segments.size == 2) {
                return "${format24To12Ar(segments[0])} - ${format24To12Ar(segments[1])}"
            }
        }
        return parseTo12hParts(trimmed).formatAr()
    }

    /**
     * يضمن أن القيمة المخزنة داخلياً بصيغة 24 ساعة "HH:mm"
     */
    fun normalizeTo24Hour(rawTime: String): String {
        return parseTo12hParts(rawTime).to24Hour()
    }

    fun to24hString(hour12: Int, minute: Int, isPm: Boolean): String {
        return to24HourString(hour12, minute, isPm)
    }

    fun formatMillisTo12Ar(millis: Long, includeDate: Boolean = false): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        val h24 = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val min = cal.get(java.util.Calendar.MINUTE)
        val time12 = from24Hour(h24, min).formatAr()
        return if (includeDate) {
            val year = cal.get(java.util.Calendar.YEAR)
            val month = cal.get(java.util.Calendar.MONTH) + 1
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            String.format(Locale.US, "%04d-%02d-%02d • %s", year, month, day, time12)
        } else {
            time12
        }
    }

    private val time24Regex = Regex("""(?<!\d)([01]?\d|2[0-3]):([0-5]\d)(?!\s*[صم])""")

    fun replaceAll24hTimesWith12hAr(text: String): String {
        if (text.isBlank()) return text
        if (text.endsWith(" ص") || text.endsWith(" م")) return text
        return time24Regex.replace(text) { matchResult ->
            val h = matchResult.groupValues[1].toIntOrNull() ?: return@replace matchResult.value
            val m = matchResult.groupValues[2].toIntOrNull() ?: return@replace matchResult.value
            from24Hour(h, m).formatAr()
        }
    }
}

/**
 * حقل اختيار وقت بنظام 12 ساعة يعرض الوقت بصيغة "8:00 ص" / "2:30 م"
 * وعند الضغط عليه يفتح نافذة منتقي الوقت 12 ساعة مع زر تبديل (ص / م) واضح،
 * ويعيد القيمة داخلياً بصيغة 24 ساعة ("HH:mm") لضمان دقة الجدولة.
 */
@Composable
fun TimePicker12hField(
    label: String,
    time24: String,
    onTime24Changed: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "time_picker_12h_field"
) {
    var showDialog by remember { mutableStateOf(false) }
    val display12h = remember(time24) { Time12hFormatter.format24To12Ar(time24) }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { showDialog = true }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = display12h.ifBlank { "8:00 ص" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showDialog) {
        TimePicker12hDialog(
            title = label,
            initialTime24 = time24,
            onDismiss = { showDialog = false },
            onConfirmTime24 = { newTime24 ->
                onTime24Changed(newTime24)
                showDialog = false
            }
        )
    }
}

/**
 * نافذة منتقي الوقت بنظام 12 ساعة (1..12) مع الدقائق (00..59)
 * وزر تبديل واضح بين صباحًا (ص) ومساءً (م).
 * تحول النتيجة النهائية إلى صيغة 24 ساعة ("HH:mm") للتخزين والجدولة:
 * - 12:00 ص = 00:00 (منتصف الليل)
 * - 12:00 م = 12:00 (الظهر)
 */
@Composable
fun TimePicker12hDialog(
    title: String,
    initialTime24: String,
    onDismiss: () -> Unit,
    onConfirmTime24: (String) -> Unit
) {
    val initialParts = remember(initialTime24) {
        Time12hFormatter.parseTo12hParts(initialTime24)
    }

    var hour12 by remember(initialParts) { mutableIntStateOf(initialParts.hour12) }
    var minute by remember(initialParts) { mutableIntStateOf(initialParts.minute) }
    var isPm by remember(initialParts) { mutableStateOf(initialParts.isPm) }

    val preview12h = remember(hour12, minute, isPm) {
        Time12hFormatter.Time12hParts(hour12, minute, isPm).formatAr()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = preview12h,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                            .testTag("time_picker_preview_12h")
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // أزرار تبديل صباحًا (ص) / مساءً (م) واضحة وكبيرة
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val amSelected = !isPm
                    val pmSelected = isPm

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isPm = false }
                            .testTag("am_toggle_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (amSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ) {
                        Text(
                            text = "صباحًا (ص)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (amSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isPm = true }
                            .testTag("pm_toggle_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (pmSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ) {
                        Text(
                            text = "مساءً (م)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (pmSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }

                // عدادات الساعة (1..12) والدقائق (00..59)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // الساعة (1..12)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = "الساعة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { hour12 = if (hour12 >= 12) 1 else hour12 + 1 },
                            modifier = Modifier.testTag("hour_up_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "زيادة الساعة")
                        }
                        Text(
                            text = String.format(Locale.US, "%d", hour12),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { hour12 = if (hour12 <= 1) 12 else hour12 - 1 },
                            modifier = Modifier.testTag("hour_down_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "إنقاص الساعة")
                        }
                    }

                    Text(
                        text = ":",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    // الدقائق (00..59)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = "الدقيقة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { minute = (minute + 1) % 60 },
                            modifier = Modifier.testTag("minute_up_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "زيادة الدقيقة")
                        }
                        Text(
                            text = String.format(Locale.US, "%02d", minute),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { minute = if (minute <= 0) 59 else minute - 1 },
                            modifier = Modifier.testTag("minute_down_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "إنقاص الدقيقة")
                        }
                    }
                }

                // اختصارات سريعة للدقائق الشائعة (00, 15, 30, 45)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 15, 30, 45).forEach { quickMin ->
                        FilterChip(
                            selected = minute == quickMin,
                            onClick = { minute = quickMin },
                            label = {
                                Text(
                                    text = String.format(Locale.US, ":%02d", quickMin),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // تذكير واضح لقاعدة 12:00 ص و 12:00 م
                if (hour12 == 12) {
                    Text(
                        text = if (!isPm) "12:${String.format(Locale.US, "%02d", minute)} ص = منتصف الليل"
                        else "12:${String.format(Locale.US, "%02d", minute)} م = وقت الظهر",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val saved24 = Time12hFormatter.to24HourString(hour12, minute, isPm)
                    onConfirmTime24(saved24)
                },
                modifier = Modifier.testTag("confirm_time_12h_button")
            ) {
                Text("تأكيد ($preview12h)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
