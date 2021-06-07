
/*
   @test
   @library  bcprov-jdk15on-168.jar
   @bug 1960024
   @bug 8266279
   @summary JDK-8266929: 8u292 Unable to use algorithms from 3p providers
   @author Maybe Severin
   @run main GOST3411WITHECGOST3410exists
*/



import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

public class GOST3411WITHECGOST3410exists {
    public static void main (String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        new javax.crypto.EncryptedPrivateKeyInfo("GOST3411WITHECGOST3410", new byte[]{0});
        Security.removeProvider("BC");
        new javax.crypto.EncryptedPrivateKeyInfo("GOST3411WITHECGOST3410", new byte[]{0}); // this second one is failing on u292
    }
}

