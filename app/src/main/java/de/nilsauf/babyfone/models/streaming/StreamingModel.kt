package de.nilsauf.babyfone.models.streaming

import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.ConnectivityManager
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import de.nilsauf.babyfone.data.StreamingData
import de.nilsauf.babyfone.data.StreamingState
import de.nilsauf.babyfone.extensions.getIpStringOfWifiNetwork
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.net.ServerSocket
import java.net.Socket

class StreamingModel : ViewModel() {

    companion object {
        const val route = "streaming"
    }

    private val streamingDisposable = SerialDisposable()
    private val streamingStateSubject = BehaviorSubject.createDefault(StreamingState.NotStreaming)

    @Composable
    fun rememberWifiIpAddresses(connectivityManager: ConnectivityManager): Observable<String> = remember {
        connectivityManager
            .getIpStringOfWifiNetwork()
            .onErrorComplete()
            .replay(1)
            .autoConnect()
    }

    @Composable
    fun rememberStreamingState(): Observable<StreamingState> = remember {
        streamingStateSubject
            .distinctUntilChanged()
    }

    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    fun stream() {
        this.stopStream()

        val serverSocket = ServerSocket(StreamingData.port)
        val serverSocketScheduler = Schedulers.io()

        val frequency = StreamingData.frequency
        val channelConfiguration = StreamingData.channelInConfiguration
        val audioEncoding = StreamingData.audioEncoding

        val bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding)

        streamingStateSubject.onNext(StreamingState.ReadyToStream)

        this.streamingDisposable.set(
            serverSocket.observeConnections()
                .subscribeOn(serverSocketScheduler)
                .take(1)
                .doAfterNext { streamingStateSubject.onNext(StreamingState.Streaming) }
                .map { socket -> socket.getOutputStream() }
                .flatMap { stream ->  createAndConnectToAudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize)
                    .observeOn(serverSocketScheduler)
                    .map { recordPair ->
                        stream.write(recordPair.first, 0, recordPair.second)
                        recordPair.first
                    }
                }
                .doFinally {
                    if(!serverSocket.isClosed) serverSocket.close()
                    streamingStateSubject.onNext(StreamingState.NotStreaming)
                }
                .subscribe()
        )
    }

    fun stopStream(){
        streamingDisposable.set(Disposable.empty())
    }

    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    private fun createAndConnectToAudioRecord(
        audioSource : Int, sampleRateInHz : Int, channelConfig : Int, audioFormat : Int,
        bufferSizeInBytes : Int): Observable<Pair<ByteArray, Int>>{
        return Observable.using<Pair<ByteArray, Int>, AudioRecord>(
            { AudioRecord(
                audioSource,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                bufferSizeInBytes
            )},
            { audioRecord ->
                val byteBufferSize = bufferSizeInBytes * 2
                val buffer = ByteArray(byteBufferSize)
                audioRecord.startRecording()
                Observable.create { obs ->
                    try {
                        while(!obs.isDisposed) {
                            val read = audioRecord.read(buffer, 0, bufferSizeInBytes)
                            obs.onNext(Pair(buffer.clone(), read))
                        }
                        obs.onComplete()
                    } catch (ex: Exception){
                        obs.onError(ex)
                    }
                }
            },
            { audioRecord ->
                audioRecord.stop()
                audioRecord.release()
            }
        ).subscribeOn(Schedulers.io())
    }

    private fun ServerSocket.observeConnections(): Observable<Socket>{
        return Observable.create<Socket> { obs ->
            try {
                while(!obs.isDisposed && !this.isClosed){
                    obs.onNext(this.accept())
                }
                if (this.isClosed)
                    obs.onComplete()
            } catch (ex: Exception){
                obs.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
    }
}