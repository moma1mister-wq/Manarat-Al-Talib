package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AdhkarAlarmEntity
import com.example.data.local.CatalogItemEntity
import com.example.notifications.AlarmCategory
import com.example.notifications.NotificationAndAlarmHelper
import com.example.ui.components.CustomSectionIcon
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.components.SectionIconType
import com.example.ui.components.Time12hFormatter
import com.example.ui.components.TimePicker12hDialog
import com.example.ui.components.TimePicker12hField
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSoft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

val ADHKAR_QUICK_PRESETS: List<String> = listOf(
    "أذكار الصباح",
    "أذكار المساء",
    "أذكار السفر",
    "أذكار النوم",
    "أذكار الاستيقاظ",
    "أذكار بعد الصلاة",
    "كل الأذكار"
)

fun filterHisnAdhkarByPreset(items: List<CatalogItemEntity>, preset: String): List<CatalogItemEntity> {
    return when (preset) {
        "أذكار الصباح" -> items.filter { item ->
            val isMorningOrCombined = item.title.contains("باب 1:") ||
                item.title.contains("أذكار الصباح") ||
                item.category.contains("الصباح")
            val isEveningOnly = item.contentOrDescription.contains("إذا أمسى)") ||
                item.contentOrDescription.contains("إذا أمسى).") ||
                item.title.contains("المساء فقط")
            isMorningOrCombined && !isEveningOnly
        }
        "أذكار المساء" -> items.filter { item ->
            val isEveningOrCombined = item.title.contains("باب 1:") ||
                item.title.contains("أذكار المساء") ||
                item.category.contains("المساء")
            val isMorningOnly = item.contentOrDescription.contains("إذا أصبحَ)") ||
                item.contentOrDescription.contains("إذا أصبحَ).") ||
                item.contentOrDescription.contains("إذا أصبح)") ||
                item.title.contains("الصباح فقط")
            isEveningOrCombined && !isMorningOnly
        }
        "أذكار السفر" -> items.filter { item ->
            item.category.contains("السفر") ||
                item.title.contains("السفر") ||
                item.title.contains("المسافر") ||
                item.title.contains("الركوب") ||
                item.title.contains("المركوب")
        }
        "أذكار النوم" -> items.filter { item ->
            item.title.contains("أذكار النوم") ||
                item.title.contains("الفزع في النوم") ||
                item.title.contains("تقلب ليلا") ||
                (item.category.contains("النوم") && !item.title.contains("الاستيقاظ"))
        }
        "أذكار الاستيقاظ" -> items.filter { item ->
            item.title.contains("الاستيقاظ")
        }
        "أذكار بعد الصلاة" -> items.filter { item ->
            item.title.contains("الأذكار بعد السلام من الصلاة") ||
                item.title.contains("الصلاة على النبي بعد التشهد") ||
                (item.category.contains("الصلاة") && item.title.contains("بعد"))
        }
        else -> items
    }
}

