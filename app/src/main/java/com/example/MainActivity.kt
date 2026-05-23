package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.SessionRepository
import com.example.data.SessionViewModel
import com.example.ui.BgDeep
import com.example.ui.DecorativeBackground
import com.example.ui.ZenAppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize databases and repository layers
        val database = AppDatabase.getDatabase(this)
        val repository = SessionRepository(database.sessionDao())
        
        // 2. Instantiate viewmodel via its designated Factory provider
        val sessionViewModel = ViewModelProvider(
            this,
            SessionViewModel.Factory(repository)
        )[SessionViewModel::class.java]
        
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDeep)
                ) {
                    // Constant beautiful floating glowing pools
                    DecorativeBackground()
                    // Main type-safe transitions router
                    ZenAppNavigation(viewModel = sessionViewModel)
                }
            }
        }
    }
}
