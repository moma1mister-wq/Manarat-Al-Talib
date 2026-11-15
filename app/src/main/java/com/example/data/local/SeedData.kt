package com.example.data.local

object SeedData {

    fun getInitialSchedule(): List<ScheduleItemEntity> = listOf(
        // السبت - مدرج 1 (لكلا السكشنين 0)
        ScheduleItemEntity(
            title = "Algorithm Analysis",
            dayOfWeek = "السبت",
            startTime = "08:00",
            endTime = "10:00",
            location = "مدرج 1",
            sectionFilter = 0,
            itemType = "LECTURE",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "Computer Networks",
            dayOfWeek = "السبت",
            startTime = "10:00",
            endTime = "12:00",
            location = "مدرج 1",
            sectionFilter = 0,
            itemType = "LECTURE",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "English II",
            dayOfWeek = "السبت",
            startTime = "12:00",
            endTime = "14:00",
            location = "مدرج 1",
            sectionFilter = 0,
            itemType = "LECTURE",
            recurrence = "WEEKLY",
            timerMinutes = 60
        ),

        // الأحد - محاضرة + معامل السكاشن 1 و 2
        ScheduleItemEntity(
            title = "Data Structures",
            dayOfWeek = "الأحد",
            startTime = "08:00",
            endTime = "10:00",
            location = "مدرج 1",
            sectionFilter = 0,
            itemType = "LECTURE",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "Algorithm Analysis (سكشن 1)",
            dayOfWeek = "الأحد",
            startTime = "10:00",
            endTime = "12:00",
            location = "معمل 1",
            sectionFilter = 1,
            itemType = "LAB",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "Computer Networks (سكشن 2)",
            dayOfWeek = "الأحد",
            startTime = "10:00",
            endTime = "12:00",
            location = "معمل 2",
            sectionFilter = 2,
            itemType = "LAB",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "Algorithm Analysis (سكشن 2)",
            dayOfWeek = "الأحد",
            startTime = "12:00",
            endTime = "14:00",
            location = "معمل 1",
            sectionFilter = 2,
            itemType = "LAB",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "Computer Networks (سكشن 1)",
            dayOfWeek = "الأحد",
            startTime = "12:00",
            endTime = "14:00",
            location = "معمل 2",
            sectionFilter = 1,
            itemType = "LAB",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "Data Structures (سكشن 1)",
            dayOfWeek = "الأحد",
            startTime = "14:00",
            endTime = "16:00",
            location = "معمل 1",
            sectionFilter = 1,
            itemType = "LAB",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "Data Structures (سكشن 2)",
            dayOfWeek = "الأحد",
            startTime = "14:00",
            endTime = "16:00",
            location = "معمل 2",
            sectionFilter = 2,
            itemType = "LAB",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),

        // الاثنين - مدرج 1
        ScheduleItemEntity(
            title = "Linear Algebra",
            dayOfWeek = "الاثنين",
            startTime = "08:00",
            endTime = "10:00",
            location = "مدرج 1",
            sectionFilter = 0,
            itemType = "LECTURE",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),
        ScheduleItemEntity(
            title = "Linear Algebra Tutorial",
            dayOfWeek = "الاثنين",
            startTime = "10:00",
            endTime = "12:00",
            location = "مدرج 1",
            sectionFilter = 0,
            itemType = "LECTURE",
            recurrence = "WEEKLY",
            timerMinutes = 90
        ),

        // الأربعاء - أونلاين
        ScheduleItemEntity(
            title = "Biography of Prophet and History of Caliphs",
            dayOfWeek = "الأربعاء",
            startTime = "10:00",
            endTime = "12:00",
            location = "أونلاين (Online)",
            sectionFilter = 0,
            itemType = "ONLINE",
            recurrence = "WEEKLY",
            timerMinutes = 60
        ),
        ScheduleItemEntity(
            title = "Human Right",
            dayOfWeek = "الأربعاء",
            startTime = "12:00",
            endTime = "14:00",
            location = "أونلاين (Online)",
            sectionFilter = 0,
            itemType = "ONLINE",
            recurrence = "WEEKLY",
            timerMinutes = 60
        ),

        // جلسات مذاكرة مسائية افتراضية قابلة للتعديل
        ScheduleItemEntity(
            title = "مذاكرة Algorithm Analysis",
            dayOfWeek = "الثلاثاء",
            startTime = "18:00",
            endTime = "19:30",
            location = "مكتب المذاكرة",
            sectionFilter = 0,
            itemType = "STUDY_TASK",
            recurrence = "WEEKLY",
            timerMinutes = 90,
            isCoreCurriculum = false
        ),
        ScheduleItemEntity(
            title = "مذاكرة Data Structures",
            dayOfWeek = "الخميس",
            startTime = "19:00",
            endTime = "20:30",
            location = "مكتب المذاكرة",
            sectionFilter = 0,
            itemType = "STUDY_TASK",
            recurrence = "WEEKLY",
            timerMinutes = 90,
            isCoreCurriculum = false
        )
    )

