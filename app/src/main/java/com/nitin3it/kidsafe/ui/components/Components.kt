package com.nitin3it.kidsafe.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.nitin3it.kidsafe.ui.theme.BrandGradient
import kotlin.math.absoluteValue

/** Rounded gradient header that sits under the status bar. */
@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    brush: Brush = BrandGradient,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(brush)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        content = content,
    )
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.onPrimary)
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text)
        }
    }
}

/** Icon in a soft tinted circle. */
@Composable
fun IconBadge(icon: ImageVector, tint: Color, size: Dp = 48.dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

private val avatarColors = listOf(
    Color(0xFF5B5BF7), Color(0xFF00BFA6), Color(0xFFFF7A59), Color(0xFF8E54E9),
    Color(0xFF2E8BFF), Color(0xFFE5484D), Color(0xFFFFB020), Color(0xFF12A150),
)

fun colorFor(key: String): Color = avatarColors[key.hashCode().absoluteValue % avatarColors.size]

/** Circle with the first letter of [name]. */
@Composable
fun LetterAvatar(name: String, size: Dp = 44.dp, color: Color = colorFor(name)) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp,
        )
    }
}

/** Shows the real app icon when the app is installed on this phone, otherwise a letter avatar. */
@Composable
fun AppIcon(packageName: String, appName: String, size: Dp = 44.dp) {
    val context = LocalContext.current
    val icon: ImageBitmap? = remember(packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(packageName).toBitmap(128, 128).asImageBitmap()
        }.getOrNull()
    }
    if (icon != null) {
        Image(icon, contentDescription = null, modifier = Modifier.size(size).clip(RoundedCornerShape(size * 0.28f)))
    } else {
        LetterAvatar(appName, size, colorFor(packageName))
    }
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxWidth().padding(top = 160.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = tint)
    }
}

fun formatDuration(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        ms > 0 -> "<1m"
        else -> "0m"
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
