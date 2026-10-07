package com.nitin3it.kidsafe.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nitin3it.kidsafe.data.model.Role
import com.nitin3it.kidsafe.ui.components.IconBadge
import com.nitin3it.kidsafe.ui.components.PrimaryButton
import com.nitin3it.kidsafe.ui.theme.BrandGradient
import com.nitin3it.kidsafe.ui.theme.Indigo
import com.nitin3it.kidsafe.ui.theme.Teal

@Composable
fun RoleSelectScreen(onRoleChosen: (Role) -> Unit) {
    var selected by rememberSaveable { mutableStateOf<Role?>(null) }

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandGradient)
                .statusBarsPadding()
                .padding(top = 40.dp, bottom = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(84.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(46.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("KidSafe", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Healthy screen time for the whole family",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Who is using this phone?", style = MaterialTheme.typography.titleLarge)
            Text(
                "Choose once — you can change it later by signing out.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            RoleCard(
                icon = Icons.Outlined.FamilyRestroom,
                title = "I'm a Parent",
                subtitle = "See which apps your child uses and for how long.",
                accent = Indigo,
                selected = selected == Role.PARENT,
                onClick = { selected = Role.PARENT },
            )
            RoleCard(
                icon = Icons.Outlined.ChildCare,
                title = "This is my Child's phone",
                subtitle = "Share app usage with a parent using a unique Child ID.",
                accent = Teal,
                selected = selected == Role.CHILD,
                onClick = { selected = Role.CHILD },
            )
        }

        PrimaryButton(
            text = "Continue",
            onClick = { selected?.let(onRoleChosen) },
            enabled = selected != null,
            modifier = Modifier.navigationBarsPadding().padding(20.dp),
        )
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border by animateColorAsState(
        if (selected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        label = "role-border",
    )
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(if (selected) 2.dp else 1.dp, border),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) accent.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, accent, size = 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                )
            }
            if (selected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = accent)
            }
        }
    }
}