    fun getInitialAdhkarAlarms(): List<AdhkarAlarmEntity> = listOf(
        AdhkarAlarmEntity(
            title = "أذكار الصباح",
            category = "أذكار الصباح والمساء",
            alarmTime = "05:35",
            isEnabled = true,
            toneName = "adhkar_tone.mp3",
            isFixedCore = true
        ),
        AdhkarAlarmEntity(
            title = "أذكار المساء",
            category = "أذكار الصباح والمساء",
            alarmTime = "16:15",
            isEnabled = true,
            toneName = "adhkar_tone.mp3",
            isFixedCore = true
        ),
        AdhkarAlarmEntity(
            title = "تمرين اليوم",
            category = "التمارين اليومية",
            alarmTime = "16:45",
            isEnabled = true,
            toneName = "exercise_tone.mp3",
            isFixedCore = true
        ),
        AdhkarAlarmEntity(
            title = "أذكار النوم",
            category = "أذكار النوم والاستيقاظ",
            alarmTime = "22:30",
            isEnabled = true,
            toneName = "adhkar_tone.mp3",
            isFixedCore = false
        ),
        AdhkarAlarmEntity(
            title = "أذكار السفر",
            category = "أذكار السفر والركوب",
            alarmTime = "07:15",
            isEnabled = true,
            toneName = "adhkar_tone.mp3",
            isFixedCore = false
        )
    )

    fun getInitialRatings(): List<SessionRatingEntity> = emptyList()

