package org.jetbrains.research.ictl.fileimportance.test;

public class Main {

    public static void main(String[] args) {
        ICalculator calc = new CalculatorImpl();
        System.out.println(calc.add(2, 3));
        System.out.println(Utils.add(2, 5));
        System.out.println(new AnonymousCalc().add(2, 7));
        System.out.println(new LambdaCalculator(Integer::sum).add(2,9));
        System.out.println(new LambdaCalculator(new LogicImpl()).add(2,11));
        System.out.println(Utils.NAME);
        System.out.println(((CalculatorImpl) calc).name);
    }

    public static class LogicImpl implements LambdaCalculator.ILogic{

        @Override
        public int add(int a, int b) {
            return a + b;
        }
    }
}
