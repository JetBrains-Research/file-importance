public class Main {
    public static void main(String[] args){
        ICalculator calc = new CalculatorImpl();
        System.out.println(calc.add(2, 4));
        System.out.println(Utils.add(2, 7));
    }
}
