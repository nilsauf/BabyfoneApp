package de.nilsauf.babyfone.ui.listening

import android.media.AudioManager
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import de.nilsauf.babyfone.data.StreamingState
import de.nilsauf.babyfone.models.listening.ListeningModel
import de.nilsauf.babyfone.ui.utils.ControlStreamButton
import de.nilsauf.babyfone.ui.utils.StateText

@Composable
fun ListeningScreen(
    listeningModel: ListeningModel
){
    val streamingState by listeningModel.rememberStreamingState()
        .subscribeAsState(StreamingState.NotStreaming)

    val audioManager = LocalContext.current.getSystemService<AudioManager>()

    Column {
        StateText(streamingState)
        ControlStreamButton(
            streamingState,
            { listeningModel.stream(audioManager!! )},
            { listeningModel.stopStream() }
        )
    }

}