// generated as
// a=`find cryptotest/ | grep .java | sed s/.java// | sed "s;/;.;g"`
// b=`echo $a` ; # to avoid quotes effect
// for x in `ls | grep .java` ; do sed -i "s/@build.*/@build $b/" $x ; done
/*
 * @test
 * @modules java.base/java.security
 *          java.base/com.sun.crypto.provider
 *          java.base/sun.security.internal.spec
 *          java.base/sun.security.ssl
 *          java.base/sun.security.x509
 *          java.security.jgss/sun.security.jgss
 *          java.security.jgss/sun.security.jgss.krb5
 *          java.security.jgss/sun.security.krb5
 *          java.smartcardio/javax.smartcardio
 *          java.xml.crypto/org.jcp.xml.dsig.internal.dom
 *          jdk.crypto.ec/sun.security.ec
 * @bug 6666666
 * @build cryptotest.CryptoTest
 *        cryptotest.Settings
 *        cryptotest.tests.AlgorithmParameterGeneratorTests
 *        cryptotest.tests.AlgorithmParametersTests
 *        cryptotest.tests.CertificateFactoryTests
 *        cryptotest.tests.CertPathBuilderTests
 *        cryptotest.tests.CertPathValidatorTests
 *        cryptotest.tests.CertStoreTests
 *        cryptotest.tests.CipherTests
 *        cryptotest.tests.ConfigurationTests
 *        cryptotest.tests.GssApiMechanismTests
 *        cryptotest.tests.KeyAgreementTests
 *        cryptotest.tests.KeyFactoryTests
 *        cryptotest.tests.KeyGeneratorTests
 *        cryptotest.tests.KeyInfoFactoryTests
 *        cryptotest.tests.KeyManagerFactoryTests
 *        cryptotest.tests.KeyPairGeneratorTests
 *        cryptotest.tests.KeyStoreTests
 *        cryptotest.tests.MacTests
 *        cryptotest.tests.MessageDigestTests
 *        cryptotest.tests.PolicyTests
 *        cryptotest.tests.SaslClientFactoryTests
 *        cryptotest.tests.SaslServerFactoryTests
 *        cryptotest.tests.SecretKeyFactoryTests
 *        cryptotest.tests.SecureRandomTests
 *        cryptotest.tests.SignatureTests
 *        cryptotest.tests.SSLContextTests
 *        cryptotest.tests.TerminalFactoryTests
 *        cryptotest.tests.TestProviders
 *        cryptotest.tests.TestServices
 *        cryptotest.tests.TransformServiceTests
 *        cryptotest.tests.TrustManagerFactoryTests
 *        cryptotest.tests.XMLSignatureFactoryTest
 *        cryptotest.utils.AlgorithmInstantiationException
 *        cryptotest.utils.AlgorithmRunException
 *        cryptotest.utils.AlgorithmTest
 *        cryptotest.utils.ClassFinder
 *        cryptotest.utils.KeysNaiveGenerator
 *        cryptotest.utils.Misc
 *        cryptotest.utils.TestResult
 *        cryptotest.utils.Xml
 * @run main/othervm/timeout=600 cryptotest.CryptoTest
 */
public class CryptoTestJtregBinding {

}


