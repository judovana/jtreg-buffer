import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/* @test
   @bug 6656625
   @summary  image stream manipulation 
   @run shell Test.sh
*/
public class Test {

    static {
        ImageReaderSpi.STANDARD_INPUT_TYPE[0] = File.class;
    }

    public static void main(String[] args) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(new File("test.png"));

        System.out.println("iis: " + iis);

        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

        if (readers == null || !readers.hasNext()) {
            throw new RuntimeException("Test failed: no readres available");
        }

        try {
            while (readers.hasNext()) {
                ImageReader r = readers.next();
                System.out.println("Reader: " + r);
                r.setInput(iis);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Test failed.", e);
        }
        System.out.println("Test passed.");
    }
}
