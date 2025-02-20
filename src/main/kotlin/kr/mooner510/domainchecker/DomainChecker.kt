package kr.mooner510.domainchecker

import kr.mooner510.domainchecker.RangeMaker.toDomain
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class DomainChecker(
    private val domains: List<String>,
) {
    private var length: Int = 0
    private val list = arrayOfNulls<Boolean>(domains.size)

    companion object {
        private var writer: BufferedWriter? = null

        val out get() = writer!!
    }

    init {
        writer = BufferedWriter(OutputStreamWriter(System.out))
        run()
    }

    private fun run() {
        out.append("thread complete\n").flush()

        Thread {
            out.append("waiting start: ${domains.size}\n").flush()

            domains.forEachIndexed { i, domain ->
                Thread.ofVirtual().inheritInheritableThreadLocals(false).start {
                    list[('z' - domain[0]) * 26 * 26 * 26 + ('z' - domain[1]) * 26 * 26 + ('z' - domain[2]) * 26 + ('z' - domain[3])] =
                        WhoisClient.query(domain)
                }
                if (i % 32768 == 0) {
                    out.append("socket: $i / ${domains.size} / ${i - length} active\n").flush()
                    Thread.sleep(12000)
                }
            }
            out.append("create successful\n").flush()
        }.start()

        Thread {
            var savedStack = 0
            var stackSize = 0
            var currentStack: Int
            val fileName = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            while (length < domains.size) {
                length = Arrays.stream(list).filter { it != null }.count().toInt()
                FileWriter(File("./last-${fileName}.txt")).use { file ->
                    val str = buildString {
                        list.forEachIndexed { i, it ->
                            if (it == false) append(i.toDomain() + '\n')
                        }
                    }
                    file.append(str).flush()
                }
                currentStack = domains.size - length
                if (savedStack == currentStack) {
                    stackSize++
                } else {
                    savedStack = currentStack
                    stackSize = 0
                }
                WhoisClient.showThrown = stackSize > 10
                out.append("write: $length / $currentStack left [${stackSize}]\n").flush()
                Thread.sleep(1000)
                if (stackSize > 30) break
            }

            FileWriter(File("./missing.txt")).use { file ->
                val str = buildString {
                    list.forEachIndexed { i, it ->
                        if (it == null) append(i.toDomain() + '\n')
                    }
                }
                file.append(str).flush()
            }

            out.append("complete\n").flush()
            out.close()
        }.start()
    }
}
