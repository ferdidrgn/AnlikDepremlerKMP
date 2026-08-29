package com.ferdidrgn.anlikdepremler.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.ui.components.RequestAppPermissions
import kotlinx.coroutines.launch

data class OnboardingItemData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val badgeTitle: String,
    val floatingCardTitle: String,
    val floatingCardSubtitle: String,
    val floatingCardImageUrl: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    var showPermissionDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        RequestAppPermissions(
            onPermissionsGranted = {
                showPermissionDialog = false
                onFinishOnboarding()
            }
        )
    }

    val onboardingPagesList = remember {
        listOf(
            OnboardingItemData(
                title = "onboarding_title_1",
                description = "onboarding_desc_1",
                imageUrl = "https://images.unsplash.com/photo-1524661135-423995f22d0b?q=80&w=1200&auto=format&fit=crop",
                badgeTitle = "onboarding_badge_1",
                floatingCardTitle = "onboarding_float_title_1",
                floatingCardSubtitle = "onboarding_float_sub_1",
                floatingCardImageUrl = "https://picsum.photos/200/200?random=10"
            ),
            OnboardingItemData(
                title = "onboarding_title_2",
                description = "onboarding_desc_2",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=1200&auto=format&fit=crop",
                badgeTitle = "onboarding_badge_2",
                floatingCardTitle = "onboarding_float_title_2",
                floatingCardSubtitle = "onboarding_float_sub_2",
                floatingCardImageUrl = "https://picsum.photos/200/200?random=20"
            ),
            OnboardingItemData(
                title = "onboarding_title_3",
                description = "onboarding_desc_3",
                imageUrl = "https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?q=80&w=1200&auto=format&fit=crop",
                badgeTitle = "onboarding_badge_3",
                floatingCardTitle = "onboarding_float_title_3",
                floatingCardSubtitle = "onboarding_float_sub_3",
                floatingCardImageUrl = "https://picsum.photos/200/200?random=30"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { onboardingPagesList.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingFullImagePage(
                data = onboardingPagesList[pageIndex],
                isLastPage = pageIndex == onboardingPagesList.size - 1
            )
        }

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.45f)
            ) {
                Text(
                    text = stringResource(R.string.app_branding),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Text(
                text = stringResource(R.string.onboarding_skip),
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { showPermissionDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPagesList.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    if (pagerState.currentPage < onboardingPagesList.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        showPermissionDialog = true
                    }
                },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = if (pagerState.currentPage == onboardingPagesList.size - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.onboarding_next),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingFullImagePage(
    data: OnboardingItemData,
    isLastPage: Boolean
) {
    val titleRes = when (data.title) {
        "onboarding_title_1" -> R.string.onboarding_title_1
        "onboarding_title_2" -> R.string.onboarding_title_2
        else -> R.string.onboarding_title_3
    }
    val descRes = when (data.description) {
        "onboarding_desc_1" -> R.string.onboarding_desc_1
        "onboarding_desc_2" -> R.string.onboarding_desc_2
        else -> R.string.onboarding_desc_3
    }
    val badgeRes = when (data.badgeTitle) {
        "onboarding_badge_1" -> R.string.onboarding_badge_1
        "onboarding_badge_2" -> R.string.onboarding_badge_2
        else -> R.string.onboarding_badge_3
    }
    val floatTitleRes = when (data.floatingCardTitle) {
        "onboarding_float_title_1" -> R.string.onboarding_float_title_1
        "onboarding_float_title_2" -> R.string.onboarding_float_title_2
        else -> R.string.onboarding_float_title_3
    }
    val floatSubRes = when (data.floatingCardSubtitle) {
        "onboarding_float_sub_1" -> R.string.onboarding_float_sub_1
        "onboarding_float_sub_2" -> R.string.onboarding_float_sub_2
        else -> R.string.onboarding_float_sub_3
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = data.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 105.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = stringResource(badgeRes),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.displayLarge,
                fontSize = if (isLastPage) 38.sp else 34.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = if (isLastPage) 44.sp else 40.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = if (isLastPage) 16.sp else 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.18f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = data.floatingCardImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(floatTitleRes),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(floatSubRes),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}