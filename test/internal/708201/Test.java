import java.awt.GraphicsEnvironment;

/*
 * 708201 is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
 * dont forget to include 0708201 as whole bug id
 * @test
 * @bug 0708201
 * @summary  verify that fontconfig is insatlled and used
 * @run shell runtest.sh
 */

public class Test {

    public static void main(String[] a) {
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

}
