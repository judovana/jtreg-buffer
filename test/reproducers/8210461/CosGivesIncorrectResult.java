/*
 * @test
 * @bug 8210461
 * @summary AArch64: Math.cos intrinsic gives incorrect results
 */

public class CosGivesIncorrectResult {
    public static void main(String[] args) {
        double res = Math.cos(1647100);
        if (res != 0.7833030468809974) {
            throw new RuntimeException("Test FAILED! Got: " + res + " instead of 0.7833030468809974");
        } else {
            System.out.println("Test passed!");
        }
    }
}
