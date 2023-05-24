package org.jetbrains.research.ictl.fileimportance.test;

public class JavaAnonymousCalc {
    private IJavaCalculator calculator;

    public JavaAnonymousCalc() {
        calculator = new IJavaCalculator() {
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
