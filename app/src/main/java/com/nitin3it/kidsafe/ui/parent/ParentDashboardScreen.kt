package com.nitin3it.kidsafe.ui.parent

import android.text.format.DateUtils
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nitin3it.kidsafe.data.model.DailyUsage
import com.nitin3it.kidsafe.data.model.UserProfile
import com.nitin3it.kidsafe.ui.components.AppUsageRow
import com.nitin3it.kidsafe.ui.components.GradientHeader
import com.nitin3it.kidsafe.ui.components.IconBadge
import com.nitin3it.kidsafe.ui.components.InfoRow
import com.nitin3it.kidsafe.ui.components.LetterAvatar
import com.nitin3it.kidsafe.ui.components.LoadingScreen
import com.nitin3it.kidsafe.ui.components.PrimaryButton
import com.nitin3it.kidsafe.ui.components.SectionCard
import com.nitin3it.kidsafe.ui.components.formatDuration
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ParentDashboardScreen(profile: UserProfile, onSignOut: () -> Unit) {
    val vm: ParentViewModel = viewModel(key = profile.id, factory = viewModelFactory { initializer { ParentViewModel(profile) } })
    val state by vm.state.collectAsStateWithLifecycle()
    val addChild by vm.addChild.collectAsStateWithLifecycle()
    var confirmSignOut by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf<UserProfile?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            if (state.children.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = vm::openAddChild,
                    icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
                    text = { Text("Add child") },
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
        },
    ) { padding ->
        val day = state.selectedDay
        val maxMs = day?.apps?.maxOfOrNull { it.totalMs } ?: 0L

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                GradientHeader {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Parent dashboard", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                            Text(
                                "Hello, ${state.parent.displayName.substringBefore(' ')}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                            )
                            Text(
                                "Parent ID · ${state.parent.publicId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f),
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        IconButton(onClick = { confirmSignOut = true }) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Sign out", tint = Color.White)
                        }
                    }
                    if (state.children.isNotEmpty()) {
                        Spacer(Modifier.height(18.dp))
                        ChildSelector(
                            children = state.children,
                            selectedId = state.selectedChild?.id,
                            onSelect = vm::selectChild,
                            onAdd = vm::openAddChild,
                        )
                    }
                }
            }

            val child = state.selectedChild
            when {
                state.loadingChildren -> item { LoadingScreen() }
                child == null -> item { EmptyState(onAdd = vm::openAddChild) }
                else -> {
                    item {
                        ChildSummaryCard(
                            child = child,
                            onRemove = { confirmRemove = child },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                    item {
                        WeekCard(
                            week = state.week,
                            selectedDate = state.selectedDate,
                            onSelect = vm::selectDate,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.Bottom) {
                            Text(dayTitle(state.selectedDate), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(
                                "${day?.apps?.size ?: 0} apps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (day == null || day.apps.isEmpty()) {
                        item { NoUsage(Modifier.padding(horizontal = 20.dp)) }
                    } else {
                        items(day.apps, key = { it.packageName }) { app ->
                            AppUsageRow(app, maxMs, Modifier.padding(horizontal = 20.dp))
                        }
                    }
                }
            }
            state.error?.let { msg ->
                item {
                    Text(
                        msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }

    if (addChild.open) {
        AddChildDialog(
            busy = addChild.busy,
            error = addChild.error,
            onAdd = vm::addChild,
            onDismiss = vm::closeAddChild,
        )
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text("Your linked children stay saved. Sign in with the same account to see them again.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text("Sign out") } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel") } },
        )
    }

    confirmRemove?.let { child ->
        AlertDialog(
            onDismissRequest = { confirmRemove = null },
            title = { Text("Remove ${child.displayName}?") },
            text = { Text("You'll stop seeing their app usage. You can add them again with their Child ID.") },
            confirmButton = {
                TextButton(onClick = { vm.removeChild(child.id); confirmRemove = null }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ChildSelector(
    children: List<UserProfile>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(children, key = { it.id }) { child ->
            val selected = child.id == selectedId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp).clip(MaterialTheme.shapes.small).clickable { onSelect(child.id) },
            ) {
                Box(
                    Modifier
                        .border(BorderStroke(if (selected) 3.dp else 0.dp, Color.White), CircleShape)
                        .padding(4.dp),
                ) {
                    LetterAvatar(child.displayName, size = 52.dp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    child.displayName.substringBefore(' '),
                    color = Color.White.copy(alpha = if (selected) 1f else 0.75f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp).clip(MaterialTheme.shapes.small).clickable(onClick = onAdd),
            ) {
                Box(
                    Modifier.padding(4.dp).size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.height(6.dp))
                Text("Add", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ChildSummaryCard(child: UserProfile, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    var menu by remember { mutableStateOf(false) }
    SectionCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LetterAvatar(child.displayName, size = 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(child.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                child.deviceModel?.let { InfoRow(Icons.Outlined.PhoneAndroid, it) }
                InfoRow(
                    Icons.Outlined.Sync,
                    child.lastSyncAt?.toDate()?.time
                        ?.let { "Updated " + DateUtils.getRelativeTimeSpanString(it) }
                        ?: "Waiting for first sync from child's phone",
                )
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, contentDescription = "More") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Remove child") },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                        onClick = { menu = false; onRemove() },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekCard(
    week: List<DailyUsage>,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = week.firstOrNull { it.date == selectedDate.toString() }
    val maxMs = week.maxOfOrNull { it.totalMs }?.takeIf { it > 0 } ?: 1L
    val nonZero = week.filter { it.totalMs > 0 }
    val average = if (nonZero.isEmpty()) 0L else nonZero.sumOf { it.totalMs } / nonZero.size

    SectionCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.Schedule, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Screen time · ${dayTitle(selectedDate)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDuration(selected?.totalMs ?: 0), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Daily avg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDuration(average), style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth().height(140.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            week.forEach { day ->
                val date = LocalDate.parse(day.date)
                val isSelected = date == selectedDate
                val fraction by animateFloatAsState((day.totalMs.toFloat() / maxMs).coerceAtLeast(0.03f), label = "bar")
                Column(
                    Modifier.weight(1f).fillMaxHeight().clip(MaterialTheme.shapes.small).clickable { onSelect(date) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                        Box(
                            Modifier
                                .width(22.dp)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                ),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(Icons.Outlined.PersonAdd, MaterialTheme.colorScheme.primary, size = 96.dp)
        Spacer(Modifier.height(24.dp))
        Text("Add your first child", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Install KidSafe on your child's phone, choose \"Child\" and sign in. " +
                "Then enter the Child ID shown on their screen here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton("Enter Child ID", onClick = onAdd, icon = Icons.Outlined.Add)
    }
}

@Composable
private fun NoUsage(modifier: Modifier = Modifier) {
    SectionCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.HourglassEmpty, MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(14.dp))
            Text(
                "No app usage recorded for this day yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddChildDialog(
    busy: Boolean,
    error: String?,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
        title = { Text("Add a child") },
        text = {
            Column {
                Text(
                    "Open KidSafe on your child's phone and type the Child ID shown there.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { v -> code = v.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(9) },
                    singleLine = true,
                    label = { Text("Child ID") },
                    placeholder = { Text("K7QM-29TX") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, letterSpacing = 2.sp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(code) }, enabled = !busy && code.isNotBlank()) {
                Text(if (busy) "Checking…" else "Add child")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}

private fun dayTitle(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) + ", " +
            date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + date.dayOfMonth
    }
}
