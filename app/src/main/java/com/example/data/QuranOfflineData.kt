package com.example.data

import android.content.Context
import org.json.JSONArray

data class SurahMetadata(
    val number: Int,
    val nameAr: String,
    val nameEn: String,
    val ayahCount: Int,
    val isMakki: Boolean,
    val startPage: Int,
    val endPage: Int,
    val juzStart: Int
)

data class MushafPageContent(
    val pageNumber: Int, // 1..604
    val juzNumber: Int,  // 1..30
    val hizbNumber: Int, // 1..60
    val surahsOnPage: List<SurahMetadata>,
    val verses: List<MushafVerse>
)

data class MushafVerse(
    val surahNumber: Int,
    val surahNameAr: String,
    val ayahNumber: Int,
    val pageNumber: Int = 1,
    val juzNumber: Int = 1,
    val hizbNumber: Int = 1,
    val textUthmani: String
)

data class WritingEvaluationResult(
    val accuracyPercent: Int,
    val matchedWordsCount: Int,
    val totalTargetWords: Int,
    val wordStatuses: List<Pair<String, Boolean>>, // word to isCorrect
    val nextHintWord: String?,
    val isFullyCompleted: Boolean
)

data class QuranJuzInfo(
    val juzNumber: Int,
    val startPage: Int,
    val firstSurahNumber: Int,
    val firstSurahNameAr: String,
    val firstAyahNumber: Int,
    val openingText: String
)

data class QuranRubInfo(
    val rubNumber: Int,      // 1..240
    val hizbNumber: Int,     // 1..60
    val juzNumber: Int,      // 1..30
    val quarterInHizb: Int,  // 1..4 (1=بداية الحزب, 2=ربع, 3=نصف, 4=ثلاثة أرباع)
    val surahNumber: Int,
    val surahNameAr: String,
    val ayahNumber: Int,
    val pageNumber: Int,
    val openingText: String
) {
    val quarterLabelAr: String
        get() = when (quarterInHizb) {
            1 -> "بداية الحزب"
            2 -> "ربع الحزب"
            3 -> "نصف الحزب"
            else -> "ثلاثة أرباع الحزب"
        }
}

data class QuranPageSummary(
    val pageNumber: Int,
    val juzNumber: Int,
    val hizbNumber: Int,
    val surahsSummary: String
)

object QuranOfflineData {

