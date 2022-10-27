package org.jetbrains.research.ictl.fileimportance.test;

public class JavaMain {

    public static void main(String[] args) {
        IJavaCalculator calc = new CalculatorImpl();
        System.out.println(calc.add(2, 3));
        System.out.println(JavaUtils.add(2, 5));
        System.out.println(new KtAnonymousCalculator().add(2, 7));
        System.out.println(new KtLambdaCalculator(Integer::sum).add(2,9));
        System.out.println(new JavaLambdaCalculator(new LogicImpl()).add(2,11));
        System.out.println(JavaUtils.NAME);
        System.out.println(((CalculatorImpl) calc).name);
    }

    public static class LogicImpl implements JavaLambdaCalculator.ILogic{

        @Override
        public int add(int a, int b) {
            return a + b;
        }
    }
}
