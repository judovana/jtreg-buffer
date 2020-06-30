/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/* @test
 * @modules java.desktop/sun.awt
   @bug 1760838
   @summary No ciphersuites available for SSLSocket in FIPS mode
   @author Jakub Jelen
   @run main/othervm Test1760838
*/

import java.net.*;
import java.io.*;
import javax.net.ssl.*;

public class Test1760838 {

    public static void main(String[] args) throws Exception {
        try {
            SSLSocketFactory factory =
                (SSLSocketFactory)SSLSocketFactory.getDefault();

             {//check suites, must be at leaset one

                String[] cipherSuites = factory.getDefaultCipherSuites();
                for (int i=0; i < cipherSuites.length; i++){
                    System.out.println(cipherSuites[i]);                
                }
				if (cipherSuites.length == 0) {
                  throw new RuntimeException("No ciphersuites!");
                }
            }

           //now try https handhake
           //String host = "duckduckgo.com";
           String host = "redhat.com";
           int port = 443;
if (args.length > 0) { host = args[0];}
if (args.length > 1) { port = Integer.parseInt(args[1]);}
            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
            
            if (args.length > 2) {
                String pickedCipher[] = { args[2] };
                socket.setEnabledCipherSuites(pickedCipher);
            }
            
            socket.addHandshakeCompletedListener(
                new HandshakeCompletedListener() {
                    public void handshakeCompleted(
                            HandshakeCompletedEvent event) {
                        System.out.println("CH:" + event.getCipherSuite());
                    }
                }
                                                 );
            socket.startHandshake();

            PrintWriter out = new PrintWriter(
                                  new BufferedWriter(
                                  new OutputStreamWriter(
                                  socket.getOutputStream())));

            out.println("GET / HTTP/1.0");
            out.println();
            out.flush();
            
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {            
            throw e;
        }
    }
}

