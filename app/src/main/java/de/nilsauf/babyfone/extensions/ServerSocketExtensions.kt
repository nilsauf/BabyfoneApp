package de.nilsauf.babyfone.extensions

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.ServerSocket
import java.net.Socket

fun ServerSocket.observeConnections(scheduler: Scheduler = Schedulers.io()): Observable<Socket> {
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
    }.subscribeOn(scheduler)
}