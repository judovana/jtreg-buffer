import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

public class HelloServer {
	/**
	 * Server program for the "Hello, world!" example.
	 * 
	 * @param argv
	 *            The command line arguments which are ignored.
	 */
	public static void main(String[] argv) {		
		try {
			//if (System.getSecurityManager() == null) {
			//	System.setSecurityManager(new SecurityManager());
			//}
			HelloInterface hello = new Hello();
			HelloInterface stub = (HelloInterface)UnicastRemoteObject.exportObject(hello, 0);
			//String name = "//192.168.209.150/hello1";
			//Naming.rebind(name, stub);
			String name = "hello1";
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(name, stub);
			System.out.println("Hello Server is ready.");
		} catch (Exception e) {
			log.warning("Hello Server failed: ");
			e.printStackTrace();
		}
	}
	
	private final static Logger log = Logger.getLogger(HelloServer.class.getName());
}
