package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExerciseAnimationPose
import com.example.data.ExerciseItem
import com.example.data.ExerciseOfflineData
import com.example.data.local.AdhkarAlarmEntity
import com.example.notifications.AlarmCategory
import com.example.ui.components.CustomSectionIcon
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.components.SectionIconType
import com.example.ui.components.Time12hFormatter
import com.example.ui.components.TimePicker12hDialog
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurplePrimary
import kotlinx.coroutines.delay

@Composable
fun ExercisesScreen(
    dailyExerciseAlarm: AdhkarAlarmEntity?,
    onUpdateDailyExerciseAlarm: (String) -> Unit,
    onToggleDailyExerciseAlarm: (Boolean) -> Unit = {},
    onTestExerciseTone: () -> Unit,
    onOpenRatingModal: (String) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("الكل") }
    var alarmTimeInput by remember(dailyExerciseAlarm) {
        mutableStateOf(dailyExerciseAlarm?.alarmTime ?: "16:45")
    }
    var showEditExerciseTimeDialog by remember { mutableStateOf(false) }

    val filteredExercises = remember(selectedCategory) {
        if (selectedCategory == "الكل") {
            ExerciseOfflineData.allExercises
        } else {
            ExerciseOfflineData.allExercises.filter { it.categoryAr == selectedCategory }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PurpleWaveBackground(height = 220.dp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header + Daily Exercise Alarm Scheduler (exercise_tone.mp3)
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
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomSectionIcon(type = SectionIconType.EXERCISES, size = 48.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "التمارين",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = PurplePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "الموعد الحالي: ${Time12hFormatter.format24To12Ar(alarmTimeInput)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(onClick = onTestExerciseTone) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "تنبيه",
                                    tint = EmeraldPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Essential Daily Exercise Alarm Row (moved from HomeScreen card, preserving Image 2 style)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showEditExerciseTimeDialog = true }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                .testTag("exercise_daily_alarm_row"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dailyExerciseAlarm?.title ?: "تمرين اليوم",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${Time12hFormatter.format24To12Ar(alarmTimeInput)} (اضغط لتعديل الوقت)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = dailyExerciseAlarm?.isEnabled ?: true,
                                onCheckedChange = { enabled ->
                                    onToggleDailyExerciseAlarm(enabled)
                                },
                                modifier = Modifier.testTag("exercise_daily_alarm_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Muscle Categories Filter Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExerciseOfflineData.categoriesAr.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("exercise_cat_$cat")
                                )
                            }
                        }
                    }
                }
            }

            items(filteredExercises, key = { it.id }) { exercise ->
                ExerciseDetailCard(
                    exercise = exercise,
                    onTimerCompleteSound = onTestExerciseTone,
                    onOpenRatingModal = onOpenRatingModal
                )
            }
        }
    }

    if (showEditExerciseTimeDialog) {
        TimePicker12hDialog(
            initialTime24 = alarmTimeInput,
            title = "تعديل موعد: ${dailyExerciseAlarm?.title ?: "تمرين اليوم"}",
            onDismiss = { showEditExerciseTimeDialog = false },
            onConfirmTime24 = { updated24 ->
                alarmTimeInput = updated24
                onUpdateDailyExerciseAlarm(updated24)
                showEditExerciseTimeDialog = false
            }
        )
    }
}

