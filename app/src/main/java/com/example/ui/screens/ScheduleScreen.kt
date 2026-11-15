package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ScheduleItemEntity
import com.example.notifications.AlarmCategory
import com.example.ui.components.CustomSectionIcon
import com.example.ui.components.MintCircleIndicator
import com.example.ui.components.PurpleCircleIndicator
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.components.ReferenceHeaderTopBar
import com.example.ui.components.SectionIconType
import com.example.ui.components.Time12hFormatter
import com.example.ui.components.TimePicker12hField
import com.example.ui.components.VerticalConnectorDots
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.viewmodel.ActiveStudyTimerState

@Composable
fun ScheduleScreen(
    sectionNumber: Int,
    scheduleItems: List<ScheduleItemEntity>,
    studyTimer: ActiveStudyTimerState,
    subjectCustomTimers: Map<String, Int> = emptyMap(),
    onChangeSection: (Int) -> Unit,
    onSaveItem: (ScheduleItemEntity) -> Unit,
    onDeleteItem: (ScheduleItemEntity) -> Unit,
    onClearAllSchedule: () -> Unit = {},
    onRestoreDefaultSchedule: () -> Unit = {},
    onStartStudyTimer: (String, Int, String) -> Unit,
    onSetCustomSubjectTimer: (String, Int, Boolean, String) -> Unit = { _, _, _, _ -> },
    onTestStudyAlert: (String, String) -> Unit,
    onOpenRatingModal: (String) -> Unit = {}
) {
    val days = listOf("الكل", "السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
    var selectedDay by remember { mutableStateOf("الكل") }
    var editingItem by remember { mutableStateOf<ScheduleItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<ScheduleItemEntity?>(null) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var showAddOrEditDialog by remember { mutableStateOf(false) }
    var showCustomSubjectTimerDialog by remember { mutableStateOf(false) }
    var customTimerTargetSubject by remember { mutableStateOf("Algorithm Analysis") }
    var customTimerTargetToneUri by remember { mutableStateOf("") }

    val filteredItems = remember(scheduleItems, sectionNumber, selectedDay) {
        scheduleItems.filter { item ->
            (item.sectionFilter == 0 || item.sectionFilter == sectionNumber) &&
                (selectedDay == "الكل" || item.dayOfWeek == selectedDay)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Right Phone Header with Diamond Grid Pattern
        PurpleWaveBackground(height = 250.dp, showDiamondGrid = true)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Matching the Right Phone ("Location 1 ⇄ Location 2" + Section/Day Filters)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ReferenceHeaderTopBar(
                        onProfileOrSettingsClick = { onChangeSection(if (sectionNumber == 1) 2 else 1) },
                        onMoreClick = {
                            customTimerTargetSubject = scheduleItems.firstOrNull()?.title ?: "Algorithm Analysis"
                            customTimerTargetToneUri = ""
                            showCustomSubjectTimerDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Summary Bar inside the Purple Diamond Header ("Location 1 ⇄ Location 2" style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "سكشن $sectionNumber",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "تبديل",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onChangeSection(if (sectionNumber == 1) 2 else 1) }
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (selectedDay == "الكل") "كل الأيام" else selectedDay,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredItems.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "السكشن:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                FilterChip(
                                    selected = sectionNumber == 1,
                                    onClick = { onChangeSection(1) },
                                    label = { Text("سكشن 1", maxLines = 1, softWrap = false) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("section_1_chip")
                                )
                                FilterChip(
                                    selected = sectionNumber == 2,
                                    onClick = { onChangeSection(2) },
                                    label = { Text("سكشن 2", maxLines = 1, softWrap = false) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("section_2_chip")
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                days.forEach { day ->
                                    FilterChip(
                                        selected = selectedDay == day,
                                        onClick = { selectedDay = day },
                                        label = { Text(day, maxLines = 1, softWrap = false) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PurplePrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        customTimerTargetSubject = scheduleItems.firstOrNull()?.title ?: "Algorithm Analysis"
                                        customTimerTargetToneUri = ""
                                        showCustomSubjectTimerDialog = true
                                    },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "تايمر مخصص",
                                        color = PurplePrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (scheduleItems.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { showClearAllConfirmDialog = true },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = CoralAlert, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "مسح الجدول",
                                            color = CoralAlert,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Schedule Item Cards Matching the Right Phone's Top-Badge Floating White Cards
            items(filteredItems, key = { it.id }) { item ->
                val isSubjectRunning = studyTimer.subjectTitle == item.title && studyTimer.isRunning
                val customSubjectMins = subjectCustomTimers[item.title]
                    ?: if (item.timerMinutes != 90) item.timerMinutes else 60

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    // Main Floating White Card (shifted down by 16.dp so the top purple circle overlaps its top edge)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Start Column: Mint Green Circle (Subject) + Purple Circle (Location & Time)
                                Column(modifier = Modifier.weight(1.18f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        MintCircleIndicator(size = 28.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${item.dayOfWeek} • ${
                                                    when (item.itemType) {
                                                        "LAB" -> "معمل عملي"
                                                        "ONLINE" -> "أونلاين"
                                                        else -> "محاضرة / مذاكرة"
                                                    }
                                                }",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    VerticalConnectorDots(height = 12.dp)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PurpleCircleIndicator(size = 28.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.location,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${Time12hFormatter.format24To12Ar(item.startTime)} - ${Time12hFormatter.format24To12Ar(item.endTime)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // End Column: Mint-Green Metadata + Large Mint Highlight Time + Purple Pill Button ("BUY TICKET" style)
                                Column(
                                    modifier = Modifier.weight(0.82f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "الأساسي: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "90 min",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "المخصص: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$customSubjectMins min",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Large Mint-Green Highlight Value (12-hour format with ص / م)
                                    Text(
                                        text = Time12hFormatter.format24To12Ar(item.startTime),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.ExtraBold
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Compact Purple Pill Button ("BUY TICKET" style in the Right Phone)
                                    Button(
                                        onClick = {
                                            onStartStudyTimer(item.title, customSubjectMins, item.customToneUri)
                                        },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PurplePrimary,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = if (isSubjectRunning) "يعمل (${studyTimer.remainingSeconds / 60}د)" else "بدء (${customSubjectMins}د)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Secondary Action Strip (90m base timer, customize timer, test sound, edit, delete)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { onStartStudyTimer(item.title, 90, item.customToneUri) },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = "الأساسي 90د",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            customTimerTargetSubject = item.title
                                            customTimerTargetToneUri = item.customToneUri
                                            showCustomSubjectTimerDialog = true
                                        },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = "تخصيص ⏱",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { onOpenRatingModal(item.title) },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = "تقييم ⭐",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val shortTitle = com.example.notifications.NotificationAndAlarmHelper.formatShortNotificationTitle(
                                                category = com.example.notifications.AlarmCategory.STUDY,
                                                rawTitle = item.title,
                                                subjectName = item.title,
                                                itemType = item.itemType
                                            )
                                            onTestStudyAlert(
                                                shortTitle,
                                                item.customToneUri
                                            )
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "تنبيه",
                                            tint = GoldAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            editingItem = item
                                            showAddOrEditDialog = true
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل",
                                            tint = PurplePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                     IconButton(
                                        onClick = { itemToDelete = item },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = CoralAlert,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top-Overlapping Circular Purple Badge (sitting on the top edge of the card, matching the Right Phone)
                    Surface(
                        modifier = Modifier
                            .padding(start = 22.dp)
                            .size(38.dp)
                            .align(Alignment.TopStart),
                        shape = CircleShape,
                        color = PurplePrimary,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .shadow(6.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "لا توجد مواد في الجدول حالياً 📅",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "يمكنك إضافة مواد جديدة في أي وقت بالنقر على زر (+) بالأسفل، أو استعادة الجدول الافتراضي.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editingItem = null
                                        showAddOrEditDialog = true
                                    },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Text("إضافة مادة +", fontWeight = FontWeight.Bold)
                                }
                                if (scheduleItems.isEmpty()) {
                                    OutlinedButton(
                                        onClick = { onRestoreDefaultSchedule() },
                                        shape = CircleShape
                                    ) {
                                        Text("استعادة الجدول 🔄", color = PurplePrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingItem = null
                showAddOrEditDialog = true
            },
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_schedule_item_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مادة أو مهمة")
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة مادة / مذاكرة", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = "تأكيد حذف المادة 🗑️",
                    fontWeight = FontWeight.Bold,
                    color = CoralAlert
                )
            },
            text = {
                Text(
                    text = "هل تريد حذف «${itemToDelete?.title}» من الجدول؟ سيتم إلغاء التنبيه المرتبط بها تلقائياً مع الحفاظ على باقي إعدادات ومواد التطبيق."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { onDeleteItem(it) }
                        itemToDelete = null
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAlert)
                ) {
                    Text("حذف المادة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = {
                Text(
                    text = "مسح الجدول بالكامل ⚠️",
                    fontWeight = FontWeight.Bold,
                    color = CoralAlert
                )
            },
            text = {
                Text(
                    text = "هل تريد مسح جميع المواد من الجدول؟ سيتم إلغاء التنبيهات الخاصة بالجدول فوراً مع الحفاظ التام على باقي أقسام التطبيق (المصحف، الأذكار، الصلوات). يمكنك إضافة مواد جديدة أو استعادة الجدول الافتراضي في أي وقت."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllSchedule()
                        showClearAllConfirmDialog = false
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAlert)
                ) {
                    Text("مسح الكل", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showCustomSubjectTimerDialog) {
        CustomSubjectTimerDialog(
            initialSubject = customTimerTargetSubject,
            availableSubjects = scheduleItems.map { it.title }.distinct(),
            subjectCustomTimers = subjectCustomTimers,
            onDismiss = { showCustomSubjectTimerDialog = false },
            onSaveCustomTimer = { subj, mins, startNow ->
                onSetCustomSubjectTimer(subj, mins, startNow, customTimerTargetToneUri)
            }
        )
    }

    if (showAddOrEditDialog) {
        AddOrEditScheduleDialog(
            initial = editingItem,
            currentSection = sectionNumber,
            onDismiss = { showAddOrEditDialog = false },
            onSave = { saved ->
                onSaveItem(saved)
                showAddOrEditDialog = false
            }
        )
    }
}

@Composable
private fun AddOrEditScheduleDialog(
    initial: ScheduleItemEntity?,
    currentSection: Int,
    onDismiss: () -> Unit,
    onSave: (ScheduleItemEntity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var dayOfWeek by remember { mutableStateOf(initial?.dayOfWeek ?: "السبت") }
    var startTime by remember { mutableStateOf(initial?.startTime ?: "18:00") }
    var endTime by remember { mutableStateOf(initial?.endTime ?: "19:30") }
    var location by remember { mutableStateOf(initial?.location ?: "مدرج 1") }
    var sectionFilter by remember { mutableIntStateOf(initial?.sectionFilter ?: currentSection) }
    var recurrence by remember { mutableStateOf(initial?.recurrence ?: "WEEKLY") }
    var timerMinutesText by remember { mutableStateOf((initial?.timerMinutes ?: 90).toString()) }
    var customToneUri by remember { mutableStateOf(initial?.customToneUri ?: "") }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            customToneUri = com.example.notifications.NotificationAndAlarmHelper.saveCustomTaskToneFromUri(context, uri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "إضافة مادة" else "تعديل المادة",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("المادة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dayOfWeek,
                        onValueChange = { dayOfWeek = it },
                        label = { Text("اليوم") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("المكان") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimePicker12hField(
                        label = "وقت البداية (من)",
                        time24 = startTime,
                        onTime24Changed = { startTime = it },
                        modifier = Modifier.weight(1f)
                    )
                    TimePicker12hField(
                        label = "وقت النهاية (إلى)",
                        time24 = endTime,
                        onTime24Changed = { endTime = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = timerMinutesText,
                    onValueChange = { timerMinutesText = it },
                    label = { Text("المدة (دقيقة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("التكرار:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DAILY" to "يومي", "WEEKLY" to "أسبوعي", "ONCE" to "مرة واحدة").forEach { (key, label) ->
                        FilterChip(
                            selected = recurrence == key,
                            onClick = { recurrence = key },
                            label = { Text(label) }
                        )
                    }
                }

                Text("السكشن:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "عام", 1 to "سكشن 1", 2 to "سكشن 2").forEach { (sec, label) ->
                        FilterChip(
                            selected = sectionFilter == sec,
                            onClick = { sectionFilter = sec },
                            label = { Text(label) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { audioPickerLauncher.launch(arrayOf("audio/mpeg", "audio/*")) },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AudioFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (customToneUri.isBlank()) "نغمة مخصصة للمهمة (أو تبقى النغمة العامة)"
                            else "تم تخصيص نغمة لهذه المهمة ✓"
                        )
                    }
                    if (customToneUri.isNotBlank()) {
                        TextButton(onClick = { customToneUri = "" }) {
                            Text("النغمة العامة", color = CoralAlert)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            ScheduleItemEntity(
                                id = initial?.id ?: 0,
                                title = title.trim(),
                                dayOfWeek = dayOfWeek.trim(),
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                location = location.trim(),
                                sectionFilter = sectionFilter,
                                itemType = initial?.itemType ?: "STUDY_TASK",
                                recurrence = recurrence,
                                timerMinutes = timerMinutesText.toIntOrNull() ?: 90,
                                customToneUri = customToneUri,
                                alarmEnabled = true
                            )
                        )
                    }
                },
                shape = CircleShape
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
