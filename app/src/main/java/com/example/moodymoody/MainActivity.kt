package com.example.moodymoody

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moodymoody.ui.MoodViewModel
import com.example.moodymoody.ui.screens.MoodApp

class MainActivity : ComponentActivity() {
    private val viewModel: MoodViewModel by viewModels {
        val app = application as MoodyMoodyApp
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = app.container.moodRepository
                val preferences = app.container.notificationPreferences
                @Suppress("UNCHECKED_CAST")
                return MoodViewModel(repository, preferences, app.applicationContext) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodyMoodyTheme(viewModel)
        }
    }
}

@Composable
private fun MoodyMoodyTheme(viewModel: MoodViewModel) {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MoodApp(viewModel)
        }
    }
}
