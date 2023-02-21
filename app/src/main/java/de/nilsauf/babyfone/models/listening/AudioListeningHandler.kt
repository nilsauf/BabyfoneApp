package de.nilsauf.babyfone.models.listening

import android.app.NotificationManager
import android.content.Context
import android.media.AudioTrack
import android.widget.Toast
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.nilsauf.babyfone.R
import de.nilsauf.babyfone.data.StreamingState
import de.nilsauf.babyfone.extensions.connectToMinAudioTrackBufferSize
import de.nilsauf.babyfone.extensions.createListeningChannel
import de.nilsauf.babyfone.models.listening.streamreader.BaseStreamReader
import de.nilsauf.babyfone.models.preferences.DataStoreManager
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AudioListeningHandler @Inject constructor(
    private val notificationManager: NotificationManager,
    private val dataStoreManager: DataStoreManager,
    private val clientStreamProvider: Provider<Maybe<BaseStreamReader>>,
    private val audioTrackProvider: Provider<Maybe<AudioTrack>>,
    @ApplicationContext private val appContext: Context
) {
    private val listeningDisposable = SerialDisposable()
    private val listeningStateSubject = BehaviorSubject.createDefault(StreamingState.NotStreaming)
    private val listeningNotification = NotificationCompat.Builder(appContext, "Listening")
        .setContentText("Listening for Baby...")
        .setContentTitle("Listening")
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .build()

    val listeningState: Observable<StreamingState>
        get() { return this.listeningStateSubject.distinctUntilChanged() }

    init {
        notificationManager.createListeningChannel()
    }

    fun startListening(){
        stopListening()

        listeningStateSubject.onNext(StreamingState.ReadyToStream)

        val compositeDisposable = CompositeDisposable()

        compositeDisposable.addAll(
            listeningStateSubject.filter { it == StreamingState.Streaming }
                .firstElement()
                .subscribe {
                    notificationManager.notify(100, this.listeningNotification)
                },

            dataStoreManager.connectToMinAudioTrackBufferSize()
                .firstElement()
                .flatMapObservable { bufferSize ->
                    audioTrackProvider.get()
                        .flatMapObservable { audioTrack ->
                            audioTrack.play()
                            clientStreamProvider.get()
                                .flatMapObservable { it.connect(bufferSize) }
                                .map { audioData -> audioTrack.write(audioData.first, 0, audioData.second)}
                                .doFinally {
                                    audioTrack.stop()
                                    audioTrack.release()
                                }
                        }
                }
                .doFinally { listeningStateSubject.onNext(StreamingState.NotStreaming) }
                .doOnError { throwable -> Toast.makeText(appContext, throwable.message, Toast.LENGTH_SHORT).show() }
                .onErrorComplete()
                .subscribe(),

            listeningStateSubject.filter { it == StreamingState.NotStreaming }
                .firstElement()
                .subscribe { notificationManager.cancelAll() }
        )

        listeningDisposable.set(compositeDisposable)
    }

    fun stopListening(){
        listeningDisposable.set(Disposable.empty())
    }
}