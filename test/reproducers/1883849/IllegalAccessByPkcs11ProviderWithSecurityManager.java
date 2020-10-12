/*
 * @test
 * @bug 1883849
 * @run main/othervm -Djava.security.manager IllegalAccessByPkcs11ProviderWithSecurityManager
 */

import java.security.*;

public class IllegalAccessByPkcs11ProviderWithSecurityManager {

    public static void main(String[] args) throws Exception {
        KeyPair keypair = KeyPairGenerator.getInstance("DH").generateKeyPair();
        PublicKey pubkey = keypair.getPublic();
        pubkey.getEncoded();
        PrivateKey privkey = keypair.getPrivate();
        privkey.getEncoded();
    }

}
