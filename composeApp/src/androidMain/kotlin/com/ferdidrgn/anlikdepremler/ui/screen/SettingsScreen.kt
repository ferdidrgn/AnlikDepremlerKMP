package com.ferdidrgn.anlikdepremler.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.ads.BannerAdView
import com.ferdidrgn.anlikdepremler.core.ads.RewardedAdManager
import com.ferdidrgn.anlikdepremler.core.billing.launchCoffeeDonationFlow
import com.ferdidrgn.anlikdepremler.core.billing.launchRemoveAdsPurchaseFlow
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations
import com.ferdidrgn.anlikdepremler.core.language.AppLanguage
import com.ferdidrgn.anlikdepremler.core.util.ReviewHelper
import com.ferdidrgn.anlikdepremler.core.worker.WeeklyDigestScheduler
import com.ferdidrgn.anlikdepremler.ui.components.NativeAdCard
import com.ferdidrgn.anlikdepremler.ui.screen.settings.ModernSettingsSwitchTile
import com.ferdidrgn.anlikdepremler.ui.screen.settings.ModernSettingsTile
import com.ferdidrgn.anlikdepremler.ui.screen.settings.ModernThemeSelectorCard
import com.ferdidrgn.anlikdepremler.ui.screen.settings.NotificationFilterCard
import com.ferdidrgn.anlikdepremler.ui.screen.settings.SavedLocationsCard
import com.ferdidrgn.anlikdepremler.ui.screen.settings.SettingsCardContainer
import com.ferdidrgn.anlikdepremler.ui.screen.settings.SettingsCategoryTitle
import com.ferdidrgn.anlikdepremler.ui.screen.settings.DividerLine
import com.ferdidrgn.anlikdepremler.ui.screen.settings.openLocationSettings
import com.ferdidrgn.anlikdepremler.ui.screen.settings.openNotificationSettings
import com.ferdidrgn.anlikdepremler.ui.screen.settings.openWebPage
import com.ferdidrgn.anlikdepremler.ui.screen.settings.sendEmailIntent
import com.ferdidrgn.anlikdepremler.ui.screen.settings.shareApp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onOpenLegalDocument: (String) -> Unit = {},
    onOpenJournal: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferencesManager: PreferencesManager = koinInject()
    val rewardedAdManager: RewardedAdManager = koinInject()
    var isRewardedAdReady by remember { mutableStateOf(rewardedAdManager.isReady) }

    val currentLang by settingsViewModel.currentLanguage.collectAsState()
    val currentTheme by settingsViewModel.currentTheme.collectAsState()
    val emergencyPhone by settingsViewModel.emergencyPhoneNumber.collectAsState()
    val nearbyNotificationsEnabled by settingsViewModel.nearbyNotificationsEnabled.collectAsState()
    val voiceAlertsEnabled by settingsViewModel.voiceAlertsEnabled.collectAsState()
    val minMagnitudeThreshold by settingsViewModel.minMagnitudeThreshold.collectAsState()
    val maxDistanceKm by settingsViewModel.maxDistanceKm.collectAsState()
    val quietHoursEnabled by settingsViewModel.quietHoursEnabled.collectAsState()
    val quietHoursStartHour by settingsViewModel.quietHoursStartHour.collectAsState()
    val quietHoursEndHour by settingsViewModel.quietHoursEndHour.collectAsState()
    val savedLocations by settingsViewModel.savedLocations.collectAsState()
    val maxSavedLocations by settingsViewModel.maxSavedLocations.collectAsState()
    val adsFreeUntilMillis by settingsViewModel.adsFreeUntilMillis.collectAsState()
    val weeklyDigestEnabled by settingsViewModel.weeklyDigestEnabled.collectAsState()
    val isAdsFree = adsFreeUntilMillis > System.currentTimeMillis()
    val adsFreeDaysLeft = ((adsFreeUntilMillis - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)
    val shakeDetectionEnabled by settingsViewModel.shakeDetectionEnabled.collectAsState()
    val shakeSensitivity by settingsViewModel.shakeSensitivity.collectAsState()
    var shakeSensitivitySlider by remember(shakeSensitivity) { mutableFloatStateOf(shakeSensitivity) }
    val coroutineScope = rememberCoroutineScope()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isRewardedAdReady = rewardedAdManager.isReady
            kotlinx.coroutines.delay(2000)
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SettingsEvent.SendEmail -> sendEmailIntent(context, event.email)
                is SettingsEvent.OpenNotificationSettings -> openNotificationSettings(context)
                is SettingsEvent.OpenLocationSettings -> openLocationSettings(context)
                is SettingsEvent.OpenEarthquakeAlertsSettings -> openLocationSettings(context)
                is SettingsEvent.RequestReview -> ReviewHelper.launchInAppReview(context)
                is SettingsEvent.ShareApp -> shareApp(context)
                is SettingsEvent.NavigateToWeb -> openWebPage(context, event.url)
                is SettingsEvent.BuyCoffee -> launchCoffeeDonationFlow(context, event.productId)
                is SettingsEvent.RemoveAds -> launchRemoveAdsPurchaseFlow(context, preferencesManager)
                is SettingsEvent.WatchAdForExtraSlot -> {
                    val activity = context as? android.app.Activity
                    if (activity != null) {
                        rewardedAdManager.show(
                            activity = activity,
                            onEarned = { settingsViewModel.grantExtraSavedLocationSlot() },
                            onDismissed = { isRewardedAdReady = rewardedAdManager.isReady }
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 100.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 1. TERCİHLER & GÖRÜNÜM
        AppAnimations.StaggeredEntrance(index = 0) {
            Column {
                SettingsCategoryTitle(stringResource(R.string.category_preferences))
                SettingsCardContainer {
                    ModernSettingsTile(
                        icon = Icons.Default.Language,
                        iconBgColor = Color(0xFF2196F3),
                        title = stringResource(R.string.select_language),
                        badgeText = "${AppLanguage.entries.size}",
                        valueText = "${currentLang.flag} ${currentLang.displayName}",
                        onClick = { showLanguageDialog = true }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.MenuBook,
                        iconBgColor = Color(0xFF6D4C41),
                        title = stringResource(R.string.journal_entry_title),
                        subtitle = stringResource(R.string.journal_entry_sub),
                        onClick = onOpenJournal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ModernThemeSelectorCard(
                    currentTheme = currentTheme,
                    onThemeSelected = { newTheme ->
                        settingsViewModel.onThemeSelected(newTheme)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. BİLDİRİM VE İZİNLER + ACİL DURUM İLETİŞİMİ
        AppAnimations.StaggeredEntrance(index = 1) {
            Column {
                SettingsCategoryTitle(stringResource(R.string.category_notifications))
                SettingsCardContainer {
                    ModernSettingsTile(
                        icon = Icons.Default.PhoneInTalk,
                        iconBgColor = Color(0xFFE53935),
                        title = stringResource(R.string.emergency_contact_phone),
                        subtitle = stringResource(R.string.emergency_contact_phone_sub),
                        valueText = if (emergencyPhone.isNotEmpty()) emergencyPhone else stringResource(R.string.not_set),
                        onClick = { showPhoneDialog = true }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.Notifications,
                        iconBgColor = Color(0xFFFF9800),
                        title = stringResource(R.string.notification_settings),
                        subtitle = stringResource(R.string.notification_settings_sub),
                        onClick = { settingsViewModel.onNotificationSettingsClick() }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.LocationOn,
                        iconBgColor = Color(0xFF4CAF50),
                        title = stringResource(R.string.location_permissions),
                        subtitle = stringResource(R.string.location_permissions_sub),
                        onClick = { settingsViewModel.onLocationSettingsClick() }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.PhoneAndroid,
                        iconBgColor = Color(0xFF3F51B5),
                        title = stringResource(R.string.earthquake_alerts_info_title),
                        subtitle = stringResource(R.string.earthquake_alerts_info_sub),
                        onClick = { settingsViewModel.onEarthquakeAlertsInfoClick() }
                    )
                    DividerLine()
                    ModernSettingsSwitchTile(
                        icon = Icons.Default.Notifications,
                        iconBgColor = Color(0xFFE91E63),
                        title = stringResource(R.string.nearby_notifications_title),
                        subtitle = stringResource(R.string.nearby_notifications_sub),
                        checked = nearbyNotificationsEnabled,
                        onCheckedChange = { settingsViewModel.onNearbyNotificationsToggled(it) }
                    )
                    DividerLine()
                    ModernSettingsSwitchTile(
                        icon = Icons.Default.VolumeUp,
                        iconBgColor = Color(0xFF009688),
                        title = stringResource(R.string.voice_alerts_title),
                        subtitle = stringResource(R.string.voice_alerts_sub),
                        checked = voiceAlertsEnabled,
                        onCheckedChange = { settingsViewModel.onVoiceAlertsToggled(it) }
                    )
                    DividerLine()
                    ModernSettingsSwitchTile(
                        icon = Icons.Default.CalendarMonth,
                        iconBgColor = Color(0xFF3F51B5),
                        title = stringResource(R.string.weekly_digest_settings_title),
                        subtitle = stringResource(R.string.weekly_digest_settings_sub),
                        checked = weeklyDigestEnabled,
                        onCheckedChange = { enabled ->
                            settingsViewModel.onWeeklyDigestToggled(enabled)
                            if (enabled) {
                                WeeklyDigestScheduler.enable(context)
                            } else {
                                WeeklyDigestScheduler.disable(context)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                NotificationFilterCard(
                    minMagnitude = minMagnitudeThreshold,
                    onMinMagnitudeChange = { settingsViewModel.onMinMagnitudeChanged(it) },
                    maxDistanceKm = maxDistanceKm,
                    onMaxDistanceChange = { settingsViewModel.onMaxDistanceChanged(it) },
                    quietHoursEnabled = quietHoursEnabled,
                    onQuietHoursToggle = { settingsViewModel.onQuietHoursToggled(it) },
                    quietHoursStartHour = quietHoursStartHour,
                    onQuietHoursStartChange = { settingsViewModel.onQuietHoursStartChanged(it) },
                    quietHoursEndHour = quietHoursEndHour,
                    onQuietHoursEndChange = { settingsViewModel.onQuietHoursEndChanged(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SavedLocationsCard(
                    locations = savedLocations,
                    maxLocations = maxSavedLocations,
                    onAddClick = { showAddLocationDialog = true },
                    onRemoveClick = { settingsViewModel.removeSavedLocation(it) },
                    isRewardedAdReady = isRewardedAdReady,
                    onWatchAdForSlotClick = { settingsViewModel.onWatchAdForExtraSlotClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BannerAdView(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        // 3. DESTEK VE İLETİŞİM
        AppAnimations.StaggeredEntrance(index = 2) {
            Column {
                SettingsCategoryTitle(stringResource(R.string.category_support))
                SettingsCardContainer {
                    ModernSettingsTile(
                        icon = Icons.Default.Block,
                        iconBgColor = Color(0xFFEF4444),
                        title = stringResource(R.string.remove_ads_title),
                        subtitle = if (isAdsFree) {
                            stringResource(R.string.remove_ads_active_sub, adsFreeDaysLeft)
                        } else {
                            stringResource(R.string.remove_ads_sub)
                        },
                        onClick = { settingsViewModel.onRemoveAdsClick() }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.LocalCafe,
                        iconBgColor = Color(0xFF795548),
                        title = stringResource(R.string.buy_coffee),
                        subtitle = stringResource(R.string.buy_coffee_sub),
                        onClick = { settingsViewModel.onBuyCoffeeClick() }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.Star,
                        iconBgColor = Color(0xFFFFC107),
                        title = stringResource(R.string.rate_app),
                        subtitle = stringResource(R.string.rate_app_sub),
                        onClick = { settingsViewModel.onRateAppClick() }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.Share,
                        iconBgColor = Color(0xFF9C27B0),
                        title = stringResource(R.string.share_app),
                        subtitle = stringResource(R.string.share_app_sub),
                        onClick = { settingsViewModel.onShareAppClick() }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.Email,
                        iconBgColor = Color(0xFF00BCD4),
                        title = stringResource(R.string.send_feedback),
                        subtitle = stringResource(R.string.send_feedback_sub),
                        onClick = { settingsViewModel.onFeedbackClick() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. BİLGİ VE YASAL HAKLAR
        AppAnimations.StaggeredEntrance(index = 3) {
            Column {
                SettingsCategoryTitle(stringResource(R.string.category_legal))
                SettingsCardContainer {
                    ModernSettingsTile(
                        icon = Icons.Default.PrivacyTip,
                        iconBgColor = Color(0xFF607D8B),
                        title = stringResource(R.string.privacy_policy),
                        subtitle = stringResource(R.string.privacy_policy_sub),
                        onClick = { onOpenLegalDocument("privacy_policy") }
                    )
                    DividerLine()
                    ModernSettingsTile(
                        icon = Icons.Default.Gavel,
                        iconBgColor = Color(0xFF3F51B5),
                        title = stringResource(R.string.terms_conditions),
                        subtitle = stringResource(R.string.terms_conditions_sub),
                        onClick = { onOpenLegalDocument("terms_and_conditions") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. DENEYSEL ÖZELLİKLER
        AppAnimations.StaggeredEntrance(index = 4) {
            Column {
                SettingsCategoryTitle(stringResource(R.string.category_experimental))
                SettingsCardContainer {
                    ModernSettingsSwitchTile(
                        icon = Icons.Default.Sensors,
                        iconBgColor = Color(0xFF8D6E63),
                        title = stringResource(R.string.shake_detection_title),
                        subtitle = stringResource(R.string.shake_detection_sub),
                        checked = shakeDetectionEnabled,
                        onCheckedChange = { settingsViewModel.onShakeDetectionToggled(it) }
                    )
                    if (shakeDetectionEnabled) {
                        DividerLine()
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = stringResource(R.string.shake_detection_sensitivity),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Slider(
                                value = shakeSensitivitySlider,
                                onValueChange = { shakeSensitivitySlider = it },
                                onValueChangeFinished = {
                                    settingsViewModel.onShakeSensitivityChanged(shakeSensitivitySlider)
                                },
                                valueRange = 2.0f..3.5f,
                                steps = 2
                            )
                            Text(
                                text = stringResource(R.string.shake_detection_disclaimer),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        NativeAdCard()

        Spacer(modifier = Modifier.height(12.dp))

        BannerAdView(modifier = Modifier.fillMaxWidth())
    }

    // 🎯 ACİL DURUM TELEFON NUMARASI AYARLAMA DİYALOĞU
    if (showPhoneDialog) {
        var tempPhoneInput by remember { mutableStateOf(emergencyPhone) }

        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.emergency_phone_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.emergency_phone_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempPhoneInput,
                        onValueChange = { tempPhoneInput = it },
                        placeholder = { Text("05XXXXXXXXX") },
                        label = { Text(stringResource(R.string.phone_number_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // GİZLİLİK VE BİLGİLENDİRME UYARISI
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.phone_privacy_notice),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.saveEmergencyPhone(tempPhoneInput)
                        showPhoneDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // DİL SEÇİM DİYALOĞU
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.select_language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AppLanguage.entries.forEach { language ->
                        val isSelected = language == currentLang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    settingsViewModel.onLanguageSelected(context, language)
                                    showLanguageDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(language.flag, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                language.displayName,
                                modifier = Modifier.weight(1f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    BannerAdView(modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // 📍 KONUM EKLEME DİYALOĞU
    if (showAddLocationDialog) {
        var locationNameInput by remember { mutableStateOf("") }
        var isGeocoding by remember { mutableStateOf(false) }
        var showNotFoundError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddLocationDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.saved_locations_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.saved_locations_dialog_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = locationNameInput,
                        onValueChange = {
                            locationNameInput = it
                            showNotFoundError = false
                        },
                        placeholder = { Text(stringResource(R.string.saved_locations_dialog_hint)) },
                        singleLine = true,
                        enabled = !isGeocoding,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showNotFoundError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.saved_locations_not_found),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (isGeocoding) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = locationNameInput.isNotBlank() && !isGeocoding,
                    onClick = {
                        isGeocoding = true
                        coroutineScope.launch {
                            val success = settingsViewModel.addSavedLocation(context, locationNameInput)
                            isGeocoding = false
                            if (success) {
                                showAddLocationDialog = false
                            } else {
                                showNotFoundError = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLocationDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
