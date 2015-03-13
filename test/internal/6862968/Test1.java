import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.plugins.jpeg.JPEGQTable;
import javax.imageio.stream.ImageOutputStream;
/*
 * @test
 * @bug 6862968
 * @summary issue in huffman jpg 1
 *
 * @run main/othervm Test1
 */
public class Test1 {
    public static void main(String[] args) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageOutputStream output = ImageIO.createImageOutputStream(baos);

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        writer.setOutput(output);

        JPEGQTable[] qTables = createQTables();

        JPEGHuffmanTable[] dcTables = createDCTables();

        JPEGHuffmanTable[] acTables = createACTables();

        WriteParam param = new WriteParam(qTables, dcTables, acTables);


        // prepare test image
        BufferedImage img = new BufferedImage(100, 100,
                BufferedImage.TYPE_INT_RGB);

        try {
            writer.write(null, new IIOImage(img, null, null), param);
        } catch (Throwable e) {
            // ignore any exception: the only failure criteria is a crash.
        }
        System.out.println("Test passed.");
    }

    private static JPEGQTable[] createQTables() {

        int num = Integer.getInteger("qt.num", 0);
        System.out.println("Number qtables: " + num);
        if (num <= 0) {
            return new JPEGQTable[] {};
        }

        JPEGQTable[] tbls = new JPEGQTable[num];

        JPEGQTable t = JPEGQTable.K2Chrominance;

        Arrays.fill(tbls, t);

        return tbls;
    }

    private static JPEGHuffmanTable[] createDCTables() {
        return createHuffmanTables("dc.num");
    }

    private static JPEGHuffmanTable[] createACTables() {
        return createHuffmanTables("ac.num");
    }


    private static JPEGHuffmanTable[] createHuffmanTables(String num_property) {
        int num = Integer.getInteger(num_property, 0);
        System.out.println("Number huffman tables: " + num);
        if (num <= 0) {
            return new JPEGHuffmanTable[] {};
        }
        JPEGHuffmanTable[] tbls = new JPEGHuffmanTable[num];

        JPEGHuffmanTable t = JPEGHuffmanTable.StdDCLuminance;

        Arrays.fill(tbls, t);

        return tbls;
    }

    private static class WriteParam extends JPEGImageWriteParam {

        public int getProgressiveMode() {
            return 0;
        }

        public boolean areTablesSet() {
            return true;
        }

        public JPEGHuffmanTable[] getACHuffmanTables() {
            return achtables;
        }

        public JPEGHuffmanTable[] getDCHuffmanTables() {
            return dchtables;
        }

        public JPEGQTable[] getQTables() {
            return qtables;
        }

        public WriteParam(JPEGQTable qtables[],
                          JPEGHuffmanTable dchtables[],
                          JPEGHuffmanTable achtables[])
        {
            super(Locale.getDefault());
            this.qtables = qtables;
            this.dchtables = dchtables;
            this.achtables = achtables;
        }
        private JPEGQTable qtables[];
        private JPEGHuffmanTable dchtables[];
        private JPEGHuffmanTable achtables[];
    }
}
