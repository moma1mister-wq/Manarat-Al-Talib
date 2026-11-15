package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MushafPageContent
import com.example.data.MushafVerse
import com.example.data.QuranOfflineData
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MushafNightBg
import com.example.ui.theme.PurplePrimary
import kotlinx.coroutines.launch

private enum class QuranNavSectionTab(val labelAr: String) {
    SURAH("السورة"),
    JUZ("الجزء"),
    RUB("الربع"),
    PAGE("الصفحة"),
    WORD_SEARCH("بحث بالكلمات")
}

@Composable
fun QuranScreen(
    lastReadPage: Int,
    bookmarkedPages: Set<Int>,
    isNightMode: Boolean,
    onSavePage: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onToggleNightMode: (Boolean) -> Unit
) {
    val context = LocalContext.current
    remember(context) {
        QuranOfflineData.ensureLoaded(context)
        true
    }

    val pagerState = rememberPagerState(
        initialPage = (lastReadPage - 1).coerceIn(0, 603),
        pageCount = { 604 }
    )
    val coroutineScope = rememberCoroutineScope()

    var activeNavigatorTab by remember { mutableStateOf<QuranNavSectionTab?>(null) }
    var showWritingAssistantDialog by remember { mutableStateOf(false) }
    var fontSizeSp by remember { mutableIntStateOf(25) }

    LaunchedEffect(pagerState.currentPage) {
        onSavePage(pagerState.currentPage + 1)
    }

    val currentPageNumber = pagerState.currentPage + 1
    val isBookmarked = bookmarkedPages.contains(currentPageNumber)
    val currentPageData = remember(currentPageNumber) {
        QuranOfflineData.getPageContent(currentPageNumber)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isNightMode) MushafNightBg else MaterialTheme.colorScheme.background)
    ) {
        // Ultra-slim Single-Line Top Control Bar (~50dp) so the Mushaf page occupies ~92% of screen height
        Surface(
            color = PurplePrimary,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { activeNavigatorTab = QuranNavSectionTab.SURAH },
                    modifier = Modifier.testTag("quran_index_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "القائمة: السورة والجزء والربع والصفحة والبحث",
                        tint = Color.White
                    )
                }

                val currentSurahLabel = currentPageData.surahsOnPage.firstOrNull()?.nameAr ?: ""
                Text(
                    text = "ص ${QuranOfflineData.toArabicDigits(currentPageNumber)} • ج ${QuranOfflineData.toArabicDigits(currentPageData.juzNumber)} • $currentSurahLabel",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { showWritingAssistantDialog = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("quran_writing_assistant_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "مساعد كتابة الآيات",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "كتابة",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                IconButton(onClick = { onToggleBookmark(currentPageNumber) }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "علامة مرجعية",
                        tint = if (isBookmarked) GoldAccent else Color.White
                    )
                }
                IconButton(onClick = { onToggleNightMode(!isNightMode) }) {
                    Icon(
                        imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "وضع القراءة الليلي",
                        tint = Color.White
                    )
                }
            }
        }

        // 604-Page Full-Screen Horizontal Pager (page-by-page without vertical verse scrolling)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("quran_horizontal_pager")
        ) { pageIdx ->
            val pageData = remember(pageIdx) { QuranOfflineData.getPageContent(pageIdx + 1) }
            MadaniMushafPageView(
                page = pageData,
                isNightMode = isNightMode,
                fontSizeSp = fontSizeSp,
                onOpenWritingForPage = { showWritingAssistantDialog = true }
            )
        }
    }

    // Interactive Quran Writing & Memorization Practice Assistant Modal
    if (showWritingAssistantDialog) {
        QuranWritingAssistantDialog(
            pageVerses = currentPageData.verses,
            pageNumber = currentPageNumber,
            onDismiss = { showWritingAssistantDialog = false }
        )
    }

    // Unified Navigation, Font Scale & Word Search inside Three-Lines Menu (السورة | الجزء | الربع | الصفحة | بحث بالكلمات)
    if (activeNavigatorTab != null) {
        QuranNavigatorAndSearchDialog(
            initialTab = activeNavigatorTab!!,
            currentPage = currentPageNumber,
            bookmarkedPages = bookmarkedPages,
            fontSizeSp = fontSizeSp,
            onChangeFontSize = { fontSizeSp = it.coerceIn(14, 34) },
            onNavigateToPage = { targetPage ->
                coroutineScope.launch {
                    pagerState.scrollToPage((targetPage - 1).coerceIn(0, 603))
                }
                activeNavigatorTab = null
            },
            onDismiss = { activeNavigatorTab = null }
        )
    }
}

