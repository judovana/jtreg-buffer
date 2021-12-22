import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class MainJavaScriptEngineRunner {
    // log without garbage from jvm
    private static final File f = new File("realLog");  // Creation of File Descriptor for output file

    public static void realLog(String s, BufferedWriter br) throws Exception {
        System.out.println(s);
        br.write(s);
        br.newLine();
    }

    public static void main(String engineName) throws Exception {
        try (BufferedWriter bw =
                     new BufferedWriter(new FileWriter(f))) {
            realLog("Testing for RHQ CLI required components:", bw);

            ScriptEngineManager engineManager = new ScriptEngineManager();

            realLog("" + "\n *** Enumerate factories ***", bw);
            for (ScriptEngineFactory engineFactory : engineManager.getEngineFactories()) {
                realLog(" " + engineFactory, bw);
            }

            realLog("\nScriptingEnginManager detected : " + (engineManager != null) + " :" + engineManager + ":", bw);

            ScriptEngine engine = engineManager.getEngineByName(engineName);
            realLog("ScriptingEngine detected: " + (engine != null) + " :" + engine + ":", bw);

            List<ScriptEngineFactory> list = engineManager.getEngineFactories();
            realLog("Scripting Factories detected: " + (list.size() > 0) + list + ":", bw);

            for (ScriptEngineFactory factory:list){
                realLog("" + factory, bw);
            }

            if ((engineManager != null) && (engine != null)) {
                realLog("\n\n Scripting components FOUND successfully. ", bw);
            } else {
                realLog("\n\n Scripting components NOT found. ", bw);
                throw new RuntimeException("Test Scripting components NOT found.");
            }
        }
    }
}
