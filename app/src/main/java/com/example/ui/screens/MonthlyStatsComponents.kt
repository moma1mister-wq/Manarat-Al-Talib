package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SessionRatingEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class MonthlyPerformanceSummary(
    val monthKey: String,          // e.g. "2026-10"
    val monthLabelAr: String,      // e.g. "أكتوبر 2026"
    val sortOrder: Long,           // Year * 100 + Month
    val items: List<SessionRatingEntity>,
    val completedCount: Int,
    val totalCount: Int,
    val totalDurationMinutes: Int,
    val adherencePercent: Int,
    val averageStars: Float
)

private val arabicMonths = arrayOf(
    "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
    "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
)

fun buildMonthlySummaries(ratings: List<SessionRatingEntity>): List<MonthlyPerformanceSummary> {
    if (ratings.isEmpty()) return emptyList()

    val grouped = ratings.groupBy { rating ->
        val cal = Calendar.getInstance().apply { timeInMillis = rating.timestamp }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0..11
        Triple(year, month, String.format(Locale.US, "%04d-%02d", year, month + 1))
    }

    return grouped.map { (keyTriple, list) ->
        val (year, monthZeroBased, monthKey) = keyTriple
        val sortedItems = list.sortedByDescending { it.timestamp }
        val completed = sortedItems.count { it.isCompleted }
        val total = sortedItems.size
        val totalMinutes = sortedItems.filter { it.isCompleted }.sumOf { it.durationMinutes }
        val adherence = if (total > 0) ((completed.toFloat() / total.toFloat()) * 100f).roundToInt() else 0
        val avgStars = if (total > 0) sortedItems.map { it.stars }.average().toFloat() else 0f
        MonthlyPerformanceSummary(
            monthKey = monthKey,
            monthLabelAr = "${arabicMonths[monthZeroBased]} $year",
            sortOrder = year * 100L + monthZeroBased,
            items = sortedItems,
            completedCount = completed,
            totalCount = total,
            totalDurationMinutes = totalMinutes,
            adherencePercent = adherence,
            averageStars = avgStars
        )
    }.sortedByDescending { it.sortOrder }
}

fun formatDurationAr(minutes: Int): String {
    if (minutes <= 0) return "0 دقيقة"
    val hours = minutes / 60
    val remMinutes = minutes % 60
    return when {
        hours > 0 && remMinutes > 0 -> "$hours ساعة و $remMinutes د"
        hours > 0 -> "$hours ساعة (${minutes} د)"
        else -> "$minutes دقيقة"
    }
}

fun formatExactDateTimeAr(timestamp: Long): String {
    return com.example.ui.components.Time12hFormatter.formatMillisTo12Ar(timestamp, includeDate = true)
}

