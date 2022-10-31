package de.nilsauf.babyfone.ui.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.nilsauf.babyfone.data.StreamingState

@Composable
fun ControlStreamButton(
    streamingState: StreamingState,
    doStream: () -> Unit,
    stopStream: () -> Unit
){
    ControlStreamButton(streamingState){
        when(it){
            StreamingState.NotStreaming -> doStream()
            else -> stopStream()
        }
    }
}

@Composable
fun ControlStreamButton(
    streamingState: StreamingState,
    onClick: (StreamingState) -> Unit){

    val buttonText = when(streamingState){
        StreamingState.NotStreaming -> "Start Stream"
        else -> "Stop Stream"
    }
    Button(onClick ={ onClick(streamingState) }){
        Text(text = buttonText)
    }
}

@Composable
fun StateText(streamingStateValue: StreamingState){
    val stateText = streamingStateValue.toString()
    TextWithLabelInRow("State: ", stateText)
}

@Composable
fun TextWithLabelInRow(label: String, text: String){
    Row {
        Text(text = label)
        Text(text = text)
    }
}