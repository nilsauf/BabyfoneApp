package de.nilsauf.babyfone.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

fun NotificationManager.createStreamingChannel(){
    this.createChannel("Streaming", "Streaming"){
        it.enableVibration(true)
        it.description = "Streaming Babysounds"
    }
}

fun NotificationManager.createListeningChannel() {
    this.createChannel("Listening", "Listening"){
        it.enableVibration(true)
        it.description = "Listening for Babysounds"
    }
}

fun NotificationManager.createChannel(
    id: String,
    name: CharSequence,
    importance: Int = NotificationManager.IMPORTANCE_LOW,
    channelModifier: (NotificationChannel) -> Unit
){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(id, name, importance)
        channelModifier(notificationChannel)
        this.createNotificationChannel(notificationChannel)
    }
}