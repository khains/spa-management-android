package com.spa.management

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.spa.management.data.api.SessionManager
import com.spa.management.navigation.AppNavGraph
import com.spa.management.ui.theme.SpaManagementTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Kiem tra da co token dang nhap luu san hay chua de quyet dinh man hinh bat dau
        val hasToken = runBlocking { SessionManager.tokenFlow().first() != null }
        val startDestination = if (hasToken) "main" else "login"

        setContent {
            SpaManagementTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
