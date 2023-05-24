package org.jetbrains.research.ictl.fileimportance.test

fun main(args: Array<String>) {
    val calc = getCalc()
    println(calc.add(2, 3))
    println(scriptAdd(2, 5))
    println(AnonymousCalculator().add(2, 7))
    println(LambdaCalculator(){ a, b -> a+b}.add(2, 7))
    println(Utils.NAME)
    println((calc as TestCalculatorImpl).name)
}

fun getCalc(): ITestCalculator {
    return TestCalculatorImpl()
}