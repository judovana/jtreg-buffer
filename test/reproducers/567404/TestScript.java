import javax.script.*;
import java.io.*;
import java.util.*;
/*
 * 567404 is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
 * dont forget to include 0567404 as whole bug id
 * @test
 * @bug 0567404
 * @summary  verify rhino minimal version and functionality
 * @run shell runtest.sh
 */

public class TestScript{
    //log without garbage from jvm
    private static final File f=new File("realLog");  //Creation of File Descriptor for output file

    public static void realLog(String s, BufferedWriter br) throws Exception{
      System.out.println(s);
      br.write(s);
      br.newLine();
    }

    public static void main(String[] args) throws Exception {
    try (BufferedWriter br =
                   new BufferedWriter(new FileWriter(f))) {
        realLog("Testing for RHQ CLI required components:", br); 
    ScriptEngineManager sem = new ScriptEngineManager();
    realLog("ScriptingEnginManager detected : "+(sem!=null)+" :"+sem+":", br);
        ScriptEngine se = sem.getEngineByName("JavaScript");
        realLog("ScriptingEngin detected: "+(se!=null)+" :"+se+":", br);
    List<ScriptEngineFactory> list = sem.getEngineFactories();
    realLog("Scripting Factories detected: "+(list.size()>0)+list+":", br);
        if((sem!=null)&&(se!=null)){
        realLog("\n\n Scripting components FOUND successfully. ", br);
    }else{
        realLog("\n\n Scripting components NOT found. ", br);
        }
    }
    }
}
