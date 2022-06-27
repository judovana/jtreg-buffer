import javax.net.ssl.SSLContext;

/*
 @test TestTrustStore
 @bug 2055274
 @summary test whether default cacert location is set correctly
 @run shell TestTrustStore.sh
*/

public class TestTrustStore {
    public static void main(String[] args) throws Exception {
        SSLContext context = SSLContext.getDefault();
        System.out.printf("Context: %s, Protocol: %s, Provider: %s", context, context.getProtocol(), context.getProvider());

    }

}
