package com.nitin3it.kidsafe.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nitin3it.kidsafe.data.model.AppUsage

/** One app in a usage list: icon, name, time and a bar relative to the most-used app. */
@Composable
fun AppUsageRow(app: AppUsage, maxMs: Long, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        if (maxMs > 0) (app.totalMs.toFloat() / maxMs).coerceIn(0.02f, 1f) else 0f,
        label = "usage-bar",
    )
    val accent = colorFor(app.packageName)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(app.packageName, app.appName)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(formatDuration(app.totalMs), style = MaterialTheme.typography.titleSmall, color = accent)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {},
                )
                if (app.launches > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Opened ${app.launches} ${if (app.launches == 1) "time" else "times"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
