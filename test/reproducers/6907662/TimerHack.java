/*
 * @test
 * @modules java.desktop/sun.applet
 * @bug 6378709
 * @summary Verifies that SecurityManager can't be fooled with javax.swing.Timer
 * @author Alexander Potochkin
 * @build HackedFileChooser
 * @run main TimerHack
 */

import javax.swing.*;
import java.awt.event.*;

import sun.applet.AppletSecurity;

public class TimerHack {

    private static volatile boolean exceptionCaught = false;
    private static volatile Throwable exceptionOccured;

    private static void createGui() {
        Timer timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // this listener will be notified after the action
                throw new RuntimeException("test failed");
            }
        });
        //the action when notified must throw the SecurityException
        //which will be caught in handle() method
        timer.addActionListener(HackedFileChooser.NEW_FOLDER_ACTION);
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) throws Throwable {
        // Use a new thread group, because all the exceptions in the old
        // group are intercepted by jtreg
        ThreadGroup tg = new ThreadGroup("Test thread group") {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (e instanceof SecurityException) {
                    System.out.println("SecurityException is caught as expected");
                    System.out.flush();
                    exceptionCaught = true;
                } else {
                    exceptionOccured = e;
                }
            }
        };
        Thread t = new Thread(tg, new Runnable() {
            @Override
            public void run() {
                try {
                    new HackedFileChooser();
                    System.setSecurityManager(new AppletSecurity());

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            TimerHack.createGui();
                        }
                    });

                    Thread.sleep(1000);

                    // Flush EDT
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                        }
                    });
                } catch (Exception t) {
                    System.err.println("Unknown exception: t");
                    exceptionOccured = t;
                }
            }
        });

        t.start();
        t.join();

        if (!exceptionCaught) {
            throw new RuntimeException("SecurityException is not thrown");
        }
        if (exceptionOccured != null) {
            throw exceptionOccured;
        }
//    System.exit(0);
    }
}
