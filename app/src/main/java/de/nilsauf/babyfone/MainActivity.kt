package de.nilsauf.babyfone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import de.nilsauf.babyfone.ui.theme.BabyfoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BabyfoneApp()
        }
    }
}

@Composable
fun BabyfoneApp(){
    BabyfoneTheme {
        val navController = rememberNavController()
        Surface() {
            BabyfoneNavHost(navController = navController)
        }
    }
}