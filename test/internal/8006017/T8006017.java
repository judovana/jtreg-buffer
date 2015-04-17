/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8006017
 * @requires jdk.version.major = 7
 * @summary Improve lookup resolutions
 * @author Andrew Gross
 */
import java.lang.invoke.*;
import java.net.*;

public class T8006017 {

    public static void main(String[] args) throws Throwable {
        String s = System.getProperty("java.version");
        if (s.startsWith("1.6")){
            return;
        }
        try {
            Class<?> c1 = null;
            //jdk8+
            try {
                c1 = Class.forName("jdk.nashorn.api.scripting.AbstractJSObject");
            } catch (java.lang.ClassNotFoundException ex1) {
                ex1.printStackTrace();
                //jdk7-
                try {
                    //openjdk
                    c1 = Class.forName("sun.org.mozilla.javascript.Context");
                } catch (java.lang.ClassNotFoundException ex2) {
                    ex2.printStackTrace();
                    //so you are on oracle?
                    c1 = Class.forName("sun.org.mozilla.javascript.internal.Context");
                }
            }

            // set security manager
            System.setSecurityManager(new SecurityManager());

            // Now use Lookup recursively
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            MethodType mt1 = MethodType.methodType(MethodHandle.class, Class.class,
                    new Class[]{MethodType.class});
            MethodHandle mh1 = lookup.findVirtual(MethodHandles.Lookup.class,
                    "findConstructor", mt1);
            MethodType mt2;

            mt2 = MethodType.methodType(Void.TYPE);

            // NB: this should fail with java.security.AccessControlException
            MethodHandle mh2 = (MethodHandle) mh1.invokeWithArguments(new Object[]{lookup, c1, mt2});
        } catch (java.security.AccessControlException e) {
            return;
        }
        throw new RuntimeException("AccessControlException not thrown");
    }
}

