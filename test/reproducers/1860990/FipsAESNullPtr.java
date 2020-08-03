/**
 * @test
 * @bug 1860990
 * @summary  FIPS: AES cipher throws NullPointerException during init
 * @author zzambers
 * @requires jdk.version.major >= 8
 * @run main/othervm FipsAESNullPtr
 */
 
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;

public class FipsAESNullPtr {

    public static void main(String[] args) throws Exception {
        String algName = "AES_256/CBC/NoPadding";

        Cipher c = Cipher.getInstance(algName);
        System.out.println("Alg: " + c.getAlgorithm());
        System.out.println("Provider: " + c.getProvider());

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        Key key = kg.generateKey();
        System.out.println("Key: " + key);
        c.init(Cipher.ENCRYPT_MODE, key);
    }
}
