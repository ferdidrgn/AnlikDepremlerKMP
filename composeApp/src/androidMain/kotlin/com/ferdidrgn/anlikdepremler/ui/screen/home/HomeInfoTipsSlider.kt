package com.ferdidrgn.anlikdepremler.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ferdi.deprem.model.InfoCardItem
import com.ferdidrgn.anlikdepremler.R

@Composable
fun InformationTipsSliderSection() {
    val localInfoCards = remember {
        listOf(
            InfoCardItem(
                1,
                "Deprem Çantası Hazırlığı",
                "Hayatta kalma çantanızda bulunması gereken temel malzemeler.",
                "Hazırlık",
                "https://picsum.photos/600/400?random=10"
            ),
            InfoCardItem(
                2,
                "Çök - Kapan - Tutun",
                "Deprem anında doğru pozisyon hayat kurtarır.",
                "Güvenlik",
                "https://picsum.photos/600/400?random=11"
            ),
            InfoCardItem(
                3,
                "Bina Sağlamlığı Testi",
                "Oturduğunuz binanın risk analizini nasıl yaptırırsınız?",
                "Bilinç",
                "https://picsum.photos/600/400?random=12"
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.info_tips_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.info_tips_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(localInfoCards) { item ->
                    Card(
                        modifier = Modifier
                            .width(230.dp)
                            .height(150.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.description,
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 10.sp,
                                        maxLines = 1
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
