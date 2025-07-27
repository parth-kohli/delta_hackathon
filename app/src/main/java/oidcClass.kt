package com.example.drooms_chat
import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider


class oidcClass : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var activity: Activity

    fun init(auth: FirebaseAuth, active: Activity) {
        firebaseAuth = auth
        activity = active
    }

    private fun oidcProvider(): OAuthProvider.Builder {
        return OAuthProvider.newBuilder("oidc.drooms")
    }

    fun getPendingAuthResult(onSignedIn: (String?) -> Unit) {
        val pendingResultTask = firebaseAuth.pendingAuthResult

        if (pendingResultTask != null) {
            // If there's a pending result, handle it
            pendingResultTask
                .addOnSuccessListener { authResult ->
                    val userEmail = authResult.user?.email
                    Log.d("OidcAuth", "Pending login successful: $userEmail")
                    onSignedIn(userEmail)
                }
                .addOnFailureListener { e ->
                    Log.e("OidcAuth", "Pending login failed", e)
                    onSignedIn(null)
                }
        } else {
            // No pending result, start new sign-in
            startOidcSignIn(onSignedIn)
        }
    }

    private fun startOidcSignIn(onSignedIn: (String?) -> Unit) {
        val providerBuilder = oidcProvider()

        firebaseAuth
            .startActivityForSignInWithProvider(activity, providerBuilder.build())
            .addOnSuccessListener { authResult ->
                val userEmail = authResult.user?.email
                Log.d("OidcAuth", "Login successful: $userEmail")
                onSignedIn(userEmail)
            }
            .addOnFailureListener { e ->
                Log.e("OidcAuth", "Login failed", e)
                onSignedIn(null)
            }
    }
}
