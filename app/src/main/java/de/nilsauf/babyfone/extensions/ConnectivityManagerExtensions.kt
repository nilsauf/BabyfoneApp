package de.nilsauf.babyfone.extensions

import android.net.*
import android.os.Parcel
import io.reactivex.rxjava3.core.Observable

@androidx.annotation.RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
fun ConnectivityManager.observeNetworks(request: NetworkRequest) : Observable<List<Network>> {
    return Observable.create<Pair<Network, Boolean>> { obs ->
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                obs.onNext(Pair(network, true))
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
                obs.onNext(Pair(network, false))
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                obs.onNext(Pair(network, true))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                obs.onNext(Pair(network, true))
            }
        }
        obs.setCancellable { this.unregisterNetworkCallback(networkCallback) }
        this.registerNetworkCallback(request, networkCallback)
    }.scan<ArrayList<Network>>(ArrayList()){ list, (network, connected) ->
        if(connected && !list.contains(network)){
            list.add(network)
        } else if (!connected && list.contains(network)) {
            list.remove(network)
        }
        list
    }.map { list -> list.toList() }
}

const val noWifiConnectionString = "No Wifi Connection"

fun ConnectivityManager.getIpStringOfObservedNetworks(request: NetworkRequest ): Observable<String> {
    return this.observeNetworks(request)
        .flatMap { list ->
            if (list.isEmpty()) {
                Observable.just(noWifiConnectionString)
            } else {
                Observable.just(list.first())
                    .map { network ->
                        this.getLinkProperties(network)
                            ?: LinkProperties.CREATOR.createFromParcel(Parcel.obtain())
                    }
                    .map { props -> props.linkAddresses
                        .map {linkAdd -> linkAdd.toString()}
                        .map {ipString -> if(ipString.contains('/')) ipString.substring(0, ipString.indexOf('/')) else ipString}}
                    .map { linkAdd -> linkAdd.joinToString("\n") }
            }
        }
}

fun ConnectivityManager.getIpStringOfWifiNetwork() : Observable<String> {
    val request = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    return this.getIpStringOfObservedNetworks(request)
}