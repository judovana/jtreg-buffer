/* @test
 * @modules java.desktop/com.sun.media.sound
   @bug 6738524
   @summary Tests that com.sun.media.sound.JDK13Services.getDefaultProvider does not allow to get system properties
   @author Alex Menkov
   @run main DefaultProvider
 */

/*
 * The test uses additional class user.home (user/home.java)
 */
public class DefaultProvider {

    public static void main(String[] args) throws Exception {
        String value = com.sun.media.sound.JDK13Services.getDefaultProviderClassName(user.home.class);
        if (value != null) {
            System.out.println("FAILED: user.home: \"" + value + "\"");
            throw new RuntimeException("got user.home: \"" + value + "\"");
        }
        System.out.println("PASSED");
    }

}

