
// based on http://slightlyrandombrokenthoughts.blogspot.com/2011/02/trusted-method-chaining-for-network.html
//
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Vector;

/*
 * @test
 * @bug 7013969
 * @requires var.msys2.enabled == "false"
 * @summary  network interface address scan
 * @run shell 7013969.sh
*/ 
public class Test7013969{
    public static void main(String[] a){
         Vector interfaceList = new Vector();
         try {
             Enumeration en = NetworkInterface.getNetworkInterfaces();
             while (en.hasMoreElements()) {
                 System.out.println(en.nextElement());
             }
         } catch (Exception e ) {
             e.printStackTrace();
         }
 }
}
