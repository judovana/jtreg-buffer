
/*
 * 751203 is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
 * dont forget to include 0751203 as whole bug id
 * @test
 * @bug 0751203
 * @summary  Test whether RMI is still working without policy file.
 * @run shell runtest.sh
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Remote Class for the "Hello, world!" example.
 */
public class Hello /*extends UnicastRemoteObject */implements HelloInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7383558678845948023L;
	private String message;
	private int counter;

	/**
	 * Construct a remote object
	 * 
	 * @param msg the message of the remote object, such as "Hello, world!".
	 * @exception RemoteException if the object handle cannot be constructed.
	 */
	public Hello() throws RemoteException {
		message = "Hello, I'm  client ";
		counter = 0;
	}

	/**
	 * Implementation of the remotely invocable method.
	 * 
	 * @return the message of the remote object, such as "Hello, world!".
	 * @exception RemoteException if the remote invocation fails.
	 */
	public String say() throws RemoteException {
		counter++;
		System.out.print(counter + ". client connected. \t");
		System.out.println("Sending message: \"" + message + counter + "\"");
		return message + counter;
	}
}
