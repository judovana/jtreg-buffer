
 /*
 * @test
 * @bug 1513989
 * @summary  verify that cacerts are set right and jdk can suceed in https connection
 * @run main/timeout=30/othervm AttemptHttps
 */


import java.net.*;
import java.io.*;

public class AttemptHttps {
    public static void main(String... args) throws IOException{
        URL u = new URL("https://www.redhat.com/index.html");
        URLConnection c = u.openConnection();
        c.connect();
    }
}
