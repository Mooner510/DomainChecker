package kr.mooner510.domainchecker

object Example {
    @JvmStatic
    fun main(args: Array<String>) {
        DomainChecker(RangeMaker.makeRange(4))
    }
}
