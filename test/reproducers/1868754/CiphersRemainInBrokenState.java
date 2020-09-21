/*
 * @test
 * @bug 1868754
 * @run main/othervm CiphersRemainInBrokenState
 */

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;

public class CiphersRemainInBrokenState {

    public static void main(String[] args) throws Exception {

        // works with true
        boolean useBlockSize = false;

        String algorithm1 = "AES";
        String algName = algorithm1 + "/CBC/NoPadding";
        Cipher c1 = Cipher.getInstance(algName);
        byte[] data1 = new byte[useBlockSize ? c1.getBlockSize() : 3];

        System.out.println("Alg 1: " + c1.getAlgorithm());
        System.out.println("Provider 1: " + c1.getProvider());
        KeyGenerator kg1 = KeyGenerator.getInstance(algorithm1);
        Key key1 = kg1.generateKey();
        System.out.println("Key 1: " + key1);
        c1.init(Cipher.ENCRYPT_MODE, key1);
        try {
            c1.doFinal(data1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String algorithm2 = "DES";
        Cipher c2 = Cipher.getInstance(algorithm2 + "/CBC/NoPadding");
        byte[] data2 = new byte[c2.getBlockSize()];
        System.out.println("Alg 2: " + c2.getAlgorithm());
        System.out.println("Provider 2: " + c2.getProvider());
        KeyGenerator kg2 = KeyGenerator.getInstance(algorithm2);
        Key key2 = kg2.generateKey();
        System.out.println("Key 2: " + key2);
        c2.init(Cipher.ENCRYPT_MODE, key2);
        c2.doFinal(data2);
    }
}