    // Complete 114 Surahs with exact Madani Mushaf start pages (1..604) and verse counts (total = 6236)
    val allSurahs: List<SurahMetadata> = listOf(
        SurahMetadata(1, "الفاتحة", "Al-Fatihah", 7, true, 1, 1, 1),
        SurahMetadata(2, "البقرة", "Al-Baqarah", 286, false, 2, 49, 1),
        SurahMetadata(3, "آل عمران", "Ali 'Imran", 200, false, 50, 76, 3),
        SurahMetadata(4, "النساء", "An-Nisa", 176, false, 77, 106, 4),
        SurahMetadata(5, "المائدة", "Al-Ma'idah", 120, false, 106, 127, 6),
        SurahMetadata(6, "الأنعام", "Al-An'am", 165, true, 128, 150, 7),
        SurahMetadata(7, "الأعراف", "Al-A'raf", 206, true, 151, 176, 8),
        SurahMetadata(8, "الأنفال", "Al-Anfal", 75, false, 177, 186, 9),
        SurahMetadata(9, "التوبة", "At-Tawbah", 129, false, 187, 207, 10),
        SurahMetadata(10, "يونس", "Yunus", 109, true, 208, 221, 11),
        SurahMetadata(11, "هود", "Hud", 123, true, 221, 235, 11),
        SurahMetadata(12, "يوسف", "Yusuf", 111, true, 235, 248, 12),
        SurahMetadata(13, "الرعد", "Ar-Ra'd", 43, false, 249, 255, 13),
        SurahMetadata(14, "إبراهيم", "Ibrahim", 52, true, 255, 261, 13),
        SurahMetadata(15, "الحجر", "Al-Hijr", 99, true, 262, 267, 14),
        SurahMetadata(16, "النحل", "An-Nahl", 128, true, 267, 281, 14),
        SurahMetadata(17, "الإسراء", "Al-Isra", 111, true, 282, 293, 15),
        SurahMetadata(18, "الكهف", "Al-Kahf", 110, true, 293, 304, 15),
        SurahMetadata(19, "مريم", "Maryam", 98, true, 305, 312, 16),
        SurahMetadata(20, "طه", "Ta-Ha", 135, true, 312, 321, 16),
        SurahMetadata(21, "الأنبياء", "Al-Anbya", 112, true, 322, 331, 17),
        SurahMetadata(22, "الحج", "Al-Hajj", 78, false, 332, 341, 17),
        SurahMetadata(23, "المؤمنون", "Al-Mu'minun", 118, true, 342, 349, 18),
        SurahMetadata(24, "النور", "An-Nur", 64, false, 350, 359, 18),
        SurahMetadata(25, "الفرقان", "Al-Furqan", 77, true, 359, 366, 18),
        SurahMetadata(26, "الشعراء", "Ash-Shu'ara", 227, true, 367, 376, 19),
        SurahMetadata(27, "النمل", "Al-Naml", 93, true, 377, 385, 19),
        SurahMetadata(28, "القصص", "Al-Qasas", 88, true, 385, 396, 20),
        SurahMetadata(29, "العنكبوت", "Al-'Ankabut", 69, true, 396, 404, 20),
        SurahMetadata(30, "الروم", "Ar-Rum", 60, true, 404, 410, 21),
        SurahMetadata(31, "لقمان", "Luqman", 34, true, 411, 414, 21),
        SurahMetadata(32, "السجدة", "As-Sajdah", 30, true, 415, 417, 21),
        SurahMetadata(33, "الأحزاب", "Al-Ahzab", 73, false, 418, 427, 21),
        SurahMetadata(34, "سبأ", "Saba", 54, true, 428, 434, 22),
        SurahMetadata(35, "فاطر", "Fatir", 45, true, 434, 440, 22),
        SurahMetadata(36, "يس", "Ya-Sin", 83, true, 440, 445, 22),
        SurahMetadata(37, "الصافات", "As-Saffat", 182, true, 446, 452, 23),
        SurahMetadata(38, "ص", "Sad", 88, true, 453, 458, 23),
        SurahMetadata(39, "الزمر", "Az-Zumar", 75, true, 458, 467, 23),
        SurahMetadata(40, "غافر", "Ghafir", 85, true, 467, 476, 24),
        SurahMetadata(41, "فصلت", "Fussilat", 54, true, 477, 482, 24),
        SurahMetadata(42, "الشورى", "Ash-Shuraa", 53, true, 483, 489, 25),
        SurahMetadata(43, "الزخرف", "Az-Zukhruf", 89, true, 489, 495, 25),
        SurahMetadata(44, "الدخان", "Ad-Dukhan", 59, true, 496, 498, 25),
        SurahMetadata(45, "الجاثية", "Al-Jathiyah", 37, true, 499, 502, 25),
        SurahMetadata(46, "الأحقاف", "Al-Ahqaf", 35, true, 502, 506, 26),
        SurahMetadata(47, "محمد", "Muhammad", 38, false, 507, 510, 26),
        SurahMetadata(48, "الفتح", "Al-Fath", 29, false, 511, 515, 26),
        SurahMetadata(49, "الحجرات", "Al-Hujurat", 18, false, 515, 517, 26),
        SurahMetadata(50, "ق", "Qaf", 45, true, 518, 520, 26),
        SurahMetadata(51, "الذاريات", "Adh-Dhariyat", 60, true, 520, 523, 26),
        SurahMetadata(52, "الطور", "At-Tur", 49, true, 523, 525, 27),
        SurahMetadata(53, "النجم", "An-Najm", 62, true, 526, 528, 27),
        SurahMetadata(54, "القمر", "Al-Qamar", 55, true, 528, 531, 27),
        SurahMetadata(55, "الرحمن", "Ar-Rahman", 78, false, 531, 534, 27),
        SurahMetadata(56, "الواقعة", "Al-Waqi'ah", 96, true, 534, 537, 27),
        SurahMetadata(57, "الحديد", "Al-Hadid", 29, false, 537, 541, 27),
        SurahMetadata(58, "المجادلة", "Al-Mujadila", 22, false, 542, 545, 28),
        SurahMetadata(59, "الحشر", "Al-Hashr", 24, false, 545, 548, 28),
        SurahMetadata(60, "الممتحنة", "Al-Mumtahanah", 13, false, 549, 551, 28),
        SurahMetadata(61, "الصف", "As-Saf", 14, false, 551, 552, 28),
        SurahMetadata(62, "الجمعة", "Al-Jumu'ah", 11, false, 553, 554, 28),
        SurahMetadata(63, "المنافقون", "Al-Munafiqun", 11, false, 554, 555, 28),
        SurahMetadata(64, "التغابن", "At-Taghabun", 18, false, 556, 557, 28),
        SurahMetadata(65, "الطلاق", "At-Talaq", 12, false, 558, 559, 28),
        SurahMetadata(66, "التحريم", "At-Tahrim", 12, false, 560, 561, 28),
        SurahMetadata(67, "الملك", "Al-Mulk", 30, true, 562, 564, 29),
        SurahMetadata(68, "القلم", "Al-Qalam", 52, true, 564, 566, 29),
        SurahMetadata(69, "الحاقة", "Al-Haqqah", 52, true, 566, 568, 29),
        SurahMetadata(70, "المعارج", "Al-Ma'arij", 44, true, 568, 570, 29),
        SurahMetadata(71, "نوح", "Nuh", 28, true, 570, 571, 29),
        SurahMetadata(72, "الجن", "Al-Jinn", 28, true, 572, 573, 29),
        SurahMetadata(73, "المزمل", "Al-Muzzammil", 20, true, 574, 575, 29),
        SurahMetadata(74, "المدثر", "Al-Muddaththir", 56, true, 575, 577, 29),
        SurahMetadata(75, "القيامة", "Al-Qiyamah", 40, true, 577, 578, 29),
        SurahMetadata(76, "الإنسان", "Al-Insan", 31, false, 578, 580, 29),
        SurahMetadata(77, "المرسلات", "Al-Mursalat", 50, true, 580, 581, 29),
        SurahMetadata(78, "النبأ", "An-Naba", 40, true, 582, 583, 30),
        SurahMetadata(79, "النازعات", "An-Nazi'at", 46, true, 583, 584, 30),
        SurahMetadata(80, "عبس", "'Abasa", 42, true, 585, 585, 30),
        SurahMetadata(81, "التكوير", "At-Takwir", 29, true, 586, 586, 30),
        SurahMetadata(82, "الانفطار", "Al-Infitar", 19, true, 587, 587, 30),
        SurahMetadata(83, "المطففين", "Al-Mutaffifin", 36, true, 587, 589, 30),
        SurahMetadata(84, "الانشقاق", "Al-Inshiqaq", 25, true, 589, 589, 30),
        SurahMetadata(85, "البروج", "Al-Buruj", 22, true, 590, 590, 30),
        SurahMetadata(86, "الطارق", "At-Tariq", 17, true, 591, 591, 30),
        SurahMetadata(87, "الأعلى", "Al-A'la", 19, true, 591, 592, 30),
        SurahMetadata(88, "الغاشية", "Al-Ghashiyah", 26, true, 592, 592, 30),
        SurahMetadata(89, "الفجر", "Al-Fajr", 30, true, 593, 594, 30),
        SurahMetadata(90, "البلد", "Al-Balad", 20, true, 594, 594, 30),
        SurahMetadata(91, "الشمس", "Ash-Shams", 15, true, 595, 595, 30),
        SurahMetadata(92, "الليل", "Al-Layl", 21, true, 595, 596, 30),
        SurahMetadata(93, "الضحى", "Ad-Duhaa", 11, true, 596, 596, 30),
        SurahMetadata(94, "الشرح", "Ash-Sharh", 8, true, 596, 596, 30),
        SurahMetadata(95, "التين", "At-Tin", 8, true, 597, 597, 30),
        SurahMetadata(96, "العلق", "Al-'Alaq", 19, true, 597, 597, 30),
        SurahMetadata(97, "القدر", "Al-Qadr", 5, true, 598, 598, 30),
        SurahMetadata(98, "البينة", "Al-Bayyinah", 8, false, 598, 599, 30),
        SurahMetadata(99, "الزلزلة", "Az-Zalzalah", 8, false, 599, 599, 30),
        SurahMetadata(100, "العاديات", "Al-'Adiyat", 11, true, 599, 600, 30),
        SurahMetadata(101, "القارعة", "Al-Qari'ah", 11, true, 600, 600, 30),
        SurahMetadata(102, "التكاثر", "At-Takathur", 8, true, 600, 600, 30),
        SurahMetadata(103, "العصر", "Al-'Asr", 3, true, 601, 601, 30),
        SurahMetadata(104, "الهمزة", "Al-Humazah", 9, true, 601, 601, 30),
        SurahMetadata(105, "الفيل", "Al-Fil", 5, true, 601, 601, 30),
        SurahMetadata(106, "قريش", "Quraysh", 4, true, 602, 602, 30),
        SurahMetadata(107, "الماعون", "Al-Ma'un", 7, true, 602, 602, 30),
        SurahMetadata(108, "الكوثر", "Al-Kawthar", 3, true, 602, 602, 30),
        SurahMetadata(109, "الكافرون", "Al-Kafirun", 6, true, 603, 603, 30),
        SurahMetadata(110, "النصر", "An-Nasr", 3, false, 603, 603, 30),
        SurahMetadata(111, "المسد", "Al-Masad", 5, true, 603, 603, 30),
        SurahMetadata(112, "الإخلاص", "Al-Ikhlas", 4, true, 604, 604, 30),
        SurahMetadata(113, "الفلق", "Al-Falaq", 5, true, 604, 604, 30),
        SurahMetadata(114, "الناس", "An-Nas", 6, true, 604, 604, 30)
    )

