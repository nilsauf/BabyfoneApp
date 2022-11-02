package de.nilsauf.babyfone.models.listening

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.*
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import de.nilsauf.babyfone.R
import de.nilsauf.babyfone.data.StreamingData
import de.nilsauf.babyfone.data.StreamingState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.IOException
import java.net.Socket

class ListeningModel : ViewModel() {
    companion object {
        const val route = "listening"
    }

    private val streamingDisposable = SerialDisposable()
    private val streamingStateSubject = BehaviorSubject.createDefault(StreamingState.NotStreaming)

    val serverIpAddress = mutableStateOf(StreamingData.serverIpAddress)

    @Composable
    fun rememberStreamingState(): Observable<StreamingState> = remember {
        streamingStateSubject
            .distinctUntilChanged()
    }

    fun stream(audioManager: AudioManager, notificationManager: NotificationManager, context: Context){
        this.stopStream()

        val ioScheduler = Schedulers.io()

        val frequency = StreamingData.frequency
        val channelConfiguration = StreamingData.channelOutConfiguration
        val audioEncoding = StreamingData.audioEncoding

        val bufferSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build())
            .setAudioFormat(
                AudioFormat.Builder()
                .setEncoding(audioEncoding)
                .setChannelMask(channelConfiguration)
                .setSampleRate(frequency)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack.preferredDevice = audioManager
            .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .filter { device -> device.isSink }
            .first { device -> device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }

        audioTrack.setVolume(3.0F)

        audioTrack.play()

        notificationManager.createChannel()

        streamingStateSubject.onNext(StreamingState.ReadyToStream)

        val compositeDisposable = CompositeDisposable()

        compositeDisposable.addAll(
            streamingStateSubject.filter { it == StreamingState.Streaming }
                .take(1)
                .subscribe {
                    val builder = NotificationCompat.Builder(context, "Listening")
                    builder.setContentText("Listening for Baby...")
                        .setContentTitle("Listening")
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                    notificationManager.notify(100, builder.build())
                },

            this.createAndObserveSocket(serverIpAddress.value, StreamingData.port, bufferSize)
                .subscribeOn(ioScheduler)
                .doAfterNext { streamingStateSubject.onNext(StreamingState.Streaming) }
                .filter { pair -> pair.second > 0 }
                .map { pair ->
                    audioTrack.write(pair.first, 0, pair.second)
                    pair.first
                }
                .doFinally {
                    audioTrack.stop()
                    audioTrack.release()
                    streamingStateSubject.onNext(StreamingState.NotStreaming)
                }
                .subscribe(),

            streamingStateSubject.filter { it == StreamingState.NotStreaming }
                .take(1)
                .subscribe { notificationManager.cancelAll() }
        )

        streamingDisposable.set(compositeDisposable)
    }

    fun stopStream(){
        streamingDisposable.set(Disposable.empty())
    }

    private fun createAndObserveSocket(host: String, port: Int, bufferSize: Int): Observable<Pair<ByteArray, Int>>{
        return Observable.using<Pair<ByteArray, Int>?, Socket?>(
            { Socket(host, port) },
            {socket -> Observable.create { obs ->
                val stream = socket.getInputStream()
                val buffer = ByteArray(bufferSize * 2)
                var read = 0
                try {
                    while (!obs.isDisposed && read != -1 && socket.isConnected && !socket.isClosed) {
                        read = stream.read(buffer, 0, bufferSize)
                        obs.onNext(Pair(buffer.clone(), read))
                    }

                    if(!obs.isDisposed) {
                        obs.onComplete()
                    }
                } catch (ex :Exception){
                    obs.onError(ex)
                }
            }},
            { socket -> socket.close() })
            .retry { throwable -> throwable is IOException }
    }

    private fun NotificationManager.createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Listening",
                "Listening",
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.enableVibration(true)
            notificationChannel.description = "Time for breakfast"

            this.createNotificationChannel(notificationChannel)
        }
    }

}