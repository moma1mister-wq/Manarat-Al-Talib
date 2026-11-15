package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleMedium
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSoft

enum class SectionIconType {
    APP_LOGO,
    SCHEDULE,
    QURAN,
    EXERCISES,
    PRICES_HISN,
    PRAYER,
    ADHKAR
}

/**
 * Full-screen Lavender-to-Royal-Purple gradient with smooth S-curve waves and subtle luminous bokeh.
 */
@Composable
fun FullScreenPurpleLoginBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    PurpleSoft,
                    PurpleMedium,
                    PurplePrimary,
                    PurpleDeep
                )
            )
        )

        // Ambient emerald & gold radial highlights
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(EmeraldPrimary.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(w * 0.85f, h * 0.15f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.85f, h * 0.15f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GoldAccent.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(w * 0.18f, h * 0.22f),
                radius = w * 0.45f
            ),
            radius = w * 0.45f,
            center = Offset(w * 0.18f, h * 0.22f)
        )

        // Left/Top sweeping soft light wave
        val upperWave = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.72f, 0f)
            cubicTo(
                w * 0.52f, h * 0.20f,
                w * 0.18f, h * 0.25f,
                0f, h * 0.44f
            )
            close()
        }
        drawPath(
            path = upperWave,
            color = Color.White.copy(alpha = 0.09f)
        )

        // Right/Bottom sweeping S-curve wave
        val rightWave = Path().apply {
            moveTo(w, h * 0.24f)
            cubicTo(
                w * 0.76f, h * 0.36f,
                w * 0.92f, h * 0.66f,
                w * 0.62f, h * 0.88f
            )
            cubicTo(
                w * 0.50f, h * 0.95f,
                w * 0.42f, h * 0.98f,
                w * 0.35f, h
            )
            lineTo(w, h)
            close()
        }
        drawPath(
            path = rightWave,
            color = Color.White.copy(alpha = 0.10f)
        )
    }
}

/**
 * Luxury Professional Brand Emblem Badge replacing the old cartoon icon.
 * Displays the high-resolution `R.drawable.img_app_logo_emblem` inside a frosted glass & gold-rimmed squircle.
 */
@Composable
fun WhiteRoundedAppLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 112.dp
) {
    Box(
        modifier = modifier
            .size(size + 14.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .border(
                BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.75f),
                            GoldAccent.copy(alpha = 0.65f),
                            EmeraldPrimary.copy(alpha = 0.65f)
                        )
                    )
                ),
                shape = RoundedCornerShape(34.dp)
            )
            .padding(7.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(16.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = PurpleDeep
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_logo_emblem),
                contentDescription = "شعار تطبيق منارة الطالب",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Curved Purple Header Banner matching the Left & Right Phones in the reference screenshot:
 * Smooth lavender-to-violet gradient, rounded bottom corners, plus optional geometric diamond grid overlay.
 */
@Composable
fun PurpleWaveBackground(
    modifier: Modifier = Modifier,
    height: Dp = 260.dp,
    showDiamondGrid: Boolean = true
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height

        // Rounded bottom header shape matching the Left & Right screens in the reference image
        val headerPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * 0.80f)
            quadraticTo(w, h, w * 0.82f, h * 0.96f)
            cubicTo(
                w * 0.58f, h * 0.91f,
                w * 0.35f, h * 1.02f,
                0f, h * 0.88f
            )
            close()
        }

        drawPath(
            path = headerPath,
            brush = Brush.linearGradient(
                colors = listOf(PurpleSoft, PurpleMedium, PurplePrimary, PurpleDeep),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        // Subtle soft S-wave highlight
        val softWave = Path().apply {
            moveTo(0f, h * 0.35f)
            cubicTo(
                w * 0.35f, h * 0.15f,
                w * 0.65f, h * 0.75f,
                w, h * 0.42f
            )
            lineTo(w, 0f)
            lineTo(0f, 0f)
            close()
        }
        drawPath(
            path = softWave,
            color = Color.White.copy(alpha = 0.08f)
        )

        // Subtle rotated rounded-square diamond grid lines (from the Right Phone in the reference image)
        if (showDiamondGrid) {
            val gridStroke = Stroke(width = 2.dp.toPx())
            rotate(degrees = 45f, pivot = Offset(w * 0.65f, h * 0.35f)) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.10f),
                    topLeft = Offset(w * 0.45f, -h * 0.05f),
                    size = Size(w * 0.42f, w * 0.42f),
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    style = gridStroke
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.08f),
                    topLeft = Offset(w * 0.15f, h * 0.18f),
                    size = Size(w * 0.38f, w * 0.38f),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    style = gridStroke
                )
            }
        }
    }
}

