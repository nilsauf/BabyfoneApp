package de.nilsauf.babyfone.models.streaming.streamwriter

import io.reactivex.rxjava3.core.Scheduler
import java.io.OutputStream

class OutputStreamWriter(private val outputStream: OutputStream, scheduler: Scheduler) : BaseStreamWriter(scheduler) {
    override fun write(bytes: ByteArray, len: Int) {
        this.outputStream.write(bytes, 0, len)
    }
}