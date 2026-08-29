package com.ferdidrgn.anlikdepremler.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.ads.BannerAdView
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations
import com.ferdidrgn.anlikdepremler.ui.components.NativeAdCard
import com.ferdidrgn.anlikdepremler.ui.util.shouldShowAdAtIndex

// 🎯 TÜRKİYE VE DÜNYA KATEGORİSİ İÇİN MODEL
data class HistoricalEarthquakeItem(
    val id: String,
    val title: String,
    val magnitude: Double,
    val date: String,
    val location: String,
    val depth: String,
    val casualties: String,
    val description: String,
    val isTurkey: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricalArchiveScreen(
    onBackClick: () -> Unit
) {
    var selectedCategoryTab by remember { mutableIntStateOf(0) } // 0: Türkiye, 1: Dünya

    val turkeyList = remember { getTurkeyHistoricalEarthquakes() }
    val worldList = remember { getWorldHistoricalEarthquakes() }

    val activeList = if (selectedCategoryTab == 0) turkeyList else worldList

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.historical_archive_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // 1. ÜST BANNER REKLAM
            BannerAdView(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(8.dp))

            // 2. TÜRKİYE / DÜNYA SEKMELERİ (TAB)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CategoryTabChip(
                    text = "Türkiye 🇹🇷",
                    isSelected = selectedCategoryTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedCategoryTab = 0 }
                )
                CategoryTabChip(
                    text = "Dünya 🌍",
                    isSelected = selectedCategoryTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedCategoryTab = 1 }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. ANİMASYONLU LİSTE VE YERLEŞİK NATIVE REKLAMLAR
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(
                    items = activeList,
                    key = { _, item -> item.id }
                ) { index, item ->
                    AppAnimations.SpringEntranceContainer {
                        HistoricalEarthquakeCard(item = item)
                    }

                    // Her kaç elemanda bir Native Reklam Gösterileceğini Belirler
                    if (shouldShowAdAtIndex(index, activeList.size)) {
                        Spacer(modifier = Modifier.height(6.dp))
                        NativeAdCard()
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // 4. ALT BANNER REKLAM
            BannerAdView(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CategoryTabChip(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.5f
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistoricalEarthquakeCard(item: HistoricalEarthquakeItem) {
    val magColor = when {
        item.magnitude >= 8.0 -> Color(0xFF991B1B)
        item.magnitude >= 7.5 -> Color(0xFFDC2626)
        item.magnitude >= 7.0 -> Color(0xFFEA580C)
        else -> Color(0xFFD97706)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = magColor
                ) {
                    Text(
                        text = "${item.magnitude} Mw",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "📍 ${item.location} • ${item.depth}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                if (item.casualties.isNotEmpty()) {
                    Text(
                        text = "⚠️ ${item.casualties}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// 🇹🇷 TÜRKİYE TARİHİ DEPREMLERİ VERİ KAYNAĞI
private fun getTurkeyHistoricalEarthquakes(): List<HistoricalEarthquakeItem> {
    return listOf(
        HistoricalEarthquakeItem(
            id = "tr_1",
            title = "Kahramanmaraş Depremleri",
            magnitude = 7.8,
            date = "6 Şubat 2023",
            location = "Pazarcık & Elbistan, Kahramanmaraş",
            depth = "8.6 km",
            casualties = "53.000+ Kayıp",
            description = "Cumhuriyet tarihinin en yıkıcı felaketi. 11 ili etkileyen 7.8 ve 7.6 büyüklüğündeki ardışık iki dev sarsıntı binlerce yapıyı yıktı.",
            isTurkey = true
        ),
        HistoricalEarthquakeItem(
            id = "tr_2",
            title = "Erzincan Depremi",
            magnitude = 7.9,
            date = "27 Aralık 1939",
            location = "Erzincan",
            depth = "20.0 km",
            casualties = "32.000+ Kayıp",
            description = "Türkiye'de kayıtlara geçen en büyük depremlerden biridir. Erzincan şehir merkezini tamamen haritadan silmiştir.",
            isTurkey = true
        ),
        HistoricalEarthquakeItem(
            id = "tr_3",
            title = "Gölcük (Marmara) Depremi",
            magnitude = 7.6,
            date = "17 Ağustos 1999",
            location = "Gölcük, Kocaeli",
            depth = "17.0 km",
            casualties = "18.000+ Kayıp",
            description = "Marmara bölgesinin sanayi kalbinde 45 saniye süren yıkım. Türkiye'de afet bilincinin ve yönetmeliklerin miladı olmuştur.",
            isTurkey = true
        ),
        HistoricalEarthquakeItem(
            id = "tr_4",
            title = "Düzce Depremi",
            magnitude = 7.2,
            date = "12 Kasım 1999",
            location = "Kaynaşlı, Düzce",
            depth = "14.0 km",
            casualties = "845 Kayıp",
            description = "17 Ağustos depreminden sadece 87 gün sonra Kuzey Anadolu Fay Hattı'nın devamında meydana gelen ikinci ağır yıkım.",
            isTurkey = true
        ),
        HistoricalEarthquakeItem(
            id = "tr_5",
            title = "Van Depremi",
            magnitude = 7.2,
            date = "23 Ekim 2011",
            location = "Tabanlı, Van",
            depth = "16.0 km",
            casualties = "604 Kayıp",
            description = "Van ve Erciş ilçesinde binaların yıkılmasına yol açan, Doğu Anadolu'daki en şiddetli sismik sarsıntılardan biridir.",
            isTurkey = true
        ),
        HistoricalEarthquakeItem(
            id = "tr_6",
            title = "İzmir (Ege Denizi) Depremi",
            magnitude = 6.9,
            date = "30 Ekim 2020",
            location = "Seferihisar Açıkları, İzmir",
            depth = "16.5 km",
            casualties = "117 Kayıp",
            description = "Ege Denizi merkezli sarsıntı Seferihisar'da küçük çaplı tsunamiye, Bayraklı ilçesinde ise yüksek binaların çökmesine sebep oldu.",
            isTurkey = true
        )
    )
}

// 🌍 DÜNYA TARİHİ DEPREMLERİ VERİ KAYNAĞI
private fun getWorldHistoricalEarthquakes(): List<HistoricalEarthquakeItem> {
    return listOf(
        HistoricalEarthquakeItem(
            id = "world_1",
            title = "Büyük Şili Depremi (Valdivia)",
            magnitude = 9.5,
            date = "22 Mayıs 1960",
            location = "Valdivia, Şili",
            depth = "33.0 km",
            casualties = "6.000+ Kayıp",
            description = "Aletsel ölçüm tarihinde kaydedilmiş dünya üzerindeki EN BÜYÜK depremdir. Pasifik okyanusunda dev tsunami dalgaları oluşturmuştur.",
            isTurkey = false
        ),
        HistoricalEarthquakeItem(
            id = "world_2",
            title = "Büyük Alaska Depremi",
            magnitude = 9.2,
            date = "27 Mart 1964",
            location = "Prince William Sound, Alaska",
            depth = "25.0 km",
            casualties = "131 Kayıp",
            description = "Kuzey Amerika tarihinin en büyük sarsıntısı. Yaklaşık 4.5 dakika sürmüş ve büyük zemin kaymalarına yol açmıştır.",
            isTurkey = false
        ),
        HistoricalEarthquakeItem(
            id = "world_3",
            title = "Hint Okyanusu Depremi & Tsunami",
            magnitude = 9.1,
            date = "26 Aralık 2004",
            location = "Sumatra, Endonezya",
            depth = "30.0 km",
            casualties = "227.000+ Kayıp",
            description = "Deniz tabanındaki muazzam kırılma 30 metreyi bulan tsunamiye sebep olmuş, 14 ülkenin kıyılarını sular altında bırakmıştır.",
            isTurkey = false
        ),
        HistoricalEarthquakeItem(
            id = "world_4",
            title = "Tōhoku Depremi & Tsunami (Fukushima)",
            magnitude = 9.1,
            date = "11 Mart 2011",
            location = "Sendai, Japonya",
            depth = "29.0 km",
            casualties = "19.000+ Kayıp",
            description = "Japonya tarihinin en büyük sarsıntısı. Tsunami dalgaları Fukushima Nükleer Santrali'nde kazaya neden olmuştur.",
            isTurkey = false
        ),
        HistoricalEarthquakeItem(
            id = "world_5",
            title = "Haiti Depremi",
            magnitude = 7.0,
            date = "12 Ocak 2010",
            location = "Port-au-Prince, Haiti",
            depth = "13.0 km",
            casualties = "220.000+ Kayıp",
            description = "Büyüklüğü orta-yüksek olsa da başkentteki dayanıksız yapı stoku nedeniyle tarihin en çok can kaybına yol açan afetlerinden biri olmuştur.",
            isTurkey = false
        )
    )
}