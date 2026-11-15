package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.domain.PrayerCalcMethod
import com.example.domain.PrayerTimesCalculator
import com.example.ui.components.Time12hFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import android.os.PowerManager

enum class AlarmCategory(
    val channelId: String,
    val channelNameAr: String,
    val toneTitleAr: String,
    val deepLinkTarget: String,
    val defaultRawRes: Int
) {
    PRAYER(
        "channel_prayer_v5",
        "مواقيت الصلاة",
        "صوت الأذان الشرعي (adhan.mp3)",
        "PRAYER",
        R.raw.adhan
    ),
    ADHKAR(
        "channel_adhkar_v5",
        "الأذكار اليومية",
        "رسم الصباح (adhkar_tone.mp3)",
        "ADHKAR",
        R.raw.adhkar_tone
    ),
    QURAN(
        "channel_quran_v1",
        "قراءة القرآن الكريم",
        "تنبيه ورد القرآن (adhkar_tone.mp3)",
        "QURAN",
        R.raw.adhkar_tone
    ),
    HAJJ(
        "channel_hajj_v1",
        "أذكار ومناسك الحج والعمرة",
        "تنبيه الحج والعمرة (adhkar_tone.mp3)",
        "HAJJ",
        R.raw.adhkar_tone
    ),
    EXERCISE(
        "channel_exercise_v5",
        "التمارين الرياضية",
        "سبحانه وبحمده - عبدالله المهداوي",
        "EXERCISES",
        R.raw.exercise_tone
    ),
    STUDY(
        "channel_study_general_v1",
        "المذاكرة والمحاضرات والإشعارات العامة",
        "والموعد في جنة عدن (study_tone.mp3)",
        "SCHEDULE",
        R.raw.study_tone
    ),
    RATING_FOLLOWUP(
        "channel_rating_general_v1",
        "تقييم الجلسات والإشعارات العامة",
        "والموعد في جنة عدن (study_tone.mp3)",
        "RATING_PROMPT",
        R.raw.study_tone
    );

    fun isGeneralCategory(): Boolean = this == STUDY || this == RATING_FOLLOWUP
}

enum class BuiltInTonePreset(
    val id: String,
    val displayNameAr: String,
    val rawRes: Int
) {
    STUDY_DEFAULT("study_tone", "والموعد في جنة عدن (study_tone.mp3)", R.raw.study_tone),
    ADHAN_DEFAULT("adhan", "صوت الأذان الشرعي (adhan.mp3)", R.raw.adhan),
    ADHKAR_DEFAULT("adhkar_tone", "رسم الصباح (adhkar_tone.mp3)", R.raw.adhkar_tone),
    EXERCISE_DEFAULT("exercise_tone", "سبحانه وبحمده - عبدالله المهداوي (exercise_tone.mp3)", R.raw.exercise_tone);

    companion object {
        fun fromId(id: String): BuiltInTonePreset? = entries.find { it.id == id }
        fun defaultForCategory(category: AlarmCategory): BuiltInTonePreset = when (category) {
            AlarmCategory.PRAYER -> ADHAN_DEFAULT
            AlarmCategory.ADHKAR, AlarmCategory.QURAN, AlarmCategory.HAJJ -> ADHKAR_DEFAULT
            AlarmCategory.EXERCISE -> EXERCISE_DEFAULT
            AlarmCategory.STUDY, AlarmCategory.RATING_FOLLOWUP -> STUDY_DEFAULT
        }
    }
}

enum class ToneTarget(
    val key: String,
    val labelAr: String,
    val category: AlarmCategory,
    val parentAllTargetKey: String? = null
) {
    GENERAL("GENERAL", "المذاكرة والإشعارات العامة", AlarmCategory.STUDY),
    ADHKAR_MORNING("ADHKAR_MORNING", "أذكار الصباح", AlarmCategory.ADHKAR, "ADHKAR_ALL"),
    ADHKAR_EVENING("ADHKAR_EVENING", "أذكار المساء", AlarmCategory.ADHKAR, "ADHKAR_ALL"),
    ADHKAR_SLEEP("ADHKAR_SLEEP", "أذكار النوم", AlarmCategory.ADHKAR, "ADHKAR_ALL"),
    ADHKAR_TRAVEL("ADHKAR_TRAVEL", "أذكار السفر", AlarmCategory.ADHKAR, "ADHKAR_ALL"),
    ADHKAR_ALL("ADHKAR_ALL", "كل الأذكار", AlarmCategory.ADHKAR),
    PRAYER_FAJR("PRAYER_FAJR", "صلاة الفجر", AlarmCategory.PRAYER, "PRAYER_ALL"),
    PRAYER_DHUHR("PRAYER_DHUHR", "صلاة الظهر", AlarmCategory.PRAYER, "PRAYER_ALL"),
    PRAYER_ASR("PRAYER_ASR", "صلاة العصر", AlarmCategory.PRAYER, "PRAYER_ALL"),
    PRAYER_MAGHRIB("PRAYER_MAGHRIB", "صلاة المغرب", AlarmCategory.PRAYER, "PRAYER_ALL"),
    PRAYER_ISHA("PRAYER_ISHA", "صلاة العشاء", AlarmCategory.PRAYER, "PRAYER_ALL"),
    PRAYER_ALL("PRAYER_ALL", "جميع الصلوات", AlarmCategory.PRAYER),
    QURAN("QURAN", "ورد قراءة القرآن", AlarmCategory.QURAN),
    HAJJ("HAJJ", "أذكار الحج والعمرة", AlarmCategory.HAJJ),
    EXERCISE("EXERCISE", "التمارين الرياضية", AlarmCategory.EXERCISE);

    val defaultPreset: BuiltInTonePreset
        get() = BuiltInTonePreset.defaultForCategory(category)
}

data class ToneAssignment(
    val storageSpec: String, // "DEFAULT", "RAW:<id>", or "FILE:<absPath>"
    val displayName: String,
    val isCustomized: Boolean
)

data class UploadedToneEntry(
    val filePath: String,
    val displayName: String
)

data class ScheduledAlarmSummary(
    val alarmId: Int,
    val categoryNameAr: String,
    val title: String,
    val scheduledTimeFormatted: String,
    val triggerAtMillis: Long
)

data class NotificationLogEntry(
    val id: Int,
    val title: String,
    val categoryNameAr: String,
    val timestampMillis: Long,
    val timeFormatted: String,
    val status: String,
    val details: String
)

object NotificationAndAlarmHelper {

    const val TEST_NOTIFICATION_ID = 9500
    const val ACTIVE_TIMER_DONE_ID = 4001
    const val QURAN_DAILY_ALARM_ID = 1501
    const val HAJJ_DAILY_ALARM_ID = 1502
    const val DEFAULT_GENERAL_TONE_NAME = "والموعد في جنة عدن (study_tone.mp3)"
    private const val DEDUPE_WINDOW_MS = 15_000L
    private const val PREFS_NAME = "study_organizer_prefs"
    private const val KEY_PENDING_IDS = "pending_alarm_ids_json"
    private const val KEY_PENDING_DETAILS = "pending_alarms_details_json"
    private const val KEY_DIAGNOSTIC_LOG = "notification_diagnostic_log_json"
    private const val KEY_GENERAL_TONE_VERSION = "general_tone_channel_version"
    private const val KEY_GENERAL_TONE_FILE_PATH = "general_tone_local_file_path"
    private const val KEY_GENERAL_TONE_DISPLAY_NAME = "general_tone_display_name"
    private const val KEY_TARGET_TONE_SPEC_PREFIX = "target_tone_spec_"
    private const val KEY_TARGET_TONE_NAME_PREFIX = "target_tone_name_"
    private const val KEY_UPLOADED_TONES_LIBRARY_JSON = "uploaded_tones_library_json"

    private val dedupeLock = Any()
    private val scheduleLock = Any()
    private val recentFiredById = mutableMapOf<Int, Long>()
    private val recentFiredBySignature = mutableMapOf<String, Long>()

    private val _activeAlertTitle = MutableStateFlow<String?>(null)
    val activeAlertTitle: StateFlow<String?> = _activeAlertTitle

    private val _generalToneName = MutableStateFlow(DEFAULT_GENERAL_TONE_NAME)
    val generalToneName: StateFlow<String> = _generalToneName

    private val _hasCustomGeneralTone = MutableStateFlow(false)
    val hasCustomGeneralTone: StateFlow<Boolean> = _hasCustomGeneralTone

    private val _targetTonesState = MutableStateFlow<Map<ToneTarget, ToneAssignment>>(emptyMap())
    val targetTonesState: StateFlow<Map<ToneTarget, ToneAssignment>> = _targetTonesState

    private val _uploadedToneLibrary = MutableStateFlow<List<UploadedToneEntry>>(emptyList())
    val uploadedToneLibrary: StateFlow<List<UploadedToneEntry>> = _uploadedToneLibrary

    @Volatile
    private var channelsCreatedOnce: Boolean = false

    @Volatile
    private var lastScheduleAllTimestamp: Long = 0L

    @Volatile
    private var lastScheduleAllSignature: String = ""

    private var activeMediaPlayer: MediaPlayer? = null
    private var alertWakeLock: PowerManager.WakeLock? = null
    private var appContextRef: Context? = null
    private var lastActiveNotificationId: Int = -1
    private var soundStartTimestamp: Long = 0L
    private var isReceiverRegistered: Boolean = false
    private var isVolumeObserverRegistered: Boolean = false
    private var initialAlarmVolume: Int = -1
    private var initialMusicVolume: Int = -1
    private var initialRingVolume: Int = -1

