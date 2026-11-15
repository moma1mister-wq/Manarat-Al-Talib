package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AdhkarAlarmEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CatalogItemEntity
import com.example.data.local.ScheduleItemEntity
import com.example.data.local.SeedData
import com.example.data.local.SessionRatingEntity
import com.example.data.local.UserAccountEntity
import com.example.domain.DailyPrayerSchedule
import com.example.domain.PrayerCalcMethod
import com.example.domain.PrayerTimesCalculator
import com.example.notifications.AlarmCategory
import com.example.notifications.NotificationAndAlarmHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

enum class MainNavTab(val route: String, val labelAr: String, val labelEn: String) {
    HOME("home", "الرئيسية", "Home"),
    SCHEDULE("schedule", "الجدول", "Schedule"),
    QURAN("quran", "المصحف", "Quran"),
    EXERCISES("exercises", "التمارين", "Exercises"),
    PRICES("adhkar", "الأذكار", "Adhkar"),
    SETTINGS("settings", "الإعدادات", "Settings")
}

data class ActiveStudyTimerState(
    val subjectTitle: String = "Algorithm Analysis",
    val totalSeconds: Int = 90 * 60,
    val remainingSeconds: Int = 90 * 60,
    val isRunning: Boolean = false,
    val customToneUri: String = ""
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("study_organizer_prefs", Context.MODE_PRIVATE)
    private val dao = AppDatabase.getInstance(context).appDao()

    // Navigation & Sub-screens
    private val _selectedTab = MutableStateFlow(MainNavTab.HOME)
    val selectedTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()

    private val _showPrayerSubScreen = MutableStateFlow(false)
    val showPrayerSubScreen: StateFlow<Boolean> = _showPrayerSubScreen.asStateFlow()

    private val _showAdhkarCounterModal = MutableStateFlow(false)
    val showAdhkarCounterModal: StateFlow<Boolean> = _showAdhkarCounterModal.asStateFlow()

    // Auth & Preferences
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isArabicRtl = MutableStateFlow(prefs.getBoolean("arabic_rtl", true))
    val isArabicRtl: StateFlow<Boolean> = _isArabicRtl.asStateFlow()

    private val _selectedSection = MutableStateFlow(prefs.getInt("section_number", 1))
    val selectedSection: StateFlow<Int> = _selectedSection.asStateFlow()

    // Master Switch + Synchronized Mode + Per-Category Notification & Alert Sound Toggles
    private val _masterNotificationsEnabled = MutableStateFlow(prefs.getBoolean("master_notifications_enabled", true))
    val masterNotificationsEnabled: StateFlow<Boolean> = _masterNotificationsEnabled.asStateFlow()

    private val _syncAlertWithNotification = MutableStateFlow(prefs.getBoolean("sync_alert_with_notification", true))
    val syncAlertWithNotification: StateFlow<Boolean> = _syncAlertWithNotification.asStateFlow()

    private val _notifyPrayer = MutableStateFlow(prefs.getBoolean("notify_prayer", true))
    val notifyPrayer: StateFlow<Boolean> = _notifyPrayer.asStateFlow()

    private val _alertSoundPrayer = MutableStateFlow(prefs.getBoolean("alert_sound_prayer", true))
    val alertSoundPrayer: StateFlow<Boolean> = _alertSoundPrayer.asStateFlow()

    private val _notifyAdhkar = MutableStateFlow(prefs.getBoolean("notify_adhkar", true))
    val notifyAdhkar: StateFlow<Boolean> = _notifyAdhkar.asStateFlow()

    private val _alertSoundAdhkar = MutableStateFlow(prefs.getBoolean("alert_sound_adhkar", true))
    val alertSoundAdhkar: StateFlow<Boolean> = _alertSoundAdhkar.asStateFlow()

    private val _notifyQuran = MutableStateFlow(prefs.getBoolean("notify_quran", true))
    val notifyQuran: StateFlow<Boolean> = _notifyQuran.asStateFlow()

    private val _alertSoundQuran = MutableStateFlow(prefs.getBoolean("alert_sound_quran", true))
    val alertSoundQuran: StateFlow<Boolean> = _alertSoundQuran.asStateFlow()

    private val _quranAlarmTime = MutableStateFlow(prefs.getString("quran_alarm_time", "21:00") ?: "21:00")
    val quranAlarmTime: StateFlow<String> = _quranAlarmTime.asStateFlow()

    private val _notifyHajj = MutableStateFlow(prefs.getBoolean("notify_hajj", true))
    val notifyHajj: StateFlow<Boolean> = _notifyHajj.asStateFlow()

    private val _alertSoundHajj = MutableStateFlow(prefs.getBoolean("alert_sound_hajj", true))
    val alertSoundHajj: StateFlow<Boolean> = _alertSoundHajj.asStateFlow()

    private val _hajjAlarmTime = MutableStateFlow(prefs.getString("hajj_alarm_time", "17:30") ?: "17:30")
    val hajjAlarmTime: StateFlow<String> = _hajjAlarmTime.asStateFlow()

    private val _notifyExercise = MutableStateFlow(prefs.getBoolean("notify_exercise", true))
    val notifyExercise: StateFlow<Boolean> = _notifyExercise.asStateFlow()

    private val _alertSoundExercise = MutableStateFlow(prefs.getBoolean("alert_sound_exercise", true))
    val alertSoundExercise: StateFlow<Boolean> = _alertSoundExercise.asStateFlow()

    private val _notifyStudy = MutableStateFlow(prefs.getBoolean("notify_study", true))
    val notifyStudy: StateFlow<Boolean> = _notifyStudy.asStateFlow()

    private val _alertSoundStudy = MutableStateFlow(prefs.getBoolean("alert_sound_study", true))
    val alertSoundStudy: StateFlow<Boolean> = _alertSoundStudy.asStateFlow()

    private val _ratingDelayMinutes = MutableStateFlow(prefs.getInt("rating_delay_minutes", 15))
    val ratingDelayMinutes: StateFlow<Int> = _ratingDelayMinutes.asStateFlow()

    // Prayer Calculation State
    private val _prayerMethod = MutableStateFlow(
        PrayerCalcMethod.fromId(prefs.getString("prayer_method", PrayerCalcMethod.EGYPTIAN.id) ?: PrayerCalcMethod.EGYPTIAN.id)
    )
    val prayerMethod: StateFlow<PrayerCalcMethod> = _prayerMethod.asStateFlow()

    private val _gpsLat = MutableStateFlow(prefs.getFloat("gps_lat", 30.0595f).toDouble())
    val gpsLat: StateFlow<Double> = _gpsLat.asStateFlow()

    private val _gpsLng = MutableStateFlow(prefs.getFloat("gps_lng", 31.3137f).toDouble())
    val gpsLng: StateFlow<Double> = _gpsLng.asStateFlow()

    private val _prayerManualOffsets = MutableStateFlow(loadManualOffsets())
    val prayerManualOffsets: StateFlow<Map<String, Int>> = _prayerManualOffsets.asStateFlow()

    private val _prayerEnabledMap = MutableStateFlow(loadPrayerEnabledMap())
    val prayerEnabledMap: StateFlow<Map<String, Boolean>> = _prayerEnabledMap.asStateFlow()

    private val _dailyPrayerSchedule = MutableStateFlow(
        PrayerTimesCalculator.calculateTodayPrayers(
            latitude = _gpsLat.value,
            longitude = _gpsLng.value,
            method = _prayerMethod.value,
            manualOffsets = _prayerManualOffsets.value
        )
    )
    val dailyPrayerSchedule: StateFlow<DailyPrayerSchedule> = _dailyPrayerSchedule.asStateFlow()

    // Mushaf State
    private val _lastReadMushafPage = MutableStateFlow(prefs.getInt("last_mushaf_page", 1))
    val lastReadMushafPage: StateFlow<Int> = _lastReadMushafPage.asStateFlow()

    private val _bookmarkedMushafPages = MutableStateFlow(loadBookmarkedPages())
    val bookmarkedMushafPages: StateFlow<Set<Int>> = _bookmarkedMushafPages.asStateFlow()

    private val _isMushafNightMode = MutableStateFlow(prefs.getBoolean("mushaf_night", false))
    val isMushafNightMode: StateFlow<Boolean> = _isMushafNightMode.asStateFlow()

    private val _todayDayNameAr = MutableStateFlow(getTodayArabicDayName())
    val todayDayNameAr: StateFlow<String> = _todayDayNameAr.asStateFlow()

    // Study Timer & Post-Notification Rating Modal
    private val _studyTimer = MutableStateFlow(ActiveStudyTimerState())
    val studyTimer: StateFlow<ActiveStudyTimerState> = _studyTimer.asStateFlow()
    private var timerJob: Job? = null
    private var prayerTickerJob: Job? = null
    private var manualSubjectOverride: Boolean = false

    private val _subjectCustomTimers = MutableStateFlow(loadSubjectCustomTimers())
    val subjectCustomTimers: StateFlow<Map<String, Int>> = _subjectCustomTimers.asStateFlow()

    private val _ratingPromptSubject = MutableStateFlow<String?>(null)
    val ratingPromptSubject: StateFlow<String?> = _ratingPromptSubject.asStateFlow()

    // Room Database Reactive Flows
    val allScheduleItems: StateFlow<List<ScheduleItemEntity>> = dao.getAllScheduleItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRatings: StateFlow<List<SessionRatingEntity>> = combine(
        dao.getAllRatings(),
        _currentUser
    ) { list, user ->
        val activeUsername = user?.username?.trim().orEmpty()
        if (activeUsername.isEmpty()) {
            emptyList()
        } else {
            list.filter { it.username.equals(activeUsername, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCatalogItems: StateFlow<List<CatalogItemEntity>> = dao.getAllCatalogItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdhkarAlarms: StateFlow<List<AdhkarAlarmEntity>> = dao.getAllAdhkarAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        com.example.data.QuranOfflineData.ensureLoaded(context)
        NotificationAndAlarmHelper.createNotificationChannels(context)
        initializeDataAndRestoreUser()
        observeScheduleForCurrentSubject()
        startLiveCountdownTicker()
    }

    private fun observeScheduleForCurrentSubject() {
        viewModelScope.launch {
            combine(allScheduleItems, _selectedSection, _subjectCustomTimers) { items, sec, customTimers ->
                Triple(items, sec, customTimers)
            }.collect { (items, sec, customTimers) ->
                syncCurrentSubjectWithSchedule(items, sec, customTimers)
            }
        }
    }

    private fun syncCurrentSubjectWithSchedule(
        items: List<ScheduleItemEntity> = allScheduleItems.value,
        section: Int = _selectedSection.value,
        customTimers: Map<String, Int> = _subjectCustomTimers.value
    ) {
        val current = _studyTimer.value
        if (current.isRunning || manualSubjectOverride) return

        val resolved = resolveScheduledCurrentItem(items, section)
        if (resolved != null) {
            val mins = customTimers[resolved.title] ?: resolved.timerMinutes.coerceAtLeast(1)
            val secs = mins * 60
            _studyTimer.value = current.copy(
                subjectTitle = resolved.title,
                totalSeconds = secs,
                remainingSeconds = secs,
                customToneUri = resolved.customToneUri
            )
        } else {
            val defaultSecs = 90 * 60
            _studyTimer.value = current.copy(
                subjectTitle = "لا توجد مواد في الجدول",
                totalSeconds = defaultSecs,
                remainingSeconds = defaultSecs,
                customToneUri = ""
            )
        }
    }

    private fun startLiveCountdownTicker() {
        prayerTickerJob?.cancel()
        prayerTickerJob = viewModelScope.launch {
            var lastMinuteBucket = -1
            var lastDayName = _todayDayNameAr.value
            while (true) {
                val prevKey = _dailyPrayerSchedule.value.nextPrayer.key
                val updated = PrayerTimesCalculator.calculateTodayPrayers(
                    latitude = _gpsLat.value,
                    longitude = _gpsLng.value,
                    method = _prayerMethod.value,
                    manualOffsets = _prayerManualOffsets.value
                )
                _dailyPrayerSchedule.value = updated
                if (updated.nextPrayer.key != prevKey) {
                    syncAllAlarms("prayer_rollover")
                }

                val cal = Calendar.getInstance()
                val currentDayName = arabicDayNameFor(cal)
                if (currentDayName != lastDayName) {
                    lastDayName = currentDayName
                    _todayDayNameAr.value = currentDayName
                    manualSubjectOverride = false
                    syncCurrentSubjectWithSchedule()
                }
                val minuteBucket = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                if (minuteBucket != lastMinuteBucket) {
                    lastMinuteBucket = minuteBucket
                    syncCurrentSubjectWithSchedule()
                }
                delay(1000L)
            }
        }
    }

    private fun initializeDataAndRestoreUser() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            com.example.data.QuranOfflineData.ensureLoaded(context)
            val existingSchedule = dao.getAllScheduleItemsOnce()
            if (existingSchedule.isEmpty()) {
                dao.insertScheduleItems(SeedData.getInitialSchedule())
            }
            val existingCatalog = dao.getAllCatalogItemsOnce()
            val hisnCount = existingCatalog.count { it.recordType == "HISN_PDF" }
            val priceCount = existingCatalog.count { it.recordType == "PRICE_LIST" }

            if (priceCount == 0) {
                dao.insertCatalogItems(
                    SeedData.getInitialCatalogItems().filter { it.recordType == "PRICE_LIST" }
                )
            }
            if (hisnCount < 200) {
                // Upgrade to the complete 132 chapters / 267 Adhkar from Hisn Al-Muslim
                val fullHisn = SeedData.loadFullHisnAlMuslimFromAssets(context)
                if (fullHisn.isNotEmpty()) {
                    dao.deleteCatalogItemsByType("HISN_PDF")
                    dao.insertCatalogItems(fullHisn)
                } else if (hisnCount == 0) {
                    dao.insertCatalogItems(
                        SeedData.getInitialCatalogItems().filter { it.recordType == "HISN_PDF" }
                    )
                }
            }
            val existingAdhkar = dao.getAllAdhkarAlarmsOnce()
            if (existingAdhkar.isEmpty()) {
                dao.insertAdhkarAlarms(SeedData.getInitialAdhkarAlarms())
            } else {
                existingAdhkar.forEach { alarm ->
                    val isEx = alarm.category.contains("التمارين")
                    val cat = if (isEx) AlarmCategory.EXERCISE else AlarmCategory.ADHKAR
                    val shortTitle = NotificationAndAlarmHelper.formatShortNotificationTitle(cat, alarm.title)
                    if (shortTitle != alarm.title) {
                        dao.insertAdhkarAlarm(alarm.copy(title = shortTitle))
                    }
                }
            }
            // One-time cleanup to remove any pre-seeded demo ratings from earlier builds
            if (!prefs.getBoolean("purged_preseeded_ratings_v2", false)) {
                dao.clearAllRatings()
                prefs.edit().putBoolean("purged_preseeded_ratings_v2", true).apply()
            }
            // Enforce ONLY the 3 authorized accounts (remove any old/unauthorized accounts)
            dao.clearAllUsers()
            AUTHORIZED_USERS.forEach { authorized ->
                dao.insertUser(authorized)
            }

            // Restore logged-in user before running the single startup alarm sync
            val savedUsername = prefs.getString("logged_in_username", null)
            if (!savedUsername.isNullOrBlank()) {
                val matchedAuthorized = AUTHORIZED_USERS.firstOrNull {
                    it.username.equals(savedUsername.trim(), ignoreCase = true)
                }
                if (matchedAuthorized == null) {
                    _currentUser.value = null
                    prefs.edit().remove("logged_in_username").apply()
                } else {
                    val savedSection = prefs.getInt("section_number", matchedAuthorized.sectionNumber).coerceIn(1, 2)
                    val activeUser = matchedAuthorized.copy(sectionNumber = savedSection)
                    dao.insertUser(activeUser)
                    _currentUser.value = activeUser
                    _selectedSection.value = activeUser.sectionNumber
                    prefs.edit().putInt("section_number", activeUser.sectionNumber).apply()
                }
            }

            // Single unified scheduleAllAlarms call on startup
            NotificationAndAlarmHelper.scheduleAllAlarms(
                context = context,
                reason = "app_startup_init",
                forceReschedule = false
            )
        }
    }

    companion object {
        private val WEEK_DAYS_AR = listOf(
            "السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة"
        )

        fun arabicDayNameFor(calendar: Calendar): String {
            return when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> "السبت"
                Calendar.SUNDAY -> "الأحد"
                Calendar.MONDAY -> "الاثنين"
                Calendar.TUESDAY -> "الثلاثاء"
                Calendar.WEDNESDAY -> "الأربعاء"
                Calendar.THURSDAY -> "الخميس"
                Calendar.FRIDAY -> "الجمعة"
                else -> "السبت"
            }
        }

        fun parseTimeToMinutesOrNull(time24: String): Int? {
            val parts = time24.trim().split(":")
            if (parts.size != 2) return null
            val hh = parts[0].toIntOrNull() ?: return null
            val mm = parts[1].toIntOrNull() ?: return null
            if (hh !in 0..23 || mm !in 0..59) return null
            return hh * 60 + mm
        }

        /**
         * Resolves the current/upcoming subject strictly synchronized with today's day in the active student schedule:
         * 1. Any class in progress right now on today's day (or "يومي").
         * 2. Earliest upcoming class later today (e.g. start of the day selects today's first class).
         * 3. If today has scheduled classes that already finished earlier today, stay strictly on today's schedule until midnight.
         * 4. Only if today has zero scheduled classes (e.g. day off), look ahead to the next scheduled day in the week.
         * Returns null when the schedule has no items for the active section.
         */
        fun resolveScheduledCurrentItem(
            scheduleItems: List<ScheduleItemEntity>,
            sectionNumber: Int,
            calendar: Calendar = Calendar.getInstance()
        ): ScheduleItemEntity? {
            val relevant = scheduleItems.filter {
                it.sectionFilter == 0 || it.sectionFilter == sectionNumber
            }
            if (relevant.isEmpty()) return null

            val todayAr = arabicDayNameFor(calendar)
            val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

            val todayItems = relevant
                .filter { it.dayOfWeek == todayAr || it.dayOfWeek == "يومي" }
                .sortedBy { parseTimeToMinutesOrNull(it.startTime) ?: 9999 }

            if (todayItems.isNotEmpty()) {
                // 1. In progress right now today
                val inProgress = todayItems.firstOrNull { item ->
                    val start = parseTimeToMinutesOrNull(item.startTime) ?: return@firstOrNull false
                    val end = parseTimeToMinutesOrNull(item.endTime) ?: (start + 60)
                    nowMinutes in start until end
                }
                if (inProgress != null) return inProgress

                // 2. Upcoming later today
                val upcomingToday = todayItems.firstOrNull { item ->
                    val start = parseTimeToMinutesOrNull(item.startTime) ?: return@firstOrNull false
                    start >= nowMinutes
                }
                if (upcomingToday != null) return upcomingToday

                // 3. Today's classes finished earlier today — keep today's day active until midnight
                return todayItems.last()
            }

            // 4. Only when today has zero classes at all, look ahead to subsequent days in order
            val startIdx = WEEK_DAYS_AR.indexOf(todayAr).coerceAtLeast(0)
            for (offset in 1..6) {
                val targetDay = WEEK_DAYS_AR[(startIdx + offset) % WEEK_DAYS_AR.size]
                val nextDayItems = relevant
                    .filter { it.dayOfWeek == targetDay }
                    .sortedBy { parseTimeToMinutesOrNull(it.startTime) ?: 9999 }
                if (nextDayItems.isNotEmpty()) return nextDayItems.first()
            }

            return relevant.firstOrNull()
        }

        val AUTHORIZED_USERS: List<UserAccountEntity> = listOf(
            UserAccountEntity(
                username = "azhar_ai1",
                passwordPlainOrHash = "Ai@2026#1",
                fullName = "المستخدم الأول",
                sectionNumber = 1
            ),
            UserAccountEntity(
                username = "azhar_ai2",
                passwordPlainOrHash = "Ai@2026#2",
                fullName = "المستخدم الثاني",
                sectionNumber = 1
            ),
            UserAccountEntity(
                username = "azhar_ai3",
                passwordPlainOrHash = "Ai@2026#3",
                fullName = "المستخدم الثالث",
                sectionNumber = 2
            )
        )
    }

    // restoreLoggedInUser is handled inside initializeDataAndRestoreUser()

    fun login(usernameInput: String, passwordInput: String) {
        val uname = usernameInput.trim()
        val pwd = passwordInput.trim()
        if (uname.isEmpty() || pwd.isEmpty()) {
            _authError.value = "أدخل اسم المستخدم وكلمة المرور"
            return
        }
        val matchedByUsername = AUTHORIZED_USERS.firstOrNull {
            it.username.equals(uname, ignoreCase = true)
        }
        if (matchedByUsername == null) {
            _authError.value = "الحساب غير مصرح"
            return
        }
        if (matchedByUsername.passwordPlainOrHash != pwd) {
            _authError.value = "كلمة المرور غير صحيحة"
            return
        }
        viewModelScope.launch {
            dao.insertUser(matchedByUsername)
            _authError.value = null
            _currentUser.value = matchedByUsername
            _selectedSection.value = matchedByUsername.sectionNumber
            prefs.edit()
                .putString("logged_in_username", matchedByUsername.username)
                .putInt("section_number", matchedByUsername.sectionNumber)
                .apply()
            syncAllAlarms("user_login", forceReschedule = true)
        }
    }

    fun createLocalAccount(usernameInput: String, passwordInput: String, fullNameInput: String, section: Int) {
        val uname = usernameInput.trim()
        val pwd = passwordInput.trim()
        if (uname.isEmpty() || pwd.isEmpty()) {
            _authError.value = "أدخل اسم المستخدم وكلمة المرور"
            return
        }
        val matchedAuthorized = AUTHORIZED_USERS.firstOrNull {
            it.username.equals(uname, ignoreCase = true) && it.passwordPlainOrHash == pwd
        }
        if (matchedAuthorized == null) {
            _authError.value = "الحساب غير مصرح"
            return
        }
        viewModelScope.launch {
            val updatedUser = matchedAuthorized.copy(
                fullName = fullNameInput.trim().ifBlank { matchedAuthorized.fullName },
                sectionNumber = section.coerceIn(1, 2)
            )
            dao.insertUser(updatedUser)
            _authError.value = null
            _currentUser.value = updatedUser
            setSectionNumber(updatedUser.sectionNumber)
            prefs.edit()
                .putString("logged_in_username", updatedUser.username)
                .putInt("section_number", updatedUser.sectionNumber)
                .apply()
            syncAllAlarms("account_login", forceReschedule = true)
        }
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().remove("logged_in_username").apply()
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun selectTab(tab: MainNavTab) {
        _selectedTab.value = tab
        _showPrayerSubScreen.value = false
    }

    fun openPrayerSubScreen(open: Boolean) {
        _showPrayerSubScreen.value = open
    }

    fun openAdhkarCounterModal(open: Boolean) {
        _showAdhkarCounterModal.value = open
    }

    private var lastHandledNotificationId: Int = -1
    private var lastHandledNotificationTime: Long = 0L

    fun handleDeepLink(target: String?, subjectName: String?, notificationId: Int = -1) {
        val now = System.currentTimeMillis()
        if (notificationId != -1 &&
            notificationId == lastHandledNotificationId &&
            (now - lastHandledNotificationTime) < 60_000L
        ) {
            return
        }
        if (notificationId != -1) {
            lastHandledNotificationId = notificationId
            lastHandledNotificationTime = now
        }

        when (target) {
            "PRAYER" -> {
                _selectedTab.value = MainNavTab.HOME
                _showPrayerSubScreen.value = true
            }
            "SCHEDULE", "STUDY_TIMER" -> {
                _selectedTab.value = MainNavTab.SCHEDULE
            }
            "QURAN" -> {
                _selectedTab.value = MainNavTab.QURAN
            }
            "ADHKAR", "HAJJ" -> {
                _selectedTab.value = MainNavTab.PRICES
                _showAdhkarCounterModal.value = true
            }
            "EXERCISES" -> {
                _selectedTab.value = MainNavTab.EXERCISES
            }
            "RATING_PROMPT" -> {
                _ratingPromptSubject.value = subjectName ?: "جلسة مذاكرة"
            }
        }
    }

    // Theme, Language, Section
    fun toggleDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("dark_theme", enabled).apply()
    }

    fun setArabicRtl(isArabic: Boolean) {
        _isArabicRtl.value = isArabic
        prefs.edit().putBoolean("arabic_rtl", isArabic).apply()
    }

    fun setSectionNumber(section: Int) {
        val safeSec = section.coerceIn(1, 2)
        manualSubjectOverride = false
        _selectedSection.value = safeSec
        prefs.edit().putInt("section_number", safeSec).apply()
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                val updated = user.copy(sectionNumber = safeSec)
                dao.insertUser(updated)
                _currentUser.value = updated
            }
        }
        syncCurrentSubjectWithSchedule(section = safeSec)
        syncAllAlarms("section_changed")
    }

    // Master & Per-Category Notification + Synchronized Alert Controls
    fun setMasterNotificationsEnabled(enabled: Boolean) {
        _masterNotificationsEnabled.value = enabled
        prefs.edit().putBoolean("master_notifications_enabled", enabled).apply()
        if (!enabled) {
            NotificationAndAlarmHelper.stopAlertSound(context)
        }
        syncAllAlarms("toggle_master", forceReschedule = true)
    }

    fun setSyncAlertWithNotification(enabled: Boolean) {
        _syncAlertWithNotification.value = enabled
        prefs.edit().putBoolean("sync_alert_with_notification", enabled).apply()
        if (!enabled) {
            NotificationAndAlarmHelper.stopAlertSound(context)
        }
        syncAllAlarms("toggle_sync_alert", forceReschedule = true)
    }

    fun setNotifyPrayer(enabled: Boolean) {
        _notifyPrayer.value = enabled
        prefs.edit().putBoolean("notify_prayer", enabled).apply()
        syncAllAlarms("toggle_prayer")
    }

    fun setAlertSoundPrayer(enabled: Boolean) {
        _alertSoundPrayer.value = enabled
        prefs.edit().putBoolean("alert_sound_prayer", enabled).apply()
        syncAllAlarms("toggle_alert_prayer")
    }

    fun setNotifyAdhkar(enabled: Boolean) {
        _notifyAdhkar.value = enabled
        prefs.edit().putBoolean("notify_adhkar", enabled).apply()
        syncAllAlarms("toggle_adhkar")
    }

    fun setAlertSoundAdhkar(enabled: Boolean) {
        _alertSoundAdhkar.value = enabled
        prefs.edit().putBoolean("alert_sound_adhkar", enabled).apply()
        syncAllAlarms("toggle_alert_adhkar")
    }

    fun setNotifyQuran(enabled: Boolean) {
        _notifyQuran.value = enabled
        prefs.edit().putBoolean("notify_quran", enabled).apply()
        syncAllAlarms("toggle_quran")
    }

    fun setAlertSoundQuran(enabled: Boolean) {
        _alertSoundQuran.value = enabled
        prefs.edit().putBoolean("alert_sound_quran", enabled).apply()
        syncAllAlarms("toggle_alert_quran")
    }

    fun setQuranAlarmTime(time24: String) {
        val clean = time24.trim().ifBlank { "21:00" }
        _quranAlarmTime.value = clean
        prefs.edit().putString("quran_alarm_time", clean).apply()
        syncAllAlarms("quran_time_updated", forceReschedule = true)
    }

    fun setNotifyHajj(enabled: Boolean) {
        _notifyHajj.value = enabled
        prefs.edit().putBoolean("notify_hajj", enabled).apply()
        syncAllAlarms("toggle_hajj")
    }

    fun setAlertSoundHajj(enabled: Boolean) {
        _alertSoundHajj.value = enabled
        prefs.edit().putBoolean("alert_sound_hajj", enabled).apply()
        syncAllAlarms("toggle_alert_hajj")
    }

    fun setHajjAlarmTime(time24: String) {
        val clean = time24.trim().ifBlank { "17:30" }
        _hajjAlarmTime.value = clean
        prefs.edit().putString("hajj_alarm_time", clean).apply()
        syncAllAlarms("hajj_time_updated", forceReschedule = true)
    }

    fun setNotifyExercise(enabled: Boolean) {
        _notifyExercise.value = enabled
        prefs.edit().putBoolean("notify_exercise", enabled).apply()
        syncAllAlarms("toggle_exercise")
    }

    fun setAlertSoundExercise(enabled: Boolean) {
        _alertSoundExercise.value = enabled
        prefs.edit().putBoolean("alert_sound_exercise", enabled).apply()
        syncAllAlarms("toggle_alert_exercise")
    }

    fun setNotifyStudy(enabled: Boolean) {
        _notifyStudy.value = enabled
        prefs.edit().putBoolean("notify_study", enabled).apply()
        syncAllAlarms("toggle_study")
    }

    fun setAlertSoundStudy(enabled: Boolean) {
        _alertSoundStudy.value = enabled
        prefs.edit().putBoolean("alert_sound_study", enabled).apply()
        syncAllAlarms("toggle_alert_study")
    }

    fun setRatingDelayMinutes(minutes: Int) {
        val safe = minutes.coerceIn(1, 120)
        _ratingDelayMinutes.value = safe
        prefs.edit().putInt("rating_delay_minutes", safe).apply()
    }

    fun clearNotificationCacheAndResync(onDone: (Int) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val count = NotificationAndAlarmHelper.clearNotificationCacheAndResync(context)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onDone(count)
            }
        }
    }

    // Prayer Calculation & GPS
    fun setPrayerCalculationMethod(method: PrayerCalcMethod) {
        _prayerMethod.value = method
        prefs.edit().putString("prayer_method", method.id).apply()
        recalculatePrayerTimes()
    }

    fun updateGpsCoordinates(lat: Double, lng: Double) {
        _gpsLat.value = lat
        _gpsLng.value = lng
        prefs.edit()
            .putFloat("gps_lat", lat.toFloat())
            .putFloat("gps_lng", lng.toFloat())
            .apply()
        recalculatePrayerTimes()
    }

    fun setPrayerMinuteOffset(prayerKey: String, offsetMinutes: Int) {
        val clamped = offsetMinutes.coerceIn(-720, 720)
        val updated = _prayerManualOffsets.value.toMutableMap()
        updated[prayerKey] = clamped
        _prayerManualOffsets.value = updated
        prefs.edit().putInt("offset_$prayerKey", clamped).apply()
        recalculatePrayerTimes()
    }

    fun setPrayerCustomTime24(prayerKey: String, targetTime24: String) {
        val targetMins = parseTimeToMinutesOrNull(targetTime24) ?: return
        val baseSchedule = PrayerTimesCalculator.calculateTodayPrayers(
            latitude = _gpsLat.value,
            longitude = _gpsLng.value,
            method = _prayerMethod.value,
            manualOffsets = emptyMap()
        )
        val basePrayer = listOf(
            baseSchedule.fajr,
            baseSchedule.sunrise,
            baseSchedule.dhuhr,
            baseSchedule.asr,
            baseSchedule.maghrib,
            baseSchedule.isha
        ).firstOrNull { it.key == prayerKey } ?: return
        val baseMins = parseTimeToMinutesOrNull(basePrayer.time24) ?: return
        var diff = targetMins - baseMins
        while (diff > 720) diff -= 1440
        while (diff < -720) diff += 1440
        setPrayerMinuteOffset(prayerKey, diff)
    }

    fun setPrayerEnabled(prayerKey: String, enabled: Boolean) {
        val updated = _prayerEnabledMap.value.toMutableMap()
        updated[prayerKey] = enabled
        _prayerEnabledMap.value = updated
        prefs.edit().putBoolean("prayer_enabled_$prayerKey", enabled).apply()
        syncAllAlarms("prayer_toggled", forceReschedule = true)
    }

    private fun loadManualOffsets(): Map<String, Int> {
        val keys = listOf("FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA")
        return keys.associateWith { prefs.getInt("offset_$it", 0) }
    }

    private fun loadPrayerEnabledMap(): Map<String, Boolean> {
        val keys = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
        return keys.associateWith { prefs.getBoolean("prayer_enabled_$it", true) }
    }

    fun recalculatePrayerTimes() {
        _dailyPrayerSchedule.value = PrayerTimesCalculator.calculateTodayPrayers(
            latitude = _gpsLat.value,
            longitude = _gpsLng.value,
            method = _prayerMethod.value,
            manualOffsets = _prayerManualOffsets.value
        )
        syncAllAlarms("prayer_recalculated", forceReschedule = true)
    }

    // Schedule CRUD & Study Timer
    fun saveScheduleItem(item: ScheduleItemEntity) {
        viewModelScope.launch {
            manualSubjectOverride = false
            if (!item.alarmEnabled && item.id > 0) {
                NotificationAndAlarmHelper.cancelSingleAlarm(context, 3000 + item.id)
            }
            dao.insertScheduleItem(item)
            syncAllAlarms("schedule_item_saved", forceReschedule = true)
        }
    }

    fun deleteScheduleItem(item: ScheduleItemEntity) {
        viewModelScope.launch {
            manualSubjectOverride = false
            if (item.id > 0) {
                NotificationAndAlarmHelper.cancelSingleAlarm(context, 3000 + item.id)
            }
            dao.deleteScheduleItem(item)
            syncAllAlarms("schedule_item_deleted", forceReschedule = true)
        }
    }

    fun clearAllScheduleItems() {
        viewModelScope.launch {
            manualSubjectOverride = false
            val allItems = dao.getAllScheduleItemsOnce()
            allItems.forEach { item ->
                if (item.id > 0) {
                    NotificationAndAlarmHelper.cancelSingleAlarm(context, 3000 + item.id)
                }
            }
            dao.clearAllScheduleItems()
            syncAllAlarms("schedule_cleared", forceReschedule = true)
        }
    }

    fun restoreDefaultSchedule() {
        viewModelScope.launch {
            manualSubjectOverride = false
            val existing = dao.getAllScheduleItemsOnce()
            existing.forEach { item ->
                if (item.id > 0) {
                    NotificationAndAlarmHelper.cancelSingleAlarm(context, 3000 + item.id)
                }
            }
            dao.clearAllScheduleItems()
            dao.insertScheduleItems(SeedData.getInitialSchedule())
            syncAllAlarms("schedule_restored", forceReschedule = true)
        }
    }

    fun syncSubjectWithTodaySchedule() {
        manualSubjectOverride = false
        timerJob?.cancel()
        _studyTimer.value = _studyTimer.value.copy(isRunning = false)
        syncCurrentSubjectWithSchedule()
    }

    fun selectStudySubjectFromSwap(subject: String, durationMinutes: Int, customToneUri: String = "") {
        manualSubjectOverride = true
        timerJob?.cancel()
        val totalSecs = (durationMinutes.coerceAtLeast(1)) * 60
        _studyTimer.value = ActiveStudyTimerState(
            subjectTitle = subject,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            isRunning = false,
            customToneUri = customToneUri
        )
    }

    fun startStudyTimerForSubject(subject: String, durationMinutes: Int, customToneUri: String = "") {
        manualSubjectOverride = true
        val totalSecs = (durationMinutes.coerceAtLeast(1)) * 60
        _studyTimer.value = ActiveStudyTimerState(
            subjectTitle = subject,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            isRunning = true,
            customToneUri = customToneUri
        )
        runTimerLoop()
    }

    fun toggleStudyTimerPauseResume() {
        val current = _studyTimer.value
        if (current.isRunning) {
            timerJob?.cancel()
            _studyTimer.value = current.copy(isRunning = false)
        } else {
            _studyTimer.value = current.copy(isRunning = true)
            runTimerLoop()
        }
    }

    fun resetStudyTimer(minutes: Int = 90) {
        timerJob?.cancel()
        val secs = minutes.coerceAtLeast(1) * 60
        _studyTimer.value = _studyTimer.value.copy(
            totalSeconds = secs,
            remainingSeconds = secs,
            isRunning = false
        )
    }

    private fun loadSubjectCustomTimers(): Map<String, Int> {
        val defaultMap = mutableMapOf(
            "Algorithm Analysis" to 60,
            "Computer Networks" to 60,
            "Data Structures" to 60,
            "Linear Algebra" to 45,
            "English II" to 45,
            "Biography of Prophet and History of Caliphs" to 30,
            "Human Right" to 30
        )
        val rawJson = prefs.getString("subject_custom_timers_json", null)
        if (!rawJson.isNullOrBlank()) {
            runCatching {
                val obj = JSONObject(rawJson)
                obj.keys().forEach { key ->
                    defaultMap[key] = obj.optInt(key, 60).coerceIn(1, 600)
                }
            }
        }
        return defaultMap
    }

    fun setCustomTimerForSubject(
        subjectTitle: String,
        customMinutes: Int,
        startImmediately: Boolean = false,
        customToneUri: String = ""
    ) {
        val safeMins = customMinutes.coerceIn(1, 600)
        val cleanSubject = subjectTitle.trim().ifBlank { _studyTimer.value.subjectTitle }
        val updated = _subjectCustomTimers.value.toMutableMap()
        updated[cleanSubject] = safeMins
        _subjectCustomTimers.value = updated

        val jsonObj = JSONObject()
        updated.forEach { (k, v) -> jsonObj.put(k, v) }
        prefs.edit().putString("subject_custom_timers_json", jsonObj.toString()).apply()

        viewModelScope.launch {
            val allItems = dao.getAllScheduleItemsOnce()
            allItems.filter {
                it.title.equals(cleanSubject, ignoreCase = true) ||
                    it.title.contains(cleanSubject, ignoreCase = true)
            }.forEach { matching ->
                dao.insertScheduleItem(matching.copy(timerMinutes = safeMins))
            }
        }

        if (startImmediately) {
            startStudyTimerForSubject(cleanSubject, safeMins, customToneUri)
        } else if (_studyTimer.value.subjectTitle == cleanSubject && !_studyTimer.value.isRunning) {
            resetStudyTimer(safeMins)
        }
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_studyTimer.value.isRunning && _studyTimer.value.remainingSeconds > 0) {
                delay(1000L)
                val next = _studyTimer.value.remainingSeconds - 1
                _studyTimer.value = _studyTimer.value.copy(remainingSeconds = next)
                if (next <= 0) {
                    _studyTimer.value = _studyTimer.value.copy(isRunning = false)
                    val subj = NotificationAndAlarmHelper.cleanSubjectName(_studyTimer.value.subjectTitle)
                    NotificationAndAlarmHelper.triggerImmediateAlertAndNotification(
                        context = context,
                        notificationId = NotificationAndAlarmHelper.ACTIVE_TIMER_DONE_ID,
                        title = "تقييم: $subj",
                        message = "",
                        category = AlarmCategory.RATING_FOLLOWUP,
                        subjectName = subj,
                        customToneUri = _studyTimer.value.customToneUri
                    )
                    _ratingPromptSubject.value = subj
                }
            }
        }
    }

    // Post-Notification Evaluation
    fun triggerRatingPrompt(subjectTitle: String) {
        _ratingPromptSubject.value = NotificationAndAlarmHelper.cleanSubjectName(subjectTitle)
    }

    fun dismissRatingPrompt() {
        _ratingPromptSubject.value = null
    }

    fun submitSessionRating(
        subjectTitle: String,
        stars: Int,
        isCompleted: Boolean,
        note: String,
        durationMinutes: Int = 90
    ) {
        val activeUsername = _currentUser.value?.username
            ?: prefs.getString("logged_in_username", null)
            ?: "azhar_ai1"
        viewModelScope.launch {
            dao.insertRating(
                SessionRatingEntity(
                    username = activeUsername,
                    subjectTitle = subjectTitle.trim().ifBlank { "جلسة مذاكرة" },
                    stars = stars.coerceIn(1, 5),
                    isCompleted = isCompleted,
                    note = note.trim(),
                    durationMinutes = durationMinutes.coerceAtLeast(1),
                    timestamp = System.currentTimeMillis()
                )
            )
            _ratingPromptSubject.value = null
        }
    }

    fun deleteSessionRating(rating: SessionRatingEntity) {
        viewModelScope.launch {
            dao.deleteRating(rating)
        }
    }

    fun clearCurrentUserRatings() {
        val activeUsername = _currentUser.value?.username ?: return
        viewModelScope.launch {
            dao.clearRatingsForUser(activeUsername)
        }
    }

    // Mushaf Bookmarks & Last Read Page
    fun saveMushafPage(page: Int) {
        val safe = page.coerceIn(1, 604)
        _lastReadMushafPage.value = safe
        prefs.edit().putInt("last_mushaf_page", safe).apply()
    }

    fun toggleMushafBookmark(page: Int) {
        val current = _bookmarkedMushafPages.value.toMutableSet()
        if (current.contains(page)) current.remove(page) else current.add(page)
        _bookmarkedMushafPages.value = current
        prefs.edit().putStringSet("mushaf_bookmarks", current.map { it.toString() }.toSet()).apply()
    }

    fun toggleMushafNightMode(night: Boolean) {
        _isMushafNightMode.value = night
        prefs.edit().putBoolean("mushaf_night", night).apply()
    }

    private fun loadBookmarkedPages(): Set<Int> {
        val raw = prefs.getStringSet("mushaf_bookmarks", setOf("1", "293", "604")) ?: emptySet()
        return raw.mapNotNull { it.toIntOrNull() }.toSet()
    }

    // Catalog Items (Prices & Hisn Al-Muslim PDF) CRUD
    fun saveCatalogItem(item: CatalogItemEntity) {
        viewModelScope.launch {
            dao.insertCatalogItem(item)
        }
    }

    fun deleteCatalogItem(item: CatalogItemEntity) {
        viewModelScope.launch {
            dao.deleteCatalogItem(item)
        }
    }

    // Adhkar & Exercise Alarms CRUD
    fun saveAdhkarAlarm(alarm: AdhkarAlarmEntity) {
        viewModelScope.launch {
            if (!alarm.isEnabled && alarm.id > 0) {
                NotificationAndAlarmHelper.cancelSingleAlarm(context, 2000 + alarm.id)
            }
            dao.insertAdhkarAlarm(alarm)
            syncAllAlarms("adhkar_saved", forceReschedule = true)
        }
    }

    fun deleteAdhkarAlarm(alarm: AdhkarAlarmEntity) {
        if (alarm.isFixedCore) return // Fixed core items cannot be deleted, only toggled
        viewModelScope.launch {
            if (alarm.id > 0) {
                NotificationAndAlarmHelper.cancelSingleAlarm(context, 2000 + alarm.id)
            }
            dao.deleteAdhkarAlarm(alarm)
            syncAllAlarms("adhkar_deleted", forceReschedule = true)
        }
    }

    fun testSoundAndNotificationNow(category: AlarmCategory, title: String, customToneUri: String = "") {
        val deterministicTestId = 9600 + category.ordinal
        val shortTitle = NotificationAndAlarmHelper.formatShortNotificationTitle(category, title, title)
        NotificationAndAlarmHelper.triggerImmediateAlertAndNotification(
            context = context,
            notificationId = deterministicTestId,
            title = shortTitle,
            message = "",
            category = category,
            subjectName = shortTitle,
            customToneUri = customToneUri,
            bypassDedupeForManualTest = true
        )
    }

    fun scheduleTestNotificationIn10Seconds() {
        NotificationAndAlarmHelper.scheduleTestNotificationIn10Seconds(context)
    }

    fun syncAllAlarms(reason: String = "app_sync", forceReschedule: Boolean = false) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            NotificationAndAlarmHelper.scheduleAllAlarms(
                context = context,
                reason = reason,
                forceReschedule = forceReschedule
            )
        }
    }

    // JSON Backup Export & Import
    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("appName", "منارة الطالب")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("sectionNumber", _selectedSection.value)
        root.put("lastMushafPage", _lastReadMushafPage.value)
        root.put("prayerMethod", _prayerMethod.value.id)
        root.put("quranAlarmTime", _quranAlarmTime.value)
        root.put("hajjAlarmTime", _hajjAlarmTime.value)

        val bookmarksArr = JSONArray()
        _bookmarkedMushafPages.value.sorted().forEach { bookmarksArr.put(it) }
        root.put("mushafBookmarks", bookmarksArr)

        val scheduleArr = JSONArray()
        dao.getAllScheduleItemsOnce().forEach { item ->
            scheduleArr.put(
                JSONObject().apply {
                    put("title", item.title)
                    put("dayOfWeek", item.dayOfWeek)
                    put("startTime", item.startTime)
                    put("endTime", item.endTime)
                    put("location", item.location)
                    put("sectionFilter", item.sectionFilter)
                    put("itemType", item.itemType)
                    put("recurrence", item.recurrence)
                    put("timerMinutes", item.timerMinutes)
                    put("customToneUri", item.customToneUri)
                    put("alarmEnabled", item.alarmEnabled)
                }
            )
        }
        root.put("schedule", scheduleArr)

        val adhkarArr = JSONArray()
        dao.getAllAdhkarAlarmsOnce().forEach { alarm ->
            adhkarArr.put(
                JSONObject().apply {
                    put("title", alarm.title)
                    put("category", alarm.category)
                    put("alarmTime", alarm.alarmTime)
                    put("isEnabled", alarm.isEnabled)
                    put("toneName", alarm.toneName)
                    put("isFixedCore", alarm.isFixedCore)
                }
            )
        }
        root.put("adhkarAlarms", adhkarArr)

        val pricesArr = JSONArray()
        dao.getAllCatalogItemsOnce().filter { it.recordType == "PRICE_LIST" }.forEach { item ->
            pricesArr.put(
                JSONObject().apply {
                    put("title", item.title)
                    put("category", item.category)
                    put("contentOrDescription", item.contentOrDescription)
                    put("priceEgp", item.priceEgp)
                    put("unitOrReference", item.unitOrReference)
                    put("pdfPageNumber", item.pdfPageNumber)
                }
            )
        }
        root.put("priceList", pricesArr)

        val activeUsername = _currentUser.value?.username ?: "azhar_ai1"
        val ratingsArr = JSONArray()
        dao.getRatingsForUserOnce(activeUsername).forEach { r ->
            ratingsArr.put(
                JSONObject().apply {
                    put("username", r.username)
                    put("subjectTitle", r.subjectTitle)
                    put("stars", r.stars)
                    put("isCompleted", r.isCompleted)
                    put("note", r.note)
                    put("durationMinutes", r.durationMinutes)
                    put("timestamp", r.timestamp)
                }
            )
        }
        root.put("ratings", ratingsArr)
        return root.toString(2)
    }

    fun importBackupJson(jsonText: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonText)
                if (root.has("sectionNumber")) {
                    setSectionNumber(root.getInt("sectionNumber"))
                }
                if (root.has("lastMushafPage")) {
                    saveMushafPage(root.getInt("lastMushafPage"))
                }
                if (root.has("quranAlarmTime")) {
                    setQuranAlarmTime(root.getString("quranAlarmTime"))
                }
                if (root.has("hajjAlarmTime")) {
                    setHajjAlarmTime(root.getString("hajjAlarmTime"))
                }
                if (root.has("mushafBookmarks")) {
                    val bArr = root.getJSONArray("mushafBookmarks")
                    val bSet = mutableSetOf<Int>()
                    for (i in 0 until bArr.length()) {
                        bSet.add(bArr.optInt(i, 1).coerceIn(1, 604))
                    }
                    if (bSet.isNotEmpty()) {
                        _bookmarkedMushafPages.value = bSet
                        prefs.edit().putStringSet("mushaf_bookmarks", bSet.map { it.toString() }.toSet()).apply()
                    }
                }
                if (root.has("schedule")) {
                    val arr = root.getJSONArray("schedule")
                    val list = mutableListOf<ScheduleItemEntity>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        list.add(
                            ScheduleItemEntity(
                                title = o.optString("title", "مادة دراسية"),
                                dayOfWeek = o.optString("dayOfWeek", "السبت"),
                                startTime = o.optString("startTime", "08:00"),
                                endTime = o.optString("endTime", "10:00"),
                                location = o.optString("location", "مدرج 1"),
                                sectionFilter = o.optInt("sectionFilter", 0),
                                itemType = o.optString("itemType", "LECTURE"),
                                recurrence = o.optString("recurrence", "WEEKLY"),
                                timerMinutes = o.optInt("timerMinutes", 90),
                                customToneUri = o.optString("customToneUri", ""),
                                alarmEnabled = o.optBoolean("alarmEnabled", true)
                            )
                        )
                    }
                    if (list.isNotEmpty()) {
                        dao.clearAllScheduleItems()
                        dao.insertScheduleItems(list)
                    }
                }
                if (root.has("adhkarAlarms")) {
                    val arr = root.getJSONArray("adhkarAlarms")
                    val existing = dao.getAllAdhkarAlarmsOnce()
                    existing.forEach { dao.deleteAdhkarAlarm(it) }
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        dao.insertAdhkarAlarm(
                            AdhkarAlarmEntity(
                                title = o.optString("title", "أذكار الصباح"),
                                category = o.optString("category", "أذكار الصباح والمساء"),
                                alarmTime = o.optString("alarmTime", "05:35"),
                                isEnabled = o.optBoolean("isEnabled", true),
                                toneName = o.optString("toneName", "adhkar_tone.mp3"),
                                isFixedCore = o.optBoolean("isFixedCore", false)
                            )
                        )
                    }
                }
                if (root.has("priceList")) {
                    val arr = root.getJSONArray("priceList")
                    if (arr.length() > 0) {
                        dao.deleteCatalogItemsByType("PRICE_LIST")
                        val items = mutableListOf<CatalogItemEntity>()
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            items.add(
                                CatalogItemEntity(
                                    recordType = "PRICE_LIST",
                                    title = o.optString("title", "صنف دراسي"),
                                    category = o.optString("category", "كتب ومراجع جامعية"),
                                    contentOrDescription = o.optString("contentOrDescription", ""),
                                    priceEgp = o.optDouble("priceEgp", 0.0),
                                    unitOrReference = o.optString("unitOrReference", "نسخة"),
                                    pdfPageNumber = o.optInt("pdfPageNumber", 1)
                                )
                            )
                        }
                        dao.insertCatalogItems(items)
                    }
                }
                if (root.has("ratings")) {
                    val activeUsername = _currentUser.value?.username ?: "azhar_ai1"
                    val arr = root.getJSONArray("ratings")
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        dao.insertRating(
                            SessionRatingEntity(
                                username = o.optString("username", activeUsername).ifBlank { activeUsername },
                                subjectTitle = o.optString("subjectTitle", "جلسة مذاكرة"),
                                stars = o.optInt("stars", 5).coerceIn(1, 5),
                                isCompleted = o.optBoolean("isCompleted", true),
                                note = o.optString("note", ""),
                                durationMinutes = o.optInt("durationMinutes", 90).coerceAtLeast(1),
                                timestamp = o.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }
                syncAllAlarms("backup_imported", forceReschedule = true)
                onResult(true, "تم استيراد النسخة الاحتياطية بنجاح!")
            } catch (e: Exception) {
                onResult(false, "فشل استيراد ملف JSON: تأكد من صحة التنسيق")
            }
        }
    }

    fun getTodayArabicDayName(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "السبت"
            Calendar.SUNDAY -> "الأحد"
            Calendar.MONDAY -> "الاثنين"
            Calendar.TUESDAY -> "الثلاثاء"
            Calendar.WEDNESDAY -> "الأربعاء"
            Calendar.THURSDAY -> "الخميس"
            Calendar.FRIDAY -> "الجمعة"
            else -> "السبت"
        }
    }
}
