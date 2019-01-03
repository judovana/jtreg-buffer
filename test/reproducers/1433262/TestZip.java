
import java.io.File;
//import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TestZip {

	public static void main(String[] args) throws Throwable {
		if(args.length != 1){
			System.err.println("<Path to file> argument is missing!");
			System.exit(1);
		}
		
		try (ZipFile zf = new ZipFile(new File(args[0]))) {
			Enumeration<? extends ZipEntry> list = zf.entries();
			for (Enumeration<? extends ZipEntry> e = list; list.hasMoreElements();) {
				System.out.println(" -> " + e.nextElement());
			}
			System.out.println("Succeeded  to inspect packed file");
		} catch (Throwable t){			
			System.err.println("Failed  to inspect packed file");
			//t.printStackTrace(System.err);
			throw t;
		}
	}

}
