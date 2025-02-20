package kr.mooner510.domainchecker

import java.io.FileReader
import java.util.*


object RangeMaker {
    private val charArray = Array(26) { 'a' + it }

    fun makeRange(digit: Int, suffix: String = ".com"): List<String> {
        var domains: List<String> = Arrays.stream(charArray).map { "$it$suffix" }.toList()
        for (i in 1 until digit) {
            domains = Arrays.stream(charArray).flatMap { outer -> domains.stream().map { "$outer$it" } }.toList()
        }
        return domains
    }

    fun readFrom(suffix: String = ".com", path: String = "./missing.txt"): List<String> {
        FileReader(path).use { reader ->
            return reader.readLines().stream().map { "$it$suffix" }.toList()
        }
    }

    fun List<String>.startAfter(domain: String): List<String> {
        return this.stream().filter { it >= domain }.toList()
    }

    fun String.toId(suffix: String = ".com"): Int {
        var id = 0
        var weight = 1
        for (i in this.length - suffix.length - 1 downTo 0) {
            id += ('z' - this[i]) * weight
            weight *= 26
        }
        return id
    }

    fun Int.toDomain(): String {
        return buildString {
            var cursor = this@toDomain
            while (cursor > 0) {
                insert(0, 'a' + cursor % 26)
                cursor /= 26
            }
            append(".com")
        }
    }
}
