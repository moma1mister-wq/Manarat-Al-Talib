package com.example.data

enum class ExerciseAnimationPose {
    PUSH_UP,
    BENCH_DIP,
    PULL_ROW,
    SQUAT,
    SHOULDER_PRESS,
    AB_CRUNCH,
    JUMPING_JACK,
    STRETCH_FLOW
}

data class ExerciseItem(
    val id: Int,
    val nameAr: String,
    val nameEn: String,
    val categoryAr: String, // صدر، ظهر، أرجل، كتف، بطن، كارديو، إطالات، وزن الجسم
    val targetMusclesAr: String,
    val setsCount: Int,
    val repsOrDurationAr: String,
    val defaultTimerSeconds: Int,
    val stepsAr: List<String>,
    val animationPose: ExerciseAnimationPose
)

object ExerciseOfflineData {

    val categoriesAr: List<String> = listOf(
        "الكل",
        "صدر",
        "ظهر",
        "أرجل",
        "كتف",
        "بطن",
        "كارديو",
        "إطالات",
        "وزن الجسم"
    )

    val allExercises: List<ExerciseItem> = listOf(
        ExerciseItem(
            id = 1,
            nameAr = "تمرين الضغط الكلاسيكي (Standard Push-Up)",
            nameEn = "Standard Push-Up",
            categoryAr = "صدر",
            targetMusclesAr = "عضلة الصدر الكبرى، الترايسبس، الكتف الأمامي",
            setsCount = 4,
            repsOrDurationAr = "12 - 15 تكرار",
            defaultTimerSeconds = 60,
            stepsAr = listOf(
                "١. ابدأ بوضع البلانك العالي مع وضع اليدين أوسع قليلاً من عرض الكتفين.",
                "٢. حافظ على استقامة الظهر والرقبة وشد عضلات البطن.",
                "٣. انزل بصدرك ببطء نحو الأرض مع ثني المرفقين بزاوية 45 درجة.",
                "٤. ادفع الأرض بقوة للعودة لوضع البداية مع الزفير."
            ),
            animationPose = ExerciseAnimationPose.PUSH_UP
        ),
        ExerciseItem(
            id = 2,
            nameAr = "تمرين الضغط الماسي والمائل للصدر (Incline & Diamond Push-Up)",
            nameEn = "Incline Chest Push-Up",
            categoryAr = "صدر",
            targetMusclesAr = "الصدر السفلي والداخلي، عضلات الذراع الخلفية",
            setsCount = 3,
            repsOrDurationAr = "12 تكرار",
            defaultTimerSeconds = 45,
            stepsAr = listOf(
                "١. ضع يديك على حافة مرتفعة ثابتة أو قريبتين من بعضهما على شكل ماسة.",
                "٢. حافظ على استقامة الجذع بالكامل من الرأس إلى الكعبين.",
                "٣. اخفض صدرك بتحكم كامل لمدة ثانيتين ثم اصعد بقوة."
            ),
            animationPose = ExerciseAnimationPose.PUSH_UP
        ),
        ExerciseItem(
            id = 3,
            nameAr = "تمرين العقلة والسحب للظهر (Pull-Up / Bodyweight Row)",
            nameEn = "Pull-Up & Inverted Row",
            categoryAr = "ظهر",
            targetMusclesAr = "العضلة العريضة الظهرية (المجنص)، البايسبس، وسط الظهر",
            setsCount = 4,
            repsOrDurationAr = "8 - 12 تكرار",
            defaultTimerSeconds = 60,
            stepsAr = listOf(
                "١. أمسك البار بقبضة أوسع قليلاً من الكتفين مع فرد الذراعين.",
                "٢. اسحب لوحي الكتف للأسفل وللخلف أولاً قبل ثني الذراعين.",
                "٣. اسحب جسمك للأعلى حتى يتجاوز الذقن مستوى البار.",
                "٤. انزل ببطء وتحكم كامل دون أرجحة الجسم."
            ),
            animationPose = ExerciseAnimationPose.PULL_ROW
        ),
        ExerciseItem(
            id = 4,
            nameAr = "تمرين سوبرمان لتقوية أسفل الظهر (Back Extension)",
            nameEn = "Superman Back Extension",
            categoryAr = "ظهر",
            targetMusclesAr = "عضلات أسفل الظهر (القطنية)، الألوية، أعلى الظهر",
            setsCount = 3,
            repsOrDurationAr = "15 تكرار مع ثبات ثانيتين",
            defaultTimerSeconds = 45,
            stepsAr = listOf(
                "١. استلقِ على بطنك فوق بساط التمرين ومد ذراعيك للأمام.",
                "٢. ارفع الذراعين والصدر والساقين معاً عن الأرض بهدوء.",
                "٣. اثبت في الأعلى لمدة ثانيتين مع عصر عضلات الظهر ثم انزل ببطء."
            ),
            animationPose = ExerciseAnimationPose.PULL_ROW
        ),
        ExerciseItem(
            id = 5,
            nameAr = "تمرين القرفصاء الكامل (Bodyweight Squat)",
            nameEn = "Deep Bodyweight Squat",
            categoryAr = "أرجل",
            targetMusclesAr = "عضلات الفخذ الأمامية (Quads)، الخلفية، والألوية",
            setsCount = 4,
            repsOrDurationAr = "15 - 20 تكرار",
            defaultTimerSeconds = 60,
            stepsAr = listOf(
                "١. قف مع فتح القدمين بعرض الكتفين وتوجيه الأصابع للخارج قليلاً.",
                "٢. ادفع الحوض للخلف وكأنك تجلس على كرسي مع إبقاء الصدر مرفوعاً.",
                "٣. انزل حتى يصبح الفخذان موازيين للأرض دون تقوس الظهر.",
                "٤. ادفع بالكعبين للوقوف والعودة لنقطة البداية."
            ),
            animationPose = ExerciseAnimationPose.SQUAT
        ),
        ExerciseItem(
            id = 6,
            nameAr = "تمرين الطعن التبادلي للأرجل (Alternating Lunges)",
            nameEn = "Forward Alternating Lunges",
            categoryAr = "أرجل",
            targetMusclesAr = "الفخذ الأمامي، عضلات المؤخرة، عضلات التوازن والسمانة",
            setsCount = 3,
            repsOrDurationAr = "12 تكرار لكل رجل",
            defaultTimerSeconds = 60,
            stepsAr = listOf(
                "١. قف باستقامة ثم اخطُ خطوة واسعة للأمام برجلك اليمنى.",
                "٢. اخفض جسمك حتى تشكل الركبتان زاوية قائمة 90 درجة.",
                "٣. ادفع بالقدم الأمامية للعودة للوقوف وبدّل الرجل اليسرى."
            ),
            animationPose = ExerciseAnimationPose.SQUAT
        ),
        ExerciseItem(
            id = 7,
            nameAr = "تمرين الضغط العلوي للكتف (Pike Push-Up / Shoulder Press)",
            nameEn = "Pike Shoulder Press",
            categoryAr = "كتف",
            targetMusclesAr = "عضلات الكتف الأمامية والجانبية، الترايسبس العلوية",
            setsCount = 4,
            repsOrDurationAr = "10 - 12 تكرار",
            defaultTimerSeconds = 60,
            stepsAr = listOf(
                "١. اتخذ وضع رقم 8 بالعربي (هرم مقلوب) برفع الحوض للأعلى.",
                "٢.اثنِ المرفقين واخفض أعلى الرأس باتجاه الأرض بين اليدين.",
                "٣. ادفع بقوة للأعلى حتى تفرد ذراعيك بالكامل لتشغيل عضلات الكتف."
            ),
            animationPose = ExerciseAnimationPose.SHOULDER_PRESS
        ),
        ExerciseItem(
            id = 8,
            nameAr = "تمرين طحن البطن والدراجة (Abdominal Crunch & Bicycle)",
            nameEn = "Abdominal Crunch & Core",
            categoryAr = "بطن",
            targetMusclesAr = "عضلات البطن العلوية والسفلية والجوانب (Obliques)",
            setsCount = 4,
            repsOrDurationAr = "20 تكرار",
            defaultTimerSeconds = 45,
            stepsAr = listOf(
                "١. استلقِ على ظهرك مع ثني الركبتين ووضع اليدين بجانب الأذنين دون شد الرقبة.",
                "٢. ارفع الكتفين عن الأرض بعصر عضلات البطن مع إخراج الزفير.",
                "٣. اثبت لحظة في الأعلى ثم انزل ببطء وتحكم."
            ),
            animationPose = ExerciseAnimationPose.AB_CRUNCH
        ),
        ExerciseItem(
            id = 9,
            nameAr = "تمرين القفز المفتوح وحرق الدهون (Jumping Jacks & High Knees)",
            nameEn = "Cardio Jumping Jacks",
            categoryAr = "كارديو",
            targetMusclesAr = "القلب والأوعية الدموية، كامل عضلات الجسم، السمانة",
            setsCount = 4,
            repsOrDurationAr = "45 ثانية متواصلة",
            defaultTimerSeconds = 45,
            stepsAr = listOf(
                "١. قف باستقامة والقدمان مضمومتان والذراعان بجانب الجسم.",
                "٢. اقفز مع فتح القدمين جانباً ورفع الذراعين فوق الرأس في نفس اللحظة.",
                "٣. اقفز مرة أخرى للعودة لوضع البداية بإيقاع رشيق ومتوازن."
            ),
            animationPose = ExerciseAnimationPose.JUMPING_JACK
        ),
        ExerciseItem(
            id = 10,
            nameAr = "إطالة الظهر والرقبة للطلاب بعد المذاكرة (Study Posture Stretch)",
            nameEn = "Full Spine & Neck Mobility Stretch",
            categoryAr = "إطالات",
            targetMusclesAr = "عضلات الرقبة، العمود الفقري، الأكتاف، وأوتار الركبة",
            setsCount = 2,
            repsOrDurationAr = "60 ثانية تنفس عميق",
            defaultTimerSeconds = 60,
            stepsAr = listOf(
                "١. اجلس أو قف باستقامة وافتح الذراعين للخلف لتوسيع القفص الصدري بعد الجلوس الطويل.",
                "٢. أمِل الرأس ببطء لليمين لمدة 15 ثانية ثم لليسار لتخفيف شد الرقبة.",
                "٣. انحنِ للأمام ببطء لإطالة عضلات الظهر الخلفية مع التنفس العميق."
            ),
            animationPose = ExerciseAnimationPose.STRETCH_FLOW
        ),
        ExerciseItem(
            id = 11,
            nameAr = "تمرين المتوازي والبيربي بوزن الجسم (Tricep Dips & Burpees)",
            nameEn = "Bodyweight Dips & Conditioning",
            categoryAr = "وزن الجسم",
            targetMusclesAr = "الترايسبس، الصدر السفلي، الجذع، واللياقة الشاملة",
            setsCount = 3,
            repsOrDurationAr = "12 تكرار",
            defaultTimerSeconds = 60,
            stepsAr = listOf(
                "١. استند بيديك على حافة كرسي ثابت خلف ظهرك مع مد الساقين للأمام.",
                "٢. اثنِ المرفقين ببطء حتى زاوية 90 درجة مع إبقاء الظهر قريباً من الكرسي.",
                "٣. ادفع براحة اليدين للصعود للأعلى وشد عضلة الترايسبس."
            ),
            animationPose = ExerciseAnimationPose.BENCH_DIP
        )
    )
}
