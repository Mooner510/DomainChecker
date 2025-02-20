package kr.mooner510.domainchecker

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.net.Socket
import java.util.*


object DomainChecker2 {
    private var length: Int = 0
    private val list = arrayOfNulls<Boolean>(11881376)
    val out = BufferedWriter(OutputStreamWriter(System.out))

    @JvmStatic
    fun main(args: Array<String>) {
        val charArray = Array(26) { 'a' + it }

        val domains = Arrays.stream(charArray).flatMap { a ->
            Arrays.stream(charArray).flatMap { b ->
                Arrays.stream(charArray).flatMap { c ->
                    Arrays.stream(charArray).map { "$a$b$c$it.com" }
                }
            }
        }.toList()

        out.append("thread complete\n").flush()

        Thread {
            out.append("waiting start: ${domains.size}\n").flush()

            domains.forEachIndexed { i, domain ->
                Thread.startVirtualThread {
                    list[('z' - domain[0]) * 26 * 26 * 26 + ('z' - domain[1]) * 26 * 26 + ('z' - domain[2]) * 26 + ('z' - domain[3])] =
                        WhoisClient.query(domain)
                }
                if (i % 32768 == 0) {
                    out.append("socket: $i / ${domains.size} / ${i - length} active\n").flush()
                    Thread.sleep(11000)
                }
            }
            out.append("create successful\n").flush()
        }.start()

        Thread {
            var savedStack = 0
            var stackSize = 0
            var currentStack: Int
            while (length < 456976) {
                length = Arrays.stream(list).filter { it != null }.count().toInt()
                FileWriter(File("./last.txt")).use { file ->
                    val str = buildString {
                        list.forEachIndexed { i, it ->
                            if (it == false) append("" + ('a' + i / 26 / 26 / 26) + ('a' + i / 26 / 26 % 26) + ('a' + i / 26 % 26) + ('a' + i % 26) + '\n')
                        }
                    }
                    file.append(str).flush()
                }
                currentStack = 456976 - length
                if (savedStack == currentStack) {
                    stackSize++
                } else {
                    savedStack = currentStack
                    stackSize = 0
                }
                WhoisClient.showThrown = stackSize > 10
                out.append("write: $length / $currentStack left [${stackSize}]\n").flush()
                Thread.sleep(1000)
                if(stackSize > 60) break
            }

            FileWriter(File("./missing.txt")).use { file ->
                val str = buildString {
                    list.forEachIndexed { i, it ->
                        if (it == null) append("" + ('a' + i / 26 / 26 / 26) + ('a' + i / 26 / 26 % 26) + ('a' + i / 26 % 26) + ('a' + i % 26) + '\n')
                    }
                }
                file.append(str).flush()
            }
            out.append("complete\n").flush()
            out.close()
        }.start()
    }

    data class WhoisResult(
        val domain: String,
        val result: Boolean
    )

//    private fun calc(id: Int, str: String) {
//        Thread.startVirtualThread {
//            try {
//                out.println("")
//                list[id] = ""
//            } catch (_: Exception) {
//                list[id] = str
//            }
//        }
//    }
}
