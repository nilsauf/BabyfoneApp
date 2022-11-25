package de.nilsauf.babyfone.ui.streaming

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.runtime.rxjava3.subscribeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import de.nilsauf.babyfone.data.StreamingState
import de.nilsauf.babyfone.extensions.noWifiConnectionString
import de.nilsauf.babyfone.models.streaming.StreamingModel
import de.nilsauf.babyfone.ui.utils.ControlStreamButton
import de.nilsauf.babyfone.ui.utils.StateText
import de.nilsauf.babyfone.ui.utils.TextWithLabelInRow

@SuppressLint("MissingPermission")
@Composable
fun StreamingScreen(
    streamingModel: StreamingModel
){
    val wifiIpAddresses by remember(streamingModel){ streamingModel.wifiIpAddresses }
        .subscribeAsState(noWifiConnectionString)

    val streamingState by streamingModel.streamingStateSubject
        .subscribeAsState(StreamingState.NotStreaming)

    Column {
        LocalIpText(wifiIpAddresses)
        StateText(streamingState)
        ControlStreamButtonWithPermissionRequest(
            streamingState,
            { streamingModel.stream() },
            { streamingModel.stopStream() })
    }

}

@Composable
fun LocalIpText(wifiIpAddresses: String){
    TextWithLabelInRow("Local Ip: ", wifiIpAddresses)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ControlStreamButtonWithPermissionRequest(
    streamingState: StreamingState,
    doStream: () -> Unit,
    stopStream: () -> Unit
){
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO) {
        if(it){
            doStream()
        }
    }

    ControlStreamButton(
        streamingState,
        {
            if (!micPermissionState.status.isGranted){
                micPermissionState.launchPermissionRequest()
            } else {
                doStream()
            }
        },
        { stopStream() }
    )
}