    fun isOtherSectionText(text: String, selectedSection: Int): Boolean {
        return (text.contains("سكشن 1") && selectedSection == 2) ||
            (text.contains("سكشن 2") && selectedSection == 1)
    }

    fun cleanSubjectName(raw: String): String {
        var s = raw
            .replace("كيف كانت الجلسة؟", "")
            .replace("انتهى وقت جلسة المذاكرة:", "")
            .replace("حان وقت مذاكرة", "")
            .replace("تقييم:", "")
            .replace("(سكشن 1)", "")
            .replace("(سكشن 2)", "")
            .replace("سكشن 1", "")
            .replace("سكشن 2", "")
            .replace("وحل المسائل", "")
            .replace("وتطبيق الكود", "")
            .replace("⭐", "")
            .replace("✅", "")
            .replace("⏹", "")
            .replace("🔔", "")
            .trim()

        if (s.startsWith("(") && s.endsWith(")") && s.length > 2) {
            s = s.substring(1, s.length - 1).trim()
        }
        s = s.removePrefix("مذاكرة ").removePrefix("مراجعة ").removePrefix("محاضرة ").trim()
        s = s.replace(Regex("\\s+"), " ").trim()
        return s.ifBlank { "Data Structures" }
    }

    fun formatShortNotificationTitle(
        category: AlarmCategory,
        rawTitle: String,
        subjectName: String = "",
        itemType: String = ""
    ): String {
        return when (category) {
            AlarmCategory.PRAYER -> {
                val pName = when {
                    rawTitle.contains("الفجر") -> "الفجر"
                    rawTitle.contains("الشروق") -> "الشروق"
                    rawTitle.contains("الظهر") -> "الظهر"
                    rawTitle.contains("العصر") -> "العصر"
                    rawTitle.contains("المغرب") -> "المغرب"
                    rawTitle.contains("العشاء") -> "العشاء"
                    else -> rawTitle.replace("حان الآن موعد أذان", "").replace("صلاة", "").trim()
                }
                "صلاة ${pName.ifBlank { "الفجر" }}"
            }
            AlarmCategory.ADHKAR -> {
                when {
                    rawTitle.contains("الصباح") -> "أذكار الصباح"
                    rawTitle.contains("المساء") -> "أذكار المساء"
                    rawTitle.contains("النوم") -> "أذكار النوم"
                    rawTitle.contains("السفر") -> "أذكار السفر"
                    else -> rawTitle.substringBefore("(").replace("⭐", "").replace("✅", "").trim().ifBlank { "أذكار الصباح" }
                }
            }
            AlarmCategory.QURAN -> "ورد قراءة القرآن"
            AlarmCategory.HAJJ -> "أذكار ومناسك الحج"
            AlarmCategory.EXERCISE -> "تمرين اليوم"
            AlarmCategory.STUDY -> {
                val subj = cleanSubjectName(subjectName.ifBlank { rawTitle })
                val isLecture = itemType.equals("LECTURE", ignoreCase = true) ||
                    rawTitle.trim().startsWith("محاضرة")
                if (isLecture) "محاضرة $subj" else "مذاكرة $subj"
            }
            AlarmCategory.RATING_FOLLOWUP -> {
                val subj = cleanSubjectName(subjectName.ifBlank { rawTitle })
                "تقييم: $subj"
            }
        }
    }

