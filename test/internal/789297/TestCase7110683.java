
/**
 * @test
 * @run main/othervm TestCase7110683
 */


import java.awt.*;
import java.util.*;


public class TestCase7110683 {

    public static void main(String[] args) {
	boolean failed = false;
    // Install a SecurityManager here.
    SecurityManager sm = new SecurityManager();
    System.setSecurityManager(sm);
	try {
	    new KFM().getGlobalFocusOwner();
	    System.out.println("getGlobalFocusOwner() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().setGlobalFocusOwner(new Component(){});
	    System.out.println("setGlobalFocusOwner() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().clearGlobalFocusOwner();
	    System.out.println("clearGlobalFocusOwner() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().getGlobalPermanentFocusOwner();
	    System.out.println("getGlobalPermanentFocusOwner() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().setGlobalPermanentFocusOwner(new Component(){});
	    System.out.println("setGlobalPermanentFocusOwner() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().getGlobalFocusedWindow();
	    System.out.println("getGlobalFocusedWindow() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().setGlobalFocusedWindow(new Frame());
	    System.out.println("setGlobalFocusedWindow() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().getGlobalActiveWindow();
	    System.out.println("getGlobalActiveWindow() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().setGlobalActiveWindow(new Window(null));
	    System.out.println("setGlobalActiveWindow() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().getGlobalCurrentFocusCycleRoot();
	    System.out.println("getGlobalCurrentFocusCycleRoot() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	try {
	    new KFM().setGlobalCurrentFocusCycleRoot(new Container(){});
	    System.out.println("setGlobalCurrentFocusCycleRoot() not secure");
	    failed = true;
	} catch (SecurityException ex) {
	    // Ok.
	}
	if (failed) throw new RuntimeException();
    }

    private static class KFM extends DefaultKeyboardFocusManager {
	public Component getGlobalFocusOwner() {
	    return super.getGlobalFocusOwner();
	}
	public void setGlobalFocusOwner(Component c) {
	    super.setGlobalFocusOwner(c);
	}
	public Component getGlobalPermanentFocusOwner() {
	    return super.getGlobalPermanentFocusOwner();
	}
	public void setGlobalPermanentFocusOwner(Component c) {
	    super.setGlobalPermanentFocusOwner(c);
	}
	public Window getGlobalFocusedWindow() {
	    return super.getGlobalFocusedWindow();
	}
	public void setGlobalFocusedWindow(Window w) {
	    super.setGlobalFocusedWindow(w);
	}
	public Window getGlobalActiveWindow() {
	    return super.getGlobalActiveWindow();
	}
	public void setGlobalActiveWindow(Window w) {
	    super.setGlobalActiveWindow(w);
	}
	public Container getGlobalCurrentFocusCycleRoot() {
	    return super.getGlobalCurrentFocusCycleRoot();
	}
    }

    // TODO getGlobalFocusOwner(), setGlobalFocusOwner(), clearGlobalFocusOwner()
}
