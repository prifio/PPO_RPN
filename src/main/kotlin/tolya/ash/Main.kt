package tolya.ash

fun main() {
    main2()
    main1()
    println()
}

fun main1() {
    val str = "2 + (3 * 4 + (5 + 6 / 2 - 7) * 8) - 9 - 10 - 11 + 12"
    val tokens = tokenizeString(str)
    val rpn = parse(tokens)
    val result = calc(rpn)
    println(result)
}

fun main2() {
    val str = "2 + (3 * 4 + (5 + 6 / 2 - 7) * 8) - 9 - 10 - 11 + 12"
    val tokens = tokenizeString(str)
    val rpn = parse(tokens)
    val printer = Printer()
    rpn.map { it.visit(printer) }
        .flatMap { it.asIterable() }
        .drop(1)
        .forEach { print(it) }
}
