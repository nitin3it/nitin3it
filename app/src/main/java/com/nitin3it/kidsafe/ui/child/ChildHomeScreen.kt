package com.nitin3it.kidsafe.ui.child

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.provider.Settings
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nitin3it.kidsafe.data.model.UserProfile
import com.nitin3it.kidsafe.ui.components.AppUsageRow
import com.nitin3it.kidsafe.ui.components.GradientHeader
import com.nitin3it.kidsafe.ui.components.IconBadge
import com.nitin3it.kidsafe.ui.components.PrimaryButton
import com.nitin3it.kidsafe.ui.components.SectionCard
import com.nitin3it.kidsafe.ui.components.formatDuration
import com.nitin3it.kidsafe.ui.theme.Amber
import com.nitin3it.kidsafe.ui.theme.ChildGradient
import com.nitin3it.kidsafe.ui.theme.Teal

@Composable
fun ChildHomeScreen(profile: UserProfile, onSignOut: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: ChildViewModel = viewModel(key = profile.id, factory = viewModelFactory { initializer { ChildViewModel(app, profile) } })
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmSignOut by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        vm.onResume()
        onPauseOrDispose { }
    }

    val today = state.today
    val maxMs = today?.apps?.maxOfOrNull { it.totalMs } ?: 0L

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            GradientHeader(brush = ChildGradient) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Child mode", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                        Text("Hi, ${profile.displayName.substringBefore(' ')} 👋", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    }
                    IconButton(onClick = { confirmSignOut = true }) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Sign out", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(20.dp))
                ChildIdCard(state.profile.publicId)
                Spacer(Modifier.height(4.dp))
            }
        }

        if (!state.hasPermission) {
            item { PermissionCard(Modifier.padding(horizontal = 20.dp)) }
        } else {
            item {
                SyncCard(
                    syncing = state.syncing,
                    lastSyncAt = state.profile.lastSyncAt?.toDate()?.time,
                    message = state.message,
                    onSync = vm::syncNow,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }

        if (today != null) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text("Today on this phone", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(formatDuration(today.totalMs), style = MaterialTheme.typography.titleMedium, color = Teal)
                }
            }
            items(today.apps, key = { it.packageName }) { app ->
                AppUsageRow(app, maxMs, Modifier.padding(horizontal = 20.dp))
            }
        }

        item {
            Text(
                "Your parent can see the apps you use and for how long.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding(),
            )
        }
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text("Usage will stop being shared with your parent until you sign in again.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text("Sign out") } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ChildIdCard(publicId: String) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Color.White.copy(alpha = 0.16f))
            .padding(18.dp),
    ) {
        Text("Your Child ID", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
        Spacer(Modifier.height(6.dp))
        Text(
            publicId,
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Ask your parent to enter this ID in their KidSafe app.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = {
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                clipboard.setPrimaryClip(ClipData.newPlainText("KidSafe Child ID", publicId))
                Toast.makeText(context, "Child ID copied", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy")
            }
            FilledTonalButton(onClick = {
                val send = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "My KidSafe Child ID is $publicId — add it in the KidSafe app (Parent mode).")
                context.startActivity(Intent.createChooser(send, "Share Child ID"))
            }) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share")
            }
        }
    }
}

@Composable
private fun PermissionCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    SectionCard(modifier, containerColor = Amber.copy(alpha = 0.12f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.WarningAmber, Amber)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("One more step", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Allow \"Usage access\" so KidSafe can share app usage with your parent.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Allow usage access",
            onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "In the list, open KidSafe and turn on \"Permit usage access\", then come back.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SyncCard(
    syncing: Boolean,
    lastSyncAt: Long?,
    message: String?,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.CheckCircle, Teal)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Sharing is on", style = MaterialTheme.typography.titleMedium)
                Text(
                    lastSyncAt?.let { "Last synced " + DateUtils.getRelativeTimeSpanString(it) } ?: "Not synced yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (syncing) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.5.dp)
            } else {
                IconButton(onClick = onSync) {
                    Icon(Icons.Outlined.Sync, contentDescription = "Sync now", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (message != null) {
            Spacer(Modifier.height(10.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
