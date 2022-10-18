class TestCalculatorImpl: ITestCalculator {
    override fun add(a: Int, b: Int): Int {
        return Utils.companionAdd(a, b)
    }
}