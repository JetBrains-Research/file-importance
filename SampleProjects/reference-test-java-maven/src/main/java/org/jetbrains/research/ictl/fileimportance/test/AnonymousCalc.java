package org.jetbrains.research.ictl.fileimportance.test;

public class AnonymousCalc {
    private ICalculator calculator;

    public AnonymousCalc() {
        calculator = new ICalculator() {
            @Override
            public int add(int a, int b) {
                return a + b;
            }
        };
    }

    public int add(int a, int b){
        return calculator.add(a, b);
    }
}