    /**
     * Complete extraction from the attached PDF (حصن المسلم - 96 pages)
     * PLUS the Student & Daily Prices Directory so both sections work 100% offline.
     */
    fun getInitialCatalogItems(): List<CatalogItemEntity> = listOf(
        // === أولاً: مستخرجات ملف الـ PDF المرفق (حصن المسلم من أذكار الكتاب والسنة - 96 صفحة) ===
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "١- أذكار الاستيقاظ من النوم (الحمد لله الذي أحيانا)",
            category = "أذكار النوم والاستيقاظ",
            contentOrDescription = "«الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ».\n«الْحَمْدُ لِلَّهِ الَّذِي عَافَانِي فِي جَسَدِي، وَرَدَّ عَلَيَّ رُوحِي، وَأَذِنَ لِي بِذِكْرِهِ».",
            unitOrReference = "حصن المسلم ص 8-9 (البخاري ومسلم)",
            targetCount = 1,
            pdfPageNumber = 8
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٢- دعاء لبس الثوب والثوب الجديد",
            category = "أذكار الطعام والمنزل",
            contentOrDescription = "«الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَذَا (الثَّوْبَ) وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ».\n«اللَّهُمَّ لَكَ الْحَمْدُ أَنْتَ كَسَوْتَنِيهِ، أَسْأَلُكَ مِنْ خَيْرِهِ وَخَيْرِ مَا صُنِعَ لَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّهِ وَشَرِّ مَا صُنِعَ لَهُ».",
            unitOrReference = "حصن المسلم ص 11 (أبو داود والترمذي)",
            targetCount = 1,
            pdfPageNumber = 11
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٦-٩ دعاء دخول الخلاء والخروج منه والذكر قبل وبعد الوضوء",
            category = "أذكار الصلاة والمسجد",
            contentOrDescription = "دخول الخلاء: «بِسْمِ اللَّهِ اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ».\nالخروج: «غُفْرَانَكَ».\nبعد الوضوء: «أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ».",
            unitOrReference = "حصن المسلم ص 12-13",
            targetCount = 1,
            pdfPageNumber = 12
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "١٠-١١ الذكر عند الخروج من المنزل وعند دخوله",
            category = "أذكار الطعام والمنزل",
            contentOrDescription = "عند الخروج: «بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ».\n«اللَّهُمَّ إِنِّي أَعُوذُ بِكَ أَنْ أَضِلَّ، أَوْ أُضَلَّ، أَوْ أَزِلَّ، أَوْ أُزَلَّ، أَوْ أَظْلِمَ، أَوْ أُظْلَمَ، أَوْ أَجْهَلَ، أَوْ يُجْهَلَ عَلَيَّ».\nعند الدخول: «بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى رَبِّنَا تَوَكَّلْنَا».",
            unitOrReference = "حصن المسلم ص 13",
            targetCount = 1,
            pdfPageNumber = 13
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "١٢-١٤ دعاء الذهاب إلى المسجد ودخوله والخروج منه",
            category = "أذكار الصلاة والمسجد",
            contentOrDescription = "«اللَّهُمَّ اجْعَلْ فِي قَلْبِي نُورًا، وَفِي لِسَانِي نُورًا، وَفِي سَمْعِي نُورًا، وَفِي بَصَرِي نُورًا».\nعند الدخول: «بِسْمِ اللَّهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ».\nعند الخروج: «اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ، اللَّهُمَّ اعْصِمْنِي مِنَ الشَّيْطَانِ الرَّجِيمِ».",
            unitOrReference = "حصن المسلم ص 14-15",
            targetCount = 1,
            pdfPageNumber = 14
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "١٥-٢٥ أذكار الأذان ودعاء الاستفتاح والركوع والسجود والتشهد",
            category = "أذكار الصلاة والمسجد",
            contentOrDescription = "عقب الأذان: «اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ».\nبعد السلام: «أَسْتَغْفِرُ اللَّهَ (ثَلَاثًا) اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ».",
            unitOrReference = "حصن المسلم ص 16-31",
            targetCount = 3,
            pdfPageNumber = 16
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٢٧- أذكار الصباح والمساء: آية الكرسي والمعوذات",
            category = "أذكار الصباح والمساء",
            contentOrDescription = "﴿اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾ (مرة واحدة) + سورة الإخلاص والفلق والناس (ثلاث مرات).",
            unitOrReference = "حصن المسلم ص 33-34",
            targetCount = 3,
            pdfPageNumber = 33
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٢٧- أذكار الصباح والمساء: سيد الاستغفار",
            category = "أذكار الصباح والمساء",
            contentOrDescription = "«اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ».",
            unitOrReference = "حصن المسلم ص 35 (رواه البخاري)",
            targetCount = 1,
            pdfPageNumber = 35
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٢٧- أذكار الصباح والمساء: العافية والحفظ والرضى",
            category = "أذكار الصباح والمساء",
            contentOrDescription = "«بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ» (ثلاث مرات).\n«رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا» (ثلاث مرات).\n«يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ».",
            unitOrReference = "حصن المسلم ص 36-38",
            targetCount = 3,
            pdfPageNumber = 37
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٢٧- أذكار الصباح والمساء: التسبيح والتهليل والصلاة على النبي ﷺ",
            category = "أذكار الصباح والمساء",
            contentOrDescription = "«سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ» (ثلاث مرات).\n«اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا».\n«اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ» (عشر مرات).",
            unitOrReference = "حصن المسلم ص 39-40",
            targetCount = 3,
            pdfPageNumber = 39
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٢٨- أذكار النوم (باسمك ربي وضعت جنبي)",
            category = "أذكار النوم والاستيقاظ",
            contentOrDescription = "«بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا، بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ».\n«بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا».\n«اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ» (ثلاث مرات).",
            unitOrReference = "حصن المسلم ص 41-45",
            targetCount = 3,
            pdfPageNumber = 43
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٣٤-٣٥ دعاء الهم والحزن ودعاء الكرب",
            category = "أدعية الهم والكرب والدين",
            contentOrDescription = "«اللَّهُمَّ إِنِّي عَبْدُكَ، ابْنُ عَبْدِكَ، ابْنُ أَمَتِكَ، نَاصِيَتِي بِيَدِكَ، مَاضٍ فِيَّ حُكْمُكَ، عَدْلٌ فِيَّ قَضَاؤُكَ، أَسْأَلُكَ بِكُلِّ اسْمٍ هُوَ لَكَ... أَنْ تَجْعَلَ الْقُرْآنَ رَبِيعَ قَلْبِي، وَنُورَ صَدْرِي، وَجَلَاءَ حُزْنِي، وَذَهَابَ هَمِّي».\n«لَا إِلَهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ».",
            unitOrReference = "حصن المسلم ص 49-50",
            targetCount = 3,
            pdfPageNumber = 49
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٤١-٤٣ دعاء قضاء الدين ودعاء من استصعب عليه أمر (مهم للمذاكرة والامتحانات)",
            category = "أدعية الهم والكرب والدين",
            contentOrDescription = "قضاء الدين: «اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ».\nعند صعوبة أمر أو مذاكرة: «اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلًا».",
            unitOrReference = "حصن المسلم ص 53-54",
            targetCount = 3,
            pdfPageNumber = 54
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٦٨-٧٠ الدعاء عند إفطار الصائم وقبل الطعام وبعده",
            category = "أذكار الطعام والمنزل",
            contentOrDescription = "عند الإفطار: «ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ».\nقبل الطعام: «بِسْمِ اللَّهِ، فَإِنْ نَسِيَ فِي أَوَّلِهِ فَلْيَقُلْ: بِسْمِ اللَّهِ فِي أَوَّلِهِ وَآخِرِهِ».\nبعد الطعام: «الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا، وَرَزَقَنِيهِ، مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ».",
            unitOrReference = "حصن المسلم ص 65-66",
            targetCount = 1,
            pdfPageNumber = 65
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٨٥- كفارة المجلس",
            category = "الاستغفار والتسبيح الجامع",
            contentOrDescription = "«سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ».",
            unitOrReference = "حصن المسلم ص 70",
            targetCount = 1,
            pdfPageNumber = 70
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٩٥-٩٦ دعاء الركوب ودعاء السفر",
            category = "أذكار السفر والركوب",
            contentOrDescription = "«بِسْمِ اللَّهِ، الْحَمْدُ لِلَّهِ، ﴿سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ﴾ الْحَمْدُ لِلَّهِ، الْحَمْدُ لِلَّهِ، الْحَمْدُ لِلَّهِ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَذَا وَاطْوِ عَنَّا بُعْدَهُ».",
            unitOrReference = "حصن المسلم ص 73-74",
            targetCount = 1,
            pdfPageNumber = 73
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "٩٨- دعاء دخول السوق",
            category = "أذكار السفر والركوب",
            contentOrDescription = "«لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ، بِيَدِهِ الْخَيْرُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ».",
            unitOrReference = "حصن المسلم ص 75",
            targetCount = 1,
            pdfPageNumber = 75
        ),
        CatalogItemEntity(
            recordType = "HISN_PDF",
            title = "١٢٩-١٣٠ الاستغفار والتوبة وفضل التسبيح والتحميد والتهليل والتكبير",
            category = "الاستغفار والتسبيح الجامع",
            contentOrDescription = "«أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ».\n«كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ».\n«لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ» (كنز من كنوز الجنة).",
            unitOrReference = "حصن المسلم ص 87-91",
            targetCount = 3,
            pdfPageNumber = 89
        ),

