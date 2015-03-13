import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.Arrays;

/*
 * @test
 * @bug 6899653
 * @summary collor profiles corruption
 * @run  main/othervm Test
 */

public class Test {
    public static void main(String[] args) {
        final byte[] tagHeader = new byte[] {
            0x6d, 0x41, 0x42, 0x20, 0x42, 0x42, 0x42, 0x42, 0x01, 0x01,
            0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x00, 0x00, 0x00, 0x00,
            0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43,
            0x43, 0x43, 0x63, 0x75, 0x72, 0x76, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x10, 0x2c
        };
        final int tagDataLength = 8280;

        final byte[] tagData = new byte [tagDataLength + tagHeader.length];

        System.arraycopy(tagHeader, 0, tagData, 0, tagHeader.length);

        Arrays.fill(tagData, tagHeader.length, tagData.length - 1, (byte)0xEE);

        ICC_Profile[] profiles = new ICC_Profile[1];

        profiles[0] = ICC_Profile.getInstance(ColorSpace.CS_GRAY);
Exception ex = null;
try{
        profiles[0].setData(ICC_Profile.icSigBToA0Tag, tagData);
} catch(java.lang.IllegalArgumentException e){
        System.out.println("newer jdk do the tested chec in this geter, so this exception is pass");
ex=e;

}
if (ex!=null){
        ColorConvertOp cco = new ColorConvertOp(profiles, new RenderingHints(null, null));

        cco.filter(new BufferedImage(128,128, 8), new BufferedImage(128, 128, 8));
        }
        System.out.println("Test passed");

    }
}