@Composable
private fun ExerciseDetailCard(
    exercise: ExerciseItem,
    onTimerCompleteSound: () -> Unit,
    onOpenRatingModal: (String) -> Unit
) {
    var completedSets by remember { mutableIntStateOf(0) }
    var remainingSecs by remember { mutableIntStateOf(exercise.defaultTimerSeconds) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && remainingSecs > 0) {
            delay(1000L)
            remainingSecs -= 1
            if (remainingSecs <= 0) {
                isTimerRunning = false
                if (completedSets < exercise.setsCount) {
                    completedSets += 1
                }
                onTimerCompleteSound()
                onOpenRatingModal("تمرين: ${exercise.nameAr}")
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "عضلة: ${exercise.categoryAr}",
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "${exercise.setsCount} مجموعات × ${exercise.repsOrDurationAr}",
                    style = MaterialTheme.typography.labelLarge,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exercise.nameAr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "العضلات المستهدفة: ${exercise.targetMusclesAr}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Animated Human Performer Demonstration Box (GIF-style live articulated human motion)
            AnimatedHumanExercisePerformer(
                pose = exercise.animationPose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PurpleDeep)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "الخطوات:",
                style = MaterialTheme.typography.labelLarge,
                color = PurplePrimary,
                fontWeight = FontWeight.Bold
            )

            exercise.stepsAr.forEach { step ->
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Set Counter & Workout Timer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (completedSets < exercise.setsCount) {
                            completedSets += 1
                            if (completedSets >= exercise.setsCount) {
                                onOpenRatingModal("تمرين: ${exercise.nameAr}")
                            }
                        } else {
                            completedSets = 0
                        }
                    },
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (completedSets >= exercise.setsCount) EmeraldPrimary else PurplePrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("المجموعات: $completedSets / ${exercise.setsCount}")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { onOpenRatingModal("تمرين: ${exercise.nameAr}") },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "تقييم ⭐",
                            style = MaterialTheme.typography.labelMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { isTimerRunning = !isTimerRunning },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTimerRunning) GoldAccent else PurplePrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${remainingSecs}ث")
                    }
                    IconButton(
                        onClick = {
                            isTimerRunning = false
                            remainingSecs = exercise.defaultTimerSeconds
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة المؤقت")
                    }
                }
            }
        }
    }
}

/**
 * Animated articulated human athlete performing the exercise movement in real-time (100% Offline GIF equivalent).
 */
