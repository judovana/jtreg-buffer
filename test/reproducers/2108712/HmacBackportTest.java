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
        public static void main(String[] args) throws Exception {
                KeyGenerator kg = KeyGenerator.getInstance("HmacSHA1");
                System.out.println(kg);
                System.out.println("KeyGenerator provider: " + kg.getProvider().getName());
        }
}

