package com.teacher.journal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// iOS-inspired neutrals
val AppleSeparator = Color(0xFFE5E5EA)
val AppleLabelSecondary = Color(0xFF8E8E93)
val AppleLabelTertiary = Color(0xFFC7C7CC)
val AppleFill = Color(0xFFF2F2F7)

// Divider inset presets — align with ListRow's `padding(16) + leading + spacer(14)`
object AppleInset {
    val Full = 16.dp           // no leading — divider from card left+16
    val LeadingIcon = 50.dp    // 20dp icon leading  (16 + 20 + 14)
    val Avatar = 70.dp         // 40dp avatar/square leading (16 + 40 + 14)
    val DateChip = 66.dp       // 36dp date chip leading (16 + 36 + 14)
    val ThreeSwatch = 86.dp    // 3×16dp dots + 2×4dp gaps (16 + 56 + 14)
}

/**
 * iOS-style large title. Placed inside a scrolling column, above content.
 */
@Composable
fun LargeTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(
            text = title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 15.sp, color = AppleLabelSecondary)
        }
    }
}

/** Section header in a grouped list. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppleLabelSecondary,
            modifier = Modifier.weight(1f)
        )
        if (action != null) action()
    }
}

/** Grouped white card. Put ListRow / RowDivider inside. */
@Composable
fun GroupedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Standard iOS list row. Leading icon → title (+ subtitle) → trailing → optional chevron.
 */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                color = titleColor,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = AppleLabelSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
        if (showChevron) {
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AppleLabelTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RowDivider(startInset: Dp = 60.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startInset),
        thickness = 0.5.dp,
        color = AppleSeparator
    )
}

/** Rounded-square tinted icon (iOS Settings style). */
@Composable
fun SquareIcon(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    background: Color = tint,
    size: Dp = 30.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(7.dp)).background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}

/** Trailing value text (right-aligned, secondary color) — for ListRow trailing. */
@Composable
fun TrailingValue(text: String, color: Color = AppleLabelSecondary) {
    Text(text, fontSize = 15.sp, color = color, maxLines = 1)
}

/** Trailing pill badge — for ListRow trailing. */
@Composable
fun TrailingPill(text: String, fg: Color, bg: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}
