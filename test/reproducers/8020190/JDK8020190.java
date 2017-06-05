import java.awt.Font;
import java.io.File;
import java.awt.geom.AffineTransform;
import java.awt.font.*;
import java.awt.Shape;
import java.awt.FontFormatException;
import java.io.IOException;

/* @test
   @bug 8020190
   @requires jdk.version.major >= 7
   @summary  crash in font transformations. See https://bugzilla.redhat.com/show_bug.cgi?id=1176718 and https://bugzilla.redhat.com/show_bug.cgi?id=1212268. Should be fixed in CPU 2015-07-14
   */

//Note, this test is known to fail aslo for jdk6, but it is not going to be fixed here...

public class JDK8020190 {

    public static void main(String[] args) throws FontFormatException, IOException{

	Font font = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("test.src","."), "LeagueGothic-Regular.otf" ));
	AffineTransform tx = new AffineTransform();
	TextLayout layout = new TextLayout(font.getName(), font, 
		new FontRenderContext(tx, true, false));
		Shape outline = layout.getOutline(tx);
		System.out.println(outline.getBounds());
    }

}
