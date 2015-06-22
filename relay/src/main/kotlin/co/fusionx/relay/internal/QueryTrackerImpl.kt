package co.fusionx.relay.internal

import co.fusionx.relay.Query
import co.fusionx.relay.QueryTracker

public class QueryTrackerImpl : QueryTracker {

    private val queryMap: MutableMap<String, Query> = hashMapOf()

    override internal fun query(nick: String): Query? = synchronized(queryMap) { queryMap[nick] }
}