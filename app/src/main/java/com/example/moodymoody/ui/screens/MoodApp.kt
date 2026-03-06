package com.example.moodymoody.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodymoody.R
import com.example.moodymoody.data.MoodEntry
import com.example.moodymoody.data.MoodIconFamily
import com.example.moodymoody.notifications.MoodReminderScheduler
import com.example.moodymoody.ui.MoodViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.text.chunked
import kotlin.text.forEach

private val NightBackground = Color(0xFF0B1621)
private val AccentGold = Color(0xFFD4AF37)
private val OnNight = Color.White
private val CardBackground = Color(0xFF131F2C)
private val CardStroke = Color(0xFF2A394A)

private val moodKeys = listOf("super", "bien", "neutre", "moyen", "mauvais")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodApp(viewModel: MoodViewModel) {
    val navController = rememberNavController()
    val iconFamily by viewModel.iconFamily.collectAsState()
    Surface(modifier = Modifier.fillMaxSize(), color = NightBackground) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    navController = navController,
                    currentFamily = iconFamily
                )
            }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    reminderTimeState = viewModel.reminderTime,
                    currentFamily = iconFamily,
                    onTimeChange = viewModel::updateReminderTime,
                    onFamilyChange = viewModel::setIconFamily
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    viewModel: MoodViewModel,
    navController: NavController,
    currentFamily: MoodIconFamily
) {
    val todayEntry by viewModel.todayEntry.collectAsState(initial = null)
    val monthEntries by viewModel.monthEntries.collectAsState(initial = emptyList())
    val selectedMonth by viewModel.selectedMonth.collectAsState(initial = YearMonth.now())
    val context = LocalContext.current
    val canRequestNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    var hasNotificationPermission by rememberSaveable { mutableStateOf(notificationPermissionGranted(context)) }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasNotificationPermission = granted
    }
    val activity = context.findActivity()
    val shouldShowRationale = remember(activity, hasNotificationPermission, canRequestNotificationPermission) {
        if (!canRequestNotificationPermission || activity == null) {
            false
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(hasNotificationPermission) {
        if (hasNotificationPermission) {
            val reminderTime = viewModel.reminderTime.value
            MoodReminderScheduler.scheduleDailyReminders(context, reminderTime)
        } else {
            MoodReminderScheduler.cancelReminders(context)
        }
    }

    val shouldShowPermissionCard = canRequestNotificationPermission && !hasNotificationPermission
    val showSettingsButton = shouldShowPermissionCard && hasRequestedPermission && !shouldShowRationale

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = OnNight,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = AccentGold,
                    actionIconContentColor = OnNight
                ),
                title = {
                    val baseStyle = MaterialTheme.typography.titleLarge
                    Text(
                        text = "MoodyMoody",
                        style = baseStyle.copy(
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            lineHeight = baseStyle.fontSize * 0.8f,
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.None
                            )
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Paramètres",
                            tint = AccentGold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        MoodScreen(
            modifier = Modifier.padding(innerPadding),
            todayEntry = todayEntry,
            monthEntries = monthEntries,
            selectedMonth = selectedMonth,
            currentFamily = currentFamily,
            onSelectEmoji = viewModel::setMood,
            onPreviousMonth = viewModel::previousMonth,
            onNextMonth = viewModel::nextMonth,
            onResetMonth = viewModel::resetMonth,
            notificationPermissionContent = if (shouldShowPermissionCard) {
                {
                    NotificationPermissionCard(
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                hasRequestedPermission = true
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onOpenSettings = { openNotificationSettings(context) },
                        showSettingsButton = showSettingsButton
                    )
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun MoodScreen(
    modifier: Modifier,
    todayEntry: MoodEntry?,
    monthEntries: List<MoodEntry>,
    selectedMonth: YearMonth,
    currentFamily: MoodIconFamily,
    onSelectEmoji: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
    notificationPermissionContent: (@Composable () -> Unit)?
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        notificationPermissionContent?.let {
            it()
        }
        Text(
            text = "Comment te sens-tu aujourd'hui ?",
            style = MaterialTheme.typography.titleMedium.copy(color = OnNight),
            fontWeight = FontWeight.SemiBold
        )
        MoodSelectionCard(
            selectedMoodKey = todayEntry?.emoji,
            currentFamily = currentFamily,
            onSelectEmoji = onSelectEmoji
        )
        MonthSection(
            selectedMonth = selectedMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onResetMonth = onResetMonth
        )
        MoodHistoryChart(
            entries = monthEntries,
            selectedMonth = selectedMonth,
            currentFamily = currentFamily
        )
    }
}

@Composable
private fun MoodSelectionCard(
    selectedMoodKey: String?,
    currentFamily: MoodIconFamily,
    onSelectEmoji: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardStroke, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                moodKeys.forEach { key ->
                    val option = MoodEmojiOption(key = key, label = key.replaceFirstChar { it.uppercase() }, resId = currentFamily.drawableForMood(key), score = 0f)
                    EmojiChip(
                        option = option,
                        isSelected = selectedMoodKey == option.key,
                        onClick = { onSelectEmoji(option.key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmojiChip(option: MoodEmojiOption, isSelected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(
        targetValue = if (isSelected) AccentGold.copy(alpha = 0.25f)
        else NightBackground.copy(alpha = 0.4f),
        label = "chipColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentGold else Color.Transparent,
        label = "borderColor"
    )
    val targetSize = if (isSelected) 48.dp else 40.dp

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(background)
            .border(width = 2.dp, color = borderColor, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = option.resId),
            contentDescription = option.label,
            modifier = Modifier.size(targetSize)
        )
    }
}

@Composable
private fun MonthSection(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Mois précédent",
                tint = OnNight
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.FRENCH).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium.copy(color = OnNight),
                fontWeight = FontWeight.Bold
            )
            Text(text = selectedMonth.year.toString(), style = MaterialTheme.typography.bodySmall.copy(color = OnNight))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onResetMonth) {
                Text("Aujourd'hui", color = AccentGold)
            }
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    contentDescription = "Mois suivant",
                    tint = OnNight
                )
            }
        }
    }
}

@Composable
private fun MoodHistoryChart(entries: List<MoodEntry>, selectedMonth: YearMonth, currentFamily: MoodIconFamily) {
    val moodLevels = moodKeys
    val days = selectedMonth.lengthOfMonth()
    val pointRadius = 6.dp
    val density = LocalDensity.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .border(1.dp, CardStroke, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Évolution du mois", style = MaterialTheme.typography.titleMedium.copy(color = OnNight))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = pointRadius),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    moodLevels.forEach { optionKey ->
                        Image(
                            painter = painterResource(id = currentFamily.drawableForMood(optionKey)),
                            contentDescription = optionKey,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(CardBackground.copy(alpha = 0.8f))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val rowCount = moodLevels.size
                        val columnCount = days
                        val pxPointRadius = with(density) { pointRadius.toPx() }
                        val strokeWidth = with(density) { 1.5.dp.toPx() }
                        val chartTop = pxPointRadius
                        val chartBottom = size.height - pxPointRadius
                        val rowSpacing = if (rowCount > 1) (chartBottom - chartTop) / (rowCount - 1) else 0f
                        val columnSpacing = if (columnCount > 1) size.width / (columnCount - 1) else size.width

                        // draw guide lines aligned with each emoji row
                        for (i in 0 until rowCount) {
                            val y = chartTop + rowSpacing * i
                            drawLine(
                                color = CardStroke,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }

                        entries.forEach { entry ->
                            val dayIndex = entry.date.dayOfMonth - 1
                            val emojiIndex = moodLevels.indexOfFirst { it == entry.emoji }
                            if (dayIndex in 0 until columnCount && emojiIndex != -1) {
                                val x = columnSpacing * dayIndex
                                val y = chartTop + rowSpacing * emojiIndex
                                drawCircle(
                                    color = AccentGold,
                                    radius = pxPointRadius,
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    showSettingsButton: Boolean
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardStroke, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium.copy(color = OnNight),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Autorise les notifications pour recevoir un rappel.",
                style = MaterialTheme.typography.bodyMedium.copy(color = OnNight)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NightBackground,
                        contentColor = OnNight
                    )
                ) {
                    Text(text = "Autoriser", color = OnNight)
                }
                if (showSettingsButton) {
                    TextButton(onClick = onOpenSettings) {
                        Text(text = "Paramètres", color = AccentGold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    navController: NavController,
    reminderTimeState: StateFlow<LocalTime>,
    currentFamily: MoodIconFamily,
    onTimeChange: (LocalTime) -> Unit,
    onFamilyChange: (MoodIconFamily) -> Unit
) {
    val reminderTime by reminderTimeState.collectAsState()
    var hour by remember(reminderTime) { mutableStateOf(reminderTime.hour) }
    var minute by remember(reminderTime) { mutableStateOf(reminderTime.minute) }
    val formattedHour = hour.toString().padStart(2, '0')
    val formattedMinute = minute.toString().padStart(2, '0')

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.headlineSmall.copy(color = OnNight),
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { navController.popBackStack() }) {
                Text(text = "Fermer", color = AccentGold)
            }
        }
        Text(
            text = "Choix du style",
            style = MaterialTheme.typography.titleMedium.copy(color = OnNight)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoodIconFamily.values().toList().chunked(2).forEach { rowFamilies ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowFamilies.forEach { family ->
                        val selected = family == currentFamily
                        Button(
                            onClick = { onFamilyChange(family) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) AccentGold else CardStroke,
                                contentColor = if (selected) NightBackground else OnNight
                            )
                        ) {
                            Image(
                                painter = painterResource(id = family.drawableForMood("super")),
                                contentDescription = family.label,
                                modifier = Modifier.size(45.dp)
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = "Heure du rappel",
            style = MaterialTheme.typography.titleMedium.copy(color = OnNight)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberPicker(value = hour, range = 0..23, label = "Heures") { newHour ->
                hour = newHour
                onTimeChange(LocalTime.of(hour, minute))
            }
            NumberPicker(value = minute, range = 0..59, label = "Minutes") { newMinute ->
                minute = newMinute
                onTimeChange(LocalTime.of(hour, minute))
            }
        }
        Text(
            text = "Rappel programmé à $formattedHour:$formattedMinute",
            style = MaterialTheme.typography.bodyLarge.copy(color = AccentGold)
        )
    }
}

@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    label: String,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, color = OnNight)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledIconButton(
                onClick = { onValueChange(maxOf(range.first, value - 1)) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CardStroke,
                    contentColor = OnNight
                )
            ) {
                Icon(imageVector = Icons.Rounded.Remove, contentDescription = "-1")
            }
            Text(
                text = value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineMedium.copy(color = AccentGold)
            )
            FilledIconButton(
                onClick = { onValueChange(minOf(range.last, value + 1)) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CardStroke,
                    contentColor = OnNight
                )
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "+1")
            }
        }
    }
}

data class MoodEmojiOption(val key: String, val resId: Int, val label: String, val score: Float)

private fun notificationPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> null
    }
}
