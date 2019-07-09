// see: https://stackoverflow.com/questions/45569367/upgrade-rhel-from-7-3-to-7-4-arrayindexoutofboundsexception-in-sun-font-composihttps://stackoverflow.com/questions/45569367/upgrade-rhel-from-7-3-to-7-4-arrayindexoutofboundsexception-in-sun-font-composi
import java.awt.*;
import java.awt.font.*;

/*
 * @test
 * @bug 1484079
 * @requires os.arch != "aarch64"
 * @summary   - when only stixfonts are installed, there is fail
 * @run main/timeout=600/othervm     TestJava1484079
 */

public class TestJava1484079 {
  public static void main(String[] args) {

    String []fontFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for (String font : fontFamilies) {
      System.err.println(font);
    }

    Font font = new Font("SansSerif", Font.PLAIN, 12);
    //Font font = new Font("STIX", Font.PLAIN, 12);

    FontRenderContext frc = new FontRenderContext(null, false, false);
    TextLayout layout = new TextLayout("\ude00", font, frc);
    layout.getCaretShapes(0);
    System.out.println(layout);
  }
}


