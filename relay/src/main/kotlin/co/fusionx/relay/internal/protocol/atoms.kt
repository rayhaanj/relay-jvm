package co.fusionx.relay.internal.protocol

import co.fusionx.relay.UserLevel
import co.fusionx.relay.internal.parse

data class LevelledNick(val level: List<UserLevel>, val nick: String) {
    companion object {
        fun parse(nick: String): LevelledNick? {
            val levelList = arrayListOf<UserLevel>()
            nick.indices.forEach {
                val char = nick[it]
                val userLevel = UserLevel.parse(char)

                if (userLevel == null) {
                    return LevelledNick(levelList, nick.substring(it))
                } else {
                    levelList.add(userLevel)
                }
            }
            return null
        }
    }
}