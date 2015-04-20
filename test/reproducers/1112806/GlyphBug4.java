import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Random;

/*
 * @test
 * @bug 1112806
 * @summary  heck that LineBreakMeasurer.nextLayout does not throw ArrayIndexOutOfBoundsException
 */

public class GlyphBug4 {

  private static final String[] FONT_NAMES = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

  public static void main(String[] args) throws InterruptedException {
    System.out.println("Iterating over " + FONT_NAMES.length + " font families...");
    for (int r = 0; r < FONT_NAMES.length; r++) {
      RandomStringFactory4 stringFactory = new RandomStringFactory4();
      int runCount = 1000; // (stringFactory.getPoolSize() / 255) + 1;
      String fontName = FONT_NAMES[r];
      System.out.println("Testing " + fontName + "...");
      for (int runs = 0; runs < runCount; ++runs) {
	String str = stringFactory.getNextUnicodeString(255);
	AttributedString formattedString = new AttributedString(str);
	try
	  {
	    formattedString.addAttribute(TextAttribute.FONT, new Font(fontName, Font.BOLD, 12));
	    formattedString.addAttribute(TextAttribute.BACKGROUND, Color.RED);
	    formattedString.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);
	  
	    AttributedCharacterIterator text = formattedString.getIterator();
	    FontRenderContext LINE_BREAK_FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);
	    LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(text, LINE_BREAK_FONT_RENDER_CONTEXT);
	    
	    while (lineMeasurer.getPosition() < text.getEndIndex()) {
	      lineMeasurer.nextLayout(100.0f);
	    }
	  }
	catch (ArrayIndexOutOfBoundsException e) 
	  {
	    System.out.println(fontName + " throws exception.");
	  }
      }
      System.out.println("Done.");
    }
  }

  static class RandomStringFactory4 {
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
    public RandomStringFactory4() {
      _rand = new Random();
    }
    
    /**
     * Creates a new {@link Random} with the given seed, to produce a well-defined, evenly distributed series.
     * @param seed
     */
    public RandomStringFactory4(long seed) {
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
      int startPos = pos;
      for (int x = 0 ; x < chars.length; x++) {
	chars[x] = CHARACTERS[pos++];
	if (pos == CHARACTERS.length) pos = 0;
      }
      int finishPos = pos - 1;
      System.err.println("Included characters " + startPos + " to " + finishPos);
      return new String(chars, 0, chars.length);
    }

    public int getPoolSize() { return CHARACTERS.length; }

  }
}
  
