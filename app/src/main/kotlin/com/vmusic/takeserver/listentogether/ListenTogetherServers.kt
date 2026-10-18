/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.listentogether

import kotlinx.serialization.Serializable
import java.net.Inet4Address
import java.net.NetworkInterface

@Serializable
data class ListenTogetherServer(
    val name: String,
    val url: String,
    val location: String,
    val operator: String
)

object ListenTogetherServers {
    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val sAddr = addr.hostAddress
                        if (sAddr != null && (sAddr.startsWith("192.168.") || sAddr.startsWith("10.") || sAddr.startsWith("172."))) {
                          return sAddr
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // ignore
        }
        return "127.0.0.1"
    }

    val defaultLanUrl: String
        get() = "ws://${getLocalIpAddress()}:8080/ws"

    val servers: List<ListenTogetherServer>
        get() = listOf(
            ListenTogetherServer(
                name = "The Meowery",
                url = "wss://metroserverx.meowery.eu/ws",
                location = "Poland",
                operator = "Nyx"
            ),
            ListenTogetherServer(
                name = "Local Network (LAN)",
                url = defaultLanUrl,
                location = "Local Network (${getLocalIpAddress()})",
                operator = "Current Device"
            )
        )

    val defaultServerUrl: String
        get() = servers.first().url

    fun findByUrl(url: String): ListenTogetherServer? = servers.firstOrNull { it.url == url }
}
