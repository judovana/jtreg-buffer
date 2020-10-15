/*
 * @test
 * @bug 1883849
 * @run main/othervm/secure=default/java.security.policy=empty.policy IllegalAccessByPkcs11ProviderWithSecurityManager
 */

// ^^ If no policy is used jtreg itself generates access denied.
// see: http://mail.openjdk.java.net/pipermail/jtreg-use/2018-March/000603.html

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
