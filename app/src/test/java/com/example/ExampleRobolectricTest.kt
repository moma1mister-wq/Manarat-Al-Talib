package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.QuranOfflineData
import com.example.data.local.AppDatabase
import com.example.data.local.ScheduleItemEntity
import com.example.data.local.SeedData
import com.example.data.local.SessionRatingEntity
import com.example.domain.PrayerCalcMethod
import com.example.domain.PrayerTimesCalculator
import com.example.notifications.AlarmCategory
import com.example.notifications.BuiltInTonePreset
import com.example.notifications.NotificationAndAlarmHelper
import com.example.notifications.ToneTarget
import com.example.ui.components.Time12hFormatter
import com.example.ui.screens.buildMonthlySummaries
import com.example.ui.screens.filterHisnAdhkarByPreset
import com.example.ui.viewmodel.AppViewModel
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun `verify arabic app name and offline prayer and monthly stats`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("منارة الطالب", appName)

        val prayerSchedule = PrayerTimesCalculator.calculateTodayPrayers(
            latitude = 30.0595,
            longitude = 31.3137,
            method = PrayerCalcMethod.EGYPTIAN,
            manualOffsets = mapOf("FAJR" to 2, "MAGHRIB" to -1)
        )
        assertEquals(5, prayerSchedule.fivePrayers.size)
        assertTrue(prayerSchedule.nextPrayer.arabicName.isNotBlank())

        // Verify no pre-populated fake ratings exist by default
        assertTrue(SeedData.getInitialRatings().isEmpty())
        assertTrue(buildMonthlySummaries(emptyList()).isEmpty())

        val summaries = buildMonthlySummaries(
            listOf(
                SessionRatingEntity(
                    id = 1,
                    username = "azhar_ai1",
                    subjectTitle = "Algorithm Analysis",
                    stars = 5,
                    isCompleted = true,
                    note = "جلسة مكتملة",
                    timestamp = System.currentTimeMillis(),
                    durationMinutes = 90
                )
            )
        )
        assertEquals(1, summaries.size)
        assertEquals(90, summaries.first().totalDurationMinutes)
    }

    @Test
    fun `verify short notification titles and section filtering`() {
        assertEquals(
            "Data Structures",
            NotificationAndAlarmHelper.cleanSubjectName("Data Structures (سكشن 1)")
        )
        assertEquals(
            "Data Structures",
            NotificationAndAlarmHelper.cleanSubjectName("مذاكرة Data Structures (سكشن 2)")
        )
        assertEquals(
            "مذاكرة Data Structures",
            NotificationAndAlarmHelper.formatShortNotificationTitle(
                category = AlarmCategory.STUDY,
                rawTitle = "Data Structures (سكشن 1)",
                subjectName = "Data Structures (سكشن 1)",
                itemType = "TASK"
            )
        )
        assertEquals(
            "محاضرة Linear Algebra",
            NotificationAndAlarmHelper.formatShortNotificationTitle(
                category = AlarmCategory.STUDY,
                rawTitle = "Linear Algebra",
                subjectName = "Linear Algebra",
                itemType = "LECTURE"
            )
        )
        assertEquals(
            "تقييم: Data Structures",
            NotificationAndAlarmHelper.formatShortNotificationTitle(
                category = AlarmCategory.RATING_FOLLOWUP,
                rawTitle = "كيف كانت الجلسة؟ ⭐ Data Structures (سكشن 1)",
                subjectName = "Data Structures (سكشن 1)"
            )
        )
        assertEquals(
            "صلاة الفجر",
            NotificationAndAlarmHelper.formatShortNotificationTitle(
                category = AlarmCategory.PRAYER,
                rawTitle = "حان وقت صلاة الفجر"
            )
        )
        assertEquals(
            "أذكار الصباح",
            NotificationAndAlarmHelper.formatShortNotificationTitle(
                category = AlarmCategory.ADHKAR,
                rawTitle = "أذكار الصباح ☀️"
            )
        )
        assertEquals(
            "تمرين اليوم",
            NotificationAndAlarmHelper.formatShortNotificationTitle(
                category = AlarmCategory.EXERCISE,
                rawTitle = "التمرين الرياضي اليومي 💪"
            )
        )
        assertTrue(
            NotificationAndAlarmHelper.isOtherSectionText("Data Structures (سكشن 2)", 1)
        )
        assertFalse(
            NotificationAndAlarmHelper.isOtherSectionText("Data Structures (سكشن 1)", 1)
        )
    }

    @Test
    fun `verify 12h time formatter boundary cases and general notification tones`() {
        // Boundary cases: 12:00 ص = midnight (00:00), 12:00 م = noon (12:00), 11:59 م = (23:59)
        assertEquals("12:00 ص", Time12hFormatter.format24To12Ar("00:00"))
        assertEquals("12:00 م", Time12hFormatter.format24To12Ar("12:00"))
        assertEquals("11:59 م", Time12hFormatter.format24To12Ar("23:59"))
        assertEquals("8:00 ص", Time12hFormatter.format24To12Ar("08:00"))
        assertEquals("2:30 م", Time12hFormatter.format24To12Ar("14:30"))

        // Round-trip conversion to 24h storage
        assertEquals("00:00", Time12hFormatter.to24hString(12, 0, false))
        assertEquals("12:00", Time12hFormatter.to24hString(12, 0, true))
        assertEquals("23:59", Time12hFormatter.to24hString(11, 59, true))
        assertEquals("08:00", Time12hFormatter.to24hString(8, 0, false))
        assertEquals("14:30", Time12hFormatter.to24hString(2, 30, true))

        // Verify notification tones mapping
        assertEquals(R.raw.adhan, AlarmCategory.PRAYER.defaultRawRes)
        assertEquals(R.raw.adhkar_tone, AlarmCategory.ADHKAR.defaultRawRes)
        assertEquals(R.raw.exercise_tone, AlarmCategory.EXERCISE.defaultRawRes)
        assertEquals("سبحانه وبحمده - عبدالله المهداوي", AlarmCategory.EXERCISE.toneTitleAr)
        assertEquals(R.raw.study_tone, AlarmCategory.STUDY.defaultRawRes)
        assertEquals(R.raw.study_tone, AlarmCategory.RATING_FOLLOWUP.defaultRawRes)
        assertEquals(
            "والموعد في جنة عدن (study_tone.mp3)",
            NotificationAndAlarmHelper.DEFAULT_GENERAL_TONE_NAME
        )
        assertEquals(
            "ورد قراءة القرآن",
            NotificationAndAlarmHelper.formatShortNotificationTitle(
                category = AlarmCategory.QURAN,
                rawTitle = "ورد قراءة القرآن الكريم"
            )
        )
        assertEquals(
            "أذكار ومناسك الحج",
            NotificationAndAlarmHelper.formatShortNotificationTitle(
                category = AlarmCategory.HAJJ,
                rawTitle = "أذكار ومناسك الحج والعمرة"
            )
        )
    }

    @Test
    fun `verify complete offline mushaf 604 pages 114 surahs and 6236 verses`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        QuranOfflineData.ensureLoaded(context)
        assertEquals(114, QuranOfflineData.allSurahs.size)
        assertEquals(6236, QuranOfflineData.getAllVerses().size)

        var totalVersesAcrossPages = 0
        for (page in 1..604) {
            val content = QuranOfflineData.getPageContent(page)
            assertEquals(page, content.pageNumber)
            assertTrue("Page $page should be in Juz 1..30", content.juzNumber in 1..30)
            assertTrue("Page $page should be in Hizb 1..60", content.hizbNumber in 1..60)
            assertTrue("Page $page must have verses", content.verses.isNotEmpty())
            totalVersesAcrossPages += content.verses.size
        }
        assertEquals(6236, totalVersesAcrossPages)

        val juzList = QuranOfflineData.getAllJuzInfo()
        assertEquals(30, juzList.size)
        assertEquals(1, juzList.first().startPage)
        assertEquals(582, juzList.last().startPage)

        val rubList = QuranOfflineData.getAllRubInfo()
        assertEquals(240, rubList.size)
        assertEquals(1, rubList.first().rubNumber)
        assertEquals(240, rubList.last().rubNumber)
        assertTrue(rubList.all { it.pageNumber in 1..604 && it.hizbNumber in 1..60 && it.juzNumber in 1..30 })

        val pageSummaries = QuranOfflineData.getAllPageSummaries()
        assertEquals(604, pageSummaries.size)

        val ikhlasMatches = QuranOfflineData.searchVersesOffline("قل هو الله احد")
        assertTrue(ikhlasMatches.any { it.surahNumber == 112 && it.ayahNumber == 1 })

        val kahfSurahs = QuranOfflineData.searchSurahsOffline("الكهف")
        assertTrue(kahfSurahs.any { it.number == 18 })

        val hizb60Rubs = QuranOfflineData.searchRubsOffline("60")
        assertTrue(hizb60Rubs.any { it.hizbNumber == 60 })
    }

    @Test
    fun `verify schedule catalog prices and alarm scheduling deduplication`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalogItems = SeedData.getInitialCatalogItems()
        val hisnItems = catalogItems.filter { it.recordType == "HISN_PDF" }
        val priceItems = catalogItems.filter { it.recordType == "PRICE_LIST" }
        assertTrue(hisnItems.isNotEmpty())
        assertTrue(priceItems.isNotEmpty())
        assertTrue(priceItems.all { it.priceEgp > 0.0 })

        val scheduleItems = SeedData.getInitialSchedule()
        assertTrue(scheduleItems.any { it.sectionFilter == 1 })
        assertTrue(scheduleItems.any { it.sectionFilter == 2 })

        val adhkarAlarms = SeedData.getInitialAdhkarAlarms()
        assertTrue(adhkarAlarms.any { it.alarmTime == "05:35" }) // Morning Adhkar after Fajr
        assertTrue(adhkarAlarms.any { it.alarmTime == "16:15" }) // Evening Adhkar after Asr

        val dao = AppDatabase.getInstance(context).appDao()
        dao.insertScheduleItems(scheduleItems)
        dao.insertAdhkarAlarms(adhkarAlarms)

        NotificationAndAlarmHelper.scheduleAllAlarms(
            context = context,
            reason = "TEST_RUN_1",
            forceReschedule = true
        )
        val pending1 = NotificationAndAlarmHelper.getPendingScheduledAlarms(context)
        assertTrue(pending1.isNotEmpty())

        // Calling scheduleAllAlarms again with identical state and forceReschedule=false skips duplicate scheduling
        NotificationAndAlarmHelper.scheduleAllAlarms(
            context = context,
            reason = "TEST_RUN_2_DUPLICATE_CHECK",
            forceReschedule = false
        )
        val pending2 = NotificationAndAlarmHelper.getPendingScheduledAlarms(context)
        assertEquals(pending1.size, pending2.size)
        // Unique deterministic IDs and no Section 2 items when selectedSection = 1
        assertEquals(pending2.size, pending2.map { it.alarmId }.distinct().size)
        assertTrue(pending2.none { it.title.contains("سكشن 2") })
    }

    @Test
    fun `verify current subject sync with schedule and decreasing live prayer countdown`() {
        val sampleSchedule = listOf(
            ScheduleItemEntity(
                title = "Data Structures",
                dayOfWeek = "الأحد",
                startTime = "08:00",
                endTime = "10:00",
                location = "مدرج 1",
                sectionFilter = 0
            ),
            ScheduleItemEntity(
                title = "Algorithm Analysis (سكشن 1)",
                dayOfWeek = "الأحد",
                startTime = "10:00",
                endTime = "12:00",
                location = "معمل 1",
                sectionFilter = 1
            ),
            ScheduleItemEntity(
                title = "Computer Networks (سكشن 2)",
                dayOfWeek = "الأحد",
                startTime = "10:00",
                endTime = "12:00",
                location = "معمل 2",
                sectionFilter = 2
            )
        )

        // Sunday at 10:30 AM: Section 1 gets Algorithm Analysis, Section 2 gets Computer Networks
        val sunday1030 = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }
        assertEquals(
            "Algorithm Analysis (سكشن 1)",
            AppViewModel.resolveScheduledCurrentItem(sampleSchedule, 1, sunday1030)?.title
        )
        assertEquals(
            "Computer Networks (سكشن 2)",
            AppViewModel.resolveScheduledCurrentItem(sampleSchedule, 2, sunday1030)?.title
        )
        // Empty schedule returns null
        assertEquals(null, AppViewModel.resolveScheduledCurrentItem(emptyList(), 1, sunday1030))

        // Decreasing prayer countdown indicator check: advancing 60 seconds strictly lowers remainingSeconds and remainingFraction
        val baseCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 13)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val laterCal = (baseCal.clone() as Calendar).apply {
            add(Calendar.SECOND, 60)
        }
        val s1 = PrayerTimesCalculator.calculateTodayPrayers(
            latitude = 30.0595,
            longitude = 31.3137,
            calendar = baseCal
        )
        val s2 = PrayerTimesCalculator.calculateTodayPrayers(
            latitude = 30.0595,
            longitude = 31.3137,
            calendar = laterCal
        )
        assertEquals(s1.nextPrayer.key, s2.nextPrayer.key)
        assertEquals(s1.remainingSecondsToNext - 60, s2.remainingSecondsToNext)
        assertTrue(s2.remainingFraction < s1.remainingFraction)
        assertTrue(Regex("\\d{2}:\\d{2}:\\d{2}").matches(s2.countdownHms))
    }

    @Test
    fun `verify strict today-day schedule sync across morning evening and day rollover`() {
        val schedule = listOf(
            ScheduleItemEntity(
                title = "Algorithm Analysis",
                dayOfWeek = "الخميس",
                startTime = "08:00",
                endTime = "10:00",
                location = "مدرج 1",
                sectionFilter = 0
            ),
            ScheduleItemEntity(
                title = "Data Structures",
                dayOfWeek = "الخميس",
                startTime = "19:00",
                endTime = "20:30",
                location = "مكتب المذاكرة",
                sectionFilter = 0
            ),
            ScheduleItemEntity(
                title = "Computer Networks",
                dayOfWeek = "الجمعة",
                startTime = "09:00",
                endTime = "11:00",
                location = "مدرج 2",
                sectionFilter = 0
            ),
            ScheduleItemEntity(
                title = "Linear Algebra",
                dayOfWeek = "السبت",
                startTime = "08:00",
                endTime = "10:00",
                location = "مدرج 1",
                sectionFilter = 0
            )
        )

        val thursdayMorning = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 30)
        }
        assertEquals(
            "Algorithm Analysis",
            AppViewModel.resolveScheduledCurrentItem(schedule, 1, thursdayMorning)?.title
        )

        val thursdayNightAfterAll = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 15)
        }
        val thursdayLateResolved = AppViewModel.resolveScheduledCurrentItem(schedule, 1, thursdayNightAfterAll)
        assertEquals("الخميس", thursdayLateResolved?.dayOfWeek)
        assertEquals("Data Structures", thursdayLateResolved?.title)

        val fridayMorning = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }
        val fridayResolved = AppViewModel.resolveScheduledCurrentItem(schedule, 1, fridayMorning)
        assertEquals("الجمعة", fridayResolved?.dayOfWeek)
        assertEquals("Computer Networks", fridayResolved?.title)
    }

    @Test
    fun `verify adhkar quick presets isolate morning evening and travel adhkar accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fullHisn = SeedData.loadFullHisnAlMuslimFromAssets(context)
        assertTrue("Full Hisn JSON should load > 200 items", fullHisn.size > 200)

        val morning = filterHisnAdhkarByPreset(fullHisn, "أذكار الصباح")
        assertTrue("Morning adhkar should be populated", morning.size >= 20)
        assertTrue(morning.all { it.title.contains("أذكار الصباح") })
        assertTrue("Evening-only dhikr must be excluded from Morning", morning.none { it.contentOrDescription.contains("إذا أمسى)") })

        val evening = filterHisnAdhkarByPreset(fullHisn, "أذكار المساء")
        assertTrue("Evening adhkar should be populated", evening.size >= 18)
        assertTrue("Morning-only adhikr must be excluded from Evening", evening.none { it.contentOrDescription.contains("إذا أصبحَ)") })

        val travel = filterHisnAdhkarByPreset(fullHisn, "أذكار السفر")
        assertTrue("Travel adhkar should include travel/riding items", travel.size >= 8)
        assertTrue(travel.any { it.title.contains("دعاء السفر") })
        assertTrue(travel.any { it.title.contains("دعاء الركوب") })
        assertTrue(travel.none { it.title.contains("أذكار الصباح") })
    }

    @Test
    fun `verify per-prayer alarm toggle and custom prayer time math`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("study_organizer_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("master_notifications", true)
            .putBoolean("notify_prayer", true)
            .putBoolean("prayer_enabled_FAJR", true)
            .putBoolean("prayer_enabled_DHUHR", true)
            .putBoolean("prayer_enabled_ASR", false)
            .putBoolean("prayer_enabled_MAGHRIB", true)
            .putBoolean("prayer_enabled_ISHA", true)
            .apply()

        NotificationAndAlarmHelper.scheduleAllAlarms(
            context = context,
            reason = "TEST_PRAYER_TOGGLE",
            forceReschedule = true
        )
        val pendingDisabledAsr = NotificationAndAlarmHelper.getPendingScheduledAlarms(context)
        assertTrue(pendingDisabledAsr.any { it.title == "صلاة الفجر" })
        assertFalse("Disabled ASR prayer must not be scheduled", pendingDisabledAsr.any { it.title == "صلاة العصر" })

        prefs.edit().putBoolean("prayer_enabled_ASR", true).apply()
        NotificationAndAlarmHelper.scheduleAllAlarms(
            context = context,
            reason = "TEST_PRAYER_REENABLED",
            forceReschedule = true
        )
        val pendingEnabledAsr = NotificationAndAlarmHelper.getPendingScheduledAlarms(context)
        assertTrue("Re-enabled ASR prayer should be scheduled", pendingEnabledAsr.any { it.title == "صلاة العصر" })

        // Verify custom prayer time calculation matches exact target HH:mm
        val base = PrayerTimesCalculator.calculateTodayPrayers(
            latitude = 30.0595,
            longitude = 31.3137,
            method = PrayerCalcMethod.EGYPTIAN,
            manualOffsets = emptyMap()
        )
        val baseAsrMins = AppViewModel.parseTimeToMinutesOrNull(base.asr.time24)!!
        val targetMins = AppViewModel.parseTimeToMinutesOrNull("15:25")!!
        var diff = targetMins - baseAsrMins
        while (diff > 720) diff -= 1440
        while (diff < -720) diff += 1440
        val updated = PrayerTimesCalculator.calculateTodayPrayers(
            latitude = 30.0595,
            longitude = 31.3137,
            method = PrayerCalcMethod.EGYPTIAN,
            manualOffsets = mapOf("ASR" to diff)
        )
        assertEquals("15:25", updated.asr.time24)
        assertEquals("3:25 م", updated.asr.timeFormatted)
    }

    @Test
    fun `verify full app tone target assignment swapping and fallback resolution`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Ensure clean defaults first
        ToneTarget.entries.forEach { target ->
            NotificationAndAlarmHelper.resetToneForTarget(context, target)
        }

        // Verify event-to-target resolution across Adhkar, Prayers, Quran, Hajj, Exercise, General
        assertEquals(
            ToneTarget.ADHKAR_MORNING,
            NotificationAndAlarmHelper.resolveToneTargetForEvent(AlarmCategory.ADHKAR, "أذكار الصباح")
        )
        assertEquals(
            ToneTarget.PRAYER_ASR,
            NotificationAndAlarmHelper.resolveToneTargetForEvent(AlarmCategory.PRAYER, "صلاة العصر")
        )
        assertEquals(
            ToneTarget.QURAN,
            NotificationAndAlarmHelper.resolveToneTargetForEvent(AlarmCategory.QURAN, "ورد قراءة القرآن")
        )
        assertEquals(
            ToneTarget.HAJJ,
            NotificationAndAlarmHelper.resolveToneTargetForEvent(AlarmCategory.HAJJ, "أذكار ومناسك الحج")
        )

        // Assign custom preset to ADHKAR_ALL and another specifically to ADHKAR_MORNING
        NotificationAndAlarmHelper.assignBuiltInPresetToTarget(
            context = context,
            target = ToneTarget.ADHKAR_ALL,
            preset = BuiltInTonePreset.ADHAN_DEFAULT
        )
        NotificationAndAlarmHelper.assignBuiltInPresetToTarget(
            context = context,
            target = ToneTarget.ADHKAR_MORNING,
            preset = BuiltInTonePreset.EXERCISE_DEFAULT
        )

        // ADHKAR_MORNING uses its explicit override while ADHKAR_EVENING inherits from ADHKAR_ALL
        val morningEffective = NotificationAndAlarmHelper.resolveEffectiveToneAssignment(
            context = context,
            category = AlarmCategory.ADHKAR,
            titleOrSubject = "أذكار الصباح"
        )
        val eveningEffective = NotificationAndAlarmHelper.resolveEffectiveToneAssignment(
            context = context,
            category = AlarmCategory.ADHKAR,
            titleOrSubject = "أذكار المساء"
        )
        assertEquals("RAW:exercise_tone", morningEffective.storageSpec)
        assertEquals("RAW:adhan", eveningEffective.storageSpec)

        // Swap PRAYER_FAJR (adhan) and EXERCISE (exercise_tone)
        NotificationAndAlarmHelper.swapTonesBetweenTargets(
            context = context,
            first = ToneTarget.PRAYER_FAJR,
            second = ToneTarget.EXERCISE
        )
        val fajrAfterSwap = NotificationAndAlarmHelper.getToneAssignment(context, ToneTarget.PRAYER_FAJR)
        val exerciseAfterSwap = NotificationAndAlarmHelper.getToneAssignment(context, ToneTarget.EXERCISE)
        assertEquals("RAW:exercise_tone", fajrAfterSwap.storageSpec)
        assertEquals("RAW:adhan", exerciseAfterSwap.storageSpec)

        // Reset restores defaults cleanly
        NotificationAndAlarmHelper.resetToneForTarget(context, ToneTarget.PRAYER_FAJR)
        NotificationAndAlarmHelper.resetToneForTarget(context, ToneTarget.EXERCISE)
        assertEquals(
            "DEFAULT",
            NotificationAndAlarmHelper.getToneAssignment(context, ToneTarget.PRAYER_FAJR).storageSpec
        )
        assertEquals(
            "DEFAULT",
            NotificationAndAlarmHelper.getToneAssignment(context, ToneTarget.EXERCISE).storageSpec
        )
    }
}
