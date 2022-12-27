package de.nilsauf.babyfone.models.streaming.streamwriter

import io.reactivex.rxjava3.core.Scheduler

abstract class BaseStreamWriter(val StreamScheduler: Scheduler) {
    abstract fun write(bytes: ByteArray, len: Int)
}