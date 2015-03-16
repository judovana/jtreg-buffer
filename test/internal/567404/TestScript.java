import javax.script.*;
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
    public static void main(String[] args) {
        System.out.println("Testing for RHQ CLI required components:"); 
    ScriptEngineManager sem = new ScriptEngineManager();
    System.out.println("ScriptingEnginManager detected : "+(sem!=null)+" :"+sem+":");
        ScriptEngine se = sem.getEngineByName("JavaScript");
        System.out.println("ScriptingEngin detected: "+(se!=null)+" :"+se+":");
    List<ScriptEngineFactory> list = sem.getEngineFactories();
    System.out.println("Scripting Factories detected: "+(list.size()>0)+list+":");
        if((sem!=null)&&(se!=null)){
        System.out.println("\n\n Scripting components FOUND successfully. ");
    }else{
        System.out.println("\n\n Scripting components NOT found. ");
        }
    }
}
