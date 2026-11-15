package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.PrayerCalcMethod
import com.example.notifications.AlarmCategory
import com.example.notifications.BuiltInTonePreset
import com.example.notifications.NotificationAndAlarmHelper
import com.example.notifications.ToneTarget
import com.example.ui.components.PurpleWaveBackground
import com.example.ui.components.Time12hFormatter
import com.example.ui.components.TimePicker12hField
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PurplePrimary
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    isArabicRtl: Boolean,
    sectionNumber: Int,
    prayerMethod: PrayerCalcMethod,
    masterNotificationsEnabled: Boolean = true,
    syncAlertWithNotification: Boolean = true,
    notifyPrayer: Boolean,
    alertSoundPrayer: Boolean = true,
    notifyAdhkar: Boolean,
    alertSoundAdhkar: Boolean = true,
    notifyQuran: Boolean = true,
    alertSoundQuran: Boolean = true,
    quranAlarmTime: String = "21:00",
    notifyHajj: Boolean = true,
    alertSoundHajj: Boolean = true,
    hajjAlarmTime: String = "17:30",
    notifyExercise: Boolean,
    alertSoundExercise: Boolean = true,
    notifyStudy: Boolean,
    alertSoundStudy: Boolean = true,
    ratingDelayMinutes: Int,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSetArabicRtl: (Boolean) -> Unit,
    onSetSectionNumber: (Int) -> Unit,
    onSetPrayerMethod: (PrayerCalcMethod) -> Unit,
    onUpdateGpsLocation: (Double, Double) -> Unit,
    onSetMasterNotificationsEnabled: (Boolean) -> Unit = {},
    onSetSyncAlertWithNotification: (Boolean) -> Unit = {},
    onSetNotifyPrayer: (Boolean) -> Unit,
    onSetAlertSoundPrayer: (Boolean) -> Unit = {},
    onSetNotifyAdhkar: (Boolean) -> Unit,
    onSetAlertSoundAdhkar: (Boolean) -> Unit = {},
    onSetNotifyQuran: (Boolean) -> Unit = {},
    onSetAlertSoundQuran: (Boolean) -> Unit = {},
    onSetQuranAlarmTime: (String) -> Unit = {},
    onSetNotifyHajj: (Boolean) -> Unit = {},
    onSetAlertSoundHajj: (Boolean) -> Unit = {},
    onSetHajjAlarmTime: (String) -> Unit = {},
    onSetNotifyExercise: (Boolean) -> Unit,
    onSetAlertSoundExercise: (Boolean) -> Unit = {},
    onSetNotifyStudy: (Boolean) -> Unit,
    onSetAlertSoundStudy: (Boolean) -> Unit = {},
    onSetRatingDelay: (Int) -> Unit,
    onClearCacheAndResync: ((Int) -> Unit) -> Unit = {},
    onTestSound: (AlarmCategory, String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var statusBanner by remember { mutableStateOf<String?>(null) }
    var showNotificationLogDialog by remember { mutableStateOf(false) }
    var showManualLocationDialog by remember { mutableStateOf(false) }
    var showSwapToneDialog by remember { mutableStateOf(false) }
    var manualLatInput by remember { mutableStateOf("30.0595") }
    var manualLonInput by remember { mutableStateOf("31.3137") }
    var selectedToneTarget by remember { mutableStateOf(ToneTarget.GENERAL) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        NotificationAndAlarmHelper.syncGeneralToneState(context)
    }

    val targetTonesState by NotificationAndAlarmHelper.targetTonesState.collectAsStateWithLifecycle()
    val uploadedToneLibrary by NotificationAndAlarmHelper.uploadedToneLibrary.collectAsStateWithLifecycle()
    val currentTargetAssignment = targetTonesState[selectedToneTarget]
        ?: NotificationAndAlarmHelper.getToneAssignment(context, selectedToneTarget)

    var hasAudioPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val targetTonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = NotificationAndAlarmHelper.saveCustomToneForTarget(
                    context = context,
                    target = selectedToneTarget,
                    sourceUri = uri
                )
                statusBanner = if (ok) {
                    "✓ تم رفع وتعيين النغمة لقسم (${selectedToneTarget.labelAr}) بنجاح"
                } else {
                    "تعذر نسخ ملف الصوت، يرجى اختيار ملف صوتي أو MP3 صالح"
                }
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        statusBanner = if (granted) {
            "✓ تم تفعيل إذن الملفات الصوتية بنجاح، يمكنك الآن رفع أي نغمة أو أنشودة"
        } else {
            "يمكنك اختيار أي ملف MP3 مباشرة عبر متصفح الملفات الآمن"
        }
    }

    // Runtime Permission Launchers
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        statusBanner = if (granted) "✓ تم منح صلاحية الإشعارات بنجاح" else "تنبيه: يرجى تفعيل الإشعارات من إعدادات الجهاز"
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        val granted = map[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            map[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        onUpdateGpsLocation(loc.latitude, loc.longitude)
                        statusBanner = "✓ تم تحديث موقع GPS لمواقيت الصلاة: (${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)})"
                    } else {
                        onUpdateGpsLocation(30.0595, 31.3137)
                        statusBanner = "✓ تم تفعيل GPS (الإحداثيات الافتراضية: القاهرة - جامعة الأزهر)"
                    }
                }
            } catch (_: Exception) {
                statusBanner = "✓ تم منح صلاحية الموقع الجغرافي"
            }
        } else {
            statusBanner = "يتم استخدام إحداثيات القاهرة (جامعة الأزهر) افتراضياً Offline"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PurpleWaveBackground(height = 210.dp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Settings Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "الإعدادات",
                            style = MaterialTheme.typography.titleLarge,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )

                        if (!statusBanner.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldPrimary.copy(alpha = 0.14f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = statusBanner!!,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Moved from HomeScreen: Quick Permissions Status ("الإشعارات: مفعّل ✓" & "المنبه الدقيق: مفعّل ✓") + Instant Test
            item {
                PermissionsStatusAndQuickVerifyCard(
                    onTestNotificationNow = onTestSound
                )
            }

            // Section 1: Android Permissions Center with Explanations
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "الصلاحيات",
                            style = MaterialTheme.typography.titleMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )

                        PermissionRowItem(
                            icon = Icons.Default.Notifications,
                            title = "الإشعارات",
                            description = "لعرض تنبيهات الصلوات والمحاضرات والأذكار في موعدها",
                            buttonLabel = "تفعيل",
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    statusBanner = "✓ الإشعارات مفعلة"
                                }
                            }
                        )

                        PermissionRowItem(
                            icon = Icons.Default.AlarmOn,
                            title = "المنبهات الدقيقة",
                            description = "لضمان انطلاق المنبه والتنبيه الصوتي في الثانية المحددة حتى والشاشة مغلقة",
                            buttonLabel = "ضبط",
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    try {
                                        context.startActivity(
                                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                        )
                                    } catch (_: Exception) {
                                        statusBanner = "✓ المنبهات الدقيقة جاهزة"
                                    }
                                } else {
                                    statusBanner = "✓ المنبهات الدقيقة مفعلة"
                                }
                            }
                        )

                        PermissionRowItem(
                            icon = Icons.Default.LocationOn,
                            title = "الموقع الجغرافي (GPS)",
                            description = "لحساب مواقيت الصلاة فلكياً Offline حسب موقعك أو إدخال الإحداثيات يدوياً",
                            buttonLabel = "تحديث GPS",
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        )

                        OutlinedButton(
                            onClick = { showManualLocationDialog = true },
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_coordinates_button")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PurplePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إدخال إحداثيات الموقع يدوياً (بدون صلاحية GPS)",
                                color = PurplePrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        PermissionRowItem(
                            icon = Icons.Default.Upload,
                            title = if (hasAudioPermission) "إذن الملفات الصوتية (MP3) • مفعّل ✓" else "إذن الملفات الصوتية ورفع الأناشيد (MP3)",
                            description = "للسماح للتطبيق بالوصول للملفات الصوتية ورفع أي نغمة أو أنشودة وتعيينها لأي قسم في التطبيق",
                            buttonLabel = if (hasAudioPermission) "اختيار ملف" else "منح الإذن",
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasAudioPermission) {
                                    audioPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                                } else {
                                    targetTonePickerLauncher.launch(arrayOf("audio/mpeg", "audio/*"))
                                }
                            }
                        )

                        PermissionRowItem(
                            icon = Icons.Default.BatteryChargingFull,
                            title = "العمل في الخلفية وتجاهل قيود البطارية",
                            description = "لمنع النظام من إيقاف المنبهات والأذان أثناء غلق التطبيق أو قفل الشاشة",
                            buttonLabel = "استثناء",
                            onClick = {
                                try {
                                    val pm = context.getSystemService(PowerManager::class.java)
                                    if (pm != null && !pm.isIgnoringBatteryOptimizations(context.packageName)) {
                                        context.startActivity(
                                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                        )
                                    } else {
                                        statusBanner = "✓ مستثنى من قيود البطارية"
                                    }
                                } catch (_: Exception) {
                                    statusBanner = "✓ تم ضبط البطارية"
                                }
                            }
                        )

                        PermissionRowItem(
                            icon = Icons.Default.Vibration,
                            title = "الاهتزاز والصوت",
                            description = "لتشغيل الاهتزاز والصوت المتزامن مع شاشة التنبيه",
                            buttonLabel = "اختبار",
                            onClick = {
                                onTestSound(AlarmCategory.STUDY, "مذاكرة")
                                statusBanner = "✓ تم الاختبار"
                            }
                        )
                    }
                }
            }


            // Section 2: General Preferences (Language, Theme, Section Number)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "المظهر، اللغة، ورقم السكشن",
                            style = MaterialTheme.typography.titleMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الوضع الداكن (Dark Mode)",
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleDarkTheme,
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("اللغة واتجاه الواجهة", fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = isArabicRtl,
                                    onClick = { onSetArabicRtl(true) },
                                    label = { Text("العربية (RTL)", maxLines = 1, softWrap = false) }
                                )
                                FilterChip(
                                    selected = !isArabicRtl,
                                    onClick = { onSetArabicRtl(false) },
                                    label = { Text("English (LTR)", maxLines = 1, softWrap = false) }
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("رقم السكشن الدراسي", fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = sectionNumber == 1,
                                    onClick = { onSetSectionNumber(1) },
                                    label = { Text("سكشن 1", maxLines = 1, softWrap = false) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = sectionNumber == 2,
                                    onClick = { onSetSectionNumber(2) },
                                    label = { Text("سكشن 2", maxLines = 1, softWrap = false) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Master Control & Synchronization Unit (الإعداد الرئيسي الشامل ووحدة التزامن بين الإشعار والتنبيه)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "التحكم الرئيسي ووحدة تزامن الإشعارات والتنبيهات",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "يتحكم في جميع الإشعارات والتنبيهات الصوتية المتزامنة معاً في نفس اللحظة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    NotificationAndAlarmHelper.stopAlertSound(context)
                                    statusBanner = "تم إيقاف الصوت والتنبيه الحالي"
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("إيقاف الصوت", maxLines = 1, softWrap = false, color = CoralAlert)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (masterNotificationsEnabled) EmeraldPrimary.copy(alpha = 0.12f) else CoralAlert.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "المفتاح الرئيسي الشامل (تشغيل / إيقاف الكل)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (masterNotificationsEnabled) EmeraldPrimary else CoralAlert
                                    )
                                    Text(
                                        text = if (masterNotificationsEnabled) "مفعّل: جميع الأقسام المحددة بالأسفل تعمل" else "متوقف: تم إيقاف كافة الإشعارات والتنبيهات",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = masterNotificationsEnabled,
                                    onCheckedChange = {
                                        onSetMasterNotificationsEnabled(it)
                                        statusBanner = if (it) "✓ تم تفعيل المفتاح الرئيسي لجميع الإشعارات والتنبيهات" else "تم إيقاف جميع الإشعارات والتنبيهات"
                                    },
                                    modifier = Modifier.testTag("master_notifications_switch")
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = PurplePrimary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "إعدادات التنبيهات والإشعارات معاً (وحدة التزامن)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimary
                                    )
                                    Text(
                                        text = "تشغيل التنبيه الصوتي والاهتزاز في نفس لحظة ظهور الإشعار بدون انقطاع",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = syncAlertWithNotification,
                                    enabled = masterNotificationsEnabled,
                                    onCheckedChange = {
                                        onSetSyncAlertWithNotification(it)
                                        statusBanner = if (it) "✓ تم تفعيل التزامن الفوري بين الإشعار والتنبيه الصوتي" else "تم قصر النظام على الإشعارات المرئية فقط بدون صوت"
                                    },
                                    modifier = Modifier.testTag("sync_alert_with_notif_switch")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onClearCacheAndResync { count ->
                                        statusBanner = "✓ تم مسح كاش وحدة التسامح وإعادة مزامنة $count منبه بنجاح"
                                    }
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clear_cache_resync_button")
                            ) {
                                Text("مسح الكاش وإعادة المزامنة 🔄", maxLines = 1, softWrap = false)
                            }

                            OutlinedButton(
                                onClick = { showNotificationLogDialog = true },
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_notification_log_button")
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = PurplePrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سجل الإشعارات", color = PurplePrimary, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }

                        Button(
                            onClick = {
                                NotificationAndAlarmHelper.scheduleTestNotificationIn10Seconds(context)
                                statusBanner = "✓ تم جدولة اختبار متزامن (إشعار + تنبيه صوتي) بعد 10 ثوانٍ"
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_notification_10s_button")
                        ) {
                            Icon(Icons.Default.AlarmOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اختبار الإشعارات (بعد 10 ثوانٍ)", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            // Separate Card 1: Prayer Notifications & Adhan Alert Settings (إعدادات تنبيه الصلاة لوحدها)
            item {
                SeparateCategorySettingsCard(
                    title = "١) إعدادات إشعارات وتنبيه الصلاة (منفصلة)",
                    subtitle = "تحكم مستقل في إشعارات مواقيت الصلاة الخمس وصوت الأذان المتزامن (الفجر، الظهر، العصر، المغرب، العشاء)",
                    masterEnabled = masterNotificationsEnabled,
                    notificationChecked = notifyPrayer,
                    onNotificationCheckedChange = onSetNotifyPrayer,
                    alertSoundChecked = alertSoundPrayer && syncAlertWithNotification,
                    onAlertSoundCheckedChange = onSetAlertSoundPrayer,
                    onTestClick = {
                        onTestSound(AlarmCategory.PRAYER, "صلاة الظهر")
                        statusBanner = "✓ جاري اختبار تزامن إشعار وتنبيه صلاة الظهر"
                    },
                    testButtonLabel = "تجربة الظهر"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onTestSound(AlarmCategory.PRAYER, "صلاة المغرب")
                                statusBanner = "✓ جاري اختبار تزامن إشعار وتنبيه صلاة المغرب"
                            },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تجربة صلاة المغرب", maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = {
                                onTestSound(AlarmCategory.PRAYER, "صلاة الفجر")
                                statusBanner = "✓ جاري اختبار تزامن إشعار وتنبيه صلاة الفجر"
                            },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تجربة صلاة الفجر", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            // Separate Card 2: Adhkar Notifications & Alert Settings (إعدادات تنبيه الأذكار لوحدها)
            item {
                SeparateCategorySettingsCard(
                    title = "٢) إعدادات إشعارات وتنبيه الأذكار (منفصلة)",
                    subtitle = "تحكم مستقل في إشعارات وتنبيهات أذكار الصباح والمساء والأذكار اليومية",
                    masterEnabled = masterNotificationsEnabled,
                    notificationChecked = notifyAdhkar,
                    onNotificationCheckedChange = onSetNotifyAdhkar,
                    alertSoundChecked = alertSoundAdhkar && syncAlertWithNotification,
                    onAlertSoundCheckedChange = onSetAlertSoundAdhkar,
                    onTestClick = {
                        onTestSound(AlarmCategory.ADHKAR, "أذكار الصباح")
                        statusBanner = "✓ جاري اختبار تزامن إشعار وتنبيه الأذكار"
                    },
                    testButtonLabel = "تجربة الأذكار"
                )
            }

            // Separate Card 3: Quran Reading Notifications & Alert Settings (إعدادات تنبيه قراءة القرآن لوحدها)
            item {
                SeparateCategorySettingsCard(
                    title = "٣) إعدادات إشعارات وتنبيه قراءة القرآن (منفصلة)",
                    subtitle = "منبه يومي مستقل للورد القرآني مع تزامن الإشعار والتنبيه الصوتي (الموعد الحالي: ${Time12hFormatter.format24To12Ar(quranAlarmTime)})",
                    masterEnabled = masterNotificationsEnabled,
                    notificationChecked = notifyQuran,
                    onNotificationCheckedChange = onSetNotifyQuran,
                    alertSoundChecked = alertSoundQuran && syncAlertWithNotification,
                    onAlertSoundCheckedChange = onSetAlertSoundQuran,
                    onTestClick = {
                        onTestSound(AlarmCategory.QURAN, "ورد قراءة القرآن")
                        statusBanner = "✓ جاري اختبار تزامن إشعار وتنبيه قراءة القرآن"
                    },
                    testButtonLabel = "تجربة القرآن"
                ) {
                    TimePicker12hField(
                        label = "موعد تنبيه قراءة القرآن اليومي (نظام 12 ساعة)",
                        time24 = quranAlarmTime,
                        onTime24Changed = { newTime ->
                            onSetQuranAlarmTime(newTime)
                            statusBanner = "✓ تم ضبط موعد تنبيه قراءة القرآن على ${Time12hFormatter.format24To12Ar(newTime)}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Separate Card 4: Hajj & Umrah Notifications & Alert Settings (إعدادات تنبيه الحج والعمرة لوحدها)
            item {
                SeparateCategorySettingsCard(
                    title = "٤) إعدادات إشعارات وتنبيه الحج والعمرة (منفصلة)",
                    subtitle = "منبه يومي مستقل لأذكار ومناسك الحج والعمرة والأدعية (الموعد الحالي: ${Time12hFormatter.format24To12Ar(hajjAlarmTime)})",
                    masterEnabled = masterNotificationsEnabled,
                    notificationChecked = notifyHajj,
                    onNotificationCheckedChange = onSetNotifyHajj,
                    alertSoundChecked = alertSoundHajj && syncAlertWithNotification,
                    onAlertSoundCheckedChange = onSetAlertSoundHajj,
                    onTestClick = {
                        onTestSound(AlarmCategory.HAJJ, "أذكار ومناسك الحج")
                        statusBanner = "✓ جاري اختبار تزامن إشعار وتنبيه الحج والعمرة"
                    },
                    testButtonLabel = "تجربة الحج"
                ) {
                    TimePicker12hField(
                        label = "موعد تنبيه أذكار الحج والعمرة (نظام 12 ساعة)",
                        time24 = hajjAlarmTime,
                        onTime24Changed = { newTime ->
                            onSetHajjAlarmTime(newTime)
                            statusBanner = "✓ تم ضبط موعد تنبيه الحج والعمرة على ${Time12hFormatter.format24To12Ar(newTime)}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Separate Card 5: Exercises Notifications & Alert Settings (إعدادات تنبيه التمارين لوحدها)
            item {
                SeparateCategorySettingsCard(
                    title = "٥) إعدادات إشعارات وتنبيه التمارين الرياضية (منفصلة)",
                    subtitle = "تحكم مستقل في إشعار التمرين اليومي ونغمة التمارين المتزامنة (سبحانه وبحمده)",
                    masterEnabled = masterNotificationsEnabled,
                    notificationChecked = notifyExercise,
                    onNotificationCheckedChange = onSetNotifyExercise,
                    alertSoundChecked = alertSoundExercise && syncAlertWithNotification,
                    onAlertSoundCheckedChange = onSetAlertSoundExercise,
                    onTestClick = {
                        onTestSound(AlarmCategory.EXERCISE, "تمرين اليوم")
                        statusBanner = "✓ جاري اختبار تزامن إشعار وتنبيه التمارين"
                    },
                    testButtonLabel = "تجربة التمرين"
                )
            }

            // Separate Card 6: Study & Schedule Notifications & Alert Settings (إعدادات تنبيه المذاكرة والمحاضرات لوحدها)
            item {
                SeparateCategorySettingsCard(
                    title = "٦) إعدادات إشعارات وتنبيه المذاكرة والمحاضرات (منفصلة)",
                    subtitle = "تحكم مستقل في إشعارات المحاضرات، جلسات المذاكرة، والتقييم بعد الجلسة",
                    masterEnabled = masterNotificationsEnabled,
                    notificationChecked = notifyStudy,
                    onNotificationCheckedChange = onSetNotifyStudy,
                    alertSoundChecked = alertSoundStudy && syncAlertWithNotification,
                    onAlertSoundCheckedChange = onSetAlertSoundStudy,
                    onTestClick = {
                        onTestSound(AlarmCategory.STUDY, "مذاكرة Data Structures")
                        statusBanner = "✓ جاري اختبار تزامن إشعار وتنبيه المذاكرة"
                    },
                    testButtonLabel = "تجربة المذاكرة"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("مهلة إشعار التقييم بعد الجلسة:", fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 5, 15, 30, 60).forEach { mins ->
                                FilterChip(
                                    selected = ratingDelayMinutes == mins,
                                    onClick = { onSetRatingDelay(mins) },
                                    label = { Text("${mins}د", maxLines = 1, softWrap = false) }
                                )
                            }
                        }
                    }
                }
            }

            // Section 3.5: App Tones Assignment & Swapping (نغمات التطبيق واستبدال وتبديل الأصوات)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "نغمات التطبيق واستبدال الأصوات",
                            style = MaterialTheme.typography.titleMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "اختر القسم أو التنبيه من الشريط بالأسفل ثم ارفع نغمة أو أنشودة MP3 مخصصة له أو بدّل النغمات بين الأقسام بحرية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Full Target Selection Strip (كل محتويات التطبيق: المذاكرة، كل ذكر، كل صلاة، القرآن، الحج، التمارين)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .testTag("tone_target_selector_strip"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ToneTarget.entries.forEach { target ->
                                val isSelected = selectedToneTarget == target
                                val isTargetCustom = targetTonesState[target]?.isCustomized == true
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedToneTarget = target },
                                    label = {
                                        Text(
                                            text = if (isTargetCustom) "${target.labelAr} ★" else target.labelAr,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("tone_target_chip_${target.key}")
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PurplePrimary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "النغمة المفعّلة حالياً لـ (${selectedToneTarget.labelAr}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currentTargetAssignment.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    targetTonePickerLauncher.launch(arrayOf("audio/mpeg", "audio/*"))
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_general_tone_button")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "رفع MP3",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    NotificationAndAlarmHelper.previewToneForTarget(context, selectedToneTarget)
                                    statusBanner = "جاري تجربة نغمة: ${selectedToneTarget.labelAr}"
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preview_general_tone_button")
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تجربة النغمة",
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    NotificationAndAlarmHelper.stopAlertSound(context)
                                    statusBanner = "تم إيقاف الصوت"
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "إيقاف الصوت",
                                    color = CoralAlert,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = { showSwapToneDialog = true },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("replace_or_reset_general_tone_button")
                            ) {
                                Text(
                                    text = "تبديل / استبدال",
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Local Account Logout Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "إدارة الحساب المحلي",
                            style = MaterialTheme.typography.titleMedium,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = onLogout,
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAlert),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل الخروج وتبديل الحساب المحلي")
                        }
                    }
                }
            }
        }
    }

    if (showManualLocationDialog) {
        AlertDialog(
            onDismissRequest = { showManualLocationDialog = false },
            title = { Text("ضبط إحداثيات الموقع يدوياً (Offline)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "يمكنك إدخال دائرة العرض (Latitude) وخط الطول (Longitude) لمدينتك لحساب مواقيت الصلاة بدقة بدون الحاجة لصلاحية GPS:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = manualLatInput,
                        onValueChange = { manualLatInput = it },
                        label = { Text("دائرة العرض (Latitude)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manualLonInput,
                        onValueChange = { manualLonInput = it },
                        label = { Text("خط الطول (Longitude)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = manualLatInput == "30.0595" && manualLonInput == "31.3137",
                            onClick = {
                                manualLatInput = "30.0595"
                                manualLonInput = "31.3137"
                            },
                            label = { Text("القاهرة (الأزهر)") }
                        )
                        FilterChip(
                            selected = manualLatInput == "31.2001" && manualLonInput == "29.9187",
                            onClick = {
                                manualLatInput = "31.2001"
                                manualLonInput = "29.9187"
                            },
                            label = { Text("الإسكندرية") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lat = manualLatInput.toDoubleOrNull() ?: 30.0595
                        val lon = manualLonInput.toDoubleOrNull() ?: 31.3137
                        onUpdateGpsLocation(lat, lon)
                        statusBanner = "✓ تم حفظ الإحداثيات يدوياً: (${"%.4f".format(lat)}, ${"%.4f".format(lon)})"
                        showManualLocationDialog = false
                    },
                    shape = CircleShape
                ) {
                    Text("حفظ الإحداثيات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualLocationDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showNotificationLogDialog) {
        var refreshTrigger by remember { mutableStateOf(0) }
        val pendingAlarms = remember(refreshTrigger) {
            com.example.notifications.NotificationAndAlarmHelper.getPendingScheduledAlarms(context)
        }
        val historyLogs = remember(refreshTrigger) {
            com.example.notifications.NotificationAndAlarmHelper.getDiagnosticLogs(context)
        }

        AlertDialog(
            onDismissRequest = { showNotificationLogDialog = false },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "سجل الإشعارات والتشخيص (Notification Log)",
                        style = MaterialTheme.typography.titleMedium,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المجدولة حالياً: ${pendingAlarms.size} • سجل الأحداث: ${historyLogs.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        com.example.notifications.NotificationAndAlarmHelper.scheduleAllAlarms(
                                            context = context,
                                            reason = "DIAGNOSTIC_RESYNC",
                                            forceReschedule = true
                                        )
                                        refreshTrigger++
                                    }
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إعادة جدولة الكل", maxLines = 1, softWrap = false)
                            }
                            OutlinedButton(
                                onClick = {
                                    com.example.notifications.NotificationAndAlarmHelper.clearDiagnosticLogs(context)
                                    refreshTrigger++
                                },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("مسح السجل", maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    item {
                        Text(
                            text = "١) الإشعارات المجدولة حالياً (Pending Alarms):",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    if (pendingAlarms.isEmpty()) {
                        item {
                            Text(
                                text = "لا توجد إشعارات معلقة حالياً. اضغط 'إعادة جدولة الكل' لتحديث الجدول.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        pendingAlarms.forEach { alarm ->
                            item(key = "pending_${alarm.alarmId}") {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.10f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "ID: ${alarm.alarmId} • ${alarm.categoryNameAr}",
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                text = alarm.scheduledTimeFormatted,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = alarm.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "٢) سجل الإشعارات المعروضة وحارس منع التكرار (60s Dedupe):",
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
                        )
                    }

                    if (historyLogs.isEmpty()) {
                        item {
                            Text(
                                text = "لا توجد إشعارات معروضة في السجل بعد. يمكنك الضغط على 'اختبار الإشعارات (بعد 10 ثوانٍ)' للتجربة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        historyLogs.forEachIndexed { idx, log ->
                            item(key = "log_${log.timestampMillis}_$idx") {
                                val isBlocked = log.status == "DEDUPE_BLOCKED"
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isBlocked) {
                                        CoralAlert.copy(alpha = 0.12f)
                                    } else {
                                        PurplePrimary.copy(alpha = 0.09f)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "ID: ${log.id} • ${log.status}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isBlocked) CoralAlert else PurplePrimary,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                text = log.timeFormatted,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = log.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = log.details,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { refreshTrigger++ },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("تحديث السجل 🔄")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationLogDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (showSwapToneDialog) {
        AlertDialog(
            onDismissRequest = { showSwapToneDialog = false },
            title = {
                Text(
                    text = "تبديل أو استبدال نغمة (${selectedToneTarget.labelAr})",
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "١) تبديل مباشر مع قسم آخر (دي مكان دي):",
                            style = MaterialTheme.typography.titleSmall,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ToneTarget.entries.filter { it != selectedToneTarget }.forEach { otherTarget ->
                        item(key = "swap_${otherTarget.key}") {
                            val otherAssign = targetTonesState[otherTarget]
                                ?: NotificationAndAlarmHelper.getToneAssignment(context, otherTarget)
                            Surface(
                                onClick = {
                                    scope.launch {
                                        NotificationAndAlarmHelper.swapTonesBetweenTargets(
                                            context = context,
                                            first = selectedToneTarget,
                                            second = otherTarget
                                        )
                                        statusBanner = "✓ تم تبديل نغمة (${selectedToneTarget.labelAr}) مع (${otherTarget.labelAr}) بنجاح"
                                        showSwapToneDialog = false
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = PurplePrimary.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("swap_with_${otherTarget.key}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تبديل مع: ${otherTarget.labelAr}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = otherAssign.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "تبديل ⇄",
                                        color = PurplePrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "٢) تعيين نغمة جاهزة من التطبيق:",
                            style = MaterialTheme.typography.titleSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    BuiltInTonePreset.entries.forEach { preset ->
                        item(key = "preset_${preset.id}") {
                            Surface(
                                onClick = {
                                    scope.launch {
                                        NotificationAndAlarmHelper.assignBuiltInPresetToTarget(
                                            context = context,
                                            target = selectedToneTarget,
                                            preset = preset
                                        )
                                        statusBanner = "✓ تم تعيين (${preset.displayNameAr}) لقسم (${selectedToneTarget.labelAr})"
                                        showSwapToneDialog = false
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldPrimary.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = preset.displayNameAr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    if (uploadedToneLibrary.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "٣) ملفات MP3 والأناشيد التي قمت برفعها:",
                                style = MaterialTheme.typography.titleSmall,
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        uploadedToneLibrary.forEachIndexed { idx, entry ->
                            item(key = "uploaded_${idx}_${entry.filePath}") {
                                Surface(
                                    onClick = {
                                        scope.launch {
                                            NotificationAndAlarmHelper.assignUploadedEntryToTarget(
                                                context = context,
                                                target = selectedToneTarget,
                                                entry = entry
                                            )
                                            statusBanner = "✓ تم تعيين (${entry.displayName}) لقسم (${selectedToneTarget.labelAr})"
                                            showSwapToneDialog = false
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = entry.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (currentTargetAssignment.isCustomized) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                NotificationAndAlarmHelper.resetToneForTarget(context, selectedToneTarget)
                                statusBanner = "✓ تمت استعادة النغمة الافتراضية لقسم (${selectedToneTarget.labelAr})"
                                showSwapToneDialog = false
                            }
                        }
                    ) {
                        Text("استعادة الافتراضي", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwapToneDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
private fun PermissionRowItem(
    icon: ImageVector,
    title: String,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PurplePrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                if (description.isNotBlank()) {
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun SeparateCategorySettingsCard(
    title: String,
    subtitle: String,
    masterEnabled: Boolean,
    notificationChecked: Boolean,
    onNotificationCheckedChange: (Boolean) -> Unit,
    alertSoundChecked: Boolean,
    onAlertSoundCheckedChange: (Boolean) -> Unit,
    onTestClick: () -> Unit,
    testButtonLabel: String,
    extraContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onTestClick,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = testButtonLabel,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إظهار الإشعار المرئي",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = notificationChecked && masterEnabled,
                            enabled = masterEnabled,
                            onCheckedChange = onNotificationCheckedChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تشغيل التنبيه الصوتي المتزامن مع الإشعار",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = alertSoundChecked && masterEnabled,
                            enabled = masterEnabled,
                            onCheckedChange = onAlertSoundCheckedChange
                        )
                    }
                }
            }

            if (extraContent != null) {
                extraContent()
            }
        }
    }
}

