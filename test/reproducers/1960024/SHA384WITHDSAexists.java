
/*
   @test
   @library  bcprov-jdk15on-168.jar
   @bug 1960024
   @bug 8266279
   @summary JDK-8266929: 8u292 Unable to use algorithms from 3p providers
   @author Maybe Severin
   @requires  jdk.version.major <= 8
   @run main SHA384WITHDSAexists
*/



import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

public class SHA384WITHDSAexists {
    public static void main (String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        new javax.crypto.EncryptedPrivateKeyInfo("SHA384WITHDSA", new byte[]{0});
        Security.removeProvider("BC");
        new javax.crypto.EncryptedPrivateKeyInfo("SHA384WITHDSA", new byte[]{0}); // this second one is failing on u292
    }
}

