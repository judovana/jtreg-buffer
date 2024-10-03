import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;

public class CheckFips {
    public static final String FIPS_PROVIDER = "SunPKCS11-NSS-FIPS";
    public static final String NONFIPS_PROVIDER = "SunPCSC";
    public static final String NONFIPS_ALGORITHM = "TLS_RSA_WITH_AES_128_CBC_SHA";

    public static void main(String[] args) throws Exception {
	if (args.length != 2) {
            System.err.println("Usage: CheckFips <true|false> <algorithms|providers>");
            System.exit(1);
        }

        // Parse the arguments
        boolean shouldHonorFips = Boolean.parseBoolean(args[0]);
        String testCategory = args[1];

        // Check if the shouldHonorFips is "true" or "false"
        if (!args[0].equalsIgnoreCase("true") && !args[0].equalsIgnoreCase("false")) {
            System.err.println("Invalid value for the first argument: " + args[0]);
            System.exit(1);
        }

        // Check if the test category is "algorithms" or "providers"
        if (!testCategory.equalsIgnoreCase("algorithms") && !testCategory.equalsIgnoreCase("providers")) {
            System.err.println("Invalid test category: " + testCategory);
            System.exit(1);
        }

	System.out.println("Is fips honoring expected? - " + shouldHonorFips);
	System.out.println("This test is set to test " + testCategory + " now.");

	if(testCategory.equals("algorithms")){
	    checkAlgorithms(shouldHonorFips);
	}
	else {
	    checkProviders(shouldHonorFips);
	}
    }

    static void checkProviders(boolean shouldHonorFips) throws Exception{

	//first half of the following condition checks that if we expect fips provider, there is a fips provider
	//the second half checks that when we expect fips provider, the non-fips provider is not there
	//inversion works as well.. 
	if(providerFound(FIPS_PROVIDER) == shouldHonorFips && !providerFound(NONFIPS_PROVIDER) == shouldHonorFips){
            return;
        }
        else{
            throw new Exception("Test failed");
        }
    }

    static void checkAlgorithms(boolean shouldHonorFips) throws Exception{
	String[] ciphers = CipherList.getCipherList();
    	for(int i=0; ciphers.length > i; i++){
            System.out.println(ciphers[i]);
	}
	if(algorithmFound(NONFIPS_ALGORITHM) == shouldHonorFips){
	    throw new Exception("Test failed");
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

