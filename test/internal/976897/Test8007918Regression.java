import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/*
 * 976897 is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
 * dont forget to include 0976897 as whole bug id
 * @test
 * @bug 0976897
 * @summary  tests memory leak in jpeg writer
 * @run main/othervm   -Xms32m -Xmx64m  Test8007918Regression
 */


/*
 * Demonstrates an out of memory error using JPEGImageWriter.
 * 
 * This is a regression that seems to be caused by the fix for bug 8007918:
 * http://hg.openjdk.java.net/jdk7u/jdk7u-dev/jdk/rev/90c9f1577a0b
 * 
 * The result of the patch is that JPEGImageWriter will retain itself
 * via a global (non-weak) JNI reference on a call to setOutput(). 
 */
public class Test8007918Regression {

	private static int amountOfJpegImageWritersToCreate = 100000;
	private static int gcGenerationsToRun = 2;
	/* Run GC every 1000 creations*/
	private static int amountBeforeGC = 1000;

	public static ImageWriterSpi getJpegImageWriterSpi() {
		Iterator<ImageWriterSpi> spiIter = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, true);
		while (spiIter.hasNext()) {
			ImageWriterSpi spi = spiIter.next();
			/* Assumption: JPEG is in the implementation class name for the JPEG image writer SPI */
			String classname = spi.getClass().getSimpleName();
			if (classname.toLowerCase().contains("jpeg")) {
				return spi;
			}
		}
		throw new RuntimeException("No JPEG image writer SPI found.");
	}

	public static void printMemoryUsage() {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		System.out.print("TOTAL: " + total/1024 + "Kb");
		System.out.print("\tFREE: " + free/1024 + "Kb");
		System.out.println("\tUSED: " + (total - free)/1024 + "Kb");
	}

    public static void main(String[] args) throws IOException {
		ImageWriterSpi jpegSpi = getJpegImageWriterSpi();

		System.out.println("Before JPEGImageWriter creations: ");
		printMemoryUsage();

		for (int i = 1; i <= amountOfJpegImageWritersToCreate; i++) {
			ImageWriter jpegWriter = jpegSpi.createWriterInstance();
			/* setOutput calls native setDest which seems to create a circular reference via a 
			 * streamBuffer in jdk/src/share/native/sun/awt/image/jpeg/imageioJPEG.c. 
			 * The actual output stream is arbitrary. */
			jpegWriter.setOutput(new MemoryCacheImageOutputStream(new ByteArrayOutputStream()));
			if (i % amountBeforeGC == 0) {
				for (int runs = 0; runs < gcGenerationsToRun; runs++) {
					System.gc();
				}
				System.out.println("After " + i + " JPEGImageWriter creations:");
				printMemoryUsage();
			}
		}
	}
}
