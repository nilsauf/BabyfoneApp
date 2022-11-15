package de.nilsauf.babyfone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import de.nilsauf.babyfone.ui.BabyfoneMainLayout
import de.nilsauf.babyfone.ui.theme.BabyfoneTheme

@AndroidEntryPoint
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
        BabyfoneMainLayout()
    }
}

@Composable
@Preview
fun BabyfoneAppPreview(){
    BabyfoneApp()
}