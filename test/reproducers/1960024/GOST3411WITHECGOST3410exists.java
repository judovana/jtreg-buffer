/*
   @test
   @library  bcprov-jdk15on-168.jar
   @bug 1960024
   @bug 8266279
   @requires jdk.version.major >= 17
   @summary JDK-8266929: 8u292 Unable to use algorithms from 3p providers
   @author Maybe Severin
   @run main GOST3411WITHECGOST3410exists
*/

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.NoSuchAlgorithmException;
import java.security.Security;

public class GOST3411WITHECGOST3410exists {
    public static void main(String[] args) throws Exception {

        String algorithmName = "GOST3411WITHECGOST3410";
        String algorithmProvider = "BC";

        Security.addProvider(new BouncyCastleProvider());
        checkAlgoritmhPresence(algorithmName);

        Security.removeProvider(algorithmProvider);
        try {
            checkAlgoritmhPresence(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            // expected exception
            return;
        }

        throw new RuntimeException("No SuchAlgorithmException not thrown");
    }

    private static void checkAlgoritmhPresence(String gost3411WITHECGOST3410) throws NoSuchAlgorithmException {
        new javax.crypto.EncryptedPrivateKeyInfo(gost3411WITHECGOST3410, new byte[]{0});
    }
}