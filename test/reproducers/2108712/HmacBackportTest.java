/*
 * @test
 * @bug 2108712
 * @requires jdk.version.major > 8
 * @summary BZ-2108712: Backport of Hmac support in fips mode to ojdk11
 * @author jandrlik
 * @run main HmacBackportTest
 */

import javax.crypto.KeyGenerator;

public class HmacBackportTest {

        private static boolean isFedoraBuild() {
            String bos = null;
            try {
                bos = System.getenv("OTOOL_BUILD_OS_NAME");
            } catch (Exception e) { /* ignore */ }
            return "f".equals(bos);
        }

        public static void main(String[] args) throws Exception {
                if (isFedoraBuild()) {
                    // backport of JDK-8242332 is only in Rhel builds
                    // https://bugzilla.redhat.com/show_bug.cgi?id=2108712
                    System.out.println("Does not apply to fedora builds, skipping...");
                    return;
                }
                KeyGenerator kg = KeyGenerator.getInstance("HmacSHA1");
                System.out.println(kg);
                System.out.println("KeyGenerator provider: " + kg.getProvider().getName());
        }
}

