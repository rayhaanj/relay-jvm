/*
 * Copyright (C) 2015 Lalit Maganti (FusionX Software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.fusionx.relay.protocol

private fun clientCommand(command: String, vararg arguments: String): String {
    val output: MutableList<String> = arrayListOf(command)
    for (arg in arguments) {
        if (arg.contains(" ")) {
            output.add(":" + arg)
            break
        }
        output.add(arg)
    }
    return output.join(" ")
}

/** Helper object which contains useful methods for creating messages to be sent over the wire by a client */
public object ClientGenerator {
    /** Creates a JOIN message */
    public fun join(channelName: String): String =
        clientCommand("JOIN", channelName)

    /** Creates a CAP message */
    public fun cap(subCommand: String, caps: List<String> = listOf()): String {
        val args = if (caps.isEmpty()) arrayOf(subCommand) else arrayOf(subCommand, caps.join(" "))
        return clientCommand("CAP", *args)
    }

    /** Creates a USER message */
    public fun user(username: String, realName: String): String =
        clientCommand("USER", username, 8.toString(), "*", realName)

    /** Creates a NICK message */
    public fun nick(nick: String): String =
        clientCommand("NICK", nick)

    /** Creates a PONG message */
    public fun pong(server: String): String =
        clientCommand("PONG", server)

    /** Creates a PRIVMSG message */
    public fun privmsg(target: String, message: String): String =
        clientCommand("PRIVMSG", target, message)
}