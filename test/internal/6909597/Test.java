import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/*
 * @test
 * @bug 6909597
 * @summary imageio jpeg(?) issue
 * @run  main/othervm Test
 */

public class Test {

    public static void main(String[] args) throws IOException {
        // prepare image
        byte[] data = prepareJpegData();

        // verify an optimized case: scanline data are copyed by memcpy
        readSubsampled(1, data);

        // verify general case: scanline deata are copyed pyxel by pixel
        readSubsampled(2, data);

        System.out.println("Test passed.");
    }

    private static void readSubsampled(int stepX, byte[] data)
        throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ImageInputStream iis = ImageIO.createImageInputStream(bais);

        ImageReader reader = ImageIO.getImageReaders(iis).next();
        reader.setInput(iis);

        ImageReadParam param = createTestParam(stepX);
        BufferedImage res = reader.read(0, param);

        System.gc();
    }

    private static ImageReadParam createTestParam(int stepX) {
        return new TestReadParam(stepX);
    }

    private static class TestReadParam extends ImageReadParam {
        private int count = 0;
        private int stepX;


        public TestReadParam(int stepX) {
            this.stepX = stepX;
            this.setSourceSubsampling(stepX, 1, 0, 0);
        }

        public int getSourceXSubsampling() {
            count++;
            return (count <= 2) ? 0x40000001 : stepX;
        }
    }

    private static byte[] prepareJpegData()  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BufferedImage img = new BufferedImage(65500, 1,
                                              BufferedImage.TYPE_INT_RGB);
//original one - BufferedImage.TYPE_4BYTE_ABGR); - casued failure
//now this test is maybe tesing nothing, but transaprency is not acepted in jpg writer for  loong time
 
        RuntimeException err = null;
        try {
            if (!ImageIO.write(img, "jpeg", baos)) {
                err = new RuntimeException("Test failed");
            }
            baos.flush();
            baos.close();
        } catch (IOException e) {
            err = new RuntimeException("Test failed", e);
        }

        if (err != null) {
            throw err;
        }

        return baos.toByteArray();
    }
}
