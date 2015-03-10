/*
 * @test
 * @bug 6351654
 * @summary Make sure that TimeZone.get/setDefault methods uses
 *          AppContext if no permission is given for global setting.
 * @compile -XDignore.symbol.file Bug6351654.java
 * @run main/othervm Bug6351654
 */

import java.util.*;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

public class Bug6351654 {
    static final TimeZone NOWHERE = new SimpleTimeZone(Integer.MAX_VALUE, "Nowhere");
    static Exception ce;

    public static void main(String[] args)   {   
      r();
   }

    public static void r()   {
        final TimeZone initialZone = TimeZone.getDefault();

        // Reset the default TimeZone
        TimeZone.setDefault(null);
        System.clearProperty("user.timezone");

        final ThreadGroup tg = new ThreadGroup("TzTest");

        Thread thread1 = new Thread(tg, new Runnable() {
                public void run() {
try{

                    // Create a new AppContext for thread group "TzTest".
                    AppContext appContext = SunToolkit.createNewAppContext();

                    // Install a SecurityManager here.
                    SecurityManager sm = new SecurityManager();
                    System.setSecurityManager(sm);

                    TimeZone tz = TimeZone.getDefault();
                    if (!tz.equals(initialZone)) {
                        throw new RuntimeException("First getDefault call in Child: got "
                                                   + tz.getID() + ", expected "
                                                   + initialZone.getID());
                    }
                    TimeZone.setDefault(NOWHERE);
                    if (initialZone.equals(TimeZone.getDefault())
                        || (!NOWHERE.equals(TimeZone.getDefault()))) {
                        throw new RuntimeException("thread1: unable to change default zone.");
                    }
                    Thread thread2 = new Thread(tg, new Runnable() {
                            public void run() {
                                TimeZone tz = TimeZone.getDefault();
                                if (!NOWHERE.equals(tz)) {
                                    throw new RuntimeException("thread2: got wrong tz: " + tz.getID());
                                }
                            }
                        });
                    thread2.start();
                    try {
                        thread2.join();
                    } catch (InterruptedException e) {
                    }
     } catch (java.security.AccessControlException ex){
     // also correct end
ce=ex;
}
            }});
        thread1.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
        }
if ( ce == null ) {
        Thread thread3 = new Thread(tg, new Runnable() {
                public void run() {
                    TimeZone tz = TimeZone.getDefault();
                    if (!tz.equals(NOWHERE)) {
                        throw new RuntimeException("thread3: got wrong tz: " + tz.getID());
                    }
                }
            });
        thread3.start();
        try {
            thread3.join();
        } catch (InterruptedException e) {
        }

        tg.destroy();

        TimeZone tz = TimeZone.getDefault();
        if (!initialZone.equals(tz)) {
            throw new RuntimeException("Default time zone has been changed by the child thread.\n"
                  + "\tinitial=" + initialZone + "\n\tafter=" + tz);
        }
}
    }
}