@Composable
private fun QuranNavigatorAndSearchDialog(
    initialTab: QuranNavSectionTab,
    currentPage: Int,
    bookmarkedPages: Set<Int>,
    fontSizeSp: Int,
    onChangeFontSize: (Int) -> Unit,
    onNavigateToPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var directPageInput by remember { mutableStateOf("") }

    val matchingSurahs = remember(searchQuery) {
        QuranOfflineData.searchSurahsOffline(searchQuery)
    }
    val matchingJuz = remember(searchQuery) {
        QuranOfflineData.searchJuzOffline(searchQuery)
    }
    val matchingRubs = remember(searchQuery) {
        QuranOfflineData.searchRubsOffline(searchQuery)
    }
    val allPageSummaries = remember {
        QuranOfflineData.getAllPageSummaries()
    }
    val matchingPages = remember(searchQuery, allPageSummaries) {
        val clean = QuranOfflineData.normalizeArabicForWritingAndSearch(searchQuery)
        val num = searchQuery.trim().toIntOrNull()
        if (clean.isBlank()) allPageSummaries
        else allPageSummaries.filter { p ->
            (num != null && p.pageNumber == num) ||
                QuranOfflineData.normalizeArabicForWritingAndSearch(p.surahsSummary).contains(clean)
        }
    }
    val matchingVerses = remember(searchQuery) {
        if (searchQuery.trim().length < 2) emptyList()
        else QuranOfflineData.searchVersesOffline(searchQuery, limit = 60)
    }
    val directPageFromQuery = searchQuery.trim().toIntOrNull()?.takeIf { it in 1..604 }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header + Font Size Controls inside the Three-Lines Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الوصول والبحث في المصحف 📖",
                            style = MaterialTheme.typography.titleMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "السورة، الجزء، الربع، الصفحة، والبحث بالكلمات",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onChangeFontSize(fontSizeSp - 1) }) {
                            Icon(
                                imageVector = Icons.Default.TextDecrease,
                                contentDescription = "تصغير حجم الخط",
                                tint = PurplePrimary
                            )
                        }
                        IconButton(onClick = { onChangeFontSize(fontSizeSp + 1) }) {
                            Icon(
                                imageVector = Icons.Default.TextIncrease,
                                contentDescription = "تكبير حجم الخط",
                                tint = PurplePrimary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Always-Accessible Word & Division Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("بحث بالكلمات أو اسم السورة أو رقم...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = PurplePrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح البحث")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quran_navigator_search_input")
                )

                // Direct Page Jump Banner if user typed a number 1..604 in the search box
                if (directPageFromQuery != null) {
                    Button(
                        onClick = { onNavigateToPage(directPageFromQuery) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "الانتقال مباشرة إلى صفحة ${QuranOfflineData.toArabicDigits(directPageFromQuery)} ($directPageFromQuery) ←",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 5 Navigation Section Tabs: السورة | الجزء | الربع | الصفحة | بحث بالكلمات
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuranNavSectionTab.entries.forEach { tab ->
                        val badgeSuffix = if (tab == QuranNavSectionTab.WORD_SEARCH && matchingVerses.isNotEmpty()) {
                            " (${matchingVerses.size})"
                        } else ""
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = {
                                Text(
                                    text = "${tab.labelAr}$badgeSuffix",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (tab == QuranNavSectionTab.WORD_SEARCH) EmeraldPrimary else PurplePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("quran_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                // Helpful quick cross-link if user typed words while on another tab
                if (selectedTab != QuranNavSectionTab.WORD_SEARCH && matchingVerses.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPrimary.copy(alpha = 0.14f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTab = QuranNavSectionTab.WORD_SEARCH }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔍 وُجدت ${matchingVerses.size} آية مطابقة لكلمات البحث",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "عرض الآيات ←",
                                style = MaterialTheme.typography.labelMedium,
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Tab Content List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedTab) {
                        QuranNavSectionTab.SURAH -> {
                            items(matchingSurahs, key = { "surah_${it.number}" }) { surah ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToPage(surah.startPage) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = PurplePrimary,
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${surah.number}",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = "سورة ${surah.nameAr}",
                                                    fontFamily = AmiriQuranFontFamily,
                                                    fontSize = 19.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${if (surah.isMakki) "مكية" else "مدنية"} • ${surah.ayahCount} آية • جزء ${surah.juzStart}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldPrimary.copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = "ص ${surah.startPage}",
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        QuranNavSectionTab.JUZ -> {
                            items(matchingJuz, key = { "juz_${it.juzNumber}" }) { juz ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToPage(juz.startPage) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "الجزء ${QuranOfflineData.toArabicDigits(juz.juzNumber)} (${juz.juzNumber})",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = PurplePrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = EmeraldPrimary.copy(alpha = 0.16f)
                                            ) {
                                                Text(
                                                    text = "سورة ${juz.firstSurahNameAr} • ص ${juz.startPage}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        if (juz.openingText.isNotBlank()) {
                                            Text(
                                                text = juz.openingText,
                                                fontFamily = AmiriQuranFontFamily,
                                                fontSize = 17.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        QuranNavSectionTab.RUB -> {
                            items(matchingRubs, key = { "rub_${it.rubNumber}" }) { rub ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToPage(rub.pageNumber) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = EmeraldPrimary
                                                ) {
                                                    Text(
                                                        text = "ربع ${rub.rubNumber}",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "${rub.quarterLabelAr} • حزب ${rub.hizbNumber} • جزء ${rub.juzNumber}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = PurplePrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "ص ${rub.pageNumber}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "سورة ${rub.surahNameAr} • آية ${rub.ayahNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = rub.openingText,
                                            fontFamily = AmiriQuranFontFamily,
                                            fontSize = 18.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        QuranNavSectionTab.PAGE -> {
                            item(key = "page_quick_jump") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = directPageInput,
                                            onValueChange = { input ->
                                                if (input.all { it.isDigit() } && input.length <= 3) {
                                                    directPageInput = input
                                                }
                                            },
                                            label = { Text("اكتب رقم الصفحة (1 - 604)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                val p = directPageInput.toIntOrNull()
                                                if (p != null && p in 1..604) {
                                                    onNavigateToPage(p)
                                                }
                                            },
                                            enabled = (directPageInput.toIntOrNull() in 1..604),
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                            modifier = Modifier.height(52.dp)
                                        ) {
                                            Text("انتقال", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (bookmarkedPages.isNotEmpty()) {
                                        Text(
                                            text = "العلامات المرجعية المحفوظة:",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            bookmarkedPages.sorted().forEach { bPage ->
                                                FilterChip(
                                                    selected = bPage == currentPage,
                                                    onClick = { onNavigateToPage(bPage) },
                                                    label = { Text("🔖 صفحة $bPage", fontWeight = FontWeight.Bold) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            items(matchingPages, key = { "page_${it.pageNumber}" }) { pInfo ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (pInfo.pageNumber == currentPage) {
                                        PurplePrimary.copy(alpha = 0.16f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToPage(pInfo.pageNumber) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "صفحة ${QuranOfflineData.toArabicDigits(pInfo.pageNumber)} (${pInfo.pageNumber})",
                                                fontWeight = FontWeight.Bold,
                                                color = PurplePrimary
                                            )
                                            Text(
                                                text = "سورة ${pInfo.surahsSummary}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "جزء ${pInfo.juzNumber} • حزب ${pInfo.hizbNumber}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        QuranNavSectionTab.WORD_SEARCH -> {
                            if (searchQuery.trim().length < 2) {
                                item(key = "word_search_hints") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "اكتب كلمة أو جملة من القرآن الكريم في مربع البحث بالأعلى (بدون تشكيل) وسيتم عرض جميع الآيات المطابقة فوراً:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "أمثلة سريعة للبحث بالكلمات:",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("الرحمن", "الصبر", "الجنة", "لا اله الا هو", "ادعوني", "الحمد لله").forEach { sample ->
                                                FilterChip(
                                                    selected = false,
                                                    onClick = { searchQuery = sample },
                                                    label = { Text(sample, fontWeight = FontWeight.Bold) }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (matchingSurahs.isNotEmpty() && directPageFromQuery == null) {
                                    items(matchingSurahs.take(3), key = { "ws_s_${it.number}" }) { s ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = PurplePrimary.copy(alpha = 0.12f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onNavigateToPage(s.startPage) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("سورة ${s.nameAr} (${s.ayahCount} آية)", fontWeight = FontWeight.Bold)
                                                Text("صفحة ${s.startPage}", color = PurplePrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                if (matchingVerses.isEmpty()) {
                                    item(key = "word_search_empty") {
                                        Text(
                                            text = "لم يتم العثور على آيات مطابقة لـ «$searchQuery». جرب كتابة كلمة أخرى أو اسم سورة.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CoralAlert,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                } else {
                                    items(matchingVerses, key = { "ws_v_${it.surahNumber}_${it.ayahNumber}" }) { v ->
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onNavigateToPage(v.pageNumber) }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "سورة ${v.surahNameAr} • آية ${v.ayahNumber} • جزء ${v.juzNumber}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = EmeraldPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = PurplePrimary.copy(alpha = 0.14f)
                                                    ) {
                                                        Text(
                                                            text = "ص ${v.pageNumber}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = PurplePrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = v.textUthmani,
                                                    fontFamily = AmiriQuranFontFamily,
                                                    fontSize = 19.sp,
                                                    lineHeight = 32.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Assistant that helps the student write & memorize Quran verses with word-by-word checking and hints.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuranWritingAssistantDialog(
    pageVerses: List<MushafVerse>,
    pageNumber: Int,
    onDismiss: () -> Unit
) {
    val safeVerses = pageVerses.ifEmpty { QuranOfflineData.getPageContent(1).verses }
    var currentVerseIdx by remember { mutableIntStateOf(0) }
    var userInputText by remember { mutableStateOf("") }
    var hideTargetText by remember { mutableStateOf(true) }

    val currentVerse = safeVerses[currentVerseIdx.coerceIn(0, safeVerses.lastIndex)]
    val evaluation = remember(currentVerse.textUthmani, userInputText) {
        QuranOfflineData.evaluateWrittenVerse(currentVerse.textUthmani, userInputText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مساعد كتابة وتسميع الآيات ✍️",
                        style = MaterialTheme.typography.titleMedium,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "سورة ${currentVerse.surahNameAr} • الآية ${currentVerse.ayahNumber} (صفحة $pageNumber)",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldPrimary
                    )
                }
                IconButton(onClick = { hideTargetText = !hideTargetText }) {
                    Icon(
                        imageVector = if (hideTargetText) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "إظهار أو إخفاء الآية للتسميع",
                        tint = PurplePrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mode Chips: Hide vs Show for Dictation/Memorization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = hideTargetText,
                        onClick = { hideTargetText = true },
                        label = { Text("تسميع غيبي (إخفاء الآية)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !hideTargetText,
                        onClick = { hideTargetText = false },
                        label = { Text("تدريب إملائي (إظهار)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Target Verse Card (Visible or Masked with First-Word Prompt)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val words = currentVerse.textUthmani.split(Regex("\\s+"))
                    val firstWord = words.firstOrNull() ?: ""
                    Text(
                        text = if (!hideTargetText) {
                            currentVerse.textUthmani
                        } else {
                            "بداية الآية: « $firstWord ... » (${words.size} كلمات)"
                        },
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = if (!hideTargetText) MaterialTheme.colorScheme.onSurface else PurplePrimary,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Writing TextField
                OutlinedTextField(
                    value = userInputText,
                    onValueChange = { userInputText = it },
                    label = { Text("اكتب الآية") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("quran_writing_input")
                )

                // Accuracy Progress & Hint Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الدقة: ${evaluation.accuracyPercent}% (${evaluation.matchedWordsCount}/${evaluation.totalTargetWords} كلمة)",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (evaluation.isFullyCompleted) EmeraldPrimary else PurplePrimary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = {
                            val nextHint = evaluation.nextHintWord
                            if (!nextHint.isNullOrBlank()) {
                                val normalizedHint = QuranOfflineData.normalizeArabicForWritingAndSearch(
                                    nextHint.replace("\u0670", "")
                                )
                                userInputText = if (userInputText.isBlank()) {
                                    normalizedHint
                                } else {
                                    "${userInputText.trim()} $normalizedHint "
                                }
                            }
                        },
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "مساعدة بالكلمة التالية",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الكلمة التالية 💡")
                    }
                }

                LinearProgressIndicator(
                    progress = { (evaluation.accuracyPercent / 100f).coerceIn(0f, 1f) },
                    color = EmeraldPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                )

                // Word-by-Word Live Visual Feedback Chips
                if (evaluation.wordStatuses.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        evaluation.wordStatuses.forEach { (word, isOk) ->
                            Surface(
                                shape = CircleShape,
                                color = if (isOk) EmeraldPrimary.copy(alpha = 0.16f) else CoralAlert.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = if (isOk) "✓ $word" else "✗ $word",
                                    color = if (isOk) EmeraldPrimary else CoralAlert,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (evaluation.isFullyCompleted) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ما شاء الله! كتابة الآية صحيحة ومكتملة ١٠٠٪",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentVerseIdx < safeVerses.lastIndex) {
                        currentVerseIdx += 1
                        userInputText = ""
                    } else {
                        currentVerseIdx = 0
                        userInputText = ""
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text(
                    if (currentVerseIdx < safeVerses.lastIndex) "الآية التالية (${currentVerseIdx + 2}/${safeVerses.size}) ←"
                    else "إعادة من أول الصفحة"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

/**
 * Renders a complete Madani Mushaf page (1..604) fitted cleanly onto a single screen with horizontal page flipping:
 * - Surah-opening pages (`startsSurahAtTop`): uses `quran_page_frame` (gold & Masjid An-Nabawi frame + green cartouche + inner rectangular Basmalah box).
 * - Continuation / regular pages: uses `img_mushaf_frame_regular` (full gold & emerald border parchment WITHOUT the Surah-title rectangular box) to maximize reading area.
 * - Dynamically auto-fits font size via `TextMeasurer` so all verses on the page appear completely visible without vertical scrolling.
 */
@Composable
private fun MadaniMushafPageView(
    page: MushafPageContent,
    isNightMode: Boolean,
    fontSizeSp: Int = 25,
    onOpenWritingForPage: () -> Unit
) {
    val mainSurah = page.surahsOnPage.first()
    val firstVerse = page.verses.firstOrNull()
    val startsSurahAtTop = firstVerse?.ayahNumber == 1
    val openingSurah = if (startsSurahAtTop) {
        page.surahsOnPage.find { it.number == firstVerse?.surahNumber } ?: mainSurah
    } else {
        mainSurah
    }

    val textColor = if (isNightMode) Color(0xFFF7E7CE) else Color(0xFF142419)
    val accentColor = if (isNightMode) GoldAccent else Color(0xFF0F5233)
    val headerMetaColor = if (isNightMode) GoldAccent.copy(alpha = 0.92f) else Color(0xFF0F5233)

    val versesBySurah = remember(page.verses) {
        page.verses.groupBy { it.surahNumber }.toList()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        val pageWidth = maxWidth
        val sideFramePadding = (pageWidth * 0.080f).coerceIn(20.dp, 38.dp)
        val regularTopMetaPadding = (pageWidth * 0.145f).coerceIn(38.dp, 84.dp)

        // Frame drawable: ornate Masjid/cartouche frame ONLY on Surah opening pages;
        // clean full-height golden frame without rectangular Surah box on regular pages.
        Image(
            painter = painterResource(
                id = if (startsSurahAtTop) R.drawable.quran_page_frame
                else R.drawable.img_mushaf_frame_regular
            ),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        if (isNightMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.74f))
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (startsSurahAtTop) {
                // 1) Top Masjid An-Nabawi illustration zone (~21.0%)
                Spacer(modifier = Modifier.weight(0.210f))

                // 2) Top dark green cartouche zone (~8.3%): Surah name flanked by Juz & Hizb
                Box(
                    modifier = Modifier
                        .weight(0.083f)
                        .fillMaxWidth()
                        .padding(horizontal = (pageWidth * 0.21f).coerceIn(58.dp, 120.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ج ${QuranOfflineData.toArabicDigits(page.juzNumber)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "سُورَةُ ${openingSurah.nameAr}",
                            fontFamily = AmiriQuranFontFamily,
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )
                        Text(
                            text = "ح ${QuranOfflineData.toArabicDigits(page.hizbNumber)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // 3) Inner rectangular golden cartouche zone (~7.2%): Basmalah or Surah info
                Box(
                    modifier = Modifier
                        .weight(0.072f)
                        .fillMaxWidth()
                        .padding(horizontal = (pageWidth * 0.16f).coerceIn(44.dp, 96.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val innerCartoucheText = when (openingSurah.number) {
                        1 -> "فَاتِحَةُ الكِتَابِ • مَكِّيَّةٌ • ٧ آيَاتٍ"
                        9 -> "سُورَةُ التَّوْبَةِ • مَدَنِيَّةٌ • ١٢٩ آيَةً"
                        else -> "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
                    }
                    Text(
                        text = innerCartoucheText,
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = if (openingSurah.number == 1 || openingSurah.number == 9) 15.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNightMode) Color(0xFFF7E7CE) else Color(0xFF0F5233),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(0.008f))
            } else {
                // Regular / Continuation Page:
                // Skip rectangular Surah cartouche completely; use clean top border margin + slim header text.
                Spacer(modifier = Modifier.weight(0.045f))

                Row(
                    modifier = Modifier
                        .weight(0.040f)
                        .fillMaxWidth()
                        .padding(horizontal = regularTopMetaPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الجزء ${QuranOfflineData.toArabicDigits(page.juzNumber)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = headerMetaColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = page.surahsOnPage.joinToString(" • ") { "سُورَةُ ${it.nameAr}" },
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 15.sp,
                        color = headerMetaColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    )
                    Text(
                        text = "حزب ${QuranOfflineData.toArabicDigits(page.hizbNumber)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = headerMetaColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            // 4) Center Parchment Reading Area:
            // - Surah opening pages: ~55.5% weight
            // - Regular pages (without Surah rectangle): ~83.5% weight!
            val bodyWeight = if (startsSurahAtTop) 0.555f else 0.835f

            BoxWithConstraints(
                modifier = Modifier
                    .weight(bodyWeight)
                    .fillMaxWidth()
                    .padding(horizontal = sideFramePadding, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                val textMeasurer = rememberTextMeasurer()
                val density = LocalDensity.current
                val maxWidthPx = with(density) { maxWidth.roundToPx() }.coerceAtLeast(1)
                val maxHeightPx = with(density) { maxHeight.roundToPx() }.coerceAtLeast(1)

                val bodyAnnotated = remember(
                    page.pageNumber,
                    startsSurahAtTop,
                    isNightMode,
                    versesBySurah
                ) {
                    buildMushafPageBodyText(
                        versesBySurah = versesBySurah,
                        surahsOnPage = page.surahsOnPage,
                        mainSurah = mainSurah,
                        startsSurahAtTop = startsSurahAtTop,
                        textColor = textColor,
                        accentColor = accentColor
                    )
                }

                val fittedFontSp = remember(
                    bodyAnnotated,
                    maxWidthPx,
                    maxHeightPx,
                    fontSizeSp
                ) {
                    computeFittedFontSizeSp(
                        text = bodyAnnotated,
                        textMeasurer = textMeasurer,
                        maxWidthPx = maxWidthPx,
                        maxHeightPx = maxHeightPx,
                        maxPreferredSp = fontSizeSp.toFloat()
                    ).first
                }

                val lineHeightMultiplier = mushafLineHeightMultiplier(fittedFontSp)

                Text(
                    text = bodyAnnotated,
                    style = TextStyle(
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = fittedFontSp.sp,
                        lineHeight = (fittedFontSp * lineHeightMultiplier).sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 5) Bottom Mosque Silhouette Footer (~8.0% weight)
            Row(
                modifier = Modifier
                    .weight(0.080f)
                    .fillMaxWidth()
                    .padding(horizontal = sideFramePadding, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0F5233),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier.clickable { onOpenWritingForPage() }
                ) {
                    Text(
                        text = "✍️ كتابة",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0F5233),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                ) {
                    Text(
                        text = "صفحة ${QuranOfflineData.toArabicDigits(page.pageNumber)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

private fun buildMushafPageBodyText(
    versesBySurah: List<Pair<Int, List<MushafVerse>>>,
    surahsOnPage: List<com.example.data.SurahMetadata>,
    mainSurah: com.example.data.SurahMetadata,
    startsSurahAtTop: Boolean,
    textColor: Color,
    accentColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        versesBySurah.forEachIndexed { index, (surahNum, surahVerses) ->
            val surahMeta = surahsOnPage.find { it.number == surahNum } ?: mainSurah
            val startsSurahHere = surahVerses.firstOrNull()?.ayahNumber == 1
            val isHandledByTopCartouche = (index == 0 && startsSurahAtTop)

            if (startsSurahHere && !isHandledByTopCartouche) {
                if (index > 0) append("\n")
                withStyle(
                    SpanStyle(
                        fontFamily = AmiriQuranFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                ) {
                    append("۞ سُورَةُ ${surahMeta.nameAr} ۞\n")
                }
                if (surahNum != 1 && surahNum != 9) {
                    withStyle(
                        SpanStyle(
                            fontFamily = AmiriQuranFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    ) {
                        append("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ\n")
                    }
                }
            }

            surahVerses.forEach { v ->
                withStyle(
                    SpanStyle(
                        fontFamily = AmiriQuranFontFamily,
                        color = textColor,
                        fontWeight = FontWeight.Normal
                    )
                ) {
                    append(v.textUthmani)
                    append(" ")
                }
                withStyle(
                    SpanStyle(
                        fontFamily = AmiriQuranFontFamily,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("﴿${QuranOfflineData.toArabicDigits(v.ayahNumber)}﴾ ")
                }
            }
        }
    }
}

private fun mushafLineHeightMultiplier(sp: Float): Float = when {
    sp >= 20f -> 1.50f
    sp >= 15f -> 1.38f
    sp >= 11.5f -> 1.28f
    else -> 1.20f
}

private fun computeFittedFontSizeSp(
    text: AnnotatedString,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    maxWidthPx: Int,
    maxHeightPx: Int,
    maxPreferredSp: Float
): Pair<Float, Boolean> {
    val minSp = 5.5f
    val upperSp = maxPreferredSp.coerceAtLeast(minSp)
    val constraints = Constraints(maxWidth = maxWidthPx)

    fun fits(candidateSp: Float): Boolean {
        val lhMult = mushafLineHeightMultiplier(candidateSp)
        val layout = textMeasurer.measure(
            text = text,
            style = TextStyle(
                fontFamily = AmiriQuranFontFamily,
                fontSize = candidateSp.sp,
                lineHeight = (candidateSp * lhMult).sp,
                textAlign = TextAlign.Center
            ),
            constraints = constraints
        )
        return !layout.didOverflowHeight && layout.size.height <= maxHeightPx
    }

    if (fits(upperSp)) return upperSp to false
    if (!fits(minSp)) return minSp to true

    var low = minSp
    var high = upperSp
    repeat(10) {
        val mid = (low + high) / 2f
        if (fits(mid)) {
            low = mid
        } else {
            high = mid
        }
    }
    return low to false
}

