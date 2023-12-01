import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

/*
 * @test
 * @bug 6914823
 * @requires os.arch != "aarch64"
 * @summary image consumer producer issue
 * @run  main/othervm Test
 */

public class Test {

    private static Toolkit tk = null;

    public static void main(String[] args) {
       try {
          tk = Toolkit.getDefaultToolkit();
       } catch(java.awt.AWTError ex) {
           System.out.println("headless system? skipped");
           return;
       }

        doTest(0x1000000);

        doTest(Integer.MIN_VALUE);
    }

    private static void doTest(int scansize) {
        TestImageProducer p = new TestImageProducer(1, 3, scansize);
        Image img = tk.createImage(p);
        loadImage(img);

        System.out.println("Test passed.");
    }

    private static void loadImage(Image img) {
        Exception err = null;
        try {
            // test producer works synchroniously
            tk.prepareImage(img, -1, -1, null);
        } catch (ArrayIndexOutOfBoundsException e) {
            err = e;
        }
        if (err == null) {
            throw new RuntimeException("Test failed.");
        }
        System.gc();
    }

    private static class TestImageProducer implements ImageProducer {
        private int w;
        private int h;
        private int scansize;

        public TestImageProducer(int w, int h, int s) {
            this.w = w;
            this.h = h;
            this.scansize = s;
        }

        public void addConsumer(ImageConsumer ic) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isConsumer(ImageConsumer ic) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeConsumer(ImageConsumer ic) {
        }

        public void startProduction(ImageConsumer ic) {
            int size = 2;
            byte r[] = new byte[]{(byte) 0x00, (byte) 0xff};
            byte g[] = new byte[]{(byte) 0x00, (byte) 0x00};
            byte b[] = new byte[]{(byte) 0xff, (byte) 0x00};

            IndexColorModel icm1 = new IndexColorModel(8, size, r, g, b);

            ic.setColorModel(icm1);
            ic.setDimensions(w, h);

            IndexColorModel icm = new IndexColorModel(1, size, r, g, b);

            byte pix[] = new byte[w * (h - 1)];
            Arrays.fill(pix, (byte) 0x00);
            ic.setPixels(0, 0, w, h, icm, pix, 0, scansize);

            ic.imageComplete(ic.STATICIMAGEDONE);
        }

        public void requestTopDownLeftRightResend(ImageConsumer ic) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
