import java.awt.color.ICC_Profile;
import java.io.IOException;

/* @test
   @bug 6631533
   @summary  unknown proffiling issue
   @run main/othervm  Test -Djava.iccprofile.path=data/cmm  ../profile.pf
*/

public class Test {
    public static void main(String[] args) {
        String fname = args[0];

        System.out.println("Profile to test: " + fname);

        ICC_Profile p = null;
        try {
            p = ICC_Profile.getInstance(fname);
        } catch (IOException e ) {
        }
        if (p == null) {
            System.out.println("Test passed.");
        } else {
            System.out.println("Test failed.");
            throw new RuntimeException("Failed");
        }
    }
}