    private val surahByNumber: Map<Int, SurahMetadata> = allSurahs.associateBy { it.number }

    @Volatile
    private var pagesMap: Map<Int, List<MushafVerse>> = emptyMap()

    @Volatile
    private var allVersesList: List<MushafVerse> = emptyList()

    @Volatile
    private var juzListCache: List<QuranJuzInfo> = emptyList()

    @Volatile
    private var rubListCache: List<QuranRubInfo> = emptyList()

    @Volatile
    private var pageSummariesCache: List<QuranPageSummary> = emptyList()

    private val rubHizbOverrides: Map<Int, List<Pair<Int, Int>>> = mapOf(
        8 to listOf(3 to 171, 3 to 186, 4 to 1, 4 to 12),
        11 to listOf(4 to 148, 4 to 163, 5 to 1, 5 to 12),
        18 to listOf(7 to 171, 7 to 189, 8 to 1, 8 to 22),
        19 to listOf(8 to 41, 8 to 61, 9 to 1, 9 to 19),
        27 to listOf(15 to 1, 15 to 49, 16 to 1, 16 to 30),
        35 to listOf(23 to 1, 23 to 36, 23 to 75, 24 to 1),
        36 to listOf(24 to 21, 24 to 35, 24 to 53, 25 to 1),
        37 to listOf(25 to 21, 25 to 53, 26 to 1, 26 to 52),
        38 to listOf(26 to 111, 26 to 181, 27 to 1, 27 to 27),
        40 to listOf(28 to 51, 28 to 76, 29 to 1, 29 to 26),
        41 to listOf(29 to 46, 30 to 1, 30 to 31, 30 to 54),
        42 to listOf(31 to 22, 32 to 11, 33 to 1, 33 to 18),
        47 to listOf(39 to 32, 39 to 53, 40 to 1, 40 to 21),
        52 to listOf(48 to 18, 49 to 1, 49 to 14, 50 to 27),
        54 to listOf(55 to 1, 56 to 1, 56 to 75, 57 to 16),
        56 to listOf(62 to 1, 63 to 4, 65 to 1, 66 to 1),
        57 to listOf(67 to 1, 68 to 1, 69 to 1, 70 to 19),
        58 to listOf(72 to 1, 73 to 20, 75 to 1, 76 to 19),
        59 to listOf(78 to 1, 80 to 1, 82 to 1, 84 to 1),
        60 to listOf(87 to 1, 90 to 1, 94 to 1, 100 to 9)
    )

