import java.io.File;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.xml.sax.helpers.DefaultHandler;

/**
 * @test %I% %E%
 * @bug 6845701
 * @summary Unit tests for: CR 6845701: Xerces Java XML library infinite loop with malformed XML input
 * @run main ParseXMLFile
 * 
 *
 * @author <a href="mailto:Joe.Wang@Sun.com">Joe Wang</a>
 * 
 */

public class ParseXMLFile {
		
    private final static String DIR = System.getProperty("test.src", ".");

	public static void main(String[] args) {
			args = new String[]{DIR+"/invalidchar.xml"};
            if (args.length == 0) {
               System.out.println("Commandline format: ParseXMLFile file");
               System.exit(1);
	    } else {
               File file = new File(args[0]);
               if (!file.exists()) {
                   System.out.println("File " + args[0] + " does not exist.");
                   System.exit(2);
               }
	       parse(file);
            }

	}
	
    public static void parse(File file) {
                
        try {
            // create and initialize the parser
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            
            SAXParser parser = spf.newSAXParser();
            
            parser.parse(file, new DefaultHandler());
        } catch (Exception e) {
            if (e.getMessage().indexOf("invalid XML character") > 0) {
                //expected failure
                System.out.println(e.getMessage());
                System.out.println("Success: File " + file.getPath() + " was parsed, error reported.");
            } else {
                throw new RuntimeException("Failure: File " + file.getPath() + " causes a parsing error. Should report error: " + e.getMessage());                
            }
        }
        
    }	
}
