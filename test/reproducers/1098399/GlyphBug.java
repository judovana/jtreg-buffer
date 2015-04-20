import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Random;

/*
 * @test
 * @bug 1098399
 * @summary   - Unsynchronized HashMap access causes endless loop
 * @run main/timeout=600/othervm     GlyphBug
 */

/** timeout is set to 10 minutes.
 */



/**
 * attempt at a reproducible test case for bug
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6367148
 */
public class GlyphBug implements Runnable {

	private static final FontRenderContext LINE_BREAK_FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);
	private static final int RUNS = 100000;
	private static final int THREADS = 5;
	private static boolean RUNNING = true;

	//HashMap maptest ;
	
	/**
	 * @return true if the Main method is still starting or waiting on threads.
	 */
	public static boolean isRunning() {
		return RUNNING;
	}

	private static String[] FONT_NAMES = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

	/**
	 * @param args optional - first is # of threads, second # of iterations per thread.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Thread thread = null;
		int threads = THREADS;
		int runs = RUNS;
		
		if (args != null) {
			if (args.length >= 1) threads = Integer.parseInt(args[0]);
			if (args.length >= 2) runs = Integer.parseInt(args[1]);
		}

		System.out.println("Starting " + threads + " threads with " + runs + " measurement iterations...");
		System.out.println("Randomly selecting from " + FONT_NAMES.length + " font families...");
		for (int t = 0; t < threads; t++) {
			thread = new Thread(new GlyphBug(runs), "GlyphLayout Thread " + (t+1));
			thread.start();
		}

		if (threads > 0) thread.join();
		synchronized (GlyphBug.class) {
			RUNNING = false;
		}

		System.out.println("Done.");
	}

	private int _runs;
	private RandomStringFactory _stringFactory = new RandomStringFactory();
	
	/**
	 * @param runs 
	 */
	public GlyphBug(int runs) {
		_runs = runs;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		for (int r = 0; r < _runs; r++) {
			
			System.out.println("Thread【" + Thread.currentThread().getName() + "】　run _runs  ★【" + String.valueOf(r) + "】" );
			
			AttributedString formattedString = new AttributedString(_stringFactory.getRandomUnicodeString(255));
			formattedString.addAttribute(TextAttribute.FONT, new Font(FONT_NAMES[r % FONT_NAMES.length], Font.BOLD, 12));
			formattedString.addAttribute(TextAttribute.BACKGROUND, Color.RED);
			formattedString.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);

			AttributedCharacterIterator text = formattedString.getIterator();
			LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(text, LINE_BREAK_FONT_RENDER_CONTEXT);
			
			while (lineMeasurer.getPosition() < text.getEndIndex()) {
				
				lineMeasurer.nextLayout(100.0f);
			}
						
		}
		
		System.out.println(Thread.currentThread().getName() + " done.");
	}

}

class RandomStringFactory {
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
	
	/**
	 * default constructor - creates a default {@link Random} instance
	 */
	public RandomStringFactory() {
		_rand = new Random();
	}
	
	/**
	 * Creates a new {@link Random} with the given seed, to produce a well-defined, evenly distributed series.
	 * @param seed
	 */
	public RandomStringFactory(long seed) {
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
}
