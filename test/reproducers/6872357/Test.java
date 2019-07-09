import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

/*
 * @test
 * @bug 6872357
 * @requires os.arch != "aarch64"
 * @summary iamge producer/observer generic error
 * @run  main/othervm Test 
 */

public class Test {
    public static void main(String[] args) {
        BufferedImage img = new BufferedImage(100, 100,
                                              BufferedImage.TYPE_BYTE_INDEXED);

        Toolkit tk = Toolkit.getDefaultToolkit();

        ImageProducer p = new TestProducer(img);

        ImageObserver o = new TestObserver();

        Image i = tk.createImage(p);
        tk.prepareImage(i, -1, -1, o);
        System.out.println("Test passed.");
    }

    private static class TestObserver implements ImageObserver {
        public boolean imageUpdate(Image img, int infoflags,
                                   int x, int y, int width, int height)
        {
            System.out.printf("infoflags: %x\n", infoflags);
            if ((infoflags & ALLBITS) != 0) {
                return false;
            } else {
                return true;
            }
        }
    }
    
    private static class TestProducer implements ImageProducer {
        private BufferedImage img;
        
        public TestProducer(BufferedImage img) {
            this.img = img;
        }
                
        public void removeConsumer(ImageConsumer ic) {
            System.out.println("Done...");
        }

        public void startProduction(ImageConsumer ic) {
            int w = img.getWidth();
            int h = img.getHeight();
            ColorModel cm = img.getColorModel();

            IndexColorModel badColorModel = new IndexColorModel(16, 10000,
                new int[10000], 0, false, 0, DataBuffer.TYPE_USHORT);
            
            ic.setDimensions(w, h);
            ic.setColorModel(cm);

            Raster r = img.getRaster();
            if (r.getTransferType() == DataBuffer.TYPE_BYTE) {
                DataBufferByte db = (DataBufferByte)r.getDataBuffer();
                byte[] data = db.getData();
                for (int y = 0; y < h; y++) {
                    if (y > h/2) {
                        cm = badColorModel;
                    }
                    ic.setPixels(0, y, w, 1, cm, data, (y * w), w);
                }
                ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
                return;
            }
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addConsumer(ImageConsumer ic) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public boolean isConsumer(ImageConsumer ic) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void requestTopDownLeftRightResend(ImageConsumer ic) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

