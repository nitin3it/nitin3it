package com.nitin3it.kidsafe

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class KidSafeApp : Application() {
    companion object {
        /** False until google-services.json is added to the project (see README). */
        fun isFirebaseConfigured(context: Context) = FirebaseApp.getApps(context).isNotEmpty()
    }
}
