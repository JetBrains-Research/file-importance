package org.jetbrains.research.ictl.fileimportance.test;

public class JavaLambdaCalculator {

    private ILogic logic;

    public JavaLambdaCalculator(ILogic logic) {
        this.logic = logic;
    }

    public int add(int a, int b){
        return logic.add(a, b);
    }

    public interface ILogic{
        int add (int a, int b);
    }
}