@Composable
fun MonthlyAchievementsAndComparisonCard(
    ratings: List<SessionRatingEntity>,
    onOpenFullReport: () -> Unit,
    onOpenAddAchievement: () -> Unit,
    onDeleteAchievement: ((SessionRatingEntity) -> Unit)? = null
) {
    val monthlySummaries = remember(ratings) { buildMonthlySummaries(ratings) }
    var selectedMonthIndex by remember(monthlySummaries.size) { mutableIntStateOf(0) }

    val safeIndex = selectedMonthIndex.coerceIn(0, (monthlySummaries.size - 1).coerceAtLeast(0))
    val currentSummary = monthlySummaries.getOrNull(safeIndex)
    val previousSummary = monthlySummaries.getOrNull(safeIndex + 1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_achievements_comparison_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "سجل الإنجازات والمقارنة",
                        style = MaterialTheme.typography.titleMedium,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = onOpenAddAchievement,
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.14f),
                        contentColor = EmeraldPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_achievement_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة إنجاز",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إنجاز جديد", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            if (monthlySummaries.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PurplePrimary.copy(alpha = 0.06f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_monthly_achievements_placeholder")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "سجل الإنجازات والمقارنات فارغ حالياً",
                            style = MaterialTheme.typography.titleSmall,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "بمجرد تسجيل أول جلسة مذاكرة أو الضغط على «إنجاز جديد»، سيبدأ التطبيق بحفظ إحصائياتك وعمل مقارنة تلقائية لمستواك.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Month selector tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    monthlySummaries.forEachIndexed { index, summary ->
                        val isSelected = index == safeIndex
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) PurplePrimary else PurplePrimary.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { selectedMonthIndex = index }
                                .testTag("month_tab_${summary.monthKey}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else PurplePrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = summary.monthLabelAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else PurplePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color.White.copy(alpha = 0.22f) else EmeraldPrimary.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "${summary.completedCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else EmeraldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected Month Summary & Comparison with Previous Month
                if (currentSummary != null) {
                    val durationDelta = if (previousSummary != null && previousSummary.totalDurationMinutes > 0) {
                        (((currentSummary.totalDurationMinutes - previousSummary.totalDurationMinutes).toFloat() /
                                previousSummary.totalDurationMinutes.toFloat()) * 100f).roundToInt()
                    } else null

                    val adherenceDelta = if (previousSummary != null) {
                        currentSummary.adherencePercent - previousSummary.adherencePercent
                    } else null

                    val countDelta = if (previousSummary != null) {
                        currentSummary.completedCount - previousSummary.completedCount
                    } else null

                    // Month-to-Month Comparison Box
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = PurplePrimary.copy(alpha = 0.06f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 مقارنة أداء ${currentSummary.monthLabelAr}" +
                                            (previousSummary?.let { " مع ${it.monthLabelAr}" } ?: ""),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (durationDelta != null) {
                                    val isPositive = durationDelta >= 0
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isPositive) EmeraldPrimary.copy(alpha = 0.16f) else GoldAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (isPositive) "📈 +$durationDelta% في الساعات" else "📉 $durationDelta% في الساعات",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isPositive) EmeraldPrimary else Color(0xFFB45309),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // 3 Comparison Metric Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ComparisonMetricChip(
                                    modifier = Modifier.weight(1f),
                                    title = "إجمالي المدة",
                                    currentValue = formatDurationAr(currentSummary.totalDurationMinutes),
                                    deltaLabel = previousSummary?.let {
                                        "السابق: ${formatDurationAr(it.totalDurationMinutes)}"
                                    } ?: "أول شهر مسجل",
                                    accentColor = EmeraldPrimary
                                )
                                ComparisonMetricChip(
                                    modifier = Modifier.weight(1f),
                                    title = "عدد الإنجازات",
                                    currentValue = "${currentSummary.completedCount} إنجاز",
                                    deltaLabel = countDelta?.let {
                                        if (it >= 0) "+$it عن الشهر السابق" else "$it عن الشهر السابق"
                                    } ?: "من أصل ${currentSummary.totalCount}",
                                    accentColor = PurplePrimary
                                )
                                ComparisonMetricChip(
                                    modifier = Modifier.weight(1f),
                                    title = "نسبة الالتزام",
                                    currentValue = "${currentSummary.adherencePercent}%",
                                    deltaLabel = adherenceDelta?.let {
                                        if (it >= 0) "+$it% تحسن" else "$it% فرق"
                                    } ?: "★ ${String.format(Locale.US, "%.1f", currentSummary.averageStars)}",
                                    accentColor = GoldAccent
                                )
                            }

                            // Visual Bar Comparison Across All Months
                            if (monthlySummaries.size > 1) {
                                HorizontalDivider(color = PurplePrimary.copy(alpha = 0.1f))
                                Text(
                                    text = "مؤشر مقارنة الشهور (إجمالي دقائق وساعات الإنجاز):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val maxMinutes = monthlySummaries.maxOf { it.totalDurationMinutes }.coerceAtLeast(60)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    monthlySummaries.forEachIndexed { idx, monthItem ->
                                        val progress = (monthItem.totalDurationMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.08f, 1f)
                                        val isCurr = idx == safeIndex
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${monthItem.monthLabelAr} (${monthItem.completedCount} إنجاز)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCurr) PurplePrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${formatDurationAr(monthItem.totalDurationMinutes)} • التزام ${monthItem.adherencePercent}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isCurr) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                color = if (isCurr) EmeraldPrimary else PurpleSoft,
                                                trackColor = PurplePrimary.copy(alpha = 0.1f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(7.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Monthly Achievements Detailed List (Name, Exact Date/Time, Exact Duration)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 قائمة إنجازات ${currentSummary.monthLabelAr} (${currentSummary.items.size}):",
                            style = MaterialTheme.typography.labelLarge,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "الاسم • الوقت • المدة",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentSummary.items.take(5).forEach { achievement ->
                            AchievementTimelineRow(
                                achievement = achievement,
                                onDelete = onDeleteAchievement
                            )
                        }
                    }

                    // Button to open full report dialog
                    Button(
                        onClick = onOpenFullReport,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_full_monthly_report_btn")
                    ) {
                        Text(
                            text = "عرض التقرير الشهري (${ratings.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonMetricChip(
    modifier: Modifier = Modifier,
    title: String,
    currentValue: String,
    deltaLabel: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = currentValue,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = deltaLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AchievementTimelineRow(
    achievement: SessionRatingEntity,
    onDelete: ((SessionRatingEntity) -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (achievement.isCompleted) {
            EmeraldPrimary.copy(alpha = 0.07f)
        } else {
            GoldAccent.copy(alpha = 0.10f)
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (achievement.isCompleted) EmeraldPrimary.copy(alpha = 0.2f) else GoldAccent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Row: Achievement Name + Duration Badge + Optional Delete
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
                        imageVector = if (achievement.isCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (achievement.isCompleted) EmeraldPrimary else GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = achievement.subjectTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Duration Pill
                    Surface(
                        shape = CircleShape,
                        color = PurplePrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "⏱ المدة: ${formatDurationAr(achievement.durationMinutes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = { onDelete(achievement) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Middle Row: Exact Date & Time + Star Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗓 الوقت: ${formatExactDateTimeAr(achievement.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "★".repeat(achievement.stars.coerceIn(1, 5)),
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldAccent
                )
            }

            // Bottom Row: Notes / Description
            if (achievement.note.isNotBlank()) {
                Text(
                    text = "📝 ${achievement.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MonthlyAchievementsFullReportDialog(
    ratings: List<SessionRatingEntity>,
    onDismiss: () -> Unit,
    onAddAchievementClick: () -> Unit,
    onDeleteAchievement: ((SessionRatingEntity) -> Unit)? = null
) {
    val monthlySummaries = remember(ratings) { buildMonthlySummaries(ratings) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Monthly List, 1 = Month-to-Month Comparison
    var selectedMonthIndex by remember(monthlySummaries.size) { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(PurpleDeep, PurplePrimary, PurpleSoft)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "التقرير الشهري للإنجازات",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.16f), CircleShape)
                                    .size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = Color.White
                                )
                            }
                        }

                        // Switch Tabs inside Dialog
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { selectedTab = 0 },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTab == 0) EmeraldPrimary else Color.White.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📋 لستة إنجازات كل شهر", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = { selectedTab = 1 },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTab == 1) EmeraldPrimary else Color.White.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📊 مقارنة شهر بشهر", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // TAB 0: Monthly Achievements List after each month
                    val safeIndex = selectedMonthIndex.coerceIn(0, (monthlySummaries.size - 1).coerceAtLeast(0))
                    val currentSummary = monthlySummaries.getOrNull(safeIndex)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Month selector chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            monthlySummaries.forEachIndexed { idx, summary ->
                                val selected = idx == safeIndex
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedMonthIndex = idx },
                                    label = {
                                        Text(
                                            "${summary.monthLabelAr} (${summary.completedCount} إنجاز)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                            }
                        }

                        if (currentSummary != null) {
                            // Summary Banner for Selected Month
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = PurplePrimary.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "حصيلة شهر ${currentSummary.monthLabelAr}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "إجمالي المدة: ${formatDurationAr(currentSummary.totalDurationMinutes)} • التزام ${currentSummary.adherencePercent}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    FilledTonalButton(
                                        onClick = onAddAchievementClick,
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("إضافة إنجاز", maxLines = 1, softWrap = false)
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentSummary.items, key = { it.id }) { item ->
                                    AchievementTimelineRow(
                                        achievement = item,
                                        onDelete = onDeleteAchievement
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: Comprehensive Month-to-Month Performance Comparison
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            val bestMonth = monthlySummaries.maxByOrNull { it.totalDurationMinutes }
                            if (bestMonth != null) {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = EmeraldPrimary.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "🌟 أفضل شهر في الأداء: ${bestMonth.monthLabelAr}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "أنجزت فيه ${bestMonth.completedCount} مهمة بإجمالي مدة ${formatDurationAr(bestMonth.totalDurationMinutes)} ونسبة التزام ${bestMonth.adherencePercent}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        items(monthlySummaries.size) { index ->
                            val curr = monthlySummaries[index]
                            val prev = monthlySummaries.getOrNull(index + 1)
                            val diffMinutes = prev?.let { curr.totalDurationMinutes - it.totalDurationMinutes }
                            val diffAdherence = prev?.let { curr.adherencePercent - it.adherencePercent }
                            val diffCount = prev?.let { curr.completedCount - it.completedCount }

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                        Text(
                                            text = "📅 ${curr.monthLabelAr}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldPrimary.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = "التقييم العام ★ ${String.format(Locale.US, "%.1f", curr.averageStars)}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• عدد الإنجازات المكتملة: ${curr.completedCount} من ${curr.totalCount}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "• نسبة الالتزام: ${curr.adherencePercent}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "• إجمالي مدة الإنجازات في الشهر: ${formatDurationAr(curr.totalDurationMinutes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PurplePrimary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (prev != null) {
                                        HorizontalDivider(color = PurplePrimary.copy(alpha = 0.12f))
                                        Text(
                                            text = "🔄 المقارنة مع الشهر السابق (${prev.monthLabelAr}):",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val minText = if ((diffMinutes ?: 0) >= 0) {
                                            "زيادة +${formatDurationAr(diffMinutes ?: 0)} في وقت المذاكرة والإنجاز 📈"
                                        } else {
                                            "أقل بـ ${formatDurationAr(-(diffMinutes ?: 0))} عن الشهر السابق 📉"
                                        }
                                        val adhText = if ((diffAdherence ?: 0) >= 0) {
                                            "تحسن +${diffAdherence}% في الالتزام"
                                        } else {
                                            "${diffAdherence}% في نسبة الالتزام"
                                        }
                                        val cntText = if ((diffCount ?: 0) >= 0) {
                                            "+${diffCount} إنجاز إضافي"
                                        } else {
                                            "${diffCount} إنجاز"
                                        }
                                        Text(
                                            text = "$minText • $adhText • ($cntText)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if ((diffMinutes ?: 0) >= 0) EmeraldPrimary else Color(0xFFB45309),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
