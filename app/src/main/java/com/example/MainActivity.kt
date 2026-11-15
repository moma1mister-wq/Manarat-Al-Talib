package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifications.AlarmCategory
import com.example.notifications.NotificationAndAlarmHelper
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ExercisesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PricesAndHisnScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.StudyOrganizerTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.MainNavTab

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            val isDark by appViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isArabicRtl by appViewModel.isArabicRtl.collectAsStateWithLifecycle()

            StudyOrganizerTheme(
                darkTheme = isDark,
                isArabicRtl = isArabicRtl
            ) {
                StudyOrganizerRootApp(viewModel = appViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_MUTE,
                KeyEvent.KEYCODE_MUTE -> {
                    if (NotificationAndAlarmHelper.isAlertPlaying()) {
                        NotificationAndAlarmHelper.stopAlertSound(this)
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val notifId = intent.getIntExtra("NOTIFICATION_ID", -1)
        val shouldStopSound = intent.getBooleanExtra("STOP_ALERT_SOUND", false)
        val target = intent.getStringExtra("DEEP_LINK_TARGET") ?: intent.data?.lastPathSegment
        val subject = intent.getStringExtra("SUBJECT_NAME")

        // Only stop the synchronized alert sound when the user explicitly tapped a notification to open the app
        if (shouldStopSound || !target.isNullOrBlank()) {
            NotificationAndAlarmHelper.stopAlertSound(this, notifId)
        }

        if (!target.isNullOrBlank()) {
            appViewModel.handleDeepLink(target, subject, notifId)
            // Consume extras so activity recreation never re-fires the same notification target
            intent.removeExtra("DEEP_LINK_TARGET")
            intent.removeExtra("STOP_ALERT_SOUND")
            intent.removeExtra("NOTIFICATION_ID")
            intent.data = null
        }
    }
}

@Composable
fun StudyOrganizerRootApp(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isArabicRtl by viewModel.isArabicRtl.collectAsStateWithLifecycle()
    val sectionNumber by viewModel.selectedSection.collectAsStateWithLifecycle()

    val scheduleItems by viewModel.allScheduleItems.collectAsStateWithLifecycle()
    val ratings by viewModel.allRatings.collectAsStateWithLifecycle()
    val catalogItems by viewModel.allCatalogItems.collectAsStateWithLifecycle()
    val adhkarAlarms by viewModel.allAdhkarAlarms.collectAsStateWithLifecycle()

    val prayerSchedule by viewModel.dailyPrayerSchedule.collectAsStateWithLifecycle()
    val prayerMethod by viewModel.prayerMethod.collectAsStateWithLifecycle()
    val prayerManualOffsets by viewModel.prayerManualOffsets.collectAsStateWithLifecycle()
    val prayerEnabledMap by viewModel.prayerEnabledMap.collectAsStateWithLifecycle()

    val studyTimer by viewModel.studyTimer.collectAsStateWithLifecycle()
    val todayDayNameAr by viewModel.todayDayNameAr.collectAsStateWithLifecycle()
    val subjectCustomTimers by viewModel.subjectCustomTimers.collectAsStateWithLifecycle()
    val ratingPromptSubject by viewModel.ratingPromptSubject.collectAsStateWithLifecycle()

    val showPrayerSubScreen by viewModel.showPrayerSubScreen.collectAsStateWithLifecycle()
    val showAdhkarModal by viewModel.showAdhkarCounterModal.collectAsStateWithLifecycle()

    val lastMushafPage by viewModel.lastReadMushafPage.collectAsStateWithLifecycle()
    val bookmarkedPages by viewModel.bookmarkedMushafPages.collectAsStateWithLifecycle()
    val isMushafNight by viewModel.isMushafNightMode.collectAsStateWithLifecycle()

    val masterNotificationsEnabled by viewModel.masterNotificationsEnabled.collectAsStateWithLifecycle()
    val syncAlertWithNotification by viewModel.syncAlertWithNotification.collectAsStateWithLifecycle()
    val notifyPrayer by viewModel.notifyPrayer.collectAsStateWithLifecycle()
    val alertSoundPrayer by viewModel.alertSoundPrayer.collectAsStateWithLifecycle()
    val notifyAdhkar by viewModel.notifyAdhkar.collectAsStateWithLifecycle()
    val alertSoundAdhkar by viewModel.alertSoundAdhkar.collectAsStateWithLifecycle()
    val notifyQuran by viewModel.notifyQuran.collectAsStateWithLifecycle()
    val alertSoundQuran by viewModel.alertSoundQuran.collectAsStateWithLifecycle()
    val quranAlarmTime by viewModel.quranAlarmTime.collectAsStateWithLifecycle()
    val notifyHajj by viewModel.notifyHajj.collectAsStateWithLifecycle()
    val alertSoundHajj by viewModel.alertSoundHajj.collectAsStateWithLifecycle()
    val hajjAlarmTime by viewModel.hajjAlarmTime.collectAsStateWithLifecycle()
    val notifyExercise by viewModel.notifyExercise.collectAsStateWithLifecycle()
    val alertSoundExercise by viewModel.alertSoundExercise.collectAsStateWithLifecycle()
    val notifyStudy by viewModel.notifyStudy.collectAsStateWithLifecycle()
    val alertSoundStudy by viewModel.alertSoundStudy.collectAsStateWithLifecycle()
    val ratingDelayMinutes by viewModel.ratingDelayMinutes.collectAsStateWithLifecycle()
    val activeAlertTitle by NotificationAndAlarmHelper.activeAlertTitle.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val startupNotifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Granted or handled */ }

    LaunchedEffect(currentUser) {
        if (currentUser != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                startupNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (currentUser == null) {
        AuthScreen(
            errorMessage = authError,
            onLogin = { u, p -> viewModel.login(u, p) },
            onCreateAccount = { u, p, name, sec -> viewModel.createLocalAccount(u, p, name, sec) },
            onClearError = { viewModel.clearAuthError() }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                shadowElevation = 14.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    MainNavTab.entries.forEach { tab ->
                        val icon = when (tab) {
                            MainNavTab.HOME -> Icons.Default.Home
                            MainNavTab.SCHEDULE -> Icons.Default.CalendarMonth
                            MainNavTab.QURAN -> Icons.Default.MenuBook
                            MainNavTab.EXERCISES -> Icons.Default.FitnessCenter
                            MainNavTab.PRICES -> Icons.Default.AutoAwesome
                            MainNavTab.SETTINGS -> Icons.Default.Settings
                        }
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.labelAr,
                                    modifier = Modifier.size(if (isSelected) 25.dp else 23.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (isArabicRtl) tab.labelAr else tab.labelEn,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PurplePrimary,
                                selectedTextColor = PurplePrimary,
                                unselectedIconColor = com.example.ui.theme.InactiveNavGray,
                                unselectedTextColor = com.example.ui.theme.InactiveNavGray,
                                indicatorColor = PurplePrimary.copy(alpha = 0.14f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!activeAlertTitle.isNullOrBlank()) {
                androidx.compose.material3.Surface(
                    color = CoralAlert,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("in_app_alert_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeAlertTitle!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { NotificationAndAlarmHelper.stopAlertSound(context) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = CoralAlert
                            ),
                            modifier = Modifier.testTag("in_app_alert_stop_button")
                        ) {
                            Text(
                                text = "إيقاف",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Crossfade(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "main_tab_crossfade"
            ) { tab ->
                when (tab) {
                    MainNavTab.HOME -> {
                        HomeScreen(
                            userName = currentUser?.fullName ?: "المستخدم",
                            sectionNumber = sectionNumber,
                            todayDayNameAr = todayDayNameAr,
                            scheduleItems = scheduleItems,
                            prayerSchedule = prayerSchedule,
                            studyTimer = studyTimer,
                            subjectCustomTimers = subjectCustomTimers,
                            adhkarAlarms = adhkarAlarms,
                            hisnItems = catalogItems,
                            ratings = ratings,
                            showPrayerSubScreen = showPrayerSubScreen,
                            showAdhkarCounterModal = showAdhkarModal,
                            prayerManualOffsets = prayerManualOffsets,
                            prayerEnabledMap = prayerEnabledMap,
                            onNavigateTab = { viewModel.selectTab(it) },
                            onOpenPrayerScreen = { viewModel.openPrayerSubScreen(it) },
                            onOpenAdhkarModal = { viewModel.openAdhkarCounterModal(it) },
                            onStartStudyTimer = { subj, mins, uri ->
                                viewModel.startStudyTimerForSubject(subj, mins, uri)
                            },
                            onSwapStudySubject = { subj, mins, uri ->
                                viewModel.selectStudySubjectFromSwap(subj, mins, uri)
                            },
                            onSyncCurrentSubjectToToday = {
                                viewModel.syncSubjectWithTodaySchedule()
                            },
                            onSetCustomSubjectTimer = { subj, mins, startNow ->
                                viewModel.setCustomTimerForSubject(subj, mins, startNow)
                            },
                            onToggleTimerPause = { viewModel.toggleStudyTimerPauseResume() },
                            onResetTimer = { viewModel.resetStudyTimer(it) },
                            onOpenRatingModal = { viewModel.triggerRatingPrompt(it) },
                            onSubmitAchievement = { subj, stars, completed, note, durationMins ->
                                viewModel.submitSessionRating(subj, stars, completed, note, durationMins)
                            },
                            onDeleteAchievement = { viewModel.deleteSessionRating(it) },
                            onClearUserRatings = { viewModel.clearCurrentUserRatings() },
                            onTestSound = { cat, title -> viewModel.testSoundAndNotificationNow(cat, title) },
                            onSaveAdhkarAlarm = { viewModel.saveAdhkarAlarm(it) },
                            onAdjustPrayerMinute = { key, off -> viewModel.setPrayerMinuteOffset(key, off) },
                            onSetPrayerCustomTime = { key, time24 -> viewModel.setPrayerCustomTime24(key, time24) },
                            onTogglePrayerEnabled = { key, enabled -> viewModel.setPrayerEnabled(key, enabled) },
                            onChangePrayerMethod = { viewModel.setPrayerCalculationMethod(it) }
                        )
                    }

                    MainNavTab.SCHEDULE -> {
                        ScheduleScreen(
                            sectionNumber = sectionNumber,
                            scheduleItems = scheduleItems,
                            studyTimer = studyTimer,
                            subjectCustomTimers = subjectCustomTimers,
                            onChangeSection = { viewModel.setSectionNumber(it) },
                            onSaveItem = { viewModel.saveScheduleItem(it) },
                            onDeleteItem = { viewModel.deleteScheduleItem(it) },
                            onClearAllSchedule = { viewModel.clearAllScheduleItems() },
                            onRestoreDefaultSchedule = { viewModel.restoreDefaultSchedule() },
                            onStartStudyTimer = { subj, mins, uri ->
                                viewModel.startStudyTimerForSubject(subj, mins, uri)
                            },
                            onSetCustomSubjectTimer = { subj, mins, startNow, uri ->
                                viewModel.setCustomTimerForSubject(subj, mins, startNow, uri)
                            },
                            onTestStudyAlert = { title, uri ->
                                viewModel.testSoundAndNotificationNow(AlarmCategory.STUDY, title, uri)
                            },
                            onOpenRatingModal = { viewModel.triggerRatingPrompt(it) }
                        )
                    }

                    MainNavTab.QURAN -> {
                        QuranScreen(
                            lastReadPage = lastMushafPage,
                            bookmarkedPages = bookmarkedPages,
                            isNightMode = isMushafNight,
                            onSavePage = { viewModel.saveMushafPage(it) },
                            onToggleBookmark = { viewModel.toggleMushafBookmark(it) },
                            onToggleNightMode = { viewModel.toggleMushafNightMode(it) }
                        )
                    }

                    MainNavTab.EXERCISES -> {
                        val dailyExAlarm = adhkarAlarms.find { it.category.contains("التمارين") }
                        ExercisesScreen(
                            dailyExerciseAlarm = dailyExAlarm,
                            onUpdateDailyExerciseAlarm = { newTime ->
                                val target = dailyExAlarm ?: com.example.data.local.AdhkarAlarmEntity(
                                    title = "تمرين اليوم",
                                    category = "التمارين اليومية",
                                    alarmTime = newTime,
                                    isEnabled = true,
                                    toneName = "exercise_tone.mp3",
                                    isFixedCore = true
                                )
                                viewModel.saveAdhkarAlarm(target.copy(alarmTime = newTime))
                            },
                            onToggleDailyExerciseAlarm = { enabled ->
                                val target = dailyExAlarm ?: com.example.data.local.AdhkarAlarmEntity(
                                    title = "تمرين اليوم",
                                    category = "التمارين اليومية",
                                    alarmTime = "16:45",
                                    isEnabled = enabled,
                                    toneName = "exercise_tone.mp3",
                                    isFixedCore = true
                                )
                                viewModel.saveAdhkarAlarm(target.copy(isEnabled = enabled))
                            },
                            onTestExerciseTone = {
                                viewModel.testSoundAndNotificationNow(
                                    AlarmCategory.EXERCISE,
                                    "تمرين اليوم"
                                )
                            },
                            onOpenRatingModal = { viewModel.triggerRatingPrompt(it) }
                        )
                    }

                    MainNavTab.PRICES -> {
                        PricesAndHisnScreen(
                            catalogItems = catalogItems,
                            adhkarAlarms = adhkarAlarms,
                            onSaveItem = { viewModel.saveCatalogItem(it) },
                            onDeleteItem = { viewModel.deleteCatalogItem(it) },
                            onSaveAdhkarAlarm = { viewModel.saveAdhkarAlarm(it) },
                            onDeleteAdhkarAlarm = { viewModel.deleteAdhkarAlarm(it) },
                            onTestAdhkarTone = {
                                viewModel.testSoundAndNotificationNow(
                                    AlarmCategory.ADHKAR,
                                    "أذكار الصباح"
                                )
                            }
                        )
                    }

                    MainNavTab.SETTINGS -> {
                        SettingsScreen(
                            isDarkTheme = isDark,
                            isArabicRtl = isArabicRtl,
                            sectionNumber = sectionNumber,
                            prayerMethod = prayerMethod,
                            masterNotificationsEnabled = masterNotificationsEnabled,
                            syncAlertWithNotification = syncAlertWithNotification,
                            notifyPrayer = notifyPrayer,
                            alertSoundPrayer = alertSoundPrayer,
                            notifyAdhkar = notifyAdhkar,
                            alertSoundAdhkar = alertSoundAdhkar,
                            notifyQuran = notifyQuran,
                            alertSoundQuran = alertSoundQuran,
                            quranAlarmTime = quranAlarmTime,
                            notifyHajj = notifyHajj,
                            alertSoundHajj = alertSoundHajj,
                            hajjAlarmTime = hajjAlarmTime,
                            notifyExercise = notifyExercise,
                            alertSoundExercise = alertSoundExercise,
                            notifyStudy = notifyStudy,
                            alertSoundStudy = alertSoundStudy,
                            ratingDelayMinutes = ratingDelayMinutes,
                            onToggleDarkTheme = { viewModel.toggleDarkTheme(it) },
                            onSetArabicRtl = { viewModel.setArabicRtl(it) },
                            onSetSectionNumber = { viewModel.setSectionNumber(it) },
                            onSetPrayerMethod = { viewModel.setPrayerCalculationMethod(it) },
                            onUpdateGpsLocation = { lat, lng -> viewModel.updateGpsCoordinates(lat, lng) },
                            onSetMasterNotificationsEnabled = { viewModel.setMasterNotificationsEnabled(it) },
                            onSetSyncAlertWithNotification = { viewModel.setSyncAlertWithNotification(it) },
                            onSetNotifyPrayer = { viewModel.setNotifyPrayer(it) },
                            onSetAlertSoundPrayer = { viewModel.setAlertSoundPrayer(it) },
                            onSetNotifyAdhkar = { viewModel.setNotifyAdhkar(it) },
                            onSetAlertSoundAdhkar = { viewModel.setAlertSoundAdhkar(it) },
                            onSetNotifyQuran = { viewModel.setNotifyQuran(it) },
                            onSetAlertSoundQuran = { viewModel.setAlertSoundQuran(it) },
                            onSetQuranAlarmTime = { viewModel.setQuranAlarmTime(it) },
                            onSetNotifyHajj = { viewModel.setNotifyHajj(it) },
                            onSetAlertSoundHajj = { viewModel.setAlertSoundHajj(it) },
                            onSetHajjAlarmTime = { viewModel.setHajjAlarmTime(it) },
                            onSetNotifyExercise = { viewModel.setNotifyExercise(it) },
                            onSetAlertSoundExercise = { viewModel.setAlertSoundExercise(it) },
                            onSetNotifyStudy = { viewModel.setNotifyStudy(it) },
                            onSetAlertSoundStudy = { viewModel.setAlertSoundStudy(it) },
                            onSetRatingDelay = { viewModel.setRatingDelayMinutes(it) },
                            onClearCacheAndResync = { cb -> viewModel.clearNotificationCacheAndResync(cb) },
                            onTestSound = { cat, title -> viewModel.testSoundAndNotificationNow(cat, title) },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }
    }

    if (ratingPromptSubject != null) {
        PostSessionRatingDialog(
            subjectTitle = ratingPromptSubject!!,
            initialDurationMinutes = (studyTimer.totalSeconds / 60).coerceAtLeast(1),
            onDismiss = { viewModel.dismissRatingPrompt() },
            onSubmit = { stars, completed, note, durationMinutes ->
                viewModel.submitSessionRating(
                    subjectTitle = ratingPromptSubject!!,
                    stars = stars,
                    isCompleted = completed,
                    note = note,
                    durationMinutes = durationMinutes
                )
            }
        )
    }
}

@Composable
fun PostSessionRatingDialog(
    subjectTitle: String,
    initialDurationMinutes: Int = 90,
    onDismiss: () -> Unit,
    onSubmit: (Int, Boolean, String, Int) -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(5) }
    var isCompleted by remember { mutableStateOf(true) }
    var durationText by remember(initialDurationMinutes) { mutableStateOf(initialDurationMinutes.toString()) }
    var note by remember { mutableStateOf("") }
    val cleanSubj = NotificationAndAlarmHelper.cleanSubjectName(subjectTitle)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تقييم: $cleanSubj",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = PurplePrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$star",
                            tint = GoldAccent,
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { selectedStars = star }
                                .padding(4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = isCompleted,
                        onClick = { isCompleted = true },
                        label = { Text("أنجزت") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isCompleted,
                        onClick = { isCompleted = false },
                        label = { Text("لم أنجز") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralAlert,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("المدة (دقيقة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = durationText.toIntOrNull()?.coerceAtLeast(5) ?: 90
                    onSubmit(selectedStars, isCompleted, note, mins)
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                modifier = Modifier.testTag("submit_session_rating_button")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
