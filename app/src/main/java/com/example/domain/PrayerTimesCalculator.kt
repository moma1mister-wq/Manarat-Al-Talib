package com.example.domain

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class PrayerCalcMethod(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesOffset: Int = 0
) {
    EGYPTIAN(
        "EGYPTIAN",
        "الهيئة المصرية العامة للمساحة (افتراضي)",
        "Egyptian General Authority of Survey",
        19.5,
        17.5
    ),
    UMM_AL_QURA(
        "UMM_AL_QURA",
        "تقويم أم القرى - مكة المكرمة",
        "Umm Al-Qura University, Makkah",
        18.5,
        0.0,
        90
    ),
    MWL(
        "MWL",
        "رابطة العالم الإسلامي",
        "Muslim World League",
        18.0,
        17.0
    ),
    KARACHI(
        "KARACHI",
        "جامعة العلوم الإسلامية بكراتشي",
        "University of Islamic Sciences, Karachi",
        18.0,
        18.0
    ),
    ISNA(
        "ISNA",
        "الجمعية الإسلامية لأمريكا الشمالية (ISNA)",
        "Islamic Society of North America",
        15.0,
        15.0
    );

    companion object {
        fun fromId(id: String): PrayerCalcMethod =
            entries.find { it.id == id } ?: EGYPTIAN
    }
}

data class PrayerTimeEntry(
    val key: String,
    val arabicName: String,
    val englishName: String,
    val time24: String,     // "04:52"
    val timeFormatted: String, // "04:52 ص"
    val minutesFromMidnight: Int,
    val isObligatoryPrayer: Boolean = true
)

