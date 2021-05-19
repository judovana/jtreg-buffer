
/*
   @test
   @library  bcprov-jdk15on-168.jar
   @bug 1960024
   @bug 8266279
   @summary JDK-8266279: 8u292 NoSuchAlgorithmException unrecognized algorithm name: PBEWithSHA1AndDESed
   @author Maybe Severin
   @run main PBEWithSHA1AndDESedeExists
*/



import javax.crypto.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;

public class PBEWithSHA1AndDESedeExists {
    public static void main(String[] args) throws Exception {
        new X500Name("CN=Test");
        new EncryptedPrivateKeyInfo("PBEWithSHA1AndDESede", new byte[] { 0 });
    }
}
