package de.nilsauf.babyfone.ui.listening

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.setValue
import de.nilsauf.babyfone.data.StreamingState
import de.nilsauf.babyfone.models.listening.ListeningModel
import de.nilsauf.babyfone.ui.utils.ControlStreamButton
import de.nilsauf.babyfone.ui.utils.StateText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningScreen(
    listeningModel: ListeningModel
){
    val streamingState by listeningModel.rememberStreamingState()
        .subscribeAsState(StreamingState.NotStreaming)

    var serverIpAddress by listeningModel.serverIpAddress

    Column {
        StateText(streamingState)
        OutlinedTextField(serverIpAddress, { serverIpAddress = it }, enabled = streamingState == StreamingState.NotStreaming)
        ControlStreamButton(
            streamingState,
            { listeningModel.stream()},
            { listeningModel.stopStream() }
        )
    }

}