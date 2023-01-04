package de.nilsauf.babyfone.models.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import androidx.datastore.preferences.rxjava3.rxPreferencesDataStore
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
    private val lastAddressString = stringPreferencesKey("lastAddress")

    fun setLastAddressString(lastAddress: String) : Single<Unit> {
        return this.setSetting(lastAddressString, lastAddress)
    }

    fun connectLastStreamAddress() : Observable<String> {
        return this.connectToSetting(lastAddressString, "0.0.0.0")
    }

    private fun <T : Any> connectToSetting(key: Preferences.Key<T>, defaultValue: T) : Observable<T> {
        return settingsDataStore.data().map { preferences -> preferences[key] ?: defaultValue }
            .toObservable()
    }
    
    private fun <T> setSetting(key: Preferences.Key<T>, newValue: T) : Single<Unit> {
        return settingsDataStore.updateDataAsync{ preferences ->
            preferences.toMutablePreferences()[key] = newValue
            Single.just(preferences)
        }.map { }
    }

}