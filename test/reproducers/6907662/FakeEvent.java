/*
 * @test
 * @modules java.desktop/sun.applet
 * @bug 6378709
 * @summary Verifies that SecurityManager can't be fooled with EventQueue.postEvent()
 * @author Alexander Potochkin
 * @build HackedFileChooser
 * @run main FakeEvent
 */

import sun.applet.AppletSecurity;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

public class FakeEvent {

    private static JButton button;

    private static volatile boolean exceptionCaught = false;
    private static volatile Throwable exceptionOccured;

    private static void createGui() {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        button = new JButton("Test");
        frame.add(button);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // this listener will be notified after the action
                throw new RuntimeException("test failed");
            }
        });
        button.addActionListener(HackedFileChooser.NEW_FOLDER_ACTION);

        frame.setSize(200, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Throwable {
        // Use a new thread group, because all the exceptions in the old
        // group are intercepted by jtreg
        ThreadGroup tg = new ThreadGroup("Test thread group") {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (e instanceof SecurityException || e instanceof java.security.AccessControlException) {
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
                            FakeEvent.createGui();
                        }
                    });
                    Thread.sleep(2000);
                    EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
                    eq.postEvent(new MouseEvent(button, MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, 0, false, MouseEvent.BUTTON1));
                    eq.postEvent(new MouseEvent(button, MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0, 0, false, MouseEvent.BUTTON1));
                    // Flush EDT
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                        }
                    });
                } catch (java.security.AccessControlException tt) {
                   throw tt;
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

//        System.exit(0);
    }
}