@Composable
fun PricesAndHisnScreen(
    catalogItems: List<CatalogItemEntity>,
    adhkarAlarms: List<AdhkarAlarmEntity> = emptyList(),
    onSaveItem: (CatalogItemEntity) -> Unit,
    onDeleteItem: (CatalogItemEntity) -> Unit,
    onSaveAdhkarAlarm: (AdhkarAlarmEntity) -> Unit = {},
    onDeleteAdhkarAlarm: (AdhkarAlarmEntity) -> Unit = {},
    onTestAdhkarTone: () -> Unit = {}
) {
    // Modes: "HISN_PDF" (الأذكار وحصن المسلم - تبدأ مباشرة بأذكار الصباح), "ALARMS_SEBHA", "RAW_PDF", "PRICE_LIST"
    var activeSectionMode by remember { mutableStateOf("HISN_PDF") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAdhkarPreset by remember { mutableStateOf("أذكار الصباح") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var activeFocusIndex by remember { mutableIntStateOf(0) }
    var editingItem by remember { mutableStateOf<CatalogItemEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddAlarmDialog by remember { mutableStateOf(false) }
    var editingAlarmForTime by remember { mutableStateOf<AdhkarAlarmEntity?>(null) }

    // Touch counter state per item ID for Hisn Al-Muslim items
    val touchCounts = remember { mutableStateMapOf<Int, Int>() }

    // Electronic Sebha state
    val sebhaPhrases = listOf(
        "سبحان الله وبحمده، سبحان الله العظيم",
        "أستغفر الله العظيم وأتوب إليه",
        "لا إله إلا الله وحده لا شريك له",
        "اللهم صلِّ وسلم على نبينا محمد",
        "لا حول ولا قوة إلا بالله العلي العظيم"
    )
    var selectedSebhaPhrase by remember { mutableStateOf(sebhaPhrases.first()) }
    var sebhaCount by remember { mutableIntStateOf(0) }
    var sebhaTarget by remember { mutableIntStateOf(33) }
    var totalSebhaSession by remember { mutableIntStateOf(0) }

    val hisnItems = remember(catalogItems) {
        catalogItems.filter { it.recordType == "HISN_PDF" }
    }

    val priceItems = remember(catalogItems) {
        catalogItems.filter { it.recordType == "PRICE_LIST" }
    }

    val activeSourceList = if (activeSectionMode == "PRICE_LIST") priceItems else hisnItems

    val categories = remember(activeSourceList) {
        listOf("الكل") + activeSourceList.map { it.category }.distinct()
    }

    val filteredItems = remember(
        activeSectionMode,
        activeSourceList,
        searchQuery,
        selectedAdhkarPreset,
        selectedCategory
    ) {
        if (activeSectionMode == "PRICE_LIST") {
            activeSourceList.filter { item ->
                (selectedCategory == "الكل" || item.category == selectedCategory) &&
                    (searchQuery.isBlank() ||
                        item.title.contains(searchQuery, ignoreCase = true) ||
                        item.contentOrDescription.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true) ||
                        item.unitOrReference.contains(searchQuery, ignoreCase = true))
            }
        } else {
            val baseHisn = if (searchQuery.isNotBlank()) {
                activeSourceList
            } else if (selectedAdhkarPreset == "كل الأذكار") {
                if (selectedCategory == "الكل") activeSourceList else activeSourceList.filter { it.category == selectedCategory }
            } else {
                filterHisnAdhkarByPreset(activeSourceList, selectedAdhkarPreset)
            }
            baseHisn.filter { item ->
                searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.contentOrDescription.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.unitOrReference.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val adhkarOnlyAlarms = remember(adhkarAlarms) {
        adhkarAlarms.filter { !it.category.contains("التمارين") }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PurpleWaveBackground(height = 230.dp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CustomSectionIcon(type = SectionIconType.ADHKAR, size = 48.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الأذكار وحصن المسلم",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mode Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = activeSectionMode == "HISN_PDF",
                                onClick = {
                                    activeSectionMode = "HISN_PDF"
                                    searchQuery = ""
                                    selectedAdhkarPreset = "أذكار الصباح"
                                    activeFocusIndex = 0
                                },
                                label = { Text("الأذكار التفاعلية 📿") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("mode_hisn_chip")
                            )
                            FilterChip(
                                selected = activeSectionMode == "ALARMS_SEBHA",
                                onClick = { activeSectionMode = "ALARMS_SEBHA" },
                                label = { Text("السبحة ومنبهات الأذكار 🔔") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("mode_alarms_sebha_chip")
                            )
                            FilterChip(
                                selected = activeSectionMode == "RAW_PDF",
                                onClick = { activeSectionMode = "RAW_PDF" },
                                label = { Text("كتاب حصن المسلم PDF (96 ص) 📖") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("mode_raw_pdf_chip")
                            )
                            FilterChip(
                                selected = activeSectionMode == "PRICE_LIST",
                                onClick = {
                                    activeSectionMode = "PRICE_LIST"
                                    selectedCategory = "الكل"
                                    searchQuery = ""
                                },
                                label = { Text("الأسعار والمستلزمات 💰") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("mode_price_list_chip")
                            )
                        }

                        if (activeSectionMode == "HISN_PDF") {
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "اختر قسم الأذكار للبدء والإتمام فوراً:",
                                style = MaterialTheme.typography.labelLarge,
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ADHKAR_QUICK_PRESETS.forEachIndexed { idx, preset ->
                                    val isSelected = selectedAdhkarPreset == preset && searchQuery.isBlank()
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedAdhkarPreset = preset
                                            selectedCategory = "الكل"
                                            searchQuery = ""
                                            val nextItems = filterHisnAdhkarByPreset(hisnItems, preset)
                                            val firstIncomplete = nextItems.indexOfFirst { item ->
                                                (touchCounts[item.id] ?: 0) < item.targetCount.coerceAtLeast(1)
                                            }
                                            activeFocusIndex = if (firstIncomplete >= 0) firstIncomplete else 0
                                        },
                                        label = {
                                            Text(
                                                text = preset,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldPrimary,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("adhkar_preset_$idx")
                                    )
                                }
                            }
                        }

                        if (activeSectionMode == "HISN_PDF" || activeSectionMode == "PRICE_LIST") {
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(if (activeSectionMode == "HISN_PDF") "بحث في جميع أذكار حصن المسلم..." else "بحث")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = "بحث", tint = PurplePrimary)
                                },
                                singleLine = true,
                                shape = CircleShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("adhkar_search_input")
                            )

                            if (activeSectionMode == "PRICE_LIST" || (selectedAdhkarPreset == "كل الأذكار" && searchQuery.isBlank())) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    categories.forEach { cat ->
                                        FilterChip(
                                            selected = selectedCategory == cat,
                                            onClick = { selectedCategory = cat },
                                            label = { Text(cat) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PurplePrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (activeSectionMode) {
                "RAW_PDF" -> {
                    item {
                        OfflineNativePdfViewerCard()
                    }
                }

                "ALARMS_SEBHA" -> {
                    // Electronic Sebha Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "السبحة الإلكترونية",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PurplePrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = EmeraldPrimary.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "مجموع الجلسة: $totalSebhaSession",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Phrase chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    sebhaPhrases.forEach { phrase ->
                                        FilterChip(
                                            selected = selectedSebhaPhrase == phrase,
                                            onClick = {
                                                selectedSebhaPhrase = phrase
                                                sebhaCount = 0
                                            },
                                            label = { Text(phrase) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PurplePrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                // Target chips (33 / 100 / 1000)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("الهدف:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    listOf(33, 100, 1000).forEach { target ->
                                        FilterChip(
                                            selected = sebhaTarget == target,
                                            onClick = {
                                                sebhaTarget = target
                                                sebhaCount = 0
                                            },
                                            label = { Text("$target مرة") }
                                        )
                                    }
                                    IconButton(onClick = { sebhaCount = 0 }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "تصفير", tint = PurplePrimary)
                                    }
                                }

                                // Big Circular Tap Button
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (sebhaCount >= sebhaTarget) EmeraldPrimary else PurplePrimary
                                        )
                                        .border(4.dp, GoldAccent, CircleShape)
                                        .clickable {
                                            if (sebhaCount >= sebhaTarget) {
                                                sebhaCount = 1
                                            } else {
                                                sebhaCount += 1
                                            }
                                            totalSebhaSession += 1
                                        }
                                        .testTag("sebha_tap_circle"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$sebhaCount / $sebhaTarget",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (sebhaCount >= sebhaTarget) "✓ اكتمل الهدف!" else "اضغط للتسبيح",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Text(
                                    text = selectedSebhaPhrase,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                LinearProgressIndicator(
                                    progress = { (sebhaCount.toFloat() / sebhaTarget.toFloat()).coerceIn(0f, 1f) },
                                    color = EmeraldPrimary,
                                    trackColor = PurpleSoft.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    // Daily Adhkar Alarms Card (Image 2 style: Adhkar-only essential alarms)
                    item {
                        DailyAdhkarAlarmsCard(
                            alarms = adhkarOnlyAlarms,
                            showSebhaButton = false,
                            onOpenSebhaClick = { activeSectionMode = "ALARMS_SEBHA" },
                            onAddAlarmClick = { showAddAlarmDialog = true },
                            onEditAlarmTime = { editingAlarmForTime = it },
                            onToggleAlarm = { alarm, enabled ->
                                onSaveAdhkarAlarm(alarm.copy(isEnabled = enabled))
                            },
                            onDeleteAlarm = onDeleteAdhkarAlarm,
                            onTestTone = onTestAdhkarTone,
                            onStopTone = { NotificationAndAlarmHelper.stopAlertSound() }
                        )
                    }
                }

                else -> {
                    // HISN_PDF (Guided Section Flow + Checklist) or PRICE_LIST Mode
                    val sectionDoneCount = if (activeSectionMode == "HISN_PDF") {
                        filteredItems.count { (touchCounts[it.id] ?: 0) >= it.targetCount.coerceAtLeast(1) }
                    } else 0

                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PurplePrimary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when {
                                            activeSectionMode == "PRICE_LIST" -> "دليل الأسعار والمستلزمات الدراسية:"
                                            searchQuery.isNotBlank() -> "نتائج البحث في حصن المسلم:"
                                            selectedAdhkarPreset == "كل الأذكار" -> "كتاب حصن المسلم كاملاً (١٣٢ باباً):"
                                            else -> "$selectedAdhkarPreset • تم إنجاز $sectionDoneCount من ${filteredItems.size}"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (activeSectionMode == "PRICE_LIST") "${filteredItems.size} صنف" else "${filteredItems.size} ذكر",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        if (activeSectionMode == "HISN_PDF" && filteredItems.any { (touchCounts[it.id] ?: 0) > 0 }) {
                                            TextButton(
                                                onClick = {
                                                    filteredItems.forEach { touchCounts.remove(it.id) }
                                                    activeFocusIndex = 0
                                                },
                                                modifier = Modifier.testTag("adhkar_reset_section_btn")
                                            ) {
                                                Text("تصفير القسم", color = PurplePrimary, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                            }
                                        }
                                    }
                                }

                                if (activeSectionMode == "HISN_PDF" && filteredItems.isNotEmpty() && searchQuery.isBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = {
                                            (sectionDoneCount.toFloat() / filteredItems.size.toFloat()).coerceIn(0f, 1f)
                                        },
                                        color = EmeraldPrimary,
                                        trackColor = PurplePrimary.copy(alpha = 0.16f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    // Guided Active Dhikr Focus Card or Section Completion Banner
                    if (activeSectionMode == "HISN_PDF" &&
                        searchQuery.isBlank() &&
                        selectedAdhkarPreset != "كل الأذكار" &&
                        filteredItems.isNotEmpty()
                    ) {
                        item {
                            if (sectionDoneCount >= filteredItems.size) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("adhkar_section_completed_card"),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.14f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "تم الإنجاز",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Text(
                                            text = "ما شاء الله! أتممت $selectedAdhkarPreset بالكامل ✓",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    filteredItems.forEach { touchCounts.remove(it.id) }
                                                    activeFocusIndex = 0
                                                },
                                                shape = CircleShape,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("إعادة القسم", maxLines = 1, softWrap = false)
                                            }
                                            val nextPreset = when (selectedAdhkarPreset) {
                                                "أذكار الصباح" -> "أذكار المساء"
                                                "أذكار المساء" -> "أذكار النوم"
                                                else -> "أذكار الصباح"
                                            }
                                            Button(
                                                onClick = {
                                                    selectedAdhkarPreset = nextPreset
                                                    activeFocusIndex = 0
                                                },
                                                shape = CircleShape,
                                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("التالي: $nextPreset", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            } else {
                                val safeIdx = activeFocusIndex.coerceIn(0, filteredItems.lastIndex)
                                val focusItem = filteredItems[safeIdx]
                                val focusTarget = focusItem.targetCount.coerceAtLeast(1)
                                val focusCount = touchCounts[focusItem.id] ?: 0
                                val focusDone = focusCount >= focusTarget

                                val advanceToNextIncomplete: () -> Unit = {
                                    val nextAfter = ((safeIdx + 1) until filteredItems.size).firstOrNull { idx ->
                                        val it = filteredItems[idx]
                                        (touchCounts[it.id] ?: 0) < it.targetCount.coerceAtLeast(1)
                                    }
                                    val wrapBefore = (0 until safeIdx).firstOrNull { idx ->
                                        val it = filteredItems[idx]
                                        (touchCounts[it.id] ?: 0) < it.targetCount.coerceAtLeast(1)
                                    }
                                    activeFocusIndex = nextAfter ?: wrapBefore ?: safeIdx
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, PurplePrimary.copy(alpha = 0.28f), RoundedCornerShape(26.dp))
                                        .testTag("adhkar_guided_focus_card"),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = EmeraldPrimary.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = "$selectedAdhkarPreset • ${safeIdx + 1} من ${filteredItems.size}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        if (safeIdx > 0) activeFocusIndex = safeIdx - 1
                                                    },
                                                    enabled = safeIdx > 0
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "الذكر السابق"
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (safeIdx < filteredItems.lastIndex) activeFocusIndex = safeIdx + 1
                                                    },
                                                    enabled = safeIdx < filteredItems.lastIndex
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = "الذكر التالي"
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = focusItem.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = focusItem.contentOrDescription,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontSize = 18.sp,
                                                    lineHeight = 30.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(14.dp)
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                val updated = (focusCount + 1).coerceAtMost(focusTarget)
                                                touchCounts[focusItem.id] = updated
                                                if (updated >= focusTarget) {
                                                    advanceToNextIncomplete()
                                                }
                                            },
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (focusDone) EmeraldPrimary else PurplePrimary
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp)
                                                .testTag("adhkar_guided_tap_button")
                                        ) {
                                            Text(
                                                text = if (focusDone) {
                                                    "✓ تم هذا الذكر ($focusTarget / $focusTarget)"
                                                } else {
                                                    "اضغط للعدّ والتسبيح ($focusCount / $focusTarget)"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (focusTarget - focusCount >= 10) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val updated = (focusCount + 10).coerceAtMost(focusTarget)
                                                        touchCounts[focusItem.id] = updated
                                                        if (updated >= focusTarget) {
                                                            advanceToNextIncomplete()
                                                        }
                                                    },
                                                    shape = CircleShape,
                                                    modifier = Modifier.weight(0.75f)
                                                ) {
                                                    Text("+10", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    touchCounts[focusItem.id] = focusTarget
                                                    advanceToNextIncomplete()
                                                },
                                                shape = CircleShape,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = EmeraldPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "إتمام والانتقال للتالي",
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

                    items(filteredItems, key = { it.id }) { item ->
                        val itemDone = item.recordType != "PRICE_LIST" &&
                            (touchCounts[item.id] ?: 0) >= item.targetCount.coerceAtLeast(1)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (itemDone) {
                                        Modifier.border(1.2.dp, EmeraldPrimary.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                                    } else {
                                        Modifier
                                    }
                                ),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (itemDone) {
                                    EmeraldPrimary.copy(alpha = 0.06f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (itemDone) 1.dp else 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = CircleShape,
                                            color = PurplePrimary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = item.category,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = PurplePrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                editingItem = item
                                                showEditDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = PurplePrimary)
                                        }
                                        IconButton(onClick = { onDeleteItem(item) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = CoralAlert)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.contentOrDescription,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.unitOrReference,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (item.recordType == "PRICE_LIST") {
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldPrimary.copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = "${"%.0f".format(item.priceEgp)} ج.م",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                            )
                                        }
                                    } else {
                                        val count = touchCounts[item.id] ?: 0
                                        val target = item.targetCount.coerceAtLeast(1)
                                        val done = count >= target
                                        Button(
                                            onClick = {
                                                touchCounts[item.id] = if (done) 0 else count + 1
                                            },
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (done) EmeraldPrimary else PurplePrimary
                                            )
                                        ) {
                                            if (done) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("✓ اكتمل ($target/$target)")
                                            } else {
                                                Text("عدّاد اللمس: $count / $target")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (activeSectionMode == "HISN_PDF") {
                        item {
                            DailyAdhkarAlarmsCard(
                                alarms = adhkarOnlyAlarms,
                                showSebhaButton = true,
                                onOpenSebhaClick = { activeSectionMode = "ALARMS_SEBHA" },
                                onAddAlarmClick = { showAddAlarmDialog = true },
                                onEditAlarmTime = { editingAlarmForTime = it },
                                onToggleAlarm = { alarm, enabled ->
                                    onSaveAdhkarAlarm(alarm.copy(isEnabled = enabled))
                                },
                                onDeleteAlarm = onDeleteAdhkarAlarm,
                                onTestTone = onTestAdhkarTone,
                                onStopTone = { NotificationAndAlarmHelper.stopAlertSound() }
                            )
                        }
                    }
                }
            }
        }

        if (activeSectionMode == "HISN_PDF" || activeSectionMode == "PRICE_LIST") {
            FloatingActionButton(
                onClick = {
                    editingItem = null
                    showEditDialog = true
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_adhkar_item_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeSectionMode == "PRICE_LIST") "إضافة صنف / سعر" else "إضافة ذكر / دعاء",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        val isPriceRecord = (editingItem?.recordType ?: activeSectionMode) == "PRICE_LIST"
        var title by remember { mutableStateOf(editingItem?.title ?: "") }
        var category by remember {
            mutableStateOf(
                editingItem?.category ?: when {
                    isPriceRecord -> "كتب ومراجع جامعية"
                    selectedAdhkarPreset != "كل الأذكار" -> selectedAdhkarPreset
                    else -> "أذكار الصباح والمساء"
                }
            )
        }
        var description by remember { mutableStateOf(editingItem?.contentOrDescription ?: "") }
        var priceText by remember { mutableStateOf((editingItem?.priceEgp ?: 150.0).toInt().toString()) }
        var targetCountText by remember { mutableStateOf((editingItem?.targetCount ?: 3).toString()) }
        var unitRef by remember {
            mutableStateOf(
                editingItem?.unitOrReference ?: if (isPriceRecord) "نسخة مطبوعة" else "حصن المسلم"
            )
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    if (isPriceRecord) {
                        if (editingItem == null) "إضافة صنف وسعر جديد 💰" else "تعديل الصنف والسعر"
                    } else {
                        if (editingItem == null) "إضافة ذكر أو دعاء جديد 📿" else "تعديل الذكر"
                    },
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isPriceRecord) "اسم الصنف / الكتاب" else "العنوان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(if (isPriceRecord) "الوصف" else "النص") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isPriceRecord) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("السعر (ج.م)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = targetCountText,
                            onValueChange = { targetCountText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("العدد") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = unitRef,
                        onValueChange = { unitRef = it },
                        label = { Text(if (isPriceRecord) "الوحدة" else "المرجع") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSaveItem(
                                CatalogItemEntity(
                                    id = editingItem?.id ?: 0,
                                    recordType = if (isPriceRecord) "PRICE_LIST" else "HISN_PDF",
                                    title = title.trim(),
                                    category = category.trim(),
                                    contentOrDescription = description.trim(),
                                    priceEgp = if (isPriceRecord) (priceText.toDoubleOrNull() ?: 0.0) else 0.0,
                                    unitOrReference = unitRef.trim(),
                                    targetCount = targetCountText.toIntOrNull()?.coerceAtLeast(1) ?: 3,
                                    pdfPageNumber = editingItem?.pdfPageNumber ?: 1
                                )
                            )
                            showEditDialog = false
                        }
                    },
                    shape = CircleShape
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showAddAlarmDialog) {
        var newAlarmTitle by remember { mutableStateOf("") }
        var newAlarmTime by remember { mutableStateOf("06:00") }
        AlertDialog(
            onDismissRequest = { showAddAlarmDialog = false },
            title = { Text("إضافة منبه ذكر جديد", color = PurplePrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newAlarmTitle,
                        onValueChange = { newAlarmTitle = it },
                        label = { Text("الاسم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TimePicker12hField(
                        label = "وقت المنبه (نظام 12 ساعة)",
                        time24 = newAlarmTime,
                        onTime24Changed = { newAlarmTime = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAlarmTitle.isNotBlank()) {
                            onSaveAdhkarAlarm(
                                AdhkarAlarmEntity(
                                    title = newAlarmTitle.trim(),
                                    category = "أذكار مخصصة",
                                    alarmTime = newAlarmTime.trim(),
                                    isEnabled = true,
                                    toneName = "adhkar_tone.mp3",
                                    isFixedCore = false
                                )
                            )
                            showAddAlarmDialog = false
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("حفظ وجدولة المنبه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAlarmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    editingAlarmForTime?.let { alarm ->
        TimePicker12hDialog(
            title = "تعديل وقت ${alarm.title}",
            initialTime24 = alarm.alarmTime,
            onDismiss = { editingAlarmForTime = null },
            onConfirmTime24 = { newTime24 ->
                onSaveAdhkarAlarm(alarm.copy(alarmTime = newTime24))
                editingAlarmForTime = null
            }
        )
    }
}

@Composable
private fun DailyAdhkarAlarmsCard(
    alarms: List<AdhkarAlarmEntity>,
    showSebhaButton: Boolean,
    onOpenSebhaClick: () -> Unit,
    onAddAlarmClick: () -> Unit,
    onEditAlarmTime: (AdhkarAlarmEntity) -> Unit,
    onToggleAlarm: (AdhkarAlarmEntity, Boolean) -> Unit,
    onDeleteAlarm: (AdhkarAlarmEntity) -> Unit,
    onTestTone: () -> Unit,
    onStopTone: () -> Unit
) {
    val essentialTitles = remember {
        setOf("أذكار الصباح", "أذكار المساء", "أذكار النوم", "أذكار السفر")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("adhkar_daily_alarms_card"),
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
                    CustomSectionIcon(type = SectionIconType.ADHKAR, size = 42.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "الأذكار اليومية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTestTone) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "تجربة النغمة",
                            tint = PurplePrimary
                        )
                    }
                    IconButton(onClick = onAddAlarmClick) {
                        Icon(
                            imageVector = Icons.Default.AddAlarm,
                            contentDescription = "إضافة",
                            tint = EmeraldPrimary
                        )
                    }
                }
            }

            if (showSebhaButton) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenSebhaClick,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adhkar_open_sebha_button")
                ) {
                    Text("سبحة الأذكار")
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onTestTone,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تشغيل")
                    }
                    TextButton(onClick = onStopTone) {
                        Text("إيقاف", color = CoralAlert, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            alarms.forEach { alarm ->
                val isCoreAdhkar = alarm.isFixedCore || alarm.title in essentialTitles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEditAlarmTime(alarm) }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                        .testTag("adhkar_alarm_row_${alarm.title}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alarm.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${Time12hFormatter.format24To12Ar(alarm.alarmTime)} (اضغط لتعديل الوقت)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isCoreAdhkar) {
                            IconButton(onClick = { onDeleteAlarm(alarm) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = CoralAlert)
                            }
                        }
                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = { enabled ->
                                onToggleAlarm(alarm, enabled)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders the actual attached 96-page PDF (`assets/hisn_almuslim.pdf`) offline using Android's PdfRenderer.
 */
@Composable
private fun OfflineNativePdfViewerCard() {
    val context = LocalContext.current
    var pageIndex by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(96) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pageIndex) {
        withContext(Dispatchers.IO) {
            try {
                val cacheFile = File(context.cacheDir, "hisn_almuslim_cached.pdf")
                if (!cacheFile.exists()) {
                    context.assets.open("hisn_almuslim.pdf").use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                val pfd = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                totalPages = renderer.pageCount.coerceAtLeast(1)
                val safeIdx = pageIndex.coerceIn(0, totalPages - 1)
                val page = renderer.openPage(safeIdx)
                val bmp = Bitmap.createBitmap(
                    (page.width * 2).coerceAtMost(1200),
                    (page.height * 2).coerceAtMost(1700),
                    Bitmap.Config.ARGB_8888
                )
                bmp.eraseColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                pfd.close()
                renderedBitmap = bmp
            } catch (_: Exception) {
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (pageIndex > 0) pageIndex -= 1 },
                    shape = CircleShape
                ) {
                    Text("الصفحة السابقة")
                }
                Text(
                    text = "صفحة ${pageIndex + 1} من $totalPages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
                Button(
                    onClick = { if (pageIndex < totalPages - 1) pageIndex += 1 },
                    shape = CircleShape
                ) {
                    Text("الصفحة التالية")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val bmp = renderedBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "صفحة ${pageIndex + 1} من حصن المسلم",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("جاري عرض صفحة كتاب حصن المسلم PDF...")
                    }
                }
            }
        }
    }
}
