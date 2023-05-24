package org.jetbrains.research.ictl.fileimportance.test;

public class LambdaCalculator {

    private ILogic logic;

    public LambdaCalculator(ILogic logic) {
        this.logic = logic;
    }

    public int add(int a, int b){
        return logic.add(a, b);
    }

    public interface ILogic{
        int add (int a, int b);
    }
}
