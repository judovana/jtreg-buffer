import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/*
 * @test
 * @bug 6874643
 * @summary invalid jpeg read error
 * @run  main/othervm Test 
 */

public class Test {
    private final static String DIR = System.getProperty("test.src", ".");

    public static void main(String[] args) throws IOException {
        String fname = "test.jpg";

        prepareTestImage(fname);

        File f = new File(fname);

        int stepX = 0x40000001;

        ImageInputStream iis =
            ImageIO.createImageInputStream(new File(fname));
        ImageReader reader = ImageIO.getImageReaders(iis).next();
        reader.setInput(iis);
        ImageReadParam param = reader.getDefaultReadParam();

        for (int i = 0; i < 2; i++) {
            param.setSourceSubsampling(stepX + i, 1, 0, 0);
            BufferedImage img = reader.read(0, param);
            System.out.println("Result image is " + img);
            // just to force heap validation
            System.gc();
        }
    }

    private static void prepareTestImage(String fname) throws IOException {
        final int w = 1600;
        final int h = 1000;
        BufferedImage dst = new BufferedImage(w, h,
                                              BufferedImage.TYPE_INT_RGB);
//original one - BufferedImage.TYPE_4BYTE_ABGR); - casued failure
//now this test is maybe tesint nothing, but transaprency is not acepted in jpg writer for  loong time

        ImageIO.write(dst, "jpeg", new File(fname));
    }
}
