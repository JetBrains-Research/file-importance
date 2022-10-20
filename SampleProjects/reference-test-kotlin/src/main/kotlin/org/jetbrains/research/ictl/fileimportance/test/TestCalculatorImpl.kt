package org.jetbrains.research.ictl.fileimportance.test

class TestCalculatorImpl: ITestCalculator {
    val name = "TestCalculatorImpl"
    override fun add(a: Int, b: Int): Int {
        return Utils.companionAdd(a, b)
    }
}