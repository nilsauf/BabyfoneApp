package de.nilsauf.babyfone.models.streaming

import io.reactivex.rxjava3.core.Scheduler
import java.io.OutputStream

class BabyfoneStreamWriter(private val serverStream: OutputStream, val StreamScheduler: Scheduler) {
    fun write(bytes: ByteArray, len: Int){
        this.serverStream.write(bytes, 0, len)
    }
}