@Composable
private fun AnimatedHumanExercisePerformer(
    pose: ExerciseAnimationPose,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "exercise_human_motion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val floorY = h * 0.84f

            // Gym Floor Line
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(w * 0.1f, floorY),
                end = Offset(w * 0.9f, floorY),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            val limbStroke = 14f
            val torsoStroke = 20f

            when (pose) {
                ExerciseAnimationPose.PUSH_UP -> {
                    // Push-up: body lowers toward floor as phase goes 0 -> 1
                    val shoulderY = floorY - (h * 0.34f) + (phase * h * 0.16f)
                    val hipY = floorY - (h * 0.24f) + (phase * h * 0.10f)
                    val feet = Offset(w * 0.25f, floorY - 6f)
                    val hip = Offset(w * 0.48f, hipY)
                    val shoulder = Offset(w * 0.70f, shoulderY)
                    val head = Offset(w * 0.78f, shoulderY - 12f)
                    val hands = Offset(w * 0.72f, floorY - 4f)
                    val elbow = Offset(w * 0.65f, (shoulderY + floorY) * 0.5f)

                    // Legs & Torso
                    drawLine(EmeraldLight, feet, hip, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(Color.White, hip, shoulder, strokeWidth = torsoStroke, cap = StrokeCap.Round)
                    // Arms
                    drawLine(GoldAccent, shoulder, elbow, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(GoldAccent, elbow, hands, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    // Head
                    drawCircle(EmeraldPrimary, radius = 18f, center = head)
                }

                ExerciseAnimationPose.SQUAT -> {
                    // Squat: hips descend toward knee level as phase goes 0 -> 1
                    val hipY = (h * 0.45f) + (phase * h * 0.22f)
                    val shoulderY = hipY - (h * 0.22f)
                    val ankle = Offset(w * 0.52f, floorY - 6f)
                    val knee = Offset(w * 0.58f + (phase * 18f), floorY - (h * 0.20f))
                    val hip = Offset(w * 0.46f - (phase * 14f), hipY)
                    val shoulder = Offset(w * 0.50f, shoulderY)
                    val head = Offset(w * 0.51f, shoulderY - 24f)
                    val hands = Offset(w * 0.64f, shoulderY + 10f)

                    drawLine(EmeraldLight, ankle, knee, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(EmeraldLight, knee, hip, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(Color.White, hip, shoulder, strokeWidth = torsoStroke, cap = StrokeCap.Round)
                    drawLine(GoldAccent, shoulder, hands, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawCircle(EmeraldPrimary, radius = 19f, center = head)
                }

                ExerciseAnimationPose.PULL_ROW -> {
                    // Pull-up: body rises toward top bar as phase goes 0 -> 1
                    val barY = h * 0.18f
                    drawLine(GoldAccent, Offset(w * 0.3f, barY), Offset(w * 0.7f, barY), strokeWidth = 8f)
                    val lift = phase * (h * 0.20f)
                    val shoulder = Offset(w * 0.5f, h * 0.46f - lift)
                    val hip = Offset(w * 0.5f, h * 0.68f - lift)
                    val feet = Offset(w * 0.5f, h * 0.86f - lift)
                    val leftHand = Offset(w * 0.42f, barY)
                    val rightHand = Offset(w * 0.58f, barY)

                    drawLine(GoldAccent, shoulder, leftHand, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(GoldAccent, shoulder, rightHand, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(Color.White, shoulder, hip, strokeWidth = torsoStroke, cap = StrokeCap.Round)
                    drawLine(EmeraldLight, hip, feet, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawCircle(EmeraldPrimary, radius = 18f, center = Offset(w * 0.5f, shoulder.y - 22f))
                }

                ExerciseAnimationPose.JUMPING_JACK -> {
                    // Jumping Jack: arms and legs open wide with phase
                    val spread = phase * (w * 0.14f)
                    val shoulder = Offset(w * 0.5f, h * 0.36f - phase * 10f)
                    val hip = Offset(w * 0.5f, h * 0.58f - phase * 10f)
                    val leftFoot = Offset(w * 0.46f - spread, floorY - 6f)
                    val rightFoot = Offset(w * 0.54f + spread, floorY - 6f)
                    val armY = h * 0.48f - (phase * h * 0.32f)
                    val leftHand = Offset(w * 0.36f - spread * 0.5f, armY)
                    val rightHand = Offset(w * 0.64f + spread * 0.5f, armY)

                    drawLine(EmeraldLight, hip, leftFoot, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(EmeraldLight, hip, rightFoot, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(Color.White, hip, shoulder, strokeWidth = torsoStroke, cap = StrokeCap.Round)
                    drawLine(GoldAccent, shoulder, leftHand, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(GoldAccent, shoulder, rightHand, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawCircle(EmeraldPrimary, radius = 18f, center = Offset(w * 0.5f, shoulder.y - 24f))
                }

                else -> {
                    // Core / Crunch / Stretch / Press motion
                    val hip = Offset(w * 0.45f, floorY - 12f)
                    val knee = Offset(w * 0.62f, floorY - (h * 0.22f))
                    val foot = Offset(w * 0.72f, floorY - 8f)
                    val torsoAngleY = floorY - (h * 0.16f) - (phase * h * 0.22f)
                    val shoulder = Offset(w * 0.32f + (phase * w * 0.08f), torsoAngleY)
                    val head = Offset(shoulder.x - 6f, shoulder.y - 22f)

                    drawLine(EmeraldLight, hip, knee, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(EmeraldLight, knee, foot, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawLine(Color.White, hip, shoulder, strokeWidth = torsoStroke, cap = StrokeCap.Round)
                    drawLine(GoldAccent, shoulder, knee, strokeWidth = limbStroke, cap = StrokeCap.Round)
                    drawCircle(EmeraldPrimary, radius = 18f, center = head)
                }
            }
        }
    }
}
