package org.jetbrains.research.ictl.fileimportance.test

class KtLambdaCalculator(val logic: (a: Int, b: Int) -> Int) {

    fun add(a: Int, b: Int): Int{
        return logic(a, b)
    }
}