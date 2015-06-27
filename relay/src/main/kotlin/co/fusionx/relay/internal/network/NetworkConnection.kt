package co.fusionx.relay.internal.network

import co.fusionx.relay.Status
import rx.Observable

public interface NetworkConnection {
    val rawSource: Observable<String>
    val rawStatusSource: Observable<Status>
    val invalid: Boolean

    fun connect()
}