data class DailyPrayerSchedule(
    val fajr: PrayerTimeEntry,
    val sunrise: PrayerTimeEntry,
    val dhuhr: PrayerTimeEntry,
    val asr: PrayerTimeEntry,
    val maghrib: PrayerTimeEntry,
    val isha: PrayerTimeEntry,
    val nextPrayer: PrayerTimeEntry,
    val previousPrayer: PrayerTimeEntry,
    val remainingMinutesToNext: Int,
    val remainingSecondsToNext: Int,
    val intervalTotalSeconds: Int,
    val latitude: Double,
    val longitude: Double,
    val method: PrayerCalcMethod
) {
    val fivePrayers: List<PrayerTimeEntry>
        get() = listOf(fajr, dhuhr, asr, maghrib, isha)

    /** Decreasing fraction from 1.0f right after previous prayer down to 0.0f when next prayer arrives. */
    val remainingFraction: Float
        get() = if (intervalTotalSeconds > 0) {
            (remainingSecondsToNext.toFloat() / intervalTotalSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val countdownHms: String
        get() {
            val total = remainingSecondsToNext.coerceAtLeast(0)
            val hrs = total / 3600
            val mins = (total % 3600) / 60
            val secs = total % 60
            return "%02d:%02d:%02d".format(java.util.Locale.US, hrs, mins, secs)
        }

    val countdownArabicDetailed: String
        get() {
            val total = remainingSecondsToNext.coerceAtLeast(0)
            val hrs = total / 3600
            val mins = (total % 3600) / 60
            val secs = total % 60
            return when {
                hrs > 0 -> "$hrs س و $mins د و $secs ث"
                mins > 0 -> "$mins د و $secs ث"
                else -> "$secs ث"
            }
        }
}

/**
 * Pure Offline Astronomical Solar Prayer Times Calculator (Adhan Algorithm)
 */
object PrayerTimesCalculator {

    private fun degToRad(d: Double): Double = (d * PI) / 180.0
    private fun radToDeg(r: Double): Double = (r * 180.0) / PI

    private fun fixAngle(a: Double): Double {
        var res = a - 360.0 * floor(a / 360.0)
        if (res < 0) res += 360.0
        return res
    }

    private fun fixHour(h: Double): Double {
        var res = h - 24.0 * floor(h / 24.0)
        if (res < 0) res += 24.0
        return res
    }

    fun calculateTodayPrayers(
        latitude: Double = 30.0595, // Default: Al-Azhar University, Nasr City, Cairo
        longitude: Double = 31.3137,
        method: PrayerCalcMethod = PrayerCalcMethod.EGYPTIAN,
        manualOffsets: Map<String, Int> = emptyMap(),
        calendar: Calendar = Calendar.getInstance()
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val tz = TimeZone.getDefault()
        val timeZoneHours = tz.getOffset(calendar.timeInMillis) / (1000.0 * 3600.0)

        // Julian Date calculation
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        // Sun declination and equation of time
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))) / 15.0
        val eqTime = q / 15.0 - fixHour(ra)
        val decl = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))

        val dhuhrBase = 12.0 + timeZoneHours - (longitude / 15.0) - eqTime

        fun sunAngleTime(angle: Double, ccw: Boolean): Double {
            val num = -sin(degToRad(angle)) - sin(degToRad(decl)) * sin(degToRad(latitude))
            val den = cos(degToRad(decl)) * cos(degToRad(latitude))
            val ratio = (num / den).coerceIn(-0.9999, 0.9999)
            val t = radToDeg(acos(ratio)) / 15.0
            return dhuhrBase + if (ccw) -t else t
        }

        // Asr Shafi'i / Standard (Shadow factor = 1)
        val asrAngle = -radToDeg(atan(1.0 / (1.0 + tan(degToRad(kotlin.math.abs(latitude - decl))))))
        val fajrHours = sunAngleTime(method.fajrAngle, ccw = true)
        val sunriseHours = sunAngleTime(0.833, ccw = true)
        val dhuhrHours = dhuhrBase + (2.0 / 60.0) // 2 min precaution after zenith
        val asrHours = sunAngleTime(asrAngle, ccw = false)
        val maghribHours = sunAngleTime(0.833, ccw = false)
        val ishaHours = if (method.ishaMinutesOffset > 0) {
            maghribHours + (method.ishaMinutesOffset / 60.0)
        } else {
            sunAngleTime(method.ishaAngle, ccw = false)
        }

        fun buildEntry(
            key: String,
            ar: String,
            en: String,
            rawHours: Double,
            obligatory: Boolean = true
        ): PrayerTimeEntry {
            val offsetMin = manualOffsets[key] ?: 0
            val totalMins = ((fixHour(rawHours) * 60.0).toInt() + offsetMin).mod(1440)
            val hh = totalMins / 60
            val mm = totalMins % 60
            val time24 = "%02d:%02d".format(java.util.Locale.US, hh, mm)
            val amPmAr = if (hh < 12) "ص" else "م"
            val hh12 = when {
                hh == 0 -> 12
                hh > 12 -> hh - 12
                else -> hh
            }
            val formatted = "%d:%02d %s".format(java.util.Locale.US, hh12, mm, amPmAr)
            return PrayerTimeEntry(
                key = key,
                arabicName = ar,
                englishName = en,
                time24 = time24,
                timeFormatted = formatted,
                minutesFromMidnight = totalMins,
                isObligatoryPrayer = obligatory
            )
        }

        val fajr = buildEntry("FAJR", "الفجر", "Fajr", fajrHours)
        val sunrise = buildEntry("SUNRISE", "الشروق", "Sunrise", sunriseHours, obligatory = false)
        val dhuhr = buildEntry("DHUHR", "الظهر", "Dhuhr", dhuhrHours)
        val asr = buildEntry("ASR", "العصر", "Asr", asrHours)
        val maghrib = buildEntry("MAGHRIB", "المغرب", "Maghrib", maghribHours)
        val isha = buildEntry("ISHA", "العشاء", "Isha", ishaHours)

        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val nowSeconds = nowMinutes * 60 + calendar.get(Calendar.SECOND)
        val five = listOf(fajr, dhuhr, asr, maghrib, isha)
        val upcoming = five.firstOrNull { (it.minutesFromMidnight * 60) > nowSeconds } ?: fajr
        val upcomingIdx = five.indexOfFirst { it.key == upcoming.key }.coerceAtLeast(0)
        val prevPrayer = five[(upcomingIdx - 1 + five.size) % five.size]

        val upcomingSecs = upcoming.minutesFromMidnight * 60
        val prevSecs = prevPrayer.minutesFromMidnight * 60
        val remainingSecs = if (upcomingSecs > nowSeconds) {
            upcomingSecs - nowSeconds
        } else {
            (86400 - nowSeconds) + upcomingSecs
        }
        val intervalSecs = if (upcomingSecs > prevSecs) {
            upcomingSecs - prevSecs
        } else {
            (86400 - prevSecs) + upcomingSecs
        }.coerceAtLeast(1)

        val remainingMins = (remainingSecs / 60).coerceAtLeast(0)

        return DailyPrayerSchedule(
            fajr = fajr,
            sunrise = sunrise,
            dhuhr = dhuhr,
            asr = asr,
            maghrib = maghrib,
            isha = isha,
            nextPrayer = upcoming,
            previousPrayer = prevPrayer,
            remainingMinutesToNext = remainingMins,
            remainingSecondsToNext = remainingSecs,
            intervalTotalSeconds = intervalSecs,
            latitude = latitude,
            longitude = longitude,
            method = method
        )
    }
}
