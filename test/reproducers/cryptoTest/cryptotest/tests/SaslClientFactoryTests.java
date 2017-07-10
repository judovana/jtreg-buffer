package cryptotest.tests;

import cryptotest.utils.AlgorithmInstantiationException;
import cryptotest.utils.AlgorithmRunException;
import cryptotest.utils.AlgorithmTest;
import cryptotest.utils.Misc;
import cryptotest.utils.TestResult;
import java.security.Provider;
import java.util.HashMap;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

public class SaslClientFactoryTests extends AlgorithmTest {

    public static void main(String[] args) {
        System.setProperty("java.security.krb5.conf", 
                Misc.createTmpKrb5File().getPath());
        TestResult r = new SaslClientFactoryTests().mainLoop();
        System.out.println(r.getExplanation());
        System.out.println(r.toString());
        r.assertItself();
    }

    @Override
    public String getTestedPart() {
        return "SaslClientFactory";
    }

    @Override
    protected void checkAlgorithm(Provider.Service service, String alias) throws AlgorithmInstantiationException, AlgorithmRunException {
        try {
            String[] mechanisms = new String[]{alias};
            SaslClient client = Sasl.createSaslClient(mechanisms, "username",
                    "ldap", "127.0.0.1", new HashMap<String, Object>(), new ClientCallbackHandler("password"));
            if (client != null) {
                printResult("Mechanism is '" + client.getMechanismName() 
                                + "' and authentication is " + (client.isComplete() ? "" : "NOT ") 
                                + "complete");
            } else {
                throw new AlgorithmRunException(new RuntimeException(
                        String.format("client null, provider '%s' and alias '%s'", service.getAlgorithm(), alias)));
            }
        } catch (SaslException ex) {
            throw new AlgorithmInstantiationException(ex);
        }
    }

    private class ClientCallbackHandler implements CallbackHandler {

        private String password = null;

        public ClientCallbackHandler(String password) {
            this.password = password;
        }

        @Override
        public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callback;
                    nc.setName(nc.getDefaultName());
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callback;
                    if (password != null && !password.isEmpty()) {
                        pc.setPassword(this.password.toCharArray());
                    } else {
                        throw new IllegalArgumentException("password empty");
                    }
                } else if (callback instanceof RealmCallback) {
                    RealmCallback rc = (RealmCallback) callback;
                    rc.setText(rc.getDefaultText());
                } else if (callback instanceof AuthorizeCallback) {
                    AuthorizeCallback ac = (AuthorizeCallback) callback;
                    String authid = ac.getAuthenticationID();
                    String authzid = ac.getAuthorizationID();
                    ac.setAuthorized(authid.equals(authzid));
                    if (ac.isAuthorized()) {
                        ac.setAuthorizedID(authzid);
                    }
                } else {
                    throw new UnsupportedCallbackException(callback, "Unrecognized SASL ClientCallback");
                }
            }
        }
    }
}
