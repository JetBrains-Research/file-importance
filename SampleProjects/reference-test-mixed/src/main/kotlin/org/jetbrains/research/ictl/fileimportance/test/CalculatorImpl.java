package org.jetbrains.research.ictl.fileimportance.test;

public class CalculatorImpl implements IJavaCalculator {

    public String name = "CalculatorImpl";
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
