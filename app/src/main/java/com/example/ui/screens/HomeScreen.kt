package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AdhkarAlarmEntity
import com.example.data.local.CatalogItemEntity
import com.example.data.local.ScheduleItemEntity
import com.example.data.local.SessionRatingEntity
import com.example.domain.DailyPrayerSchedule
import com.example.domain.PrayerCalcMethod
import com.example.notifications.AlarmCategory
import com.example.ui.components.CustomSectionIcon
import com.example.ui.components.MintCircleIndicator
import com.example.ui.components.PurpleCircleIndicator
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.components.ReferenceHeaderTopBar
import com.example.ui.components.SectionIconType
import com.example.ui.components.Time12hFormatter
import com.example.ui.components.TimePicker12hDialog
import com.example.ui.components.VerticalConnectorDots
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSoft
import com.example.ui.viewmodel.ActiveStudyTimerState
import com.example.ui.viewmodel.MainNavTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    userName: String,
    sectionNumber: Int,
    todayDayNameAr: String,
    scheduleItems: List<ScheduleItemEntity>,
    prayerSchedule: DailyPrayerSchedule,
    studyTimer: ActiveStudyTimerState,
    subjectCustomTimers: Map<String, Int> = emptyMap(),
    adhkarAlarms: List<AdhkarAlarmEntity>,
    hisnItems: List<CatalogItemEntity>,
    ratings: List<SessionRatingEntity>,
    showPrayerSubScreen: Boolean,
    showAdhkarCounterModal: Boolean,
    prayerManualOffsets: Map<String, Int>,
    prayerEnabledMap: Map<String, Boolean> = emptyMap(),
    onNavigateTab: (MainNavTab) -> Unit,
    onOpenPrayerScreen: (Boolean) -> Unit,
    onOpenAdhkarModal: (Boolean) -> Unit,
    onStartStudyTimer: (String, Int, String) -> Unit,
    onSwapStudySubject: (String, Int, String) -> Unit = { _, _, _ -> },
    onSyncCurrentSubjectToToday: () -> Unit = {},
    onSetCustomSubjectTimer: (String, Int, Boolean) -> Unit = { _, _, _ -> },
    onToggleTimerPause: () -> Unit,
    onResetTimer: (Int) -> Unit,
    onOpenRatingModal: (String) -> Unit,
    onSubmitAchievement: (String, Int, Boolean, String, Int) -> Unit = { _, _, _, _, _ -> },
    onDeleteAchievement: (SessionRatingEntity) -> Unit = {},
    onClearUserRatings: () -> Unit = {},
    onTestSound: (AlarmCategory, String) -> Unit,
    onSaveAdhkarAlarm: (AdhkarAlarmEntity) -> Unit = {},
    onAdjustPrayerMinute: (String, Int) -> Unit,
    onSetPrayerCustomTime: (String, String) -> Unit = { _, _ -> },
    onTogglePrayerEnabled: (String, Boolean) -> Unit = { _, _ -> },
    onChangePrayerMethod: (PrayerCalcMethod) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statsPeriod by remember { mutableStateOf("شهري") } // يومي / أسبوعي / شهري
    var showMonthlyReportDialog by remember { mutableStateOf(false) }
    var showAddAchievementDialog by remember { mutableStateOf(false) }
    var showCustomSubjectTimerDialog by remember { mutableStateOf(false) }
    var customTimerTargetSubject by remember { mutableStateOf(studyTimer.subjectTitle) }

    if (showPrayerSubScreen) {
        PrayerDetailSubScreen(
            prayerSchedule = prayerSchedule,
            manualOffsets = prayerManualOffsets,
            prayerEnabledMap = prayerEnabledMap,
            onBack = { onOpenPrayerScreen(false) },
            onTestAdhan = { onTestSound(AlarmCategory.PRAYER, "صلاة ${prayerSchedule.nextPrayer.arabicName}") },
            onAdjustOffset = onAdjustPrayerMinute,
            onSetPrayerCustomTime = onSetPrayerCustomTime,
            onTogglePrayerEnabled = onTogglePrayerEnabled,
            onChangeMethod = onChangePrayerMethod
        )
        return
    }

    val todayOnlyClasses = remember(scheduleItems, sectionNumber, todayDayNameAr) {
        scheduleItems.filter { item ->
            (item.sectionFilter == 0 || item.sectionFilter == sectionNumber) &&
                (item.dayOfWeek == todayDayNameAr || item.dayOfWeek == "يومي")
        }.sortedBy { com.example.ui.viewmodel.AppViewModel.parseTimeToMinutesOrNull(it.startTime) ?: 9999 }
    }

    val filteredTodayClasses = remember(scheduleItems, sectionNumber, todayDayNameAr, searchQuery) {
        val base = scheduleItems.filter { item ->
            (item.sectionFilter == 0 || item.sectionFilter == sectionNumber) &&
                (searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.dayOfWeek.contains(searchQuery))
        }
        val todayOnly = base.filter { it.dayOfWeek == todayDayNameAr || it.dayOfWeek == "يومي" }
        if (searchQuery.isNotBlank()) base else todayOnly.ifEmpty { base.take(4) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PurpleWaveBackground(height = 295.dp, showDiamondGrid = true)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reference Left Phone Header + Two Floating White Cards + Pill Primary Button
            item {
                val totalSelectedMins = (studyTimer.totalSeconds / 60).coerceAtLeast(1)
                val timerMins = studyTimer.remainingSeconds / 60
                val timerSecs = studyTimer.remainingSeconds % 60
                val subjectItems = remember(scheduleItems, sectionNumber) {
                    scheduleItems.filter { it.sectionFilter == 0 || it.sectionFilter == sectionNumber }
                }
                val subjectNames = remember(subjectItems) {
                    subjectItems.map { it.title }.distinct()
                }
                val todayResolvedItem = remember(subjectItems, sectionNumber, todayDayNameAr) {
                    com.example.ui.viewmodel.AppViewModel.resolveScheduledCurrentItem(subjectItems, sectionNumber)
                }
                val matchedScheduleSlot = remember(subjectItems, todayDayNameAr, studyTimer.subjectTitle) {
                    val todayMatches = subjectItems
                        .filter {
                            it.title == studyTimer.subjectTitle &&
                                (it.dayOfWeek == todayDayNameAr || it.dayOfWeek == "يومي")
                        }
                        .sortedBy { com.example.ui.viewmodel.AppViewModel.parseTimeToMinutesOrNull(it.startTime) ?: 9999 }
                    if (todayMatches.isNotEmpty()) {
                        com.example.ui.viewmodel.AppViewModel.resolveScheduledCurrentItem(todayMatches, sectionNumber)
                            ?: todayMatches.first()
                    } else {
                        subjectItems.firstOrNull { it.title == studyTimer.subjectTitle }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top bar with circular white profile avatar and 3 dots (• • •)
                    ReferenceHeaderTopBar(
                        onProfileOrSettingsClick = { onNavigateTab(MainNavTab.SETTINGS) },
                        onMoreClick = { onNavigateTab(MainNavTab.SCHEDULE) }
                    )

                    // Greeting & App Title ("منارة الطالب") with frosted section badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مرحباً، $userName 👋",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                            Text(
                                text = "منارة الطالب",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "السكشن $sectionNumber • $todayDayNameAr",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // FLOATING WHITE CARD 1: FROM (Next Prayer) / TO (Current Subject) + Purple Swap Circle Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Row 1: Mint-Green Circle + Current/Upcoming Prayer + Live Decreasing Countdown Timer
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenPrayerScreen(true) }
                                        .testTag("home_top_prayer_countdown_row")
                                ) {
                                    MintCircleIndicator(size = 34.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "الصلاة الحالية: صلاة ${prayerSchedule.nextPrayer.arabicName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "المؤقت: ${prayerSchedule.countdownHms} • متبقي ${prayerSchedule.countdownArabicDetailed} (${prayerSchedule.nextPrayer.timeFormatted})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { prayerSchedule.remainingFraction },
                                            color = EmeraldPrimary,
                                            trackColor = EmeraldPrimary.copy(alpha = 0.16f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                }

                                VerticalConnectorDots(height = 16.dp)

                                // Row 2: Royal-Purple Circle + Active Study Subject Synchronized with Schedule
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateTab(MainNavTab.SCHEDULE) }
                                        .testTag("home_top_current_subject_row")
                                ) {
                                    PurpleCircleIndicator(size = 34.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "المادة الحالية • $todayDayNameAr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = studyTimer.subjectTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (matchedScheduleSlot != null) {
                                            val isTodayMatch = matchedScheduleSlot.dayOfWeek == todayDayNameAr ||
                                                matchedScheduleSlot.dayOfWeek == "يومي"
                                            Text(
                                                text = if (isTodayMatch) {
                                                    "جدول اليوم ($todayDayNameAr): ${Time12hFormatter.format24To12Ar(matchedScheduleSlot.startTime)} - ${Time12hFormatter.format24To12Ar(matchedScheduleSlot.endTime)} • ${matchedScheduleSlot.location}"
                                                } else {
                                                    "لا توجد مواد اليوم ($todayDayNameAr) • القادم (${matchedScheduleSlot.dayOfWeek}): ${Time12hFormatter.format24To12Ar(matchedScheduleSlot.startTime)}"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PurplePrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (todayResolvedItem != null && todayResolvedItem.title != studyTimer.subjectTitle) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = EmeraldPrimary.copy(alpha = 0.14f),
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable { onSyncCurrentSubjectToToday() }
                                                    .testTag("sync_today_subject_chip")
                                            ) {
                                                Text(
                                                    text = "مزامنة مع جدول اليوم ($todayDayNameAr) ↺",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Circular Purple Swap/Cycle Button (50.dp) cycling through scheduled subjects
                            Surface(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (subjectNames.isNotEmpty()) {
                                            val idx = subjectNames.indexOf(studyTimer.subjectTitle)
                                            val nextSubj = subjectNames[(idx + 1).coerceAtLeast(0) % subjectNames.size]
                                            val slot = subjectItems.firstOrNull { it.title == nextSubj }
                                            val nextMins = subjectCustomTimers[nextSubj] ?: slot?.timerMinutes ?: 90
                                            val nextTone = slot?.customToneUri ?: studyTimer.customToneUri
                                            onSwapStudySubject(nextSubj, nextMins, nextTone)
                                        } else {
                                            onNavigateTab(MainNavTab.SCHEDULE)
                                        }
                                    },
                                shape = CircleShape,
                                color = PurplePrimary,
                                shadowElevation = 6.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "تبديل المادة التالية",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // FLOATING WHITE CARD 2: Timer Stepper (• 90 •) + Section TYPE + Today's Schedule Summary
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Top Row: Green Circle + Stepper (- 90 +) | TYPE (AI • Section)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "المؤقت (دقيقة)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        val updated = (totalSelectedMins - 15).coerceAtLeast(15)
                                                        onSetCustomSubjectTimer(studyTimer.subjectTitle, updated, false)
                                                    },
                                                shape = CircleShape,
                                                color = EmeraldPrimary
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Remove,
                                                        contentDescription = "تقليل الوقت",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "%02d".format(totalSelectedMins),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        val updated = (totalSelectedMins + 15).coerceAtMost(180)
                                                        onSetCustomSubjectTimer(studyTimer.subjectTitle, updated, false)
                                                    },
                                                shape = CircleShape,
                                                color = EmeraldPrimary
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "زيادة الوقت",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(0.8f),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "السكشن",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "سكشن $sectionNumber",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(PurplePrimary.copy(alpha = 0.78f))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "جدول اليوم",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$todayDayNameAr (${todayOnlyClasses.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onToggleTimerPause,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .height(54.dp)
                            .shadow(10.dp, CircleShape)
                    ) {
                        Text(
                            text = if (studyTimer.isRunning) {
                                "إيقاف • %02d:%02d".format(timerMins, timerSecs)
                            } else {
                                "ابدأ المذاكرة (%02d:%02d)".format(timerMins, timerSecs)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = PurplePrimary
                            )
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, CircleShape)
                            .testTag("home_search_input")
                    )
                }
            }

            // Custom Icons Quick Access Row (الجدول، الصلاة، الأذكار، المصحف، التمارين، حصن المسلم)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "الأقسام الرئيسية السريعة",
                            style = MaterialTheme.typography.titleMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickSectionBadge("الجدول", SectionIconType.SCHEDULE, Modifier.weight(1f)) {
                                onNavigateTab(MainNavTab.SCHEDULE)
                            }
                            QuickSectionBadge("الصلاة", SectionIconType.PRAYER, Modifier.weight(1f)) {
                                onOpenPrayerScreen(true)
                            }
                            QuickSectionBadge("الأذكار", SectionIconType.ADHKAR, Modifier.weight(1f)) {
                                onNavigateTab(MainNavTab.PRICES)
                            }
                            QuickSectionBadge("المصحف", SectionIconType.QURAN, Modifier.weight(1f)) {
                                onNavigateTab(MainNavTab.QURAN)
                            }
                            QuickSectionBadge("التمارين", SectionIconType.EXERCISES, Modifier.weight(1f)) {
                                onNavigateTab(MainNavTab.EXERCISES)
                            }
                            QuickSectionBadge("السبحة", SectionIconType.ADHKAR, Modifier.weight(1f)) {
                                onOpenAdhkarModal(true)
                            }
                        }
                    }
                }
            }

            // Permissions Verification Card moved to SettingsScreen & Next Prayer Card moved to PrayerDetailSubScreen per user layout request

            // Current Study Session & Timer Card (Base 90m Timer + Custom Per-Subject Timer)
            item {
                val mins = studyTimer.remainingSeconds / 60
                val secs = studyTimer.remainingSeconds % 60
                val totalSelectedMins = (studyTimer.totalSeconds / 60).coerceAtLeast(1)
                val customSubjectMins = subjectCustomTimers[studyTimer.subjectTitle] ?: 60
                val progress = if (studyTimer.totalSeconds > 0) {
                    1f - (studyTimer.remainingSeconds.toFloat() / studyTimer.totalSeconds.toFloat())
                } else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = if (totalSelectedMins == 90) {
                                        "جلسة المذاكرة • التايمر الأساسي (90 د)"
                                    } else {
                                        "جلسة المذاكرة • تايمر مخصص ($totalSelectedMins د)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = studyTimer.subjectTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val endAt12h = Time12hFormatter.formatMillisTo12Ar(
                                    System.currentTimeMillis() + (studyTimer.remainingSeconds * 1000L),
                                    includeDate = false
                                )
                                Text(
                                    text = "موعد الانتهاء المتوقع: $endAt12h",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = PurplePrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "%02d:%02d".format(mins, secs),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            color = EmeraldPrimary,
                            trackColor = PurpleSoft.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch between Base Timer (90m) vs Subject Custom Timer vs Customizing per Subject
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = totalSelectedMins == 90,
                                onClick = {
                                    onStartStudyTimer(studyTimer.subjectTitle, 90, studyTimer.customToneUri)
                                },
                                label = { Text("التايمر الأساسي (90 د)", maxLines = 1, softWrap = false) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = totalSelectedMins == customSubjectMins && customSubjectMins != 90,
                                onClick = {
                                    onStartStudyTimer(studyTimer.subjectTitle, customSubjectMins, studyTimer.customToneUri)
                                },
                                label = { Text("تايمر المادة المخصص ($customSubjectMins د)", maxLines = 1, softWrap = false) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                            listOf(25, 45, 60, 120).forEach { preset ->
                                FilterChip(
                                    selected = totalSelectedMins == preset,
                                    onClick = {
                                        onSetCustomSubjectTimer(studyTimer.subjectTitle, preset, true)
                                    },
                                    label = { Text("${preset}د", maxLines = 1, softWrap = false) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FilledTonalButton(
                            onClick = {
                                customTimerTargetSubject = studyTimer.subjectTitle
                                showCustomSubjectTimerDialog = true
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = PurplePrimary.copy(alpha = 0.10f),
                                contentColor = PurplePrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customize_subject_timer_btn")
                        ) {
                            Text(
                                text = "تايمر مخصص",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onToggleTimerPause,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (studyTimer.isRunning) GoldAccent else PurplePrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("study_timer_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (studyTimer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (studyTimer.isRunning) "إيقاف مؤقت" else "ابدأ الجلسة",
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            OutlinedButton(
                                onClick = { onResetTimer(totalSelectedMins) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط")
                            }

                            OutlinedButton(
                                onClick = { onOpenRatingModal(studyTimer.subjectTitle) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier.testTag("open_rating_modal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "تقييم",
                                    tint = GoldAccent
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تقييم",
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Today's University Schedule Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "جدول اليوم والمحاضرات (سكشن $sectionNumber)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { onNavigateTab(MainNavTab.SCHEDULE) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "عرض الجدول الكامل ←",
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (todayOnlyClasses.isEmpty() && searchQuery.isBlank() && filteredTodayClasses.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PurplePrimary.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "لا توجد حصص مجدولة يوم $todayDayNameAr • الحصص القادمة في الأسبوع:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        filteredTodayClasses.forEach { item ->
                            val subjCustomMins = subjectCustomTimers[item.title]
                                ?: if (item.timerMinutes != 90) item.timerMinutes else 60
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${item.dayOfWeek} • ${Time12hFormatter.format24To12Ar(item.startTime)} - ${Time12hFormatter.format24To12Ar(item.endTime)} • ${item.location}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = PurplePrimary,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable {
                                                    onStartStudyTimer(item.title, 90, item.customToneUri)
                                                }
                                        ) {
                                            Text(
                                                text = "▶ التايمر الأساسي (90د)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldPrimary,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable {
                                                    onStartStudyTimer(item.title, subjCustomMins, item.customToneUri)
                                                }
                                        ) {
                                            Text(
                                                text = "▶ تايمر مخصص (${subjCustomMins}د)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = PurplePrimary.copy(alpha = 0.14f),
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable {
                                                    customTimerTargetSubject = item.title
                                                    showCustomSubjectTimerDialog = true
                                                }
                                        ) {
                                            Text(
                                                text = "تغيير ⏱",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PurplePrimary,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section [4] Statistics & Commitment Rate Per Subject (Daily / Weekly / Monthly)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_statistics_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إحصائيات التقييم ومعدل الالتزام بالمواد 📊",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (ratings.isNotEmpty()) {
                                TextButton(
                                    onClick = onClearUserRatings,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("clear_user_stats_button")
                                ) {
                                    Text(
                                        text = "تصفير",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("يومي", "أسبوعي", "شهري").forEach { period ->
                                FilterChip(
                                    selected = statsPeriod == period,
                                    onClick = { statsPeriod = period },
                                    label = { Text(period) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val now = System.currentTimeMillis()
                        val windowMs = when (statsPeriod) {
                            "يومي" -> 86_400_000L
                            "أسبوعي" -> 86_400_000L * 7
                            else -> 86_400_000L * 30
                        }
                        val cutoffMs = now - windowMs
                        val prevCutoffMs = cutoffMs - windowMs

                        val periodRatings = ratings.filter { it.timestamp >= cutoffMs }
                        val prevPeriodRatings = ratings.filter { it.timestamp in prevCutoffMs until cutoffMs }

                        val completedCount = periodRatings.count { it.isCompleted }
                        val totalCount = periodRatings.size
                        val adherencePercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0
                        val avgStars = if (totalCount > 0) {
                            periodRatings.map { it.stars }.average().takeIf { !it.isNaN() } ?: 0.0
                        } else {
                            0.0
                        }
                        val totalMinutesInPeriod = periodRatings.filter { it.isCompleted }.sumOf { it.durationMinutes }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatSummaryPill("معدل الالتزام", "$adherencePercent%", EmeraldPrimary, Modifier.weight(1f))
                            StatSummaryPill("متوسط التقييم", "%.1f ⭐".format(avgStars), GoldAccent, Modifier.weight(1f))
                            StatSummaryPill("الجلسات المنجزة", "$completedCount / $totalCount", PurplePrimary, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (ratings.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = PurplePrimary.copy(alpha = 0.06f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("empty_statistics_placeholder")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "لا توجد إحصائيات مسجلة بعد لهذا الحساب",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = PurplePrimary,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "ابدأ جلسة مذاكرة أو قيّم جلستك أو أضف إنجازاً جديداً، وسيقوم التطبيق بحفظ إحصائياتك ومقارنتها تلقائياً من أول استخدام.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else if (periodRatings.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = PurplePrimary.copy(alpha = 0.06f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "لا توجد جلسات مسجلة في النطاق ($statsPeriod) حالياً. إجمالي جلساتك المحفوظة: ${ratings.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            // Dynamic comparison summary banner based on user's actual recorded sessions
                            val comparisonText = when {
                                prevPeriodRatings.isNotEmpty() -> {
                                    val prevMins = prevPeriodRatings.filter { it.isCompleted }.sumOf { it.durationMinutes }
                                    val diffMins = totalMinutesInPeriod - prevMins
                                    if (diffMins >= 0) {
                                        "📈 مقارنة بـ ($statsPeriod) السابق: +${formatDurationAr(diffMins)} في وقت الإنجاز"
                                    } else {
                                        "📉 مقارنة بـ ($statsPeriod) السابق: فرق ${formatDurationAr(-diffMins)}"
                                    }
                                }
                                periodRatings.size >= 2 -> {
                                    val latest = periodRatings.first()
                                    val previous = periodRatings[1]
                                    val diffMins = latest.durationMinutes - previous.durationMinutes
                                    if (diffMins >= 0) {
                                        "📈 مقارنة آخر جلسة (${latest.subjectTitle}) بالجلسة السابقة: +$diffMins دقيقة (${latest.stars}⭐ مقابل ${previous.stars}⭐)"
                                    } else {
                                        "📊 مقارنة آخر جلسة (${latest.subjectTitle}) بالجلسة السابقة: فرق ${-diffMins} دقيقة (${latest.stars}⭐ مقابل ${previous.stars}⭐)"
                                    }
                                }
                                else -> {
                                    "✓ تم حفظ أول جلسة (${periodRatings.first().subjectTitle} • ${formatDurationAr(totalMinutesInPeriod)}) — ستظهر المقارنات التلقائية مع كل جلسة جديدة تضيفها"
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldPrimary.copy(alpha = 0.10f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = comparisonText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom Bar Chart of Subject Ratings
                            val bySubject = periodRatings.groupBy { it.subjectTitle }
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                val entries = bySubject.entries.take(5)
                                if (entries.isNotEmpty()) {
                                    val barWidth = size.width / (entries.size * 2f)
                                    entries.forEachIndexed { idx, entry ->
                                        val subCompleted = entry.value.count { it.isCompleted }.toFloat()
                                        val ratio = (subCompleted / entry.value.size.coerceAtLeast(1)).coerceIn(0.15f, 1f)
                                        val barHeight = size.height * ratio
                                        val x = idx * (size.width / entries.size) + barWidth * 0.4f
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(EmeraldPrimary, PurplePrimary)
                                            ),
                                            topLeft = Offset(x, size.height - barHeight),
                                            size = Size(barWidth, barHeight),
                                            cornerRadius = CornerRadius(12f, 12f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            bySubject.forEach { (subject, list) ->
                                val rate = (list.count { it.isCompleted } * 100) / list.size.coerceAtLeast(1)
                                val starsAvg = list.map { it.stars }.average()
                                val subjMins = list.filter { it.isCompleted }.sumOf { it.durationMinutes }
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = subject,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${formatDurationAr(subjMins)} • التزام $rate% • ${"%.1f".format(starsAvg)} ⭐",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { (rate / 100f).coerceIn(0f, 1f) },
                                        color = PurplePrimary,
                                        trackColor = PurpleSoft.copy(alpha = 0.2f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Monthly Achievements Log & Month-to-Month Performance Comparison Card
            item {
                MonthlyAchievementsAndComparisonCard(
                    ratings = ratings,
                    onOpenFullReport = { showMonthlyReportDialog = true },
                    onOpenAddAchievement = { showAddAchievementDialog = true },
                    onDeleteAchievement = onDeleteAchievement
                )
            }
        }
    }

    if (showCustomSubjectTimerDialog) {
        CustomSubjectTimerDialog(
            initialSubject = customTimerTargetSubject,
            availableSubjects = scheduleItems.map { it.title }.distinct(),
            subjectCustomTimers = subjectCustomTimers,
            onDismiss = { showCustomSubjectTimerDialog = false },
            onSaveCustomTimer = { subj, mins, startNow ->
                onSetCustomSubjectTimer(subj, mins, startNow)
            }
        )
    }

    if (showMonthlyReportDialog) {
        MonthlyAchievementsFullReportDialog(
            ratings = ratings,
            onDismiss = { showMonthlyReportDialog = false },
            onAddAchievementClick = {
                showMonthlyReportDialog = false
                showAddAchievementDialog = true
            },
            onDeleteAchievement = onDeleteAchievement
        )
    }

    if (showAddAchievementDialog) {
        var achName by remember { mutableStateOf("") }
        var achDuration by remember { mutableStateOf("90") }
        var achNote by remember { mutableStateOf("") }
        var achStars by remember { mutableIntStateOf(5) }

        AlertDialog(
            onDismissRequest = { showAddAchievementDialog = false },
            title = {
                Text(
                    text = "إنجاز جديد",
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = achName,
                        onValueChange = { achName = it },
                        label = { Text("الاسم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = achDuration,
                        onValueChange = { achDuration = it },
                        label = { Text("المدة (دقيقة)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = achNote,
                        onValueChange = { achNote = it },
                        label = { Text("ملاحظة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("التقييم:", fontWeight = FontWeight.Bold)
                        Row {
                            (1..5).forEach { s ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (s <= achStars) GoldAccent else Color.LightGray,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { achStars = s }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (achName.isNotBlank()) {
                            onSubmitAchievement(
                                achName.trim(),
                                achStars,
                                true,
                                achNote.trim(),
                                achDuration.toIntOrNull()?.coerceAtLeast(5) ?: 90
                            )
                            showAddAchievementDialog = false
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAchievementDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showAdhkarCounterModal) {
        AdhkarTouchCounterDialog(
            adhkarItems = hisnItems.filter { it.recordType == "HISN_PDF" },
            onDismiss = { onOpenAdhkarModal(false) },
            onPlayTone = { onTestSound(AlarmCategory.ADHKAR, "أذكار الصباح") },
            onCompleteAndRate = { categoryTitle ->
                onOpenAdhkarModal(false)
                onOpenRatingModal(categoryTitle)
            }
        )
    }
}

@Composable
private fun QuickSectionBadge(
    label: String,
    iconType: SectionIconType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        CustomSectionIcon(type = iconType, size = 44.dp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatSummaryPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AdhkarTouchCounterDialog(
    adhkarItems: List<CatalogItemEntity>,
    onDismiss: () -> Unit,
    onPlayTone: () -> Unit,
    onCompleteAndRate: (String) -> Unit = {}
) {
    val categories = remember(adhkarItems) {
        listOf("الكل") + adhkarItems.map { it.category }.distinct()
    }
    var selectedCat by remember { mutableStateOf("أذكار الصباح والمساء") }
    val filteredList = remember(adhkarItems, selectedCat) {
        val sub = if (selectedCat == "الكل") adhkarItems else adhkarItems.filter { it.category == selectedCat }
        sub.ifEmpty { adhkarItems }
    }

    var currentIndex by remember(selectedCat) { mutableIntStateOf(0) }
    var currentCount by remember(selectedCat) { mutableIntStateOf(0) }
    var completedSet by remember { mutableStateOf(setOf<Int>()) }

    val safeList = filteredList.ifEmpty {
        listOf(
            CatalogItemEntity(
                recordType = "HISN_PDF",
                title = "أذكار الصباح والمساء",
                category = "أذكار الصباح والمساء",
                contentOrDescription = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                targetCount = 3
            )
        )
    }
    val item = safeList[currentIndex.coerceIn(0, safeList.lastIndex)]
    val target = item.targetCount.coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "حصن المسلم (${currentIndex + 1}/${safeList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onPlayTone) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "نغمة الأذكار", tint = PurplePrimary)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCat == cat,
                            onClick = { selectedCat = cat },
                            label = {
                                Text(
                                    text = cat.replace("أذكار ", ""),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.contentOrDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Big Circular Touch Counter Button (3 taps -> ✓ -> transitions to next dhikr)
                val isDone = currentCount >= target || completedSet.contains(currentIndex)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDone) EmeraldPrimary else PurplePrimary
                        )
                        .clickable {
                            val nextTap = currentCount + 1
                            currentCount = nextTap
                            if (nextTap >= target) {
                                completedSet = completedSet + currentIndex
                                if (currentIndex < safeList.lastIndex) {
                                    currentIndex += 1
                                    currentCount = 0
                                } else {
                                    onCompleteAndRate(if (selectedCat == "الكل") "أذكار اليوم" else selectedCat)
                                }
                            }
                        }
                        .testTag("adhkar_touch_counter_circle"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "تم",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                            Text(
                                text = "✓ اكتمل",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "$currentCount / $target",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "المس للعدّ",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentIndex < safeList.lastIndex) {
                        currentIndex += 1
                        currentCount = 0
                    } else {
                        onCompleteAndRate(if (selectedCat == "الكل") "أذكار اليوم" else selectedCat)
                    }
                },
                shape = CircleShape
            ) {
                Text(if (currentIndex < safeList.lastIndex) "الذكر التالي ←" else "إنهاء وتقييم ⭐")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCompleteAndRate(if (selectedCat == "الكل") "أذكار اليوم" else selectedCat)
                }
            ) {
                Text("تقييم الآن ⭐")
            }
        }
    )
}

@Composable
fun PrayerDetailSubScreen(
    prayerSchedule: DailyPrayerSchedule,
    manualOffsets: Map<String, Int>,
    prayerEnabledMap: Map<String, Boolean> = emptyMap(),
    onBack: () -> Unit,
    onTestAdhan: () -> Unit,
    onAdjustOffset: (String, Int) -> Unit,
    onSetPrayerCustomTime: (String, String) -> Unit = { _, _ -> },
    onTogglePrayerEnabled: (String, Boolean) -> Unit = { _, _ -> },
    onChangeMethod: (PrayerCalcMethod) -> Unit
) {
    androidx.activity.compose.BackHandler { onBack() }
    var editingPrayerForTime by remember { mutableStateOf<com.example.domain.PrayerTimeEntry?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = PurplePrimary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مواقيت الصلاة",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        OutlinedButton(
                            onClick = onBack,
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("رجوع", maxLines = 1, softWrap = false)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = prayerSchedule.method.arabicName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onTestAdhan,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تشغيل الأذان",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                com.example.notifications.NotificationAndAlarmHelper.stopAlertSound()
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("إيقاف", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        // Moved Live Next Prayer Card with Ticking Second-by-Second Timer + Decreasing Progress Indicator
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_prayer_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomSectionIcon(type = SectionIconType.PRAYER, size = 46.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الصلاة الحالية: صلاة ${prayerSchedule.nextPrayer.arabicName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${prayerSchedule.nextPrayer.timeFormatted} • متبقي ${prayerSchedule.countdownArabicDetailed}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = PurplePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = prayerSchedule.countdownHms,
                                style = MaterialTheme.typography.titleMedium,
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Decreasing visual progress bar shrinking until the next prayer arrives
                    LinearProgressIndicator(
                        progress = { prayerSchedule.remainingFraction },
                        color = EmeraldPrimary,
                        trackColor = EmeraldPrimary.copy(alpha = 0.16f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .testTag("prayer_countdown_progress_indicator")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5 Prayers Pill Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        prayerSchedule.fivePrayers.forEach { p ->
                            val isNext = p.key == prayerSchedule.nextPrayer.key
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isNext) PurplePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = p.arabicName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isNext) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = p.timeFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isNext) EmeraldLight else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Essential Prayers Alarms Card (matching Image 2 row UX: title + clickable time + Switch)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayers_essential_alarms_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomSectionIcon(type = SectionIconType.PRAYER, size = 42.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "الصلوات اليومية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (manualOffsets.values.any { it != 0 }) {
                            TextButton(
                                onClick = {
                                    prayerSchedule.fivePrayers.forEach { p ->
                                        if ((manualOffsets[p.key] ?: 0) != 0) {
                                            onAdjustOffset(p.key, 0)
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = "استعادة الفلكي",
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    prayerSchedule.fivePrayers.forEach { prayer ->
                        val isEnabled = prayerEnabledMap[prayer.key] ?: true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { editingPrayerForTime = prayer }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                .testTag("prayer_alarm_row_${prayer.key}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "صلاة ${prayer.arabicName}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${Time12hFormatter.format24To12Ar(prayer.time24)} (اضغط لتعديل الوقت)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { enabled ->
                                    onTogglePrayerEnabled(prayer.key, enabled)
                                },
                                modifier = Modifier.testTag("prayer_alarm_switch_${prayer.key}")
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تعديل الدقائق يدوياً لكل صلاة (± دقيقة)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        prayerSchedule.fajr,
                        prayerSchedule.sunrise,
                        prayerSchedule.dhuhr,
                        prayerSchedule.asr,
                        prayerSchedule.maghrib,
                        prayerSchedule.isha
                    ).forEach { p ->
                        val offset = manualOffsets[p.key] ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = p.arabicName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الموعد: ${p.timeFormatted} (تعديل: ${if (offset >= 0) "+$offset" else "$offset"} د)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldPrimary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { onAdjustOffset(p.key, offset - 1) },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Text("-1", maxLines = 1, softWrap = false)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { onAdjustOffset(p.key, offset + 1) },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Text("+1", maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "طريقة حساب المواقيت الفلكية (Offline Adhan)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PrayerCalcMethod.entries.forEach { method ->
                        FilterChip(
                            selected = prayerSchedule.method == method,
                            onClick = { onChangeMethod(method) },
                            label = { Text(method.arabicName) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    editingPrayerForTime?.let { prayer ->
        TimePicker12hDialog(
            initialTime24 = prayer.time24,
            title = "تعديل موعد: صلاة ${prayer.arabicName}",
            onDismiss = { editingPrayerForTime = null },
            onConfirmTime24 = { updated24 ->
                onSetPrayerCustomTime(prayer.key, updated24)
                editingPrayerForTime = null
            }
        )
    }
}
