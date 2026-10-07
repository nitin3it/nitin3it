package com.nitin3it.kidsafe.ui.auth

import android.app.Activity
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.nitin3it.kidsafe.data.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    /** Set once an OTP has been sent; the screen then asks for the code. */
    val otpSentTo: String? = null,
    val resendInSeconds: Int = 0,
    val signedIn: Boolean = false,
)

class AuthViewModel : ViewModel() {

    private val auth = AuthRepository()
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun signInWithGoogle(activity: Activity) = runAuth {
        try {
            auth.signInWithGoogle(activity)
        } catch (_: GetCredentialCancellationException) {
            return@runAuth false
        } catch (_: NoCredentialException) {
            error("No Google account found on this phone. Add one in Settings or use your mobile number.")
        }
        true
    }

    fun sendOtp(activity: Activity, phoneNumber: String) {
        _state.update { it.copy(loading = true, error = null) }
        auth.sendOtp(activity, phoneNumber, resendToken, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // Instant verification or SMS auto-retrieval: no need to type the code.
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                runAuth { auth.signInWithPhone(credential); true }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _state.update { it.copy(loading = false, error = e.localizedMessage ?: "Could not send OTP") }
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                resendToken = token
                _state.update { it.copy(loading = false, otpSentTo = phoneNumber) }
                startResendCountdown()
            }
        })
    }

    fun verifyOtp(code: String) {
        val id = verificationId ?: return
        runAuth { auth.verifyOtp(id, code); true }
    }

    /** Resets the screen so it starts fresh if the user signs out and comes back. */
    fun onSignInHandled() = editPhoneNumber()

    fun editPhoneNumber() {
        verificationId = null
        resendToken = null
        _state.update { AuthUiState() }
    }

    private fun startResendCountdown() = viewModelScope.launch {
        for (s in 60 downTo 0) {
            _state.update { it.copy(resendInSeconds = s) }
            delay(1_000)
        }
    }

    /** Runs [block]; it returns true when the user ended up signed in. */
    private fun runAuth(block: suspend () -> Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val signedIn = block()
                _state.update { it.copy(loading = false, signedIn = signedIn) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.localizedMessage ?: "Sign-in failed") }
            }
        }
    }
}
