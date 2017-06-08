package cryptotest.tests;

import cryptotest.utils.AlgorithmInstantiationException;
import cryptotest.utils.AlgorithmRunException;
import cryptotest.utils.AlgorithmTest;
import cryptotest.utils.TestResult;

import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHPublicKeySpec;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Random;

import static java.math.BigInteger.ONE;

public class KeyFactoryTests extends AlgorithmTest {
    private final Random random = new SecureRandom(new byte[]{6, 6, 6});

    public static void main(String[] args) {
        TestResult r = new KeyFactoryTests().mainLoop();
        System.out.println(r.getExplanation());
        System.out.println(r.toString());
        r.assertItself();
    }

    @Override
    protected void checkAlgorithm(Provider.Service service, String alias) throws AlgorithmInstantiationException,
            AlgorithmRunException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(alias, service.getProvider());
            KeySpec privateKeySpec;
            KeySpec publicKeySpec;

            if (service.getAlgorithm().contains("DSA")) {
                privateKeySpec = new DSAPrivateKeySpec(ONE, ONE, ONE, ONE);
                publicKeySpec = new DSAPublicKeySpec(ONE, ONE, ONE, ONE);
            } else if (service.getAlgorithm().contains("RSA")) {
                privateKeySpec = new RSAPrivateKeySpec(BigInteger.probablePrime(512, random), ONE);
                publicKeySpec = new RSAPublicKeySpec(BigInteger.probablePrime(512, random), ONE);
            } else if (service.getAlgorithm().contains("EC")) {
                KeyPair keyPair = KeyPairGenerator.getInstance("EC").genKeyPair();

                ECPrivateKey ecPrivateKey = ((ECPrivateKey) keyPair.getPrivate());
                ECPublicKey ecPublicKey = ((ECPublicKey) keyPair.getPublic());

                privateKeySpec = new ECPrivateKeySpec(ONE, ecPrivateKey.getParams());
                publicKeySpec = new ECPublicKeySpec(ecPublicKey.getW(), ecPublicKey.getParams());
            } else if (service.getAlgorithm().contains("DiffieHellman")) {
                privateKeySpec = new DHPrivateKeySpec(ONE, ONE, ONE);
                publicKeySpec = new DHPublicKeySpec(ONE, ONE, ONE);
            } else {
                privateKeySpec = null;
                publicKeySpec = null;
            }

            if (keyFactory.generatePrivate(privateKeySpec) == null || keyFactory.generatePublic(publicKeySpec) ==
                    null) {
                throw new UnsupportedOperationException("Generated key is null for " + service.getAlgorithm() + " in"
                        + service.getProvider().getName());
            }
        } catch (NoSuchAlgorithmException e) {
            throw new AlgorithmInstantiationException(e);
        } catch (UnsupportedOperationException | InvalidKeySpecException e) {
            throw new AlgorithmRunException(e);
        }
    }
}
