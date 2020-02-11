/*
   @test
   @bug 1699068
   @requires jdk.version.major >= 8
   @summary Elliptic Curve secp256k1 is missing in jdk
   @run main/othervm EllipticCurve
*/

import java.security.Security;
import java.util.*;
import java.security.KeyPair;
import java.security.spec.ECGenParameterSpec;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class EllipticCurve {

    public static void main(String[] args) throws Exception {
        String curves = Security.getProvider("SunEC").getProperty("AlgorithmParameters.EC SupportedCurves");
        if (curves.indexOf("secp256k1") < 0) {
    	    throw new Exception("secp256k1 is not in supported curves");
        }

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecs = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecs, new SecureRandom());
        KeyPair pair = keyGen.genKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();
    }

}
