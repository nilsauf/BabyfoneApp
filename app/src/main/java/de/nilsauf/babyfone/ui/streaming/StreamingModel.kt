package de.nilsauf.babyfone.ui.streaming

import android.net.ConnectivityManager
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.nilsauf.babyfone.data.StreamingState
import de.nilsauf.babyfone.extensions.getIpStringOfWifiNetwork
import de.nilsauf.babyfone.models.streaming.AudioStreamingHandler
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

@HiltViewModel
class StreamingModel @Inject constructor(
    private val audioStreamingHandler: AudioStreamingHandler,
    private val connectivityManager: ConnectivityManager,
) : ViewModel() {

    companion object {
        const val route = "streaming"
    }

    val streamingStateSubject : Observable<StreamingState>
        get() = audioStreamingHandler.streamingState

    val wifiIpAddresses : Observable<String>
        get() { return connectivityManager.getIpStringOfWifiNetwork() }

    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    fun stream() { audioStreamingHandler.startStream() }

    fun stopStream() { audioStreamingHandler.stopStream() }
}