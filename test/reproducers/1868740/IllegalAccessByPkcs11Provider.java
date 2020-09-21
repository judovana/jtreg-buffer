/*
 * @test
 * @bug 1868740
 * @run main/othervm IllegalAccessByPkcs11Provider
 */

import java.security.*;

public class IllegalAccessByPkcs11Provider {

    public static void main(String[] args) throws Exception {
        KeyPair keypair = KeyPairGenerator.getInstance("DH").generateKeyPair();
        PublicKey pubkey = keypair.getPublic();
        pubkey.getEncoded();
        PrivateKey privkey = keypair.getPrivate();
        privkey.getEncoded();
    }

}
