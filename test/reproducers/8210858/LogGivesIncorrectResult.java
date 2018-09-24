/*
 * @test
 * @bug 8210858
 * @summary AArch64: Math.log intrinsic gives incorrect results
 */

public class LogGivesIncorrectResult {
    public static void main(String[] args) {
        double x = 4.9E-324;
        double res = Math.log(x);
        double expected = -744.4400719213812;
        if (res != expected) {
            throw new RuntimeException("Test Math.log() FAILED! Got: " + res + " instead of " + expected);
        } else {
            System.out.println("Test passed!");
        }
    }
}
