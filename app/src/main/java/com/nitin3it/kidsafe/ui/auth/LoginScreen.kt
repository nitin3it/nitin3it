package com.nitin3it.kidsafe.ui.auth

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nitin3it.kidsafe.data.model.Role
import com.nitin3it.kidsafe.ui.components.GradientHeader
import com.nitin3it.kidsafe.ui.components.PrimaryButton
import com.nitin3it.kidsafe.ui.components.SectionCard
import com.nitin3it.kidsafe.ui.components.findActivity
import com.nitin3it.kidsafe.ui.theme.BrandGradient
import com.nitin3it.kidsafe.ui.theme.ChildGradient

@Composable
fun LoginScreen(
    role: Role,
    onSignedIn: () -> Unit,
    onChangeRole: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()

    LaunchedEffect(state.signedIn) {
        if (state.signedIn) {
            vm.onSignInHandled()
            onSignedIn()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState()),
    ) {
        GradientHeader(brush = if (role == Role.CHILD) ChildGradient else BrandGradient) {
            IconButton(onClick = onChangeRole) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Change role", tint = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (role == Role.CHILD) Icons.Outlined.ChildCare else Icons.Outlined.FamilyRestroom,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        if (role == Role.CHILD) "Child mode" else "Parent mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Text("Welcome! Let's sign in", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionCard {
                AnimatedContent(targetState = state.otpSentTo, label = "login-step") { sentTo ->
                    if (sentTo == null) {
                        PhoneAndGoogleStep(
                            loading = state.loading,
                            onGoogle = { activity?.let(vm::signInWithGoogle) },
                            onSendOtp = { phone -> activity?.let { vm.sendOtp(it, phone) } },
                        )
                    } else {
                        OtpStep(
                            phone = sentTo,
                            loading = state.loading,
                            resendInSeconds = state.resendInSeconds,
                            onVerify = vm::verifyOtp,
                            onResend = { activity?.let { vm.sendOtp(it, sentTo) } },
                            onEditNumber = vm::editPhoneNumber,
                        )
                    }
                }
            }

            state.error?.let { ErrorBanner(it) }

            Text(
                "Your account gets its own unique ${if (role == Role.CHILD) "Child" else "Parent"} ID after signing in.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun PhoneAndGoogleStep(
    loading: Boolean,
    onGoogle: () -> Unit,
    onSendOtp: (String) -> Unit,
) {
    var countryCode by rememberSaveable { mutableStateOf("+91") }
    var number by rememberSaveable { mutableStateOf("") }
    val valid = number.length in 6..14

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(
            onClick = onGoogle,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            GoogleMark()
            Spacer(Modifier.width(12.dp))
            Text("Continue with Google", color = MaterialTheme.colorScheme.onSurface)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text(
                "  or use mobile number  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = countryCode,
                onValueChange = { v -> countryCode = "+" + v.filter(Char::isDigit).take(4) },
                modifier = Modifier.width(88.dp),
                singleLine = true,
                label = { Text("Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = MaterialTheme.shapes.medium,
            )
            OutlinedTextField(
                value = number,
                onValueChange = { v -> number = v.filter(Char::isDigit).take(14) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Mobile number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = MaterialTheme.shapes.medium,
            )
        }

        PrimaryButton(
            text = "Send OTP",
            icon = Icons.Outlined.Sms,
            onClick = { onSendOtp(countryCode + number) },
            enabled = valid,
            loading = loading,
        )
    }
}

@Composable
private fun OtpStep(
    phone: String,
    loading: Boolean,
    resendInSeconds: Int,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    onEditNumber: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Enter verification code", style = MaterialTheme.typography.titleMedium)
        Text(
            "We sent a 6-digit code to $phone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = code,
            onValueChange = { v -> code = v.filter(Char::isDigit).take(6) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("● ● ● ● ● ●") },
            textStyle = TextStyle(fontSize = 24.sp, letterSpacing = 10.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            shape = MaterialTheme.shapes.medium,
        )
        PrimaryButton("Verify & continue", onClick = { onVerify(code) }, enabled = code.length == 6, loading = loading)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onEditNumber) { Text("Change number") }
            TextButton(onClick = onResend, enabled = resendInSeconds == 0 && !loading) {
                Text(if (resendInSeconds > 0) "Resend in ${resendInSeconds}s" else "Resend code")
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.width(10.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

/** Multi-coloured "G" so the Google button is recognisable without bundling the logo asset. */
@Composable
private fun GoogleMark() {
    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 20.sp)
}
