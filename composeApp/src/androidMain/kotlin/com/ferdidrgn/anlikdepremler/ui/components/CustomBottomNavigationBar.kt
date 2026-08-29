package com.ferdidrgn.anlikdepremler.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdidrgn.anlikdepremler.navigation.Screen
import com.ferdidrgn.anlikdepremler.navigation.bottomNavItems

@Composable
fun CustomBottomNavigationBar(
    currentRoute: String?,
    onNavItemClick: (Screen) -> Unit
) {
    // 🎯 Alt Gezinme Çubuğu İçin Inset Güvenlik Kalkanı
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val title = stringResource(screen.titleRes)

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "iconScale"
                    )

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        animationSpec = tween(durationMillis = 300),
                        label = "containerColor"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 300),
                        label = "contentColor"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(containerColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isSelected) {
                                    onNavItemClick(screen)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = title,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )

                            AnimatedVisibility(
                                visible = isSelected,
                                enter = expandHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeIn(),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        color = contentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
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