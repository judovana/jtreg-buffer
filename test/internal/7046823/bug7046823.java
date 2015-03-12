/*
  @test
  @bug       7046823 
  @summary   Problem with scripting/javascript
  @build     bug7046823
  @run shell bug7046823.sh
*/

import java.applet.*;
import javax.script.*;
import javax.swing.*;

/*
<applet code = "bug7046823.class" width=100 height=100>
</applet>
*/
public class bug7046823 extends Applet {
    @Override
    public void init() {
        try {
            ScriptEngineManager m = new ScriptEngineManager();
            ScriptEngine se = m.getEngineByName("js");
            se.put("app", this);
            Object value = se.eval(
                "toString = function() { " +
                    "try { " +
                    "   java.lang.System.setSecurityManager(null); " +
                    "} catch(e) { println(e); java.lang.System.exit(0); }; " +
                    "return app.doBad() " +
                "}; " +
                "x = new URIError(); " +
                "x.message = this; " +
                "x");
            add(new JList(new Object[] { value }));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void doBad() {
        System.err.println("in doBad!, should have thrown exception!");
        System.exit(1);
    }
}
