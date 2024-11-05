import java.security.*;
import java.util.Arrays;
import java.util.List;

public class CheckAlgorithms {
    public static final String FIPS_PROVIDER = "SunPKCS11-NSS-FIPS";
    public static final String NONFIPS_PROVIDER = "SunPCSC";
    public static final String NONFIPS_ALGORITHM = "TLS_RSA_WITH_AES_128_CBC_SHA";

    private static final List<String> possibleFirstArgs = Arrays.asList("assert", "true", "list", "false");
    private static final List<String> possibleSecondArgs = Arrays.asList("algorithms", "providers", "both");

    public static void main(String[] args) throws Exception {
        if (args.length != 2 || args[0].equals("--help") || args[0].equals("-h")) {
            System.err.println("Test for listing available algorithms and providers and checking their FIPS compatibility");
            System.err.println("Usage: CheckAlgorithms " + possibleFirstArgs + " " + possibleSecondArgs);
            System.err.println("First argument: specify whether to check FIPS compatibility (assert/true) or just list the items (list/false)");
            System.err.println("Second argument: specify what to check - algorithms, providers or both");
            System.exit(1);
        }

        // Parse the arguments
        String shouldHonorFips = args[0].toLowerCase();
        String testCategory = args[1].toLowerCase();

        // Check if the shouldHonorFips is valid value
        if (!possibleFirstArgs.contains(shouldHonorFips)) {
            System.err.println("Invalid value for the first argument: '" + args[0] + "', use --help for more info.");
            System.exit(1);
        }

        // Check if the testCategory is valid value
        if (!possibleSecondArgs.contains(testCategory)) {
            System.err.println("Invalid test category: '" + args[1] + "', use --help for more info.");
            System.exit(1);
        }

        System.out.println("Is fips honoring expected? - " + shouldHonorFips);
        System.out.println("This test is set to test " + testCategory + " now.");

        boolean honorFipsHere = shouldHonorFips.equals("assert") || shouldHonorFips.equals("true");

        if (testCategory.equals("algorithms") || testCategory.equals("both")){
            checkAlgorithms(honorFipsHere);
        }
        if (testCategory.equals("providers") || testCategory.equals("both")) {
            checkProviders(honorFipsHere);
        }
    }

    static void checkProviders(boolean shouldHonorFips) throws Exception{
        System.out.println(">>>LIST OF PROVIDERS:<<<");
        for(Provider provider : Security.getProviders()){
            System.out.println(provider);
        }

        //checks that if we expect fips provider, there is a fips provider
        if (shouldHonorFips && !providerFound(FIPS_PROVIDER)) {
            throw new Exception("Test failed, FIPS provider was not found when honoring FIPS.");
        }

        //checks that when we except fips provider, the non-fips provider is not there
        if(shouldHonorFips && providerFound(NONFIPS_PROVIDER)){
            throw new Exception("Test failed, found non-FIPS provider when honoring FIPS.");
        }
    }

    static void checkAlgorithms(boolean shouldHonorFips) throws Exception{
        System.out.println(">>>LIST OF ALGORITHMS:<<<");
        for (String cipher : CipherList.getCipherList()) {
            System.out.println(cipher);
        }

        //if the test found nonfips algorithm, and it should honor FIPS, throw an exception
        //otherwise, everything is ok
        if(shouldHonorFips && algorithmFound(NONFIPS_ALGORITHM)){
            throw new Exception("Test failed, found non-FIPS algorithm when honoring FIPS.");
        }
    }

    static boolean algorithmFound(String algorithm) throws Exception{
        for(String cipher : CipherList.getCipherList()){
            if(cipher.contains(algorithm)){
                return true;
            }
        }
        return false;
    }

    static boolean providerFound(String provider){
        for(Provider p : Security.getProviders()){
            if(p.contains(provider)){
                return true;
            }
        }
        return false;
    }
}

