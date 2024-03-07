package com.sun.imageio.plugins.jpeg;

import java.security.AccessControlException;

public
/* @test
   @bug 6657133
   @requires var.msys2.enabled == "false"
   @summary  inaccessibile com.sun.imageio.plugins.jpeg.JPEG.names
   @run shell Test.sh
*/
 class Test {

    public static void main(String[] args) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            throw new RuntimeException("Test failed: no security manager");
        }
        System.out.println("SecurityManager: " + sm);

        Throwable err = null;
        try {
            String[] jpeg_names = com.sun.imageio.plugins.jpeg.JPEG.names;

            for (String n : jpeg_names) {
                System.out.println(n);
            }
        } catch (AccessControlException e) {
            err = e;
        }

        if (err == null) {
            throw new RuntimeException("Test failed.");
        }
        System.out.println("Test passed.");
    }
}
