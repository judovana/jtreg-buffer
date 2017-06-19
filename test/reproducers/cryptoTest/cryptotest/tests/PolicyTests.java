package cryptotest.tests;

import cryptotest.utils.AlgorithmInstantiationException;
import cryptotest.utils.AlgorithmRunException;
import cryptotest.utils.AlgorithmTest;
import cryptotest.utils.TestResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.Provider;

public class PolicyTests extends AlgorithmTest {

    public static void main(String[] args) {
        TestResult r = new PolicyTests().mainLoop();
        System.out.println(r.getExplanation());
        System.out.println(r.toString());
        r.assertItself();
    }

    @Override
    protected void checkAlgorithm(Provider.Service service, String alias) throws AlgorithmInstantiationException,
            AlgorithmRunException {
        try {
            final CodeSource codeSource = new CodeSource(new URL("http://localhost"), (CodeSigner[]) null);
            Policy policy = Policy.getInstance(alias, null, service.getProvider());

            policy.refresh();
            PermissionCollection permissions = policy.getPermissions(codeSource);
            if (permissions == null) {
                throw new UnsupportedOperationException("Permission cant be reached for " + service.getAlgorithm() +
                        " in" + service.getProvider().getName());
            }
            policy.implies(new ProtectionDomain(codeSource, null), new RuntimePermission("blabol"));
        } catch (NoSuchAlgorithmException e) {
            throw new AlgorithmInstantiationException(e);
        } catch (UnsupportedOperationException | MalformedURLException e) {
            throw new AlgorithmRunException(e);
        }
    }
}