    private val volumeContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val ctx = appContextRef ?: return
            if (!canStopOnShadeInteraction()) return
            val am = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val curAlarm = am.getStreamVolume(AudioManager.STREAM_ALARM)
            val curMusic = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val curRing = am.getStreamVolume(AudioManager.STREAM_RING)
            if ((initialAlarmVolume != -1 && curAlarm != initialAlarmVolume) ||
                (initialMusicVolume != -1 && curMusic != initialMusicVolume) ||
                (initialRingVolume != -1 && curRing != initialRingVolume)
            ) {
                stopAlertSound(ctx)
            }
        }
    }

    private val alertDismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            if (!canStopOnShadeInteraction()) return
            when (action) {
                "android.media.VOLUME_CHANGED_ACTION",
                "android.media.MASTER_MUTE_CHANGED_ACTION",
                AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                    stopAlertSound(context ?: appContextRef)
                }
            }
        }
    }

    fun isAlertPlaying(): Boolean {
        return try {
            activeMediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    fun canStopOnShadeInteraction(): Boolean {
        return isAlertPlaying() && (System.currentTimeMillis() - soundStartTimestamp > 2000L)
    }

    fun getGeneralToneVersion(context: Context): Int {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_GENERAL_TONE_VERSION, 1).coerceAtLeast(1)
    }

    fun getActiveChannelId(context: Context, category: AlarmCategory): String {
        val v = getGeneralToneVersion(context)
        return when (category) {
            AlarmCategory.STUDY -> "channel_study_general_v$v"
            AlarmCategory.RATING_FOLLOWUP -> "channel_rating_general_v$v"
            else -> category.channelId
        }
    }

    fun getCustomGeneralToneFile(context: Context): File? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val spec = prefs.getString("$KEY_TARGET_TONE_SPEC_PREFIX${ToneTarget.GENERAL.key}", null)
        if (!spec.isNullOrBlank() && spec.startsWith("FILE:")) {
            val file = File(spec.removePrefix("FILE:"))
            if (file.exists() && file.canRead() && file.length() > 0L) return file
        }
        val path = prefs.getString(KEY_GENERAL_TONE_FILE_PATH, null) ?: return null
        if (path.isBlank()) return null
        val file = File(path)
        return if (file.exists() && file.canRead() && file.length() > 0L) file else null
    }

    fun getToneAssignment(context: Context, target: ToneTarget): ToneAssignment {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawSpec = prefs.getString("$KEY_TARGET_TONE_SPEC_PREFIX${target.key}", null)
        val rawName = prefs.getString("$KEY_TARGET_TONE_NAME_PREFIX${target.key}", null)

        if (!rawSpec.isNullOrBlank() && rawSpec != "DEFAULT") {
            when {
                rawSpec.startsWith("RAW:") -> {
                    val presetId = rawSpec.removePrefix("RAW:")
                    val preset = BuiltInTonePreset.fromId(presetId)
                    if (preset != null) {
                        val isDiffFromDefault = preset != target.defaultPreset
                        return ToneAssignment(
                            storageSpec = rawSpec,
                            displayName = rawName?.takeIf { it.isNotBlank() } ?: preset.displayNameAr,
                            isCustomized = isDiffFromDefault
                        )
                    }
                }
                rawSpec.startsWith("FILE:") -> {
                    val file = File(rawSpec.removePrefix("FILE:"))
                    if (file.exists() && file.canRead() && file.length() > 0L) {
                        return ToneAssignment(
                            storageSpec = rawSpec,
                            displayName = rawName?.takeIf { it.isNotBlank() } ?: file.name,
                            isCustomized = true
                        )
                    }
                }
            }
        }

        // Legacy fallback for GENERAL custom tone uploaded earlier
        if (target == ToneTarget.GENERAL) {
            val legacyPath = prefs.getString(KEY_GENERAL_TONE_FILE_PATH, null)
            if (!legacyPath.isNullOrBlank()) {
                val legacyFile = File(legacyPath)
                if (legacyFile.exists() && legacyFile.canRead() && legacyFile.length() > 0L) {
                    val legacyName = prefs.getString(KEY_GENERAL_TONE_DISPLAY_NAME, null)
                        ?.takeIf { it.isNotBlank() }
                        ?: legacyFile.name
                    return ToneAssignment(
                        storageSpec = "FILE:${legacyFile.absolutePath}",
                        displayName = legacyName,
                        isCustomized = true
                    )
                }
            }
        }

        val def = target.defaultPreset
        return ToneAssignment(
            storageSpec = "DEFAULT",
            displayName = def.displayNameAr,
            isCustomized = false
        )
    }

    private fun loadUploadedToneLibrary(context: Context): List<UploadedToneEntry> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_UPLOADED_TONES_LIBRARY_JSON, null) ?: return emptyList()
        val list = mutableListOf<UploadedToneEntry>()
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val path = obj.optString("filePath")
                val name = obj.optString("displayName")
                if (path.isNotBlank()) {
                    val f = File(path)
                    if (f.exists() && f.canRead() && f.length() > 0L) {
                        list.add(UploadedToneEntry(filePath = path, displayName = name.ifBlank { f.name }))
                    }
                }
            }
        }
        return list
    }

    private fun addEntryToUploadedToneLibrary(context: Context, entry: UploadedToneEntry) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = loadUploadedToneLibrary(context)
            .filterNot { it.filePath == entry.filePath }
            .toMutableList()
        current.add(0, entry)
        val arr = JSONArray()
        current.take(25).forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("filePath", item.filePath)
                    put("displayName", item.displayName)
                }
            )
        }
        prefs.edit().putString(KEY_UPLOADED_TONES_LIBRARY_JSON, arr.toString()).apply()
    }

    fun syncGeneralToneState(context: Context) {
        val appCtx = context.applicationContext
        val generalAssign = getToneAssignment(appCtx, ToneTarget.GENERAL)
        _generalToneName.value = generalAssign.displayName
        _hasCustomGeneralTone.value = generalAssign.isCustomized
        _targetTonesState.value = ToneTarget.entries.associateWith { target ->
            getToneAssignment(appCtx, target)
        }
        _uploadedToneLibrary.value = loadUploadedToneLibrary(appCtx)
    }

    fun getGeneralToneSoundUri(context: Context): Uri {
        val appCtx = context.applicationContext
        val generalAssign = getToneAssignment(appCtx, ToneTarget.GENERAL)
        return when {
            generalAssign.storageSpec.startsWith("FILE:") -> {
                val file = File(generalAssign.storageSpec.removePrefix("FILE:"))
                if (file.exists()) Uri.fromFile(file)
                else Uri.parse("android.resource://${appCtx.packageName}/${R.raw.study_tone}")
            }
            generalAssign.storageSpec.startsWith("RAW:") -> {
                val preset = BuiltInTonePreset.fromId(generalAssign.storageSpec.removePrefix("RAW:"))
                    ?: BuiltInTonePreset.STUDY_DEFAULT
                Uri.parse("android.resource://${appCtx.packageName}/${preset.rawRes}")
            }
            else -> Uri.parse("android.resource://${appCtx.packageName}/${R.raw.study_tone}")
        }
    }

    suspend fun saveCustomGeneralToneFromUri(
        context: Context,
        sourceUri: Uri,
        explicitDisplayName: String? = null
    ): Boolean {
        return saveCustomToneForTarget(
            context = context,
            target = ToneTarget.GENERAL,
            sourceUri = sourceUri,
            explicitDisplayName = explicitDisplayName
        )
    }

    suspend fun saveCustomToneForTarget(
        context: Context,
        target: ToneTarget,
        sourceUri: Uri,
        explicitDisplayName: String? = null
    ): Boolean {
        val appCtx = context.applicationContext
        return try {
            val resolvedName = explicitDisplayName?.takeIf { it.isNotBlank() }
                ?: resolveDisplayNameFromUri(appCtx, sourceUri)
                ?: "نغمة مخصصة (MP3)"

            val tonesDir = File(appCtx.filesDir, "custom_tones")
            if (!tonesDir.exists()) {
                tonesDir.mkdirs()
            }

            val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val nextVersion = (prefs.getInt(KEY_GENERAL_TONE_VERSION, 1) + 1).coerceAtLeast(2)
            val safeFileBase = resolvedName.replace(Regex("[^a-zA-Z0-9._\\-\\u0600-\\u06FF]"), "_").take(40)
            val targetFile = File(tonesDir, "tone_v${nextVersion}_${safeFileBase}.mp3")

            val inputStream = appCtx.contentResolver.openInputStream(sourceUri) ?: return false
            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (!targetFile.exists() || targetFile.length() <= 0L) {
                return false
            }

            val spec = "FILE:${targetFile.absolutePath}"
            addEntryToUploadedToneLibrary(appCtx, UploadedToneEntry(targetFile.absolutePath, resolvedName))
            assignToneSpecInternal(appCtx, target, spec, resolvedName, bumpGeneralVersion = true)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun assignBuiltInPresetToTarget(
        context: Context,
        target: ToneTarget,
        preset: BuiltInTonePreset
    ) {
        val spec = if (preset == target.defaultPreset) "DEFAULT" else "RAW:${preset.id}"
        assignToneSpecInternal(context.applicationContext, target, spec, preset.displayNameAr, bumpGeneralVersion = true)
    }

    suspend fun assignUploadedEntryToTarget(
        context: Context,
        target: ToneTarget,
        entry: UploadedToneEntry
    ) {
        assignToneSpecInternal(
            context.applicationContext,
            target,
            "FILE:${entry.filePath}",
            entry.displayName,
            bumpGeneralVersion = true
        )
    }

    suspend fun swapTonesBetweenTargets(
        context: Context,
        first: ToneTarget,
        second: ToneTarget
    ) {
        if (first == second) return
        val appCtx = context.applicationContext
        val firstAssign = getToneAssignment(appCtx, first)
        val secondAssign = getToneAssignment(appCtx, second)

        val specForFirst = if (secondAssign.storageSpec == "DEFAULT") {
            if (second.defaultPreset == first.defaultPreset) "DEFAULT" else "RAW:${second.defaultPreset.id}"
        } else {
            secondAssign.storageSpec
        }
        val specForSecond = if (firstAssign.storageSpec == "DEFAULT") {
            if (first.defaultPreset == second.defaultPreset) "DEFAULT" else "RAW:${first.defaultPreset.id}"
        } else {
            firstAssign.storageSpec
        }

        writeTargetTonePrefs(appCtx, first, specForFirst, secondAssign.displayName)
        writeTargetTonePrefs(appCtx, second, specForSecond, firstAssign.displayName)

        finalizeToneConfigChange(appCtx, reason = "TONES_SWAPPED_${first.key}_${second.key}")
    }

    private fun writeTargetTonePrefs(
        appCtx: Context,
        target: ToneTarget,
        storageSpec: String,
        displayName: String
    ) {
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (storageSpec == "DEFAULT") {
            editor.remove("$KEY_TARGET_TONE_SPEC_PREFIX${target.key}")
            editor.remove("$KEY_TARGET_TONE_NAME_PREFIX${target.key}")
            if (target == ToneTarget.GENERAL) {
                editor.remove(KEY_GENERAL_TONE_FILE_PATH)
                editor.putString(KEY_GENERAL_TONE_DISPLAY_NAME, DEFAULT_GENERAL_TONE_NAME)
            }
        } else {
            editor.putString("$KEY_TARGET_TONE_SPEC_PREFIX${target.key}", storageSpec)
            editor.putString("$KEY_TARGET_TONE_NAME_PREFIX${target.key}", displayName)
            if (target == ToneTarget.GENERAL) {
                if (storageSpec.startsWith("FILE:")) {
                    editor.putString(KEY_GENERAL_TONE_FILE_PATH, storageSpec.removePrefix("FILE:"))
                } else {
                    editor.remove(KEY_GENERAL_TONE_FILE_PATH)
                }
                editor.putString(KEY_GENERAL_TONE_DISPLAY_NAME, displayName)
            }
        }
        editor.apply()
    }

    private suspend fun assignToneSpecInternal(
        appCtx: Context,
        target: ToneTarget,
        storageSpec: String,
        displayName: String,
        bumpGeneralVersion: Boolean
    ) {
        writeTargetTonePrefs(appCtx, target, storageSpec, displayName)
        finalizeToneConfigChange(
            appCtx = appCtx,
            reason = "TONE_ASSIGNED_${target.key}",
            bumpVersion = bumpGeneralVersion
        )
    }

    private suspend fun finalizeToneConfigChange(
        appCtx: Context,
        reason: String,
        bumpVersion: Boolean = true
    ) {
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (bumpVersion) {
            val nextVersion = (prefs.getInt(KEY_GENERAL_TONE_VERSION, 1) + 1).coerceAtLeast(2)
            prefs.edit().putInt(KEY_GENERAL_TONE_VERSION, nextVersion).apply()
        }
        syncGeneralToneState(appCtx)
        channelsCreatedOnce = false
        createNotificationChannels(appCtx, forceRecreateGeneral = true)
        scheduleAllAlarms(appCtx, reason = reason, forceReschedule = true)
    }

    suspend fun resetToneForTarget(context: Context, target: ToneTarget) {
        val appCtx = context.applicationContext
        assignToneSpecInternal(
            appCtx = appCtx,
            target = target,
            storageSpec = "DEFAULT",
            displayName = target.defaultPreset.displayNameAr,
            bumpGeneralVersion = true
        )
    }

    suspend fun resetGeneralToneToDefault(context: Context) {
        resetToneForTarget(context, ToneTarget.GENERAL)
    }

    fun previewGeneralTone(context: Context) {
        previewToneForTarget(context, ToneTarget.GENERAL)
    }

    fun previewToneForTarget(context: Context, target: ToneTarget) {
        val appCtx = context.applicationContext
        syncGeneralToneState(appCtx)
        val assign = getToneAssignment(appCtx, target)
        playAlertSound(
            context = appCtx,
            category = target.category,
            customToneUri = "",
            alertTitle = "معاينة (${target.labelAr}): ${assign.displayName}",
            explicitTarget = target
        )
    }

    fun resolveToneTargetForEvent(category: AlarmCategory, titleOrSubject: String): ToneTarget {
        val clean = titleOrSubject.trim()
        return when (category) {
            AlarmCategory.PRAYER -> when {
                clean.contains("الفجر") -> ToneTarget.PRAYER_FAJR
                clean.contains("الظهر") -> ToneTarget.PRAYER_DHUHR
                clean.contains("العصر") -> ToneTarget.PRAYER_ASR
                clean.contains("المغرب") -> ToneTarget.PRAYER_MAGHRIB
                clean.contains("العشاء") -> ToneTarget.PRAYER_ISHA
                else -> ToneTarget.PRAYER_ALL
            }
            AlarmCategory.ADHKAR -> when {
                clean.contains("الصباح") -> ToneTarget.ADHKAR_MORNING
                clean.contains("المساء") -> ToneTarget.ADHKAR_EVENING
                clean.contains("النوم") -> ToneTarget.ADHKAR_SLEEP
                clean.contains("السفر") -> ToneTarget.ADHKAR_TRAVEL
                else -> ToneTarget.ADHKAR_ALL
            }
            AlarmCategory.QURAN -> ToneTarget.QURAN
            AlarmCategory.HAJJ -> ToneTarget.HAJJ
            AlarmCategory.EXERCISE -> ToneTarget.EXERCISE
            AlarmCategory.STUDY, AlarmCategory.RATING_FOLLOWUP -> ToneTarget.GENERAL
        }
    }

    fun resolveEffectiveToneAssignment(
        context: Context,
        category: AlarmCategory,
        titleOrSubject: String = "",
        explicitTarget: ToneTarget? = null
    ): ToneAssignment {
        val appCtx = context.applicationContext
        val primaryTarget = explicitTarget ?: resolveToneTargetForEvent(category, titleOrSubject)
        val primaryAssign = getToneAssignment(appCtx, primaryTarget)
        if (primaryAssign.isCustomized || primaryAssign.storageSpec != "DEFAULT") {
            return primaryAssign
        }
        val parentKey = primaryTarget.parentAllTargetKey
        if (parentKey != null) {
            val parentTarget = ToneTarget.entries.find { it.key == parentKey }
            if (parentTarget != null) {
                val parentAssign = getToneAssignment(appCtx, parentTarget)
                if (parentAssign.isCustomized || parentAssign.storageSpec != "DEFAULT") {
                    return parentAssign
                }
            }
        }
        return primaryAssign
    }

    fun saveCustomTaskToneFromUri(context: Context, sourceUri: Uri): String {
        val appCtx = context.applicationContext
        return try {
            val taskTonesDir = File(appCtx.filesDir, "task_tones")
            if (!taskTonesDir.exists()) {
                taskTonesDir.mkdirs()
            }
            val safeName = (resolveDisplayNameFromUri(appCtx, sourceUri) ?: "task_tone.mp3")
                .replace(Regex("[^a-zA-Z0-9._\\-\\u0600-\\u06FF]"), "_")
            val targetFile = File(taskTonesDir, "${System.currentTimeMillis()}_$safeName")
            val inputStream = appCtx.contentResolver.openInputStream(sourceUri) ?: return sourceUri.toString()
            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() > 0L) {
                Uri.fromFile(targetFile).toString()
            } else {
                sourceUri.toString()
            }
        } catch (_: Exception) {
            sourceUri.toString()
        }
    }

    private fun resolveDisplayNameFromUri(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        }.getOrNull()
    }

    fun createNotificationChannels(context: Context, forceRecreateGeneral: Boolean = false) {
        syncGeneralToneState(context)
        if (channelsCreatedOnce && !forceRecreateGeneral) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            synchronized(this) {
                if (channelsCreatedOnce && !forceRecreateGeneral) return
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val currentGenVer = getGeneralToneVersion(context)

                listOf(
                    "channel_prayer_v1", "channel_adhkar_v1", "channel_exercise_v1", "channel_study_v1", "channel_rating_v1",
                    "channel_prayer_v2", "channel_adhkar_v2", "channel_exercise_v2", "channel_study_v2", "channel_rating_v2",
                    "channel_prayer_v3", "channel_adhkar_v3", "channel_exercise_v3", "channel_study_v3", "channel_rating_v3",
                    "channel_exercise_v4", "channel_study_v4", "channel_rating_v4"
                ).forEach { oldId ->
                    runCatching { manager.deleteNotificationChannel(oldId) }
                }

                for (v in 1..currentGenVer) {
                    runCatching { manager.deleteNotificationChannel("channel_general_study_v$v") }
                    runCatching { manager.deleteNotificationChannel("channel_general_rating_v$v") }
                }
                for (v in 1 until currentGenVer) {
                    runCatching { manager.deleteNotificationChannel("channel_study_general_v$v") }
                    runCatching { manager.deleteNotificationChannel("channel_rating_general_v$v") }
                }

                val alarmAudioAttrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val generalSoundUri = getGeneralToneSoundUri(context)

                AlarmCategory.entries.forEach { cat ->
                    val activeChannelId = getActiveChannelId(context, cat)
                    val existing = manager.getNotificationChannel(activeChannelId)
                    if (existing == null) {
                        val channel = NotificationChannel(
                            activeChannelId,
                            if (cat.isGeneralCategory()) "${cat.channelNameAr} (v$currentGenVer)" else cat.channelNameAr,
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = cat.channelNameAr
                            enableVibration(true)
                            vibrationPattern = longArrayOf(0, 500, 250, 500)
                            if (cat.isGeneralCategory()) {
                                setSound(generalSoundUri, alarmAudioAttrs)
                            } else {
                                setSound(null, null)
                            }
                            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                        }
                        manager.createNotificationChannel(channel)
                    }
                }
                channelsCreatedOnce = true
            }
        }
    }

    suspend fun clearNotificationCacheAndResync(context: Context): Int {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        synchronized(dedupeLock) {
            recentFiredById.clear()
            recentFiredBySignature.clear()
            val editor = prefs.edit()
            prefs.all.keys.filter { it.startsWith("dedupe_") }.forEach { key ->
                editor.remove(key)
            }
            editor.apply()
        }
        synchronized(scheduleLock) {
            lastScheduleAllSignature = ""
            lastScheduleAllTimestamp = 0L
            channelsCreatedOnce = false
        }
        createNotificationChannels(appCtx, forceRecreateGeneral = true)
        scheduleAllAlarms(appCtx, reason = "CACHE_CLEARED_AND_RESYNCED", forceReschedule = true)
        return getPendingScheduledAlarms(appCtx).size
    }

    suspend fun scheduleAllAlarms(
        context: Context,
        reason: String = "sync",
        forceReschedule: Boolean = false
    ) {
        val appCtx = context.applicationContext
        createNotificationChannels(appCtx)

        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val masterEnabled = prefs.getBoolean("master_notifications_enabled", true)
        val syncAlertWithNotif = prefs.getBoolean("sync_alert_with_notification", true)

        val notifyPrayer = prefs.getBoolean("notify_prayer", true)
        val alertSoundPrayer = prefs.getBoolean("alert_sound_prayer", true)

        val notifyAdhkar = prefs.getBoolean("notify_adhkar", true)
        val alertSoundAdhkar = prefs.getBoolean("alert_sound_adhkar", true)

        val notifyQuran = prefs.getBoolean("notify_quran", true)
        val alertSoundQuran = prefs.getBoolean("alert_sound_quran", true)
        val quranAlarmTime = prefs.getString("quran_alarm_time", "21:00") ?: "21:00"

        val notifyHajj = prefs.getBoolean("notify_hajj", true)
        val alertSoundHajj = prefs.getBoolean("alert_sound_hajj", true)
        val hajjAlarmTime = prefs.getString("hajj_alarm_time", "17:30") ?: "17:30"

        val notifyExercise = prefs.getBoolean("notify_exercise", true)
        val alertSoundExercise = prefs.getBoolean("alert_sound_exercise", true)

        val notifyStudy = prefs.getBoolean("notify_study", true)
        val alertSoundStudy = prefs.getBoolean("alert_sound_study", true)

        val selectedSection = prefs.getInt("section_number", 1).coerceIn(1, 2)
        val methodId = prefs.getString("prayer_method", PrayerCalcMethod.EGYPTIAN.id) ?: PrayerCalcMethod.EGYPTIAN.id
        val lat = prefs.getFloat("gps_lat", 30.0595f).toDouble()
        val lng = prefs.getFloat("gps_lng", 31.3137f).toDouble()
        val manualOffsets = listOf("FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA")
            .associateWith { prefs.getInt("offset_$it", 0) }
        val prayerEnabledMap = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
            .associateWith { prefs.getBoolean("prayer_enabled_$it", true) }

        val dao = AppDatabase.getInstance(appCtx).appDao()
        val adhkarList = runCatching { dao.getAllAdhkarAlarmsOnce() }.getOrDefault(emptyList())
        val scheduleItems = runCatching { dao.getAllScheduleItemsOnce() }.getOrDefault(emptyList())

        val stateSignature = buildString {
            append("$masterEnabled|$syncAlertWithNotif|$notifyPrayer|$alertSoundPrayer|$notifyAdhkar|$alertSoundAdhkar|")
            append("$notifyQuran|$alertSoundQuran|$quranAlarmTime|$notifyHajj|$alertSoundHajj|$hajjAlarmTime|")
            append("$notifyExercise|$alertSoundExercise|$notifyStudy|$alertSoundStudy|$selectedSection|$methodId|$lat|$lng|")
            append(manualOffsets.entries.joinToString(",") { "${it.key}=${it.value}" })
            append("|PE:")
            append(prayerEnabledMap.entries.joinToString(",") { "${it.key}=${it.value}" })
            append("|A:")
            append(adhkarList.joinToString(";") { "${it.id}:${it.title}:${it.alarmTime}:${it.isEnabled}:${it.category}" })
            append("|S:")
            append(scheduleItems.joinToString(";") { "${it.id}:${it.title}:${it.dayOfWeek}:${it.startTime}:${it.endTime}:${it.recurrence}:${it.customToneUri}:${it.alarmEnabled}:${it.sectionFilter}" })
        }

        val now = System.currentTimeMillis()
        synchronized(scheduleLock) {
            if (!forceReschedule &&
                stateSignature == lastScheduleAllSignature &&
                (now - lastScheduleAllTimestamp) < 2_000L
            ) {
                return
            }
            lastScheduleAllSignature = stateSignature
            lastScheduleAllTimestamp = now

            cancelAllPendingAlarmsInternal(appCtx, prefs)

            val scheduledSummaries = mutableListOf<ScheduledAlarmSummary>()

            if (!masterEnabled) {
                savePendingAlarmsMetadata(prefs, scheduledSummaries)
                appendDiagnosticLog(
                    context = appCtx,
                    id = 0,
                    title = "إيقاف شامل (0)",
                    categoryNameAr = "النظام",
                    status = "MASTER_DISABLED",
                    details = "المفتاح الرئيسي متوقف ($reason)"
                )
                return
            }

            // 1. Schedule 5 Daily Prayers (Deterministic IDs: 1001..1005)
            if (notifyPrayer || alertSoundPrayer) {
                val prayerSchedule = PrayerTimesCalculator.calculateTodayPrayers(
                    latitude = lat,
                    longitude = lng,
                    method = PrayerCalcMethod.fromId(methodId),
                    manualOffsets = manualOffsets
                )
                prayerSchedule.fivePrayers.forEachIndexed { idx, p ->
                    if (prayerEnabledMap[p.key] != false) {
                        val deterministicId = 1001 + idx
                        val triggerAt = computeNextDailyTriggerMillis(p.time24)
                        if (triggerAt > 0L) {
                            val title = "صلاة ${p.arabicName}"
                            scheduleExactAtMillisInternal(
                                context = appCtx,
                                requestCode = deterministicId,
                                triggerAtMillis = triggerAt,
                                title = title,
                                message = "",
                                category = AlarmCategory.PRAYER,
                                subjectName = title,
                                customToneUri = "",
                                recurrenceType = "DAILY",
                                time24 = p.time24,
                                dayOfWeekAr = ""
                            )
                            scheduledSummaries.add(
                                ScheduledAlarmSummary(
                                    alarmId = deterministicId,
                                    categoryNameAr = "الصلاة",
                                    title = title,
                                    scheduledTimeFormatted = formatDateTime(triggerAt),
                                    triggerAtMillis = triggerAt
                                )
                            )
                        }
                    }
                }
            }

            // 1.5 Schedule Daily Quran Reading Alarm (ID: 1501) & Daily Hajj/Umrah Dhikr Alarm (ID: 1502)
            if (notifyQuran || alertSoundQuran) {
                val triggerAt = computeNextDailyTriggerMillis(quranAlarmTime)
                if (triggerAt > 0L) {
                    val title = "ورد قراءة القرآن"
                    scheduleExactAtMillisInternal(
                        context = appCtx,
                        requestCode = QURAN_DAILY_ALARM_ID,
                        triggerAtMillis = triggerAt,
                        title = title,
                        message = "",
                        category = AlarmCategory.QURAN,
                        subjectName = title,
                        customToneUri = "",
                        recurrenceType = "DAILY",
                        time24 = quranAlarmTime,
                        dayOfWeekAr = ""
                    )
                    scheduledSummaries.add(
                        ScheduledAlarmSummary(
                            alarmId = QURAN_DAILY_ALARM_ID,
                            categoryNameAr = "قراءة القرآن",
                            title = title,
                            scheduledTimeFormatted = formatDateTime(triggerAt),
                            triggerAtMillis = triggerAt
                        )
                    )
                }
            }

            if (notifyHajj || alertSoundHajj) {
                val triggerAt = computeNextDailyTriggerMillis(hajjAlarmTime)
                if (triggerAt > 0L) {
                    val title = "أذكار ومناسك الحج"
                    scheduleExactAtMillisInternal(
                        context = appCtx,
                        requestCode = HAJJ_DAILY_ALARM_ID,
                        triggerAtMillis = triggerAt,
                        title = title,
                        message = "",
                        category = AlarmCategory.HAJJ,
                        subjectName = title,
                        customToneUri = "",
                        recurrenceType = "DAILY",
                        time24 = hajjAlarmTime,
                        dayOfWeekAr = ""
                    )
                    scheduledSummaries.add(
                        ScheduledAlarmSummary(
                            alarmId = HAJJ_DAILY_ALARM_ID,
                            categoryNameAr = "الحج والعمرة",
                            title = title,
                            scheduledTimeFormatted = formatDateTime(triggerAt),
                            triggerAtMillis = triggerAt
                        )
                    )
                }
            }

            // 2. Schedule Adhkar & Daily Exercise Alarms (Deterministic IDs: 2000 + alarm.id)
            val adhkarActive = notifyAdhkar || alertSoundAdhkar
            val exerciseActive = notifyExercise || alertSoundExercise
            if (adhkarActive || exerciseActive) {
                val seenAdhkarSlots = mutableSetOf<String>()
                adhkarList.filter { it.isEnabled }.forEach { alarm ->
                    val isEx = alarm.category.contains("التمارين")
                    val allowed = (isEx && exerciseActive) || (!isEx && adhkarActive)
                    val slotKey = "${if (isEx) "EX" else "AD"}_${alarm.alarmTime.trim()}_${alarm.title.trim()}"
                    if (allowed && seenAdhkarSlots.add(slotKey)) {
                        val deterministicId = 2000 + alarm.id
                        val triggerAt = computeNextDailyTriggerMillis(alarm.alarmTime)
                        if (triggerAt > 0L) {
                            val cat = if (isEx) AlarmCategory.EXERCISE else AlarmCategory.ADHKAR
                            val shortTitle = formatShortNotificationTitle(cat, alarm.title)
                            scheduleExactAtMillisInternal(
                                context = appCtx,
                                requestCode = deterministicId,
                                triggerAtMillis = triggerAt,
                                title = shortTitle,
                                message = "",
                                category = cat,
                                subjectName = shortTitle,
                                customToneUri = "",
                                recurrenceType = "DAILY",
                                time24 = alarm.alarmTime,
                                dayOfWeekAr = ""
                            )
                            scheduledSummaries.add(
                                ScheduledAlarmSummary(
                                    alarmId = deterministicId,
                                    categoryNameAr = if (isEx) "التمارين" else "الأذكار",
                                    title = shortTitle,
                                    scheduledTimeFormatted = formatDateTime(triggerAt),
                                    triggerAtMillis = triggerAt
                                )
                            )
                        }
                    }
                }
            }

            // 3. Schedule Study & Lecture Items for ONLY the User's Selected Section (Deterministic IDs: 3000 + item.id)
            if (notifyStudy || alertSoundStudy) {
                val seenStudySlots = mutableSetOf<String>()
                scheduleItems
                    .filter {
                        it.alarmEnabled &&
                            (it.sectionFilter == 0 || it.sectionFilter == selectedSection) &&
                            !isOtherSectionText(it.title, selectedSection)
                    }
                    .forEach { item ->
                        val cleanSubj = cleanSubjectName(item.title)
                        val slotKey = "${item.dayOfWeek.trim()}_${item.startTime.trim()}"
                        val subjectTimeKey = "${item.dayOfWeek.trim()}_${item.startTime.trim()}_${cleanSubj.lowercase()}"
                        if (seenStudySlots.add(slotKey) && seenStudySlots.add(subjectTimeKey)) {
                            val deterministicId = 3000 + item.id
                            val normalizedRecurrence = when {
                                item.recurrence.equals("DAILY", ignoreCase = true) -> "DAILY"
                                item.recurrence.equals("ONCE", ignoreCase = true) -> "ONCE"
                                else -> "WEEKLY"
                            }
                            val triggerAt = when (normalizedRecurrence) {
                                "DAILY" -> computeNextDailyTriggerMillis(item.startTime)
                                "ONCE" -> computeNextWeeklyTriggerMillis(item.dayOfWeek, item.startTime)
                                else -> computeNextWeeklyTriggerMillis(item.dayOfWeek, item.startTime)
                            }
                            if (triggerAt > 0L) {
                                val shortTitle = formatShortNotificationTitle(
                                    category = AlarmCategory.STUDY,
                                    rawTitle = item.title,
                                    subjectName = cleanSubj,
                                    itemType = item.itemType
                                )
                                scheduleExactAtMillisInternal(
                                    context = appCtx,
                                    requestCode = deterministicId,
                                    triggerAtMillis = triggerAt,
                                    title = shortTitle,
                                    message = "",
                                    category = AlarmCategory.STUDY,
                                    subjectName = cleanSubj,
                                    customToneUri = item.customToneUri,
                                    recurrenceType = normalizedRecurrence,
                                    time24 = item.startTime,
                                    dayOfWeekAr = item.dayOfWeek
                                )
                                scheduledSummaries.add(
                                    ScheduledAlarmSummary(
                                        alarmId = deterministicId,
                                        categoryNameAr = "المذاكرة",
                                        title = shortTitle,
                                        scheduledTimeFormatted = formatDateTime(triggerAt),
                                        triggerAtMillis = triggerAt
                                    )
                                )
                            }
                        }
                    }
            }

            savePendingAlarmsMetadata(prefs, scheduledSummaries)
            appendDiagnosticLog(
                context = appCtx,
                id = 0,
                title = "جدولة ومزامنة (${scheduledSummaries.size})",
                categoryNameAr = "النظام",
                status = "SCHEDULED_ALL",
                details = "سكشن $selectedSection ($reason)"
            )
        }
    }

    fun cancelAllPendingAlarms(context: Context) {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        synchronized(scheduleLock) {
            cancelAllPendingAlarmsInternal(appCtx, prefs)
        }
    }

    fun cancelSingleAlarm(context: Context, alarmId: Int) {
        val appCtx = context.applicationContext
        val alarmManager = appCtx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val followupId = 50000 + (alarmId % 9000)
        listOf(alarmId, 50000 + alarmId, followupId).forEach { code ->
            listOf(
                Intent(appCtx, AlarmReceiver::class.java),
                Intent(appCtx, AlarmReceiver::class.java).apply {
                    action = "com.example.notifications.ACTION_TRIGGER_ALARM_$code"
                }
            ).forEach { cancelIntent ->
                val pi = PendingIntent.getBroadcast(
                    appCtx,
                    code,
                    cancelIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pi != null) {
                    runCatching {
                        alarmManager.cancel(pi)
                        pi.cancel()
                    }
                }
            }
        }
    }

    private fun cancelAllPendingAlarmsInternal(context: Context, prefs: android.content.SharedPreferences) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val idsToCancel = mutableSetOf<Int>()

        val rawIds = prefs.getString(KEY_PENDING_IDS, null)
        if (!rawIds.isNullOrBlank()) {
            runCatching {
                val arr = JSONArray(rawIds)
                for (i in 0 until arr.length()) {
                    val id = arr.optInt(i)
                    idsToCancel.add(id)
                    idsToCancel.add(50000 + id)
                }
            }
        }

        for (id in 1000..1010) {
            idsToCancel.add(id)
            idsToCancel.add(50000 + id)
        }
        idsToCancel.add(QURAN_DAILY_ALARM_ID)
        idsToCancel.add(50000 + QURAN_DAILY_ALARM_ID)
        idsToCancel.add(HAJJ_DAILY_ALARM_ID)
        idsToCancel.add(50000 + HAJJ_DAILY_ALARM_ID)
        for (id in 2000..2050) {
            idsToCancel.add(id)
            idsToCancel.add(50000 + id)
        }
        for (id in 3000..3080) {
            idsToCancel.add(id)
            idsToCancel.add(50000 + id)
        }
        idsToCancel.add(50000)

        idsToCancel.forEach { code ->
            val legacyIntent = Intent(context, AlarmReceiver::class.java)
            val legacyPi = PendingIntent.getBroadcast(
                context,
                code,
                legacyIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (legacyPi != null) {
                runCatching {
                    alarmManager.cancel(legacyPi)
                    legacyPi.cancel()
                }
            }

            val actionIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.notifications.ACTION_TRIGGER_ALARM_$code"
            }
            val actionPi = PendingIntent.getBroadcast(
                context,
                code,
                actionIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (actionPi != null) {
                runCatching {
                    alarmManager.cancel(actionPi)
                    actionPi.cancel()
                }
            }
        }

        prefs.edit()
            .remove(KEY_PENDING_IDS)
            .remove(KEY_PENDING_DETAILS)
            .apply()
    }

    fun scheduleTestNotificationIn10Seconds(context: Context) {
        val appCtx = context.applicationContext
        createNotificationChannels(appCtx)
        val alarmManager = appCtx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        listOf(
            Intent(appCtx, AlarmReceiver::class.java),
            Intent(appCtx, AlarmReceiver::class.java).apply {
                action = "com.example.notifications.ACTION_TRIGGER_ALARM_$TEST_NOTIFICATION_ID"
            }
        ).forEach { cancelIntent ->
            val existingPi = PendingIntent.getBroadcast(
                appCtx,
                TEST_NOTIFICATION_ID,
                cancelIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (existingPi != null) {
                runCatching {
                    alarmManager.cancel(existingPi)
                    existingPi.cancel()
                }
            }
        }

        val triggerAt = System.currentTimeMillis() + 10_000L
        scheduleExactAtMillisInternal(
            context = appCtx,
            requestCode = TEST_NOTIFICATION_ID,
            triggerAtMillis = triggerAt,
            title = "مذاكرة Data Structures",
            message = "",
            category = AlarmCategory.STUDY,
            subjectName = "Data Structures",
            customToneUri = "",
            recurrenceType = "ONCE",
            time24 = "",
            dayOfWeekAr = ""
        )

        appendDiagnosticLog(
            context = appCtx,
            id = TEST_NOTIFICATION_ID,
            title = "مذاكرة Data Structures",
            categoryNameAr = "المذاكرة",
            status = "SCHEDULED_TEST",
            details = formatDateTime(triggerAt)
        )
    }

    fun scheduleDailyExactAlarm(
        context: Context,
        requestCode: Int,
        time24: String,
        title: String,
        message: String,
        category: AlarmCategory,
        subjectName: String = "",
        customToneUri: String = ""
    ) {
        val triggerAt = computeNextDailyTriggerMillis(time24)
        if (triggerAt <= 0L) return
        scheduleExactAtMillisInternal(
            context = context.applicationContext,
            requestCode = requestCode,
            triggerAtMillis = triggerAt,
            title = title,
            message = message,
            category = category,
            subjectName = subjectName,
            customToneUri = customToneUri,
            recurrenceType = "DAILY",
            time24 = time24,
            dayOfWeekAr = ""
        )
    }

    fun scheduleWeeklyExactAlarm(
        context: Context,
        requestCode: Int,
        dayOfWeekAr: String,
        time24: String,
        title: String,
        message: String,
        category: AlarmCategory,
        subjectName: String = "",
        customToneUri: String = ""
    ) {
        val triggerAt = computeNextWeeklyTriggerMillis(dayOfWeekAr, time24)
        if (triggerAt <= 0L) return
        scheduleExactAtMillisInternal(
            context = context.applicationContext,
            requestCode = requestCode,
            triggerAtMillis = triggerAt,
            title = title,
            message = message,
            category = category,
            subjectName = subjectName,
            customToneUri = customToneUri,
            recurrenceType = "WEEKLY",
            time24 = time24,
            dayOfWeekAr = dayOfWeekAr
        )
    }

    fun scheduleExactAtMillis(
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        message: String,
        category: AlarmCategory,
        subjectName: String = "",
        customToneUri: String = ""
    ) {
        scheduleExactAtMillisInternal(
            context = context.applicationContext,
            requestCode = requestCode,
            triggerAtMillis = triggerAtMillis,
            title = title,
            message = message,
            category = category,
            subjectName = subjectName,
            customToneUri = customToneUri,
            recurrenceType = "ONCE",
            time24 = "",
            dayOfWeekAr = ""
        )
    }

    private fun scheduleExactAtMillisInternal(
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        message: String,
        category: AlarmCategory,
        subjectName: String,
        customToneUri: String,
        recurrenceType: String,
        time24: String,
        dayOfWeekAr: String
    ) {
        val cleanTitle = formatShortNotificationTitle(category, title, subjectName)
        val cleanSubj = cleanSubjectName(subjectName.ifBlank { cleanTitle })
        val shortTime12h = when {
            time24.isNotBlank() -> Time12hFormatter.format24To12Ar(time24)
            message.isNotBlank() -> Time12hFormatter.replaceAll24hTimesWith12hAr(message)
            else -> ""
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.notifications.ACTION_TRIGGER_ALARM_$requestCode"
            putExtra("ALARM_ID", requestCode)
            putExtra("TITLE", cleanTitle)
            putExtra("MESSAGE", shortTime12h)
            putExtra("CATEGORY", category.name)
            putExtra("SUBJECT_NAME", cleanSubj)
            putExtra("CUSTOM_TONE_URI", customToneUri)
            putExtra("RECURRENCE_TYPE", recurrenceType)
            putExtra("TIME_24", time24)
            putExtra("DAY_OF_WEEK_AR", dayOfWeekAr)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        runCatching { alarmManager.cancel(pendingIntent) }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun isCategoryNotificationEnabled(prefs: android.content.SharedPreferences, category: AlarmCategory): Boolean {
        return when (category) {
            AlarmCategory.PRAYER -> prefs.getBoolean("notify_prayer", true)
            AlarmCategory.ADHKAR -> prefs.getBoolean("notify_adhkar", true)
            AlarmCategory.QURAN -> prefs.getBoolean("notify_quran", true)
            AlarmCategory.HAJJ -> prefs.getBoolean("notify_hajj", true)
            AlarmCategory.EXERCISE -> prefs.getBoolean("notify_exercise", true)
            AlarmCategory.STUDY, AlarmCategory.RATING_FOLLOWUP -> prefs.getBoolean("notify_study", true)
        }
    }

    private fun isCategoryAlertSoundEnabled(prefs: android.content.SharedPreferences, category: AlarmCategory): Boolean {
        val syncMaster = prefs.getBoolean("sync_alert_with_notification", true)
        if (!syncMaster) return false
        return when (category) {
            AlarmCategory.PRAYER -> prefs.getBoolean("alert_sound_prayer", true)
            AlarmCategory.ADHKAR -> prefs.getBoolean("alert_sound_adhkar", true)
            AlarmCategory.QURAN -> prefs.getBoolean("alert_sound_quran", true)
            AlarmCategory.HAJJ -> prefs.getBoolean("alert_sound_hajj", true)
            AlarmCategory.EXERCISE -> prefs.getBoolean("alert_sound_exercise", true)
            AlarmCategory.STUDY, AlarmCategory.RATING_FOLLOWUP -> prefs.getBoolean("alert_sound_study", true)
        }
    }

    private fun resolveDisplayNotificationId(category: AlarmCategory, cleanTitle: String, notificationId: Int): Int {
        return when (category) {
            AlarmCategory.PRAYER -> when {
                cleanTitle.contains("الفجر") -> 7101
                cleanTitle.contains("الشروق") -> 7102
                cleanTitle.contains("الظهر") -> 7103
                cleanTitle.contains("العصر") -> 7104
                cleanTitle.contains("المغرب") -> 7105
                cleanTitle.contains("العشاء") -> 7106
                else -> 7100 + (notificationId.coerceAtLeast(0) % 10)
            }
            AlarmCategory.ADHKAR -> 7200 + (notificationId.coerceAtLeast(0) % 40)
            AlarmCategory.QURAN -> 7301
            AlarmCategory.HAJJ -> 7302
            AlarmCategory.EXERCISE -> 7401
            AlarmCategory.STUDY -> 7500 + (notificationId.coerceAtLeast(0) % 80)
            AlarmCategory.RATING_FOLLOWUP -> 7600 + (notificationId.coerceAtLeast(0) % 80)
        }
    }

    fun triggerImmediateAlertAndNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        category: AlarmCategory,
        subjectName: String = "",
        customToneUri: String = "",
        bypassDedupeForManualTest: Boolean = false
    ): Boolean {
        val appCtx = context.applicationContext
        appContextRef = appCtx

        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val masterEnabled = prefs.getBoolean("master_notifications_enabled", true)
        if (!masterEnabled && !bypassDedupeForManualTest) {
            appendDiagnosticLog(
                context = appCtx,
                id = notificationId,
                title = title,
                categoryNameAr = category.channelNameAr,
                status = "MASTER_DISABLED",
                details = "المفتاح الرئيسي متوقف"
            )
            return false
        }

        val showNotif = bypassDedupeForManualTest || isCategoryNotificationEnabled(prefs, category)
        val playSound = bypassDedupeForManualTest || isCategoryAlertSoundEnabled(prefs, category)
        if (!showNotif && !playSound) {
            appendDiagnosticLog(
                context = appCtx,
                id = notificationId,
                title = title,
                categoryNameAr = category.channelNameAr,
                status = "CATEGORY_DISABLED",
                details = "القسم متوقف من الإعدادات"
            )
            return false
        }

        val activeSection = prefs.getInt("section_number", 1).coerceIn(1, 2)
        val combinedText = "$title $subjectName"
        if (isOtherSectionText(combinedText, activeSection)) {
            appendDiagnosticLog(
                context = appCtx,
                id = notificationId,
                title = title,
                categoryNameAr = category.channelNameAr,
                status = "SECTION_FILTERED",
                details = "سكشن $activeSection"
            )
            return false
        }

        val cleanTitle = formatShortNotificationTitle(category, title, subjectName)
        val cleanSubj = cleanSubjectName(subjectName.ifBlank { cleanTitle })

        val now = System.currentTimeMillis()
        val eventSignature = "${category.name}_${notificationId}_${cleanTitle}"

        synchronized(dedupeLock) {
            val lastByIdMem = recentFiredById[notificationId] ?: 0L
            val lastByIdPref = prefs.getLong("dedupe_id_$notificationId", 0L)
            val lastById = maxOf(lastByIdMem, lastByIdPref)

            val lastBySigMem = recentFiredBySignature[eventSignature] ?: 0L
            val lastBySigPref = prefs.getLong("dedupe_sig_${eventSignature.hashCode()}", 0L)
            val lastBySig = maxOf(lastBySigMem, lastBySigPref)

            // Guard against future timestamps if clock was adjusted
            val elapsedId = if (now >= lastById) now - lastById else Long.MAX_VALUE
            val elapsedSig = if (now >= lastBySig) now - lastBySig else Long.MAX_VALUE

            if (!bypassDedupeForManualTest &&
                (elapsedId in 0..DEDUPE_WINDOW_MS || elapsedSig in 0..DEDUPE_WINDOW_MS)
            ) {
                val minElapsedSec = minOf(elapsedId, elapsedSig).coerceAtLeast(0L) / 1000L
                appendDiagnosticLog(
                    context = appCtx,
                    id = notificationId,
                    title = cleanTitle,
                    categoryNameAr = category.channelNameAr,
                    status = "TOLERANCE_GUARD",
                    details = "تم منع التكرار اللحظي (${minElapsedSec}s)"
                )
                return false
            }

            recentFiredById[notificationId] = now
            recentFiredBySignature[eventSignature] = now
            prefs.edit()
                .putLong("dedupe_id_$notificationId", now)
                .putLong("dedupe_sig_${eventSignature.hashCode()}", now)
                .apply()
        }

        val displayNotificationId = resolveDisplayNotificationId(category, cleanTitle, notificationId)
        createNotificationChannels(appCtx)

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("studyorganizer://section/${category.deepLinkTarget}"),
            appCtx,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("DEEP_LINK_TARGET", category.deepLinkTarget)
            putExtra("SUBJECT_NAME", cleanSubj)
            putExtra("STOP_ALERT_SOUND", true)
            putExtra("NOTIFICATION_ID", displayNotificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            appCtx,
            displayNotificationId,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopSoundIntent = Intent(appCtx, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_STOP_ALARM_SOUND
            putExtra("ALARM_ID", displayNotificationId)
        }
        val stopSoundPendingIntent = PendingIntent.getBroadcast(
            appCtx,
            displayNotificationId + 90000,
            stopSoundIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeEmblem = runCatching {
            android.graphics.BitmapFactory.decodeResource(appCtx.resources, R.drawable.img_app_logo_emblem)
        }.getOrNull()

        val activeChannelId = getActiveChannelId(appCtx, category)
        val cleanTimeBody = message.trim().let { rawMsg ->
            if (rawMsg.isNotBlank()) Time12hFormatter.replaceAll24hTimesWith12hAr(rawMsg) else ""
        }

        // Step 1 of Synchronization Bridge: Post the system notification FIRST (canceling any prior instance with same ID)
        if (showNotif) {
            val notification = NotificationCompat.Builder(appCtx, activeChannelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .apply {
                    if (largeEmblem != null) {
                        setLargeIcon(largeEmblem)
                    }
                    if (cleanTimeBody.isNotBlank()) {
                        setContentText(cleanTimeBody)
                    }
                }
                .setContentTitle(cleanTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .setSilent(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setDeleteIntent(stopSoundPendingIntent)
                .addAction(
                    android.R.drawable.ic_lock_silent_mode_off,
                    "إيقاف",
                    stopSoundPendingIntent
                )
                .build()

            val manager = appCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                runCatching { manager.cancel(notificationId) }
                runCatching { manager.cancel(displayNotificationId) }
                manager.notify(displayNotificationId, notification)
                lastActiveNotificationId = displayNotificationId
            } catch (_: SecurityException) {
            }
        }

        // Step 2 of Synchronization Bridge: Immediately trigger synchronized vibration, in-app alert banner, and WakeLock-protected audio alert!
        vibrateDevice(appCtx)
        if (playSound) {
            playAlertSound(appCtx, category, customToneUri, alertTitle = cleanTitle, keepNotificationAlive = true)
        } else {
            _activeAlertTitle.value = cleanTitle
        }

        appendDiagnosticLog(
            context = appCtx,
            id = notificationId,
            title = cleanTitle,
            categoryNameAr = category.channelNameAr,
            status = if (showNotif && playSound) "SYNCED_NOTIF_AND_ALERT" else if (showNotif) "NOTIF_ONLY" else "ALERT_ONLY",
            details = "ID: $displayNotificationId"
        )
        return true
    }

    private fun acquireAlertWakeLock(context: Context) {
        runCatching {
            releaseAlertWakeLock()
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            alertWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ManaratAlTalib:SynchronizedAlertWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(3 * 60 * 1000L) // Hold up to 3 minutes max for full Adhan/Nasheed playback
            }
        }
    }

    private fun releaseAlertWakeLock() {
        runCatching {
            alertWakeLock?.let {
                if (it.isHeld) it.release()
            }
        }
        alertWakeLock = null
    }

    private fun stopActiveMediaPlayerOnly(appCtx: Context?) {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {
        } finally {
            activeMediaPlayer = null
        }
        unregisterDismissListeners(appCtx)
        releaseAlertWakeLock()
    }

    fun playAlertSound(
        context: Context,
        category: AlarmCategory,
        customToneUri: String = "",
        alertTitle: String? = null,
        keepNotificationAlive: Boolean = false,
        explicitTarget: ToneTarget? = null
    ) {
        val appCtx = context.applicationContext
        appContextRef = appCtx
        if (keepNotificationAlive) {
            stopActiveMediaPlayerOnly(appCtx)
        } else {
            stopAlertSound(appCtx)
        }
        val resolvedTitle = alertTitle ?: formatShortNotificationTitle(category, category.channelNameAr)
        _activeAlertTitle.value = resolvedTitle
        acquireAlertWakeLock(appCtx)

        try {
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val effectiveAssignment = resolveEffectiveToneAssignment(
                context = appCtx,
                category = category,
                titleOrSubject = resolvedTitle,
                explicitTarget = explicitTarget
            )

            val mp = MediaPlayer().apply {
                setAudioAttributes(audioAttrs)
                when {
                    // Priority 1: Task-specific custom tone URI (takes strict priority)
                    customToneUri.isNotBlank() -> {
                        val trimmedUri = customToneUri.trim()
                        val localFile = when {
                            trimmedUri.startsWith("file://") -> Uri.parse(trimmedUri).path?.let { File(it) }
                            trimmedUri.startsWith("/") -> File(trimmedUri)
                            else -> null
                        }
                        if (localFile != null && localFile.exists() && localFile.canRead()) {
                            setDataSource(localFile.absolutePath)
                        } else {
                            setDataSource(appCtx, Uri.parse(trimmedUri))
                        }
                    }
                    // Priority 2: Target-specific or Category-level custom uploaded MP3 file
                    effectiveAssignment.storageSpec.startsWith("FILE:") -> {
                        val customFile = File(effectiveAssignment.storageSpec.removePrefix("FILE:"))
                        if (customFile.exists() && customFile.canRead()) {
                            setDataSource(customFile.absolutePath)
                        } else {
                            val afd = appCtx.resources.openRawResourceFd(category.defaultRawRes)
                            if (afd != null) {
                                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                afd.close()
                            }
                        }
                    }
                    // Priority 3: Target-specific swapped built-in preset
                    effectiveAssignment.storageSpec.startsWith("RAW:") -> {
                        val preset = BuiltInTonePreset.fromId(effectiveAssignment.storageSpec.removePrefix("RAW:"))
                        val rawRes = preset?.rawRes ?: category.defaultRawRes
                        val afd = appCtx.resources.openRawResourceFd(rawRes)
                        if (afd != null) {
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                        }
                    }
                    // Priority 4: Default tone for category
                    else -> {
                        val afd = appCtx.resources.openRawResourceFd(category.defaultRawRes)
                        if (afd != null) {
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                        }
                    }
                }
                prepare()
            }

            activeMediaPlayer = mp
            soundStartTimestamp = System.currentTimeMillis()
            registerDismissListeners(appCtx)

            mp.setOnCompletionListener { player ->
                runCatching { player.release() }
                if (activeMediaPlayer == player) {
                    activeMediaPlayer = null
                    _activeAlertTitle.value = null
                }
                unregisterDismissListeners(appCtx)
                releaseAlertWakeLock()
            }
            mp.start()
        } catch (_: Exception) {
            try {
                // Safe fallback to the bundled default resource so it never crashes
                val fallback = MediaPlayer.create(appCtx, category.defaultRawRes)
                activeMediaPlayer = fallback
                soundStartTimestamp = System.currentTimeMillis()
                registerDismissListeners(appCtx)
                fallback?.setOnCompletionListener { player ->
                    runCatching { player.release() }
                    if (activeMediaPlayer == player) {
                        activeMediaPlayer = null
                        _activeAlertTitle.value = null
                    }
                    unregisterDismissListeners(appCtx)
                    releaseAlertWakeLock()
                }
                fallback?.start()
            } catch (_: Exception) {
                _activeAlertTitle.value = null
                releaseAlertWakeLock()
            }
        }
    }

    private fun registerDismissListeners(appCtx: Context) {
        if (!isReceiverRegistered) {
            runCatching {
                val filter = IntentFilter().apply {
                    addAction("android.media.VOLUME_CHANGED_ACTION")
                    addAction("android.media.MASTER_MUTE_CHANGED_ACTION")
                    addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
                }
                ContextCompat.registerReceiver(
                    appCtx,
                    alertDismissReceiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
                isReceiverRegistered = true
            }
        }

        if (!isVolumeObserverRegistered) {
            runCatching {
                val am = appCtx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                initialAlarmVolume = am?.getStreamVolume(AudioManager.STREAM_ALARM) ?: -1
                initialMusicVolume = am?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: -1
                initialRingVolume = am?.getStreamVolume(AudioManager.STREAM_RING) ?: -1
                appCtx.contentResolver.registerContentObserver(
                    Settings.System.CONTENT_URI,
                    true,
                    volumeContentObserver
                )
                isVolumeObserverRegistered = true
            }
        }
    }

    private fun unregisterDismissListeners(appCtx: Context?) {
        val ctx = appCtx ?: appContextRef ?: return
        if (isReceiverRegistered) {
            runCatching { ctx.unregisterReceiver(alertDismissReceiver) }
            isReceiverRegistered = false
        }
        if (isVolumeObserverRegistered) {
            runCatching { ctx.contentResolver.unregisterContentObserver(volumeContentObserver) }
            isVolumeObserverRegistered = false
        }
    }

    fun stopAlertSound(context: Context? = null, notificationIdToCancel: Int = -1) {
        val ctx = context?.applicationContext ?: appContextRef
        _activeAlertTitle.value = null
        stopActiveMediaPlayerOnly(ctx)

        if (ctx != null) {
            runCatching {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vm.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                vibrator.cancel()
            }

            val idToClear = if (notificationIdToCancel != -1) notificationIdToCancel else lastActiveNotificationId
            if (idToClear != -1) {
                runCatching {
                    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(idToClear)
                }
                if (idToClear == lastActiveNotificationId) {
                    lastActiveNotificationId = -1
                }
            }
        }
    }

    fun vibrateDevice(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(600)
            }
        } catch (_: Exception) {
        }
    }

    private fun computeNextDailyTriggerMillis(time24: String): Long {
        val parts = time24.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return -1L
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return -1L

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    private fun computeNextWeeklyTriggerMillis(dayOfWeekAr: String, time24: String): Long {
        val parts = time24.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return -1L
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return -1L
        val targetCalendarDay = mapArabicDayToCalendarDay(dayOfWeekAr) ?: return computeNextDailyTriggerMillis(time24)

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var daysAdded = 0
        while (calendar.get(Calendar.DAY_OF_WEEK) != targetCalendarDay || calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            daysAdded++
            if (daysAdded > 8) break
        }
        return calendar.timeInMillis
    }

    private fun mapArabicDayToCalendarDay(dayAr: String): Int? {
        val normalized = dayAr.trim()
        return when {
            normalized.contains("السبت") -> Calendar.SATURDAY
            normalized.contains("الأحد") || normalized.contains("الاحد") -> Calendar.SUNDAY
            normalized.contains("الاثنين") || normalized.contains("الإثنين") -> Calendar.MONDAY
            normalized.contains("الثلاثاء") -> Calendar.TUESDAY
            normalized.contains("الأربعاء") || normalized.contains("الاربعاء") -> Calendar.WEDNESDAY
            normalized.contains("الخميس") -> Calendar.THURSDAY
            normalized.contains("الجمعة") || normalized.contains("الجمعه") -> Calendar.FRIDAY
            else -> null
        }
    }

    private fun savePendingAlarmsMetadata(
        prefs: android.content.SharedPreferences,
        summaries: List<ScheduledAlarmSummary>
    ) {
        val idsArr = JSONArray()
        val detailsArr = JSONArray()
        summaries.sortedBy { it.triggerAtMillis }.forEach { item ->
            idsArr.put(item.alarmId)
            detailsArr.put(
                JSONObject().apply {
                    put("alarmId", item.alarmId)
                    put("categoryNameAr", item.categoryNameAr)
                    put("title", item.title)
                    put("scheduledTimeFormatted", item.scheduledTimeFormatted)
                    put("triggerAtMillis", item.triggerAtMillis)
                }
            )
        }
        prefs.edit()
            .putString(KEY_PENDING_IDS, idsArr.toString())
            .putString(KEY_PENDING_DETAILS, detailsArr.toString())
            .apply()
    }

    fun getPendingScheduledAlarms(context: Context): List<ScheduledAlarmSummary> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_PENDING_DETAILS, null) ?: return emptyList()
        val list = mutableListOf<ScheduledAlarmSummary>()
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    ScheduledAlarmSummary(
                        alarmId = o.optInt("alarmId"),
                        categoryNameAr = o.optString("categoryNameAr"),
                        title = o.optString("title"),
                        scheduledTimeFormatted = Time12hFormatter.replaceAll24hTimesWith12hAr(o.optString("scheduledTimeFormatted")),
                        triggerAtMillis = o.optLong("triggerAtMillis")
                    )
                )
            }
        }
        return list.sortedBy { it.triggerAtMillis }
    }

    fun appendDiagnosticLog(
        context: Context,
        id: Int,
        title: String,
        categoryNameAr: String,
        status: String,
        details: String
    ) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val formatted = Time12hFormatter.formatMillisTo12Ar(now, includeDate = true)
        val existingRaw = prefs.getString(KEY_DIAGNOSTIC_LOG, null)
        val newArr = JSONArray()

        newArr.put(
            JSONObject().apply {
                put("id", id)
                put("title", title)
                put("categoryNameAr", categoryNameAr)
                put("timestampMillis", now)
                put("timeFormatted", formatted)
                put("status", status)
                put("details", details)
            }
        )

        if (!existingRaw.isNullOrBlank()) {
            runCatching {
                val oldArr = JSONArray(existingRaw)
                val limit = minOf(oldArr.length(), 49)
                for (i in 0 until limit) {
                    newArr.put(oldArr.getJSONObject(i))
                }
            }
        }

        prefs.edit().putString(KEY_DIAGNOSTIC_LOG, newArr.toString()).apply()
    }

    fun getDiagnosticLogs(context: Context): List<NotificationLogEntry> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_DIAGNOSTIC_LOG, null) ?: return emptyList()
        val list = mutableListOf<NotificationLogEntry>()
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    NotificationLogEntry(
                        id = o.optInt("id"),
                        title = o.optString("title"),
                        categoryNameAr = o.optString("categoryNameAr"),
                        timestampMillis = o.optLong("timestampMillis"),
                        timeFormatted = Time12hFormatter.replaceAll24hTimesWith12hAr(o.optString("timeFormatted")),
                        status = o.optString("status"),
                        details = Time12hFormatter.replaceAll24hTimesWith12hAr(o.optString("details"))
                    )
                )
            }
        }
        return list
    }

    fun clearDiagnosticLogs(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_DIAGNOSTIC_LOG).apply()
    }

    private fun formatDateTime(millis: Long): String {
        return Time12hFormatter.formatMillisTo12Ar(millis, includeDate = true)
    }
}
