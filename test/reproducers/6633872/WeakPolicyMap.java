/*
 * @test %I%, %E%
 * @bug 6633872
 * @summary Policy/PolicyFile leak dynamic ProtectionDomains
 * @requires jdk.version.major < 24
 * @run main/othervm/policy=WeakPolicyMap.policy WeakPolicyMap
 */

// * @key closed-security

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WeakPolicyMap {
    public static void main(String[] args) throws Exception {

	// Make sure SecurityManager is enabled
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        ArrayList<ClassLoader> loaders = new ArrayList<ClassLoader>();
	// this loop adds 1000 instances of a ClassLoader for Dyn.class
        for (int runCt = 0; runCt < 1000; ++runCt) {
            ClassLoader loader = URLClassLoader.newInstance(
                new URL[] { new URL(
                    new Object() {}.getClass().getResource("/Dyn.class"), "."
                )}, null
            );
            Class.forName("Dyn", true, loader);
            loaders.add(loader);
        }
            
        final AtomicBoolean stop = new AtomicBoolean(false);
        final AtomicInteger hash = new AtomicInteger(1);
        final AllPermission all = new AllPermission();

	// loop runs 1000 times
        for (int runCt = 0; runCt < 1000; ++runCt) {
	    // create a ProtectionDomain
            ProtectionDomain domain = new ProtectionDomain(null, null, null, null) {
		// atomically returns the current hash value
                @Override
                public int hashCode() {
                    return hash.get();
                }
		// atomically sets stop to true
                @Override
                public boolean equals(Object obj) {
//		    Thread.currentThread().dumpStack();
                    stop.set(true);
                    return true;
                }
            };
	    // loop runs 1000 times
            for (int ct = 0; ct < 1000; ++ct) {
                domain.implies(all);
                if (stop.get()) {
                    throw new Exception("Test FAILED");
                }
                hash.incrementAndGet();
            }
        }
    }
}
class Dyn {
    static {
        try {
            System.exit(0);
        } catch (SecurityException exc) {
        }
    }
}
