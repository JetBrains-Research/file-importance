package org.jetbrains.research.ictl.fileimportance.test

class AnonymousCalculator {
    val calc: ITestCalculator;

    constructor() {
        this.calc = object : ITestCalculator {
            override fun add(a: Int, b: Int): Int {
                return a + b
            }
        }
    }

    fun add(a: Int, b: Int): Int{
        return calc.add(a, b)
    }
}