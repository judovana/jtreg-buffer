import java.io.*;
import java.util.*;
import java.security.*;
import javax.net.ssl.*;

class Main {
	public static String db_password = "nss.SECret.123";

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: java Main password-for-nssdb");
			System.exit(1);
		}

		KeyStore ks = KeyStore.getInstance("PKCS11", "SunPKCS11-NSS-FIPS");
		ks.load(null, args[0].toCharArray());

		System.out.println("All known SunJSSE.PKCS12 aliases:");
		for (Enumeration<String> e = ks.aliases(); e.hasMoreElements(); ) {
			System.out.println(" - " + e.nextElement());
		}
		System.out.println();
	}
}
