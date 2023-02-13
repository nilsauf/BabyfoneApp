package de.nilsauf.babyfone.models.listening.streamreader

import io.reactivex.rxjava3.core.Scheduler

abstract class BaseStreamReader(val StreamScheduler: Scheduler) {
    abstract fun read(bytes: ByteArray, len: Int) : Int
}