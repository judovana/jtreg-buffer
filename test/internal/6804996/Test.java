/* @test
   @bug 6804996
   @summary  some splash issue 
   @run shell Test.sh
*/
//   run main/othervm -splash:splash.png -showversion Test
//   ^ do not load splash
import java.io.*;
public class Test {

    public static void main(String[] args) {
		File dir = new File(".");
		File[] filesList = dir.listFiles();
		for (File file : filesList) {
   		    System.out.println(file.getName());
		}
        long timeout = Long.getLong("test.timeout", 5000);

        long finish = System.currentTimeMillis() + timeout;

        System.out.print("Test .");

        while (System.currentTimeMillis() < finish) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.print(".");
        }
        System.out.println(" done.");
    }
}
