package org.jetbrains.research.ictl.fileimportance.test

fun main(args: Array<String>) {
    val calc = getCalc()
    println(calc.add(2, 3))
    println(scriptAdd(2, 5))
    println(JavaAnonymousCalc().add(2, 7))
    println(JavaLambdaCalculator(){ a, b -> a+b}.add(2, 7))
    println(KtUtils.NAME)
    println((calc as TestCalculatorImpl).name)
}

fun getCalc(): IKtCalculator {
    return TestCalculatorImpl()
}