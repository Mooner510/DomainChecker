package kr.mooner510.domainchecker

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException


object WhoisClient {
    private const val HOST = "whois.internic.net"
    private const val PORT = 43
    var showThrown = false

    @JvmStatic
    fun query(domain: String, thrown: Boolean = false): Boolean {
        while (true) {
            try {
                Socket(HOST, PORT).use { socket ->
                    val output = socket.getOutputStream()
                    val writer = PrintWriter(output, true)
                    writer.println(domain)

                    val input = socket.getInputStream()

                    val reader = BufferedReader(InputStreamReader(input))

                    val line = reader.readLine() ?: return false

                    return !(line[0] == 'N' && line[1] == 'o')
                }
            } catch (ex: UnknownHostException) {
                DomainChecker.out.append("$domain ${ex.message}").flush()
                return false
            } catch (ex: IOException) {
                if(thrown) DomainChecker.out.append("$domain ${ex.message}").flush()
                Thread.sleep((Math.random() * 1000 + 500).toLong())
            }
        }
    }
}