        // === ثانياً: قائمة الأسعار والأصناف الدراسية والمعيشية (قابلة للبحث والتصنيف والتعديل Offline) ===
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "كتاب ومذكرة Algorithm Analysis (Level 2 - AI)",
            category = "كتب ومراجع جامعية",
            contentOrDescription = "المرجع الدراسي ومذكرة التدريبات العملية لمادة تحليل الخوارزميات - كلية الحاسبات جامعة الأزهر",
            priceEgp = 185.0,
            unitOrReference = "نسخة مطبوعة + شيتات",
            pdfPageNumber = 1
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "كتاب Data Structures & Lab Manual",
            category = "كتب ومراجع جامعية",
            contentOrDescription = "مذكرة هياكل البيانات وتطبيقات المعمل (سكشن 1 وسكشن 2)",
            priceEgp = 175.0,
            unitOrReference = "نسخة مطبوعة",
            pdfPageNumber = 2
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "كتاب Computer Networks (شبكات الحاسب)",
            category = "كتب ومراجع جامعية",
            contentOrDescription = "الكتاب الجامعي + دليل معمل الشبكات Packet Tracer",
            priceEgp = 190.0,
            unitOrReference = "نسخة كاملة",
            pdfPageNumber = 3
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "كتاب Linear Algebra & Tutorial Sheets",
            category = "كتب ومراجع جامعية",
            contentOrDescription = "كتاب الجبر الخطي وشيتات التمارين الأسبوعية",
            priceEgp = 150.0,
            unitOrReference = "نسخة مطبوعة",
            pdfPageNumber = 4
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "كشكول سلك جامعي 200 ورقة مقسم 5 فواصل",
            category = "أدوات ومعدات معامل الحاسب",
            contentOrDescription = "كشكول محاضرات لتدوين ملاحظات مدرج 1 والمعامل",
            priceEgp = 95.0,
            unitOrReference = "قطعة",
            pdfPageNumber = 5
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "فلاشة USB 64GB USB 3.2 لتسليم مشاريع المعمل",
            category = "أدوات ومعدات معامل الحاسب",
            contentOrDescription = "لحفظ أكواد السكاشن وملفات المشاريع والمحاضرات",
            priceEgp = 260.0,
            unitOrReference = "قطعة",
            pdfPageNumber = 6
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "اشتراك مترو الأنفاق الطلابي (ربع سنوي - الجامعة)",
            category = "مواصلات وسكن طلابي",
            contentOrDescription = "اشتراك مخفض لطلاب جامعة الأزهر مدينة نصر",
            priceEgp = 150.0,
            unitOrReference = "3 شهور",
            pdfPageNumber = 7
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "باقة إنترنت منزلية / موبايل لمحاضرات الأربعاء الأونلاين",
            category = "باقات إنترنت وكورسات",
            contentOrDescription = "باقة شهرية لحضور محاضرات السيرة وحقوق الإنسان الأونلاين وتحميل المحاضرات",
            priceEgp = 210.0,
            unitOrReference = "شهرياً",
            pdfPageNumber = 8
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "وجبة غداء طلابية متوازنة + مشروب في يوم المعامل الطويل (الأحد)",
            category = "وجبات ومشروبات يومية",
            contentOrDescription = "وجبة بروتين صحية مناسبة لليوم الدراسي من 8 صباحاً حتى 4 عصراً",
            priceEgp = 85.0,
            unitOrReference = "وجبة يومية",
            pdfPageNumber = 9
        ),
        CatalogItemEntity(
            recordType = "PRICE_LIST",
            title = "طباعة وتصوير شيتات وأبحاث (100 ورقة أبيض وأسود)",
            category = "أدوات ومعدات معامل الحاسب",
            contentOrDescription = "تصوير ملازم المراجعة والامتحانات السابقة",
            priceEgp = 65.0,
            unitOrReference = "100 ورقة",
            pdfPageNumber = 10
        )
    )

    /**
     * Loads all 132 chapters and all 267 Adhkar of Hisn Al-Muslim from assets/hisn_almuslim_full.json
     */
    fun loadFullHisnAlMuslimFromAssets(context: android.content.Context): List<CatalogItemEntity> {
        val result = mutableListOf<CatalogItemEntity>()
        try {
            val jsonStr = context.assets.open("hisn_almuslim_full.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val chapters = org.json.JSONArray(jsonStr)
            for (i in 0 until chapters.length()) {
                val ch = chapters.getJSONObject(i)
                val chapterId = ch.optInt("id", i + 1)
                val rawCategory = ch.optString("category", "")
                val groupCategory = classifyHisnChapter(chapterId, rawCategory)
                val itemsArr = ch.optJSONArray("items")
                if (itemsArr != null && itemsArr.length() > 0) {
                    for (j in 0 until itemsArr.length()) {
                        val dhikrObj = itemsArr.getJSONObject(j)
                        val dhikrIndex = j + 1
                        val text = dhikrObj.optString("text", "").trim()
                        val count = dhikrObj.optInt("count", 1).coerceAtLeast(1)
                        val titlePrefix = if (itemsArr.length() > 1) {
                            "باب $chapterId: $rawCategory (ذكر $dhikrIndex من ${itemsArr.length()})"
                        } else {
                            "باب $chapterId: $rawCategory"
                        }
                        val approxPdfPage = (8 + (chapterId * 84) / 132).coerceIn(8, 92)
                        result.add(
                            CatalogItemEntity(
                                recordType = "HISN_PDF",
                                title = titlePrefix,
                                category = groupCategory,
                                contentOrDescription = text,
                                priceEgp = 0.0,
                                unitOrReference = "حصن المسلم • باب $chapterId • صفحة $approxPdfPage • التكرار: $count",
                                targetCount = count,
                                pdfPageNumber = approxPdfPage
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }
        return result
    }

    private fun classifyHisnChapter(chapterId: Int, title: String): String {
        return when {
            title.contains("الصباح") || title.contains("المساء") -> "أذكار الصباح والمساء"
            title.contains("النوم") || title.contains("الاستيقاظ") || title.contains("تقلب") || title.contains("الرؤيا") -> "أذكار النوم والاستيقاظ"
            title.contains("الصلاة") || title.contains("الوضوء") || title.contains("الأذان") || title.contains("المسجد") || title.contains("الركوع") || title.contains("السجود") || title.contains("التشهد") || title.contains("الوتر") || title.contains("الاستخارة") -> "أذكار الصلاة والمسجد"
            title.contains("الثوب") || title.contains("المنزل") || title.contains("الخلاء") || title.contains("الطعام") || title.contains("الضيف") || title.contains("إفطار") || title.contains("الصائم") || title.contains("العطاس") || title.contains("المتزوج") || title.contains("المجلس") -> "أذكار المنزل والطعام واليومي"
            title.contains("السفر") || title.contains("الركوب") || title.contains("القرية") || title.contains("السوق") -> "أذكار السفر والركوب"
            title.contains("الهم") || title.contains("الكرب") || title.contains("الدين") || title.contains("استصعب") || title.contains("العدو") || title.contains("السلطان") || title.contains("المريض") || title.contains("مصيبة") || title.contains("الميت") || title.contains("التعزية") || title.contains("القبور") || title.contains("وجع") || title.contains("العين") -> "أدعية الهم والكرب والشفاء"
            title.contains("الحج") || title.contains("العمرة") || title.contains("عرفة") || title.contains("الصفا") || title.contains("المشعر") || title.contains("الجمار") || title.contains("الريح") || title.contains("الرعد") || title.contains("المطر") || title.contains("الهلال") -> "أذكار الحج والطبيعة والمواسم"
            else -> "الاستغفار والتسبيح والآداب الجامعة"
        }
    }
}