    /**
     * Loads all 6,236 authentic Uthmani verses from assets/quran_full_6236.json into memory once.
     */
    fun ensureLoaded(context: Context) {
        if (allVersesList.isNotEmpty()) return
        synchronized(this) {
            if (allVersesList.isNotEmpty()) return
            try {
                val jsonText = context.assets.open("quran_full_6236.json")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
                val arr = JSONArray(jsonText)
                val loaded = ArrayList<MushafVerse>(arr.length())
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val sNum = o.getInt("s")
                    val aNum = o.getInt("a")
                    val page = o.getInt("p")
                    val juz = o.getInt("j")
                    val hizb = o.getInt("h")
                    var text = o.getString("t").removePrefix("\uFEFF")
                    // Strip leading Basmalah from verse 1 of Surahs other than Al-Fatihah (1) and At-Tawbah (9)
                    // because the Mushaf Page header already renders the ornate Basmalah banner!
                    if (aNum == 1 && sNum != 1 && sNum != 9) {
                        val basmalahPrefix = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ "
                        if (text.startsWith(basmalahPrefix)) {
                            text = text.removePrefix(basmalahPrefix).trim()
                        }
                    }
                    val sMeta = surahByNumber[sNum]
                    loaded.add(
                        MushafVerse(
                            surahNumber = sNum,
                            surahNameAr = sMeta?.nameAr ?: "",
                            ayahNumber = aNum,
                            pageNumber = page,
                            juzNumber = juz,
                            hizbNumber = hizb,
                            textUthmani = text
                        )
                    )
                }
                allVersesList = loaded
                pagesMap = loaded.groupBy { it.pageNumber }
                buildDivisionsCaches(loaded)
            } catch (_: Exception) {
            }
        }
    }

    private fun buildDivisionsCaches(verses: List<MushafVerse>) {
        // 1. Build exact 30 Juz info
        val juzMap = LinkedHashMap<Int, MushafVerse>(30)
        for (v in verses) {
            if (!juzMap.containsKey(v.juzNumber)) {
                juzMap[v.juzNumber] = v
            }
        }
        juzListCache = (1..30).mapNotNull { j ->
            val first = juzMap[j] ?: return@mapNotNull null
            QuranJuzInfo(
                juzNumber = j,
                startPage = first.pageNumber,
                firstSurahNumber = first.surahNumber,
                firstSurahNameAr = first.surahNameAr,
                firstAyahNumber = first.ayahNumber,
                openingText = first.textUthmani
            )
        }

        // 2. Build exact 240 Rub (Quarters) across all 60 Hizbs
        val byHizb = verses.groupBy { it.hizbNumber }
        val builtRubs = ArrayList<QuranRubInfo>(240)
        var rubCounter = 0
        for (h in 1..60) {
            val hVerses = byHizb[h] ?: continue
            val overrideCoords = rubHizbOverrides[h]
            val quarterStarts: List<MushafVerse> = if (overrideCoords != null) {
                overrideCoords.mapNotNull { (s, a) ->
                    hVerses.firstOrNull { it.surahNumber == s && it.ayahNumber == a }
                }
            } else {
                val list = ArrayList<MushafVerse>(4)
                list.add(hVerses.first())
                for (idx in 1 until hVerses.size) {
                    val v = hVerses[idx]
                    if (v.textUthmani.contains('\u06DE')) {
                        list.add(v)
                    }
                }
                list
            }
            quarterStarts.forEachIndexed { idx, v ->
                rubCounter++
                builtRubs.add(
                    QuranRubInfo(
                        rubNumber = rubCounter,
                        hizbNumber = h,
                        juzNumber = v.juzNumber,
                        quarterInHizb = (idx + 1).coerceIn(1, 4),
                        surahNumber = v.surahNumber,
                        surahNameAr = v.surahNameAr,
                        ayahNumber = v.ayahNumber,
                        pageNumber = v.pageNumber,
                        openingText = v.textUthmani
                    )
                )
            }
        }
        rubListCache = builtRubs

        // 3. Build 604 Page Summaries
        pageSummariesCache = (1..604).map { p ->
            val pVerses = pagesMap[p].orEmpty()
            val surahs = pVerses.map { it.surahNameAr }.distinct()
                .ifEmpty { allSurahs.filter { p in it.startPage..it.endPage }.map { it.nameAr } }
            QuranPageSummary(
                pageNumber = p,
                juzNumber = pVerses.firstOrNull()?.juzNumber ?: getJuzForPage(p),
                hizbNumber = pVerses.firstOrNull()?.hizbNumber ?: getHizbForPage(p),
                surahsSummary = surahs.joinToString(" • ")
            )
        }
    }

    fun getAllJuzInfo(): List<QuranJuzInfo> {
        if (juzListCache.isNotEmpty()) return juzListCache
        return (1..30).map { j ->
            val approxPage = if (j == 1) 1 else ((j - 1) * 20 + 2).coerceAtMost(604)
            val surah = allSurahs.firstOrNull { approxPage in it.startPage..it.endPage } ?: allSurahs.first()
            QuranJuzInfo(j, approxPage, surah.number, surah.nameAr, 1, "")
        }
    }

    fun getAllRubInfo(): List<QuranRubInfo> = rubListCache

    fun getAllPageSummaries(): List<QuranPageSummary> {
        if (pageSummariesCache.isNotEmpty()) return pageSummariesCache
        return (1..604).map { p ->
            val surahs = allSurahs.filter { p in it.startPage..it.endPage }.joinToString(" • ") { it.nameAr }
            QuranPageSummary(p, getJuzForPage(p), getHizbForPage(p), surahs)
        }
    }

    fun getAllVerses(): List<MushafVerse> = allVersesList

    fun getVersesForSurah(surahNumber: Int): List<MushafVerse> {
        return allVersesList.filter { it.surahNumber == surahNumber }
    }

    /**
     * Normalizes Arabic text (removes tashkeel/diacritics, Uthmani signs, and unifies Alef/Ya/Ta-Marbuta)
     * so users can search or practice writing Quran verses using a standard Arabic keyboard.
     */
    fun normalizeArabicForWritingAndSearch(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                // Convert Uthmani dagger alef (ٰ) into standard alef when needed, or skip diacritics:
                in '\u064B'..'\u065F', // Standard tashkeel
                in '\u0610'..'\u061A',
                in '\u06D6'..'\u06ED', // Quranic pause marks & rub el hizb
                '\u0640' -> {
                    // Skip diacritics/tatweel
                }
                '\u0670' -> sb.append('ا') // Superscript alef -> standard alef
                'أ', 'إ', 'آ', 'ٱ' -> sb.append('ا')
                'ى', 'ئ' -> sb.append('ي')
                'ؤ' -> sb.append('و')
                'ة' -> sb.append('ه')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun normalizeWordFlexible(word: String): Set<String> {
        // Both with and without dagger-alef expansion so words like "الرحمن" or "هذا" or "العالمين" match standard Arabic spelling!
        val withDagger = normalizeArabicForWritingAndSearch(word)
        val withoutDagger = normalizeArabicForWritingAndSearch(word.replace("\u0670", ""))
        return setOf(withDagger, withoutDagger)
    }

    /**
     * Evaluates the user's written text against the target Quran verse word by word.
     */
    fun evaluateWrittenVerse(targetUthmani: String, userWritten: String): WritingEvaluationResult {
        val targetWordsRaw = targetUthmani
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && normalizeArabicForWritingAndSearch(it).isNotBlank() }

        val userWordsRaw = userWritten
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        if (targetWordsRaw.isEmpty()) {
            return WritingEvaluationResult(100, 0, 0, emptyList(), null, true)
        }

        val statuses = mutableListOf<Pair<String, Boolean>>()
        var matched = 0

        for (i in userWordsRaw.indices) {
            val uWord = userWordsRaw[i]
            val uNorm = normalizeArabicForWritingAndSearch(uWord.replace("\u0670", ""))
            val targetRaw = targetWordsRaw.getOrNull(i)
            val isMatch = if (targetRaw != null) {
                val validForms = normalizeWordFlexible(targetRaw)
                validForms.any { it == uNorm }
            } else {
                false
            }
            if (isMatch) matched++
            statuses.add(uWord to isMatch)
        }

        val nextHint = if (matched < targetWordsRaw.size) {
            targetWordsRaw.getOrNull(userWordsRaw.size) ?: targetWordsRaw.getOrNull(matched)
        } else null

        val accuracy = ((matched.toFloat() / targetWordsRaw.size.toFloat()) * 100f).toInt().coerceIn(0, 100)
        val completed = matched == targetWordsRaw.size && userWordsRaw.size == targetWordsRaw.size

        return WritingEvaluationResult(
            accuracyPercent = accuracy,
            matchedWordsCount = matched,
            totalTargetWords = targetWordsRaw.size,
            wordStatuses = statuses,
            nextHintWord = nextHint,
            isFullyCompleted = completed
        )
    }

    /**
     * Searches across all 6,236 verses offline using normalized Arabic text (ignoring tashkeel/hamza variants).
     */
    fun searchVersesOffline(query: String, limit: Int = 60): List<MushafVerse> {
        val cleanQuery = normalizeArabicForWritingAndSearch(query.replace("\u0670", ""))
        if (cleanQuery.isBlank()) return emptyList()
        val queryTokens = cleanQuery.split(" ").filter { it.isNotBlank() }
        val results = mutableListOf<MushafVerse>()
        for (v in allVersesList) {
            val forms = normalizeWordFlexible(v.textUthmani)
            val surahNorm = normalizeArabicForWritingAndSearch(v.surahNameAr)
            val directMatch = forms.any { it.contains(cleanQuery) } || surahNorm.contains(cleanQuery)
            val multiWordMatch = queryTokens.size > 1 && forms.any { textForm ->
                queryTokens.all { token -> textForm.contains(token) }
            }
            if (directMatch || multiWordMatch) {
                results.add(v)
                if (results.size >= limit) break
            }
        }
        return results
    }

    fun searchSurahsOffline(query: String): List<SurahMetadata> {
        val cleanQuery = normalizeArabicForWritingAndSearch(query.replace("\u0670", ""))
        if (cleanQuery.isBlank()) return allSurahs
        val numQuery = query.trim().toIntOrNull()
        return allSurahs.filter { s ->
            (numQuery != null && s.number == numQuery) ||
                normalizeArabicForWritingAndSearch(s.nameAr).contains(cleanQuery) ||
                s.nameEn.contains(query.trim(), ignoreCase = true)
        }
    }

    fun searchJuzOffline(query: String): List<QuranJuzInfo> {
        val allJuz = getAllJuzInfo()
        val cleanQuery = normalizeArabicForWritingAndSearch(query.replace("\u0670", ""))
        if (cleanQuery.isBlank()) return allJuz
        val numQuery = query.trim().toIntOrNull()
        return allJuz.filter { j ->
            (numQuery != null && j.juzNumber == numQuery) ||
                normalizeArabicForWritingAndSearch(j.firstSurahNameAr).contains(cleanQuery) ||
                normalizeWordFlexible(j.openingText).any { it.contains(cleanQuery) } ||
                "الجزء ${j.juzNumber}".contains(query.trim()) ||
                "جزء ${j.juzNumber}".contains(query.trim())
        }
    }

    fun searchRubsOffline(query: String): List<QuranRubInfo> {
        val allRubs = getAllRubInfo()
        val cleanQuery = normalizeArabicForWritingAndSearch(query.replace("\u0670", ""))
        if (cleanQuery.isBlank()) return allRubs
        val numQuery = query.trim().toIntOrNull()
        val queryTokens = cleanQuery.split(" ").filter { it.isNotBlank() }
        return allRubs.filter { r ->
            (numQuery != null && (r.rubNumber == numQuery || r.hizbNumber == numQuery || r.juzNumber == numQuery || r.pageNumber == numQuery)) ||
                normalizeArabicForWritingAndSearch(r.surahNameAr).contains(cleanQuery) ||
                normalizeArabicForWritingAndSearch(r.quarterLabelAr).contains(cleanQuery) ||
                normalizeWordFlexible(r.openingText).any { form ->
                    form.contains(cleanQuery) || (queryTokens.size > 1 && queryTokens.all { form.contains(it) })
                }
        }
    }

    fun toArabicDigits(number: Int): String {
        val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { ch ->
            if (ch in '0'..'9') arabicChars[ch - '0'] else ch
        }.joinToString("")
    }

    fun getJuzForPage(page: Int): Int {
        val verses = pagesMap[page]
        if (!verses.isNullOrEmpty()) return verses.first().juzNumber
        if (page <= 1) return 1
        if (page >= 602) return 30
        return (((page - 2) / 20) + 1).coerceIn(1, 30)
    }

    fun getHizbForPage(page: Int): Int {
        val verses = pagesMap[page]
        if (!verses.isNullOrEmpty()) return verses.first().hizbNumber
        if (page <= 1) return 1
        return (((page - 2) / 10) + 1).coerceIn(1, 60)
    }

    fun getPageContent(pageNumber: Int): MushafPageContent {
        val safePage = pageNumber.coerceIn(1, 604)
        val pageVerses = pagesMap[safePage]

        if (!pageVerses.isNullOrEmpty()) {
            val surahNums = pageVerses.map { it.surahNumber }.distinct()
            val surahsOnPage = surahNums.mapNotNull { surahByNumber[it] }
                .ifEmpty { listOf(allSurahs.first()) }
            return MushafPageContent(
                pageNumber = safePage,
                juzNumber = pageVerses.first().juzNumber,
                hizbNumber = pageVerses.first().hizbNumber,
                surahsOnPage = surahsOnPage,
                verses = pageVerses
            )
        }

        // Fallback if getPageContent is called before context initialization
        val surahsOnPage = allSurahs.filter { safePage in it.startPage..it.endPage }
            .ifEmpty { listOf(allSurahs.first()) }
        return MushafPageContent(
            pageNumber = safePage,
            juzNumber = getJuzForPage(safePage),
            hizbNumber = getHizbForPage(safePage),
            surahsOnPage = surahsOnPage,
            verses = listOf(
                MushafVerse(
                    surahNumber = 1,
                    surahNameAr = "الفاتحة",
                    ayahNumber = 1,
                    pageNumber = 1,
                    juzNumber = 1,
                    hizbNumber = 1,
                    textUthmani = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
                )
            )
        )
    }
}
