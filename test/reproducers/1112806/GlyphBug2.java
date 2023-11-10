import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Random;

/*
 * @test
 * @bug 1112806
 * @requires os.arch != "aarch64"
 * @summary  heck that LineBreakMeasurer.nextLayout does not throw ArrayIndexOutOfBoundsException
 * @run main/timeout=60000     GlyphBug2
 */

/**
This is a test for BZ#112806. With some fonts (like STIX General), there have
been problems with so-called canonical processing, the process of blending
multiple characters together (in particular, this applies to combining marks
like various accents). Fonts can have a flag set to opt out of the canonical
processing, which was not respected by OpenJDK 6, leading to an exception
(ArrayIndexOutOfBoundsException) being thrown if such an attempt was made. The
test checks if this does not happen any longer.
**/

public class GlyphBug2 {
  public static void main(String[] args) {
  try{
    int runs = 1000;
    if (args.length != 0) {
      runs = Integer.parseInt(args[0]);
    }

    RandomStringFactory2 stringFactory = new RandomStringFactory2();

    for (int r = 0; r < runs; r++) {
      String str = stringFactory.getNextUnicodeString(255);
      System.err.println("String: " + str);
      AttributedString formattedString = new AttributedString(str);
      formattedString.addAttribute(TextAttribute.FONT, new Font("STIXGeneral", Font.BOLD, 12));
      formattedString.addAttribute(TextAttribute.BACKGROUND, Color.RED);
      formattedString.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);
      
      AttributedCharacterIterator text = formattedString.getIterator();
      FontRenderContext LINE_BREAK_FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);
      LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(text, LINE_BREAK_FONT_RENDER_CONTEXT);
    
      while (lineMeasurer.getPosition() < text.getEndIndex()) {
	lineMeasurer.nextLayout(100.0f);
      }
    }

    System.out.println("Done.");
  } catch(java.awt.AWTError ex) {
    System.out.println("headless system? skipped");
  }
  }

  static class RandomStringFactory2 {
    /**
     * array of all valid non-control Unicode characters
     */
    private static char[] CHARACTERS;
    
    static {
      StringBuilder b = new StringBuilder(Character.MAX_VALUE);
      for (int i=Character.MIN_CODE_POINT+1; i <= Character.MAX_VALUE; i++) {
	if (Character.isLetterOrDigit(i) || Character.isWhitespace(i) ) b.append((char) i);
      }
      CHARACTERS = new char[b.length()];
      b.getChars(0, b.length(), CHARACTERS, 0);
      System.out.println("Using a pool of " + CHARACTERS.length + " Unicode characters");
    }
    
    private Random _rand;
    
    private int pos = 0;

    /**
     * default constructor - creates a default {@link Random} instance
     */
    public RandomStringFactory2() {
      _rand = new Random();
    }
    
    /**
     * Creates a new {@link Random} with the given seed, to produce a well-defined, evenly distributed series.
     * @param seed
     */
    public RandomStringFactory2(long seed) {
      _rand = new Random(seed);
    }
    
    /**
     * @param length
     * @return random string from all Unicode letter, number, and whitespace characters with spaces every 10th character
     */
    public String getRandomUnicodeString(int length) {
      char[] chars = new char[length];
      int x = 0;
      while (x < chars.length) {
	if (x % 10 == 0) chars[x++] = ' ';
	else chars[x++] = CHARACTERS[_rand.nextInt(CHARACTERS.length)];
      }
      return new String(chars, 0, chars.length);
    }

    public String getNextUnicodeString(int length) {
      char[] chars = new char[length];
      for (int x = 0 ; x < chars.length; x++) {
	chars[x] = CHARACTERS[pos++];
	if (pos == CHARACTERS.length) pos = 0;
      }
      return new String(chars, 0, chars.length);
    }
  }
}
  
