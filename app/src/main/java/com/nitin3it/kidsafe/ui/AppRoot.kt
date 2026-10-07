package com.nitin3it.kidsafe.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nitin3it.kidsafe.data.model.Role
import com.nitin3it.kidsafe.ui.auth.LoginScreen
import com.nitin3it.kidsafe.ui.child.ChildHomeScreen
import com.nitin3it.kidsafe.ui.components.IconBadge
import com.nitin3it.kidsafe.ui.components.LoadingScreen
import com.nitin3it.kidsafe.ui.components.PrimaryButton
import com.nitin3it.kidsafe.ui.onboarding.RoleSelectScreen
import com.nitin3it.kidsafe.ui.parent.ParentDashboardScreen

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "app-state",
        ) { s ->
            when (s) {
                AppState.Loading -> LoadingScreen()
                AppState.FirebaseNotConfigured -> MessageScreen(
                    icon = Icons.Outlined.Settings,
                    title = "Almost there",
                    message = "Firebase isn't configured yet. Add your google-services.json to the app " +
                        "module and rebuild — see the README for the 5-minute setup.",
                )
                AppState.ChooseRole -> RoleSelectScreen(onRoleChosen = vm::chooseRole)
                is AppState.SignIn -> LoginScreen(
                    role = s.role,
                    onSignedIn = vm::refresh,
                    onChangeRole = vm::changeRole,
                )
                is AppState.Ready -> when (s.role) {
                    Role.CHILD -> ChildHomeScreen(profile = s.profile, onSignOut = vm::signOut)
                    Role.PARENT -> ParentDashboardScreen(profile = s.profile, onSignOut = vm::signOut)
                }
                is AppState.Error -> MessageScreen(
                    icon = Icons.Outlined.CloudOff,
                    title = "Couldn't connect",
                    message = s.message,
                    actionLabel = "Try again",
                    onAction = vm::refresh,
                    secondaryLabel = "Sign out",
                    onSecondary = vm::signOut,
                )
            }
        }
    }
}

@Composable
private fun MessageScreen(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(icon, MaterialTheme.colorScheme.primary, size = 88.dp)
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null) {
            Spacer(Modifier.height(28.dp))
            PrimaryButton(actionLabel, onAction)
        }
        if (secondaryLabel != null) {
            TextButton(onClick = onSecondary) { Text(secondaryLabel) }
        }
    }
}