/**
 * Top Header Action Bar from the Left & Right Phones:
 * Three dots `• • •` on one side and a clean white circular profile/action icon on the other.
 */
@Composable
fun ReferenceHeaderTopBar(
    onProfileOrSettingsClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onProfileOrSettingsClick() },
                shape = RoundedCornerShape(14.dp),
                color = PurpleDeep,
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.65f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_logo_emblem),
                    contentDescription = "شعار منارة الطالب",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { onProfileOrSettingsClick() },
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.92f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "الملف الشخصي",
                        tint = PurplePrimary,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable { onMoreClick() },
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "المزيد",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Mint-Green Circular Icon Badge (matching the green paper-plane circle in the reference cards).
 */
@Composable
fun MintCircleIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    icon: ImageVector = Icons.Default.Navigation
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(EmeraldPrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

/**
 * Royal-Purple Circular Icon Badge (matching the purple location-pin circle in the reference cards).
 */
@Composable
fun PurpleCircleIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    icon: ImageVector = Icons.Default.LocationOn
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(PurplePrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

/**
 * Vertical dotted connector line between the Mint-Green circle and the Purple circle inside cards.
 */
@Composable
fun VerticalConnectorDots(
    modifier: Modifier = Modifier,
    height: Dp = 18.dp
) {
    Canvas(
        modifier = modifier
            .width(32.dp)
            .height(height)
    ) {
        val cx = size.width / 2f
        val dotCount = 3
        val step = size.height / (dotCount + 1)
        for (i in 1..dotCount) {
            drawCircle(
                color = PurpleSoft.copy(alpha = 0.55f),
                radius = 2.2.dp.toPx(),
                center = Offset(cx, step * i)
            )
        }
    }
}

/**
 * Custom Bespoke Vector/Canvas Icon for every section of the app in the unified Purple & Mint style.
 */
@Composable
fun CustomSectionIcon(
    type: SectionIconType,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(PurpleMedium, PurplePrimary, PurpleDeep)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.64f)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = Stroke(width = w * 0.085f, cap = StrokeCap.Round)

            when (type) {
                SectionIconType.APP_LOGO -> {
                    val cap = Path().apply {
                        moveTo(w * 0.5f, h * 0.08f)
                        lineTo(w * 0.95f, h * 0.28f)
                        lineTo(w * 0.5f, h * 0.48f)
                        lineTo(w * 0.05f, h * 0.28f)
                        close()
                    }
                    drawPath(cap, color = EmeraldLight)
                    val book = Path().apply {
                        moveTo(w * 0.15f, h * 0.52f)
                        lineTo(w * 0.48f, h * 0.62f)
                        lineTo(w * 0.48f, h * 0.92f)
                        lineTo(w * 0.15f, h * 0.82f)
                        close()
                        moveTo(w * 0.85f, h * 0.52f)
                        lineTo(w * 0.52f, h * 0.62f)
                        lineTo(w * 0.52f, h * 0.92f)
                        lineTo(w * 0.85f, h * 0.82f)
                        close()
                    }
                    drawPath(book, color = Color.White)
                }

                SectionIconType.SCHEDULE -> {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.1f, h * 0.15f),
                        size = Size(w * 0.8f, h * 0.75f),
                        cornerRadius = CornerRadius(w * 0.15f, w * 0.15f)
                    )
                    drawRoundRect(
                        color = EmeraldPrimary,
                        topLeft = Offset(w * 0.1f, h * 0.15f),
                        size = Size(w * 0.8f, h * 0.24f),
                        cornerRadius = CornerRadius(w * 0.15f, w * 0.15f)
                    )
                    val check = Path().apply {
                        moveTo(w * 0.30f, h * 0.62f)
                        lineTo(w * 0.45f, h * 0.76f)
                        lineTo(w * 0.72f, h * 0.50f)
                    }
                    drawPath(check, color = PurplePrimary, style = stroke)
                }

                SectionIconType.QURAN -> {
                    val stand = Path().apply {
                        moveTo(w * 0.15f, h * 0.88f)
                        lineTo(w * 0.85f, h * 0.52f)
                        moveTo(w * 0.85f, h * 0.88f)
                        lineTo(w * 0.15f, h * 0.52f)
                    }
                    drawPath(stand, color = EmeraldLight, style = stroke)
                    val pages = Path().apply {
                        moveTo(w * 0.12f, h * 0.42f)
                        quadraticTo(w * 0.3f, h * 0.35f, w * 0.5f, h * 0.48f)
                        quadraticTo(w * 0.7f, h * 0.35f, w * 0.88f, h * 0.42f)
                    }
                    drawPath(pages, color = Color.White, style = stroke)
                    drawCircle(
                        color = GoldAccent,
                        radius = w * 0.13f,
                        center = Offset(w * 0.5f, h * 0.2f)
                    )
                }

                SectionIconType.EXERCISES -> {
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.22f, h * 0.5f),
                        end = Offset(w * 0.78f, h * 0.5f),
                        strokeWidth = w * 0.11f,
                        cap = StrokeCap.Round
                    )
                    drawRoundRect(
                        color = EmeraldLight,
                        topLeft = Offset(w * 0.08f, h * 0.25f),
                        size = Size(w * 0.14f, h * 0.5f),
                        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                    )
                    drawRoundRect(
                        color = EmeraldLight,
                        topLeft = Offset(w * 0.78f, h * 0.25f),
                        size = Size(w * 0.14f, h * 0.5f),
                        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                    )
                }

                SectionIconType.PRICES_HISN -> {
                    val tag = Path().apply {
                        moveTo(w * 0.15f, h * 0.2f)
                        lineTo(w * 0.65f, h * 0.2f)
                        lineTo(w * 0.9f, h * 0.5f)
                        lineTo(w * 0.65f, h * 0.8f)
                        lineTo(w * 0.15f, h * 0.8f)
                        close()
                    }
                    drawPath(tag, color = EmeraldPrimary)
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.09f,
                        center = Offset(w * 0.35f, h * 0.5f)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.52f, h * 0.42f),
                        end = Offset(w * 0.72f, h * 0.42f),
                        strokeWidth = w * 0.07f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.52f, h * 0.58f),
                        end = Offset(w * 0.66f, h * 0.58f),
                        strokeWidth = w * 0.07f,
                        cap = StrokeCap.Round
                    )
                }

                SectionIconType.PRAYER -> {
                    val dome = Path().apply {
                        moveTo(w * 0.18f, h * 0.78f)
                        quadraticTo(w * 0.18f, h * 0.35f, w * 0.5f, h * 0.24f)
                        quadraticTo(w * 0.82f, h * 0.35f, w * 0.82f, h * 0.78f)
                        close()
                    }
                    drawPath(dome, color = Color.White)
                    drawLine(
                        color = EmeraldLight,
                        start = Offset(w * 0.1f, h * 0.82f),
                        end = Offset(w * 0.9f, h * 0.82f),
                        strokeWidth = w * 0.09f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = GoldAccent,
                        radius = w * 0.07f,
                        center = Offset(w * 0.5f, h * 0.14f)
                    )
                }

                SectionIconType.ADHKAR -> {
                    val beadCount = 8
                    val radius = w * 0.32f
                    val center = Offset(w * 0.5f, h * 0.48f)
                    for (i in 0 until beadCount) {
                        val angle = (2 * Math.PI * i / beadCount)
                        val bx = center.x + (radius * kotlin.math.cos(angle)).toFloat()
                        val by = center.y + (radius * kotlin.math.sin(angle)).toFloat()
                        drawCircle(
                            color = if (i % 2 == 0) EmeraldLight else Color.White,
                            radius = w * 0.08f,
                            center = Offset(bx, by)
                        )
                    }
                    drawLine(
                        color = GoldAccent,
                        start = Offset(w * 0.5f, h * 0.8f),
                        end = Offset(w * 0.5f, h * 0.95f),
                        strokeWidth = w * 0.08f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
