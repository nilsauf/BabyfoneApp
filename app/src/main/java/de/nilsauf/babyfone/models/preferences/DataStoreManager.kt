package de.nilsauf.babyfone.models.preferences

import android.content.Context
import android.media.AudioFormat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import androidx.datastore.preferences.rxjava3.rxPreferencesDataStore
import de.nilsauf.babyfone.models.streaming.StreamType
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by rxPreferencesDataStore("settings", scheduler = Schedulers.io())

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreManager @Inject constructor(@ApplicationContext appContext: Context) {

    private val settingsDataStore = appContext.dataStore
    private val serverIpAddressKey = stringPreferencesKey("serverIpAddress")
    private val serverPortKey = intPreferencesKey("serverPort")
    private val frequencyKey = intPreferencesKey("frequencyKey")
    private val channelConfigurationInKey = intPreferencesKey("channelConfigurationIn")
    private val channelConfigurationOutKey = intPreferencesKey("channelConfigurationOut")
    private val audioEncodingKey = intPreferencesKey("audioEncoding")
    private val streamTypeKey = stringPreferencesKey("streamType")

    fun setServerIpAddress(lastAddress: String) : Single<Unit> {
        return this.setSetting(serverIpAddressKey, lastAddress)
    }

    fun connectToServerIpAddress() : Observable<String> {
        return this.connectToSetting(serverIpAddressKey, "0.0.0.0")
    }

    fun setServerPort(serverPort: Int) : Single<Unit> {
        return this.setSetting(serverPortKey, serverPort)
    }

    fun connectToServerPort() : Observable<Int> {
        return this.connectToSetting(serverPortKey, 10000)
    }

    fun setFrequency(frequency: Int) : Single<Unit> {
        return this.setSetting(frequencyKey, frequency)
    }

    fun connectToFrequency() : Observable<Int> {
        return this.connectToSetting(frequencyKey, 44100)
    }

    fun setChannelConfigurationIn(channelConfigurationIn: Int) : Single<Unit> {
        return this.setSetting(channelConfigurationInKey, channelConfigurationIn)
    }

    fun connectToChannelConfigurationIn() : Observable<Int> {
        return this.connectToSetting(channelConfigurationInKey, AudioFormat.CHANNEL_IN_MONO)
    }

    fun setChannelConfigurationOut(channelConfigurationOut: Int) : Single<Unit> {
        return this.setSetting(channelConfigurationOutKey, channelConfigurationOut)
    }

    fun connectToChannelConfigurationOut() : Observable<Int> {
        return this.connectToSetting(channelConfigurationOutKey, AudioFormat.CHANNEL_OUT_MONO)
    }

    fun setAudioEncoding(audioEncoding: Int) : Single<Unit> {
        return this.setSetting(audioEncodingKey, audioEncoding)
    }

    fun connectToAudioEncoding() : Observable<Int> {
        return this.connectToSetting(audioEncodingKey, AudioFormat.ENCODING_PCM_16BIT)
    }

    fun setStreamType(streamType: StreamType) : Single<Unit> {
        return this.setSetting(streamTypeKey, streamType.name)
    }

    fun connectToStreamType() : Observable<StreamType> {
        return this.connectToSetting(streamTypeKey, StreamType.Socket.name)
            .map { StreamType.valueOf(it) }
    }

    private fun <T : Any> connectToSetting(key: Preferences.Key<T>, defaultValue: T) : Observable<T> {
        return settingsDataStore.data().map { preferences -> preferences[key] ?: defaultValue }
            .distinctUntilChanged()
            .toObservable()
    }
    
    private fun <T> setSetting(key: Preferences.Key<T>, newValue: T) : Single<Unit> {
        return settingsDataStore.updateDataAsync{ preferences ->
            preferences.toMutablePreferences()[key] = newValue
            Single.just(preferences)
        }.map { }
    }

}