package de.nilsauf.babyfone.models.listening.streamreader

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler

abstract class BaseStreamReader(val StreamScheduler: Scheduler) {
    abstract fun read(bytes: ByteArray, len: Int) : Int
    fun connect(bufferSize: Int) : Observable<Pair<ByteArray, Int>> {
        return Observable.create {
            val buffer = ByteArray(bufferSize * 2)
            var readCount = 0
            try {
                while(!it.isDisposed && readCount != -1){
                    readCount = read(buffer, bufferSize)
                    it.onNext(Pair(buffer.clone(), readCount))
                }

                if(!it.isDisposed) {
                    it.onComplete()
                }
            } catch (ex :Exception) {
                it.onError(ex)
            }
        }
            .subscribeOn(StreamScheduler)
    }
}