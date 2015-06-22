package co.fusionx.relay

import java.util.*

public interface EventListener {
    public fun onSocketConnect(): Unit = Unit

    public fun onGenericCode(code: Int, text: String): Unit = Unit
    public fun onWelcome(code: Int, text: String): Unit = Unit
    public fun onNames(channelName: String, nickList: List<String>) = Unit

    public fun onNickChange(oldNick: String, newNick: String): Unit = Unit
}

internal class EventDispatcher constructor(
        private val listeners: MutableList<EventListener> = ArrayList()) : EventListener {
    override fun onSocketConnect() {
        for (i in 0..listeners.size() - 1) listeners[i].onSocketConnect()
    }

    override fun onGenericCode(code: Int, text: String) {
        for (i in 0..listeners.size() - 1) listeners[i].onGenericCode(code, text)
    }
    override fun onWelcome(code: Int, text: String) {
        for (i in 0..listeners.size() - 1) listeners[i].onWelcome(code, text)
    }
    override fun onNames(channelName: String, nickList: List<String>) {
        for (i in 0..listeners.size() - 1) listeners[i].onNames(channelName, nickList)
    }

    override fun onNickChange(oldNick: String, newNick: String) {
        for (i in 0..listeners.size() - 1) listeners[i].onNickChange(oldNick, newNick)
    }

    public fun addEventListener(listener: EventListener) {
        listeners.add(listener)
    }

    public fun removeEventListener(listener: EventListener) {
        listeners.remove(listener)
    }
}