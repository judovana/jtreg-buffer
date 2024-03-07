
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

/* @test
   @bug 6804998
   @requires var.msys2.enabled == "false"
   @summary  invalid gif read issue
   @run shell Test.sh
*/
public class Test {

    private static final boolean verbose = Boolean.getBoolean("verbose");

    public static void main(String[] args) {
       Toolkit tk = null;
       try {
          tk = Toolkit.getDefaultToolkit();
       } catch(java.awt.AWTError ex) {
           System.out.println("headless system? skipped");
           return;
       }

        String fname = "test.gif";

        if (args.length > 0) {
            fname = args[0];
        }

        Image img = tk.createImage(fname);

        Observer o = new Observer();
        tk.prepareImage(img, -1, -1, o);
        System.out.println("Test passed.");
    }

    private static class Observer implements ImageObserver {
        public boolean imageUpdate(Image img, int infoflags, int x, int y,
                                    int width, int height)
        {
            if (verbose) {
                System.out.printf("Got update: %s, x=%d, y=%d, w=%d, h=%d\n",
                                  dump(infoflags), x, y, width, height);
            }
            boolean isReady = ((infoflags & FRAMEBITS) != 0) ||
                    ((infoflags & ALLBITS) != 0);

            return !isReady;
        }

        private static String dump(int f) {
            String res = "";
            if ((f & ABORT) != 0) {
                res += " ABORT";
            }
            if ((f & ALLBITS) != 0) {
                res += " ALLBITS";
            }
            if ((f & ERROR) != 0) {
                res += " ERROR";
            }
            if ((f & FRAMEBITS) != 0) {
                res += " FRAMEBITS";
            }
            if ((f & HEIGHT) != 0) {
                res += " HEIGHT";
            }
            if ((f & WIDTH) != 0) {
                res += " WIDTH";
            }
            if ((f & PROPERTIES) != 0) {
                res += " PROPERTIES";
            }
            if ((f & SOMEBITS) != 0) {
                res += " SOMEBITS";
            }
            
            return res;
        }

    }

}
