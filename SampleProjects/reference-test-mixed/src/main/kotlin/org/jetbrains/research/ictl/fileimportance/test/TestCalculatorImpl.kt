package org.jetbrains.research.ictl.fileimportance.test

class TestCalculatorImpl: IKtCalculator {
    val name = "TestCalculatorImpl"
    override fun add(a: Int, b: Int): Int {
        return KtUtils.companionAdd(a, b)
    }
}