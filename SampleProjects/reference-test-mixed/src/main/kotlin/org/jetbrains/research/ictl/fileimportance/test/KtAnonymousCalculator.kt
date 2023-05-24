package org.jetbrains.research.ictl.fileimportance.test

class KtAnonymousCalculator {
    val calc: IKtCalculator;

    constructor() {
        this.calc = object : IKtCalculator {
            override fun add(a: Int, b: Int): Int {
                return a + b
            }
        }
    }

    fun add(a: Int, b: Int): Int{
        return calc.add(a, b)
    }
}