package de.nilsauf.babyfone.ui.start

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview

const val startRoute = "start"

@Composable
@androidx.annotation.RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
fun StartScreen(
    onClickSetupStream: () -> Unit,
    onClickStartListening: () -> Unit
){

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SetupStreamCard(onClickSetupStream)
        StartListeningCard(onClickStartListening)
    }
}

@Composable
@Preview
fun StartScreenPreview()
{
    StartScreen(
        onClickSetupStream = { },
        onClickStartListening = { })
}

@Composable
@androidx.annotation.RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
fun SetupStreamCard(onClickSetupStream: () -> Unit){
    Button(onClick = onClickSetupStream) {
        Text(text = "Setup Stream")
    }
}

@Composable
fun StartListeningCard(onClickStartListening: () -> Unit){
    Button(onClick = onClickStartListening) {
        Text(text = "Start Listening")
    }
}