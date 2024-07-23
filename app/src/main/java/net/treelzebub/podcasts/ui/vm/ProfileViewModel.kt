package net.treelzebub.podcasts.ui.vm

import android.app.Application
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.actionCodeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import net.treelzebub.podcasts.data.Prefs
import net.treelzebub.podcasts.ui.vm.ProfileViewModel.State
import timber.log.Timber
import javax.inject.Inject

// https://github.com/firebase/quickstart-android/blob/master/auth/app/src/main/java/com/google/firebase/quickstart/auth/kotlin/PasswordlessActivity.kt
@HiltViewModel
class ProfileViewModel @Inject constructor(
    app: Application,
    private val prefs: Prefs,
    private val auth: FirebaseAuth
) : StatefulViewModel<State>(State()) {

    data class State(
        val loading: Boolean = false
    )

    private val packageName = app.packageName

    fun sendLink(email: String, continuation: (Boolean) -> Unit) {
        loading(true)
        val actionCodeSettings = actionCodeSettings {
            // Emailed link will redirect the user to this URL if the app is not installed on
            // user's device and the app was not able to be installed.
            url = "https://podcasts.treelzebub.net"
            handleCodeInApp = true // required
            setAndroidPackageName(
                packageName,
                true, // installIfNotAvailable
                null, // minimumVersion
            )
        }
        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                loading(false)
                val e = task.exception
                Timber.e("Auth email failed to send.", e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid email address
                }
                continuation(task.isSuccessful)
            }
    }

    fun containsLink(intent: Intent): Boolean {
        return auth.isSignInWithEmailLink(intent.data.toString())
    }

    private fun loading(value: Boolean) {
        _state.update { it.copy(loading = value) }
    }

    private fun signInWithEmailLink(email: String, link: String) {
        loading(true)

        auth.signInWithEmailLink(email, link)
            .addOnCompleteListener { task ->
                loading(false)
                if (task.isSuccessful) {
                    val user = task.result!!.user
                } else {
                    Timber.e("signInWithEmailLink failed.", task.exception)
                    if (task.exception is FirebaseAuthActionCodeException) {
                        // Invalid or expired sign-in link
                    }
                }
            }
    }

    private fun onSignInClicked(email: String, emailLink: String) {
        signInWithEmailLink(email, emailLink)
    }

    private fun signOut() = auth.signOut()
}
