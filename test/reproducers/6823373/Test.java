/* @test
   @bug 6823373
   @requires var.msys2.enabled == "false"
   @summary  some splash issue 
   @run shell Test.sh
*/
//   run main/othervm -splash:splash.jpg -showversion Test
//   ^ do not load splash

public class Test {

    public static void main(String[] args) {
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
