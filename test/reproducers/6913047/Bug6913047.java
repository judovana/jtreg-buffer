/*
 * Copyright (c) 2017, Red Hat, Inc. and/or its affiliates.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 6913047
 * @summary Native memory exhaustion using SunPKCS11 on 32 bits JVM.
 * @run shell/timeout=120 run.sh
 * @author Martin Balao (mbalao@redhat.com)
 */


import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.security.pkcs11.SunPKCS11;

public class Bug6913047 {

    public static void main(String[] args) throws Exception {

        Security.setProperty("crypto.policy", "unlimited");

        Boolean isWindows = false;

        String name = System.getProperty("os.name");
        if (name.startsWith("Windows")) {
            isWindows = true;
        }

        String nssLibDir = null;

        if (isWindows) {
            nssLibDir = System.getProperty("test.nssLibDir", "nssLibDir_NOT_DEFINED");
        } else {
            boolean x64 = "64".equals(System.getProperty("sun.arch.data.model"));
            boolean aarch64 = "aarch64".equals(System.getProperty("os.arch"));
            String ldPath = System.getProperty("java.library.path");
            if (x64 && aarch64) {
                //there is bug on aarch64 missing /usr/lib64 on library path; which is crucial
                ldPath="/usr/lib64"+":"+ldPath;
            }
            for (String libDir : ldPath.split(":")) {
                if (Pattern.matches("^/usr/lib[0-9]*$", libDir)) {
                    nssLibDir = libDir;
                    break;
                }
            }
            if (nssLibDir == null) {
                if (x64) {
                    nssLibDir = "/usr/lib64";
                } else {
                    nssLibDir = "/usr/lib";
                }
            }
        }

        String nSSConfigString = "name = NSS\n" +
                "nssLibraryDirectory = " + nssLibDir + "\n" +
                "nssDbMode = noDb\n" +
                "attributes = compatibility\n";
        InputStream nSSConfigStream = new ByteArrayInputStream(nSSConfigString.getBytes(StandardCharsets.UTF_8));
        Provider pkcs11provider;

        try {
            // configure does not seem to support ByteArrayInputStream config files
            // double hyphen is an undocumented feature, so it might change
            Method configure = Provider.class.getMethod("configure", String.class);
            Provider pkcs11 = Security.getProvider("SunPKCS11");
            pkcs11provider = (Provider) configure.invoke(pkcs11, "--" + nSSConfigString);

        } catch (NoSuchMethodException e) {
            // for java versions lower than 9
            java.lang.reflect.Constructor<SunPKCS11> pkcs11;
            pkcs11 = SunPKCS11.class.getConstructor(InputStream.class);
            pkcs11provider = pkcs11.newInstance(nSSConfigStream);
        }

        try {
            Security.addProvider(pkcs11provider);
        } catch (java.security.ProviderException e) {
        }

        byte[] plaintext = new byte[32];
        byte[] rawKey = new byte[32];
        Key commonkey = new SecretKeySpec(rawKey, "AES");

        List<Object> keysHolderList = new ArrayList<>();

        Class<?> cipherClass = Class.forName("javax.crypto.Cipher");
        Class<?> p11CipherClass = Class.forName("sun.security.pkcs11.P11Cipher");

        Field spiField = cipherClass.getDeclaredField("spi");
        spiField.setAccessible(true);
        Field p11KeyField = p11CipherClass.getDeclaredField("p11Key");
        p11KeyField.setAccessible(true);

        int i = 0;
        while (i++ < 100000) {

            // DEBUG (uncomment to enable)
            //System.out.println("Iteration: " + i);

            Cipher c = Cipher.getInstance("AES/CBC/NoPadding", pkcs11provider);

            // Generate leak with unique keys

            // We need to generate a unique key so the key is not cached and we can
            // create multiple native key objects. Key is unique based on object id.

            try {
                c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(rawKey, "AES"));
            } catch (Exception e) {
                // Unfortunately, this code is not always executed. Under heavy
                // native heap pressure, a different JVM thread may fail before
                // trying to allocate memory in the native heap. In example,
                // InterpreterRuntime::exception_handler_for_exception fails when
                // calling JvmtiExport::post_exception_throw.
                System.out.println("=== Exception when initializing Cipher ===");
                throw e;
            }

            // DEBUG (uncomment to enable)
            // Do not generate leaks using a cached-key 
            //c.init(Cipher.ENCRYPT_MODE, commonkey);

            c.update(plaintext);

            // Hold references to the P11Key objects, so the native key is not destroyed.
            // This is artificial to speed-up the test reproduction. However, memory exhaustion 
            // will not be caused by holding these Java objects in memory (and will not occur in 
            // the Java heap). In real scenarios, P11Key.SessionKeyRef objects will be in a queue
            // waiting for "finalizer" execution (and P11Key objects already deleted).
            Object cSpi = spiField.get(c);
            Object p11Key = p11KeyField.get(cSpi);
            keysHolderList.add(p11Key);

            c.doFinal();
        }

        System.out.println("TEST PASS - OK");
    }

}
