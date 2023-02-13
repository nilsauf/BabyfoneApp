package de.nilsauf.babyfone.models.listening.streamreader

import io.reactivex.rxjava3.core.Scheduler
import java.io.InputStream

class InputStreamReader(private val inputStream : InputStream, scheduler: Scheduler) : BaseStreamReader(scheduler) {
    override fun read(bytes: ByteArray, len: Int) : Int {
        return inputStream.read(bytes, 0, len)
    }
}