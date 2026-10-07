package com.nitin3it.kidsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nitin3it.kidsafe.ui.AppRoot
import com.nitin3it.kidsafe.ui.theme.KidSafeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidSafeTheme {
                AppRoot()
            }
        }
    }
}
