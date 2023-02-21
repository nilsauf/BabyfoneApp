package de.nilsauf.babyfone.ui.listening

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.nilsauf.babyfone.models.listening.AudioListeningHandler
import de.nilsauf.babyfone.models.preferences.DataStoreManager
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import javax.inject.Inject

@HiltViewModel
class ListeningModel @Inject constructor(
    private val audioListeningHandler: AudioListeningHandler,
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {
    companion object {
        const val route = "listening"
    }

    private val listingDisposable = SerialDisposable()

    val serverIpAddress = mutableStateOf(dataStoreManager.connectToServerIpAddress()
        .firstElement()
        .blockingGet()
        .orEmpty())
    val streamingStateSubject = audioListeningHandler.listeningState

    fun stream() {
        listingDisposable.set(dataStoreManager.setServerIpAddress(serverIpAddress.value)
            .doOnSuccess { audioListeningHandler.startListening() }
            .doOnError { error -> }
            .subscribe())
    }

    fun stopStream() {
        listingDisposable.set(Disposable.empty())
        audioListeningHandler.stopListening()
    }

    override fun onCleared() {
        listingDisposable.dispose()
        super.onCleared()
    }
}