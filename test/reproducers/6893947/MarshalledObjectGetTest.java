/*
 * @test
 * @bug 6893947
 * @summary Do not call MarshalledObject.get from inside doPrivileged
 * @author Eamonn McManus
 * @requires jdk.version.major < 24
 * @run  main/othervm MarshalledObjectGetTest
 */

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.MarshalledObject;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.loading.ClassLoaderRepository;
import javax.management.remote.rmi.RMIConnectionImpl;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServerImpl;

public class MarshalledObjectGetTest {
    private static final ClassLoader myClassLoader = new Object(){}.getClass().getClassLoader();
    private static List<ClassLoader> rogues = new ArrayList<ClassLoader>();

    public static class RogueClassLoader extends ClassLoader implements Serializable {
        public Object readResolve() {
            rogues.add(this);
            return this;
        }
    }

    public static void main(String[] args) throws Exception {
        // Setup without SecurityManager.
        ClassLoader rogue1 = new RogueClassLoader();
        MarshalledObject<ClassLoader> mo = new MarshalledObject<ClassLoader>(rogue1);

        // Put a SecurityManager in place.  Now we have no permissions whatever.
        System.setSecurityManager(new SecurityManager());
        try {
            ClassLoader rogue2 = new RogueClassLoader();
            fail("Test error: should not have permission to create ClassLoader");
        } catch (SecurityException e) {
            System.out.println("Attempt to create RogueClassLoader normally was refused as expected");
        }
        RMIServerImpl rmiServer = new RMIJRMPServerImpl(0, null, null, null);
        MBeanServer mbs = (MBeanServer) Proxy.newProxyInstance(
                myClassLoader, new Class<?>[] {MBeanServer.class}, new NoddyIH());
        rmiServer.setMBeanServer(mbs);
        RMIConnectionImpl rmiConnection = new RMIConnectionImpl(rmiServer, "connection id", null, null, null);
        boolean ace = false;
        try {
            rmiConnection.setAttribute(new ObjectName("JMImplementation:type=MBeanServerDelegate"), mo, null);
        } catch (Exception e) {
            System.out.println("setAttribute call produced an exception as expected: " + e);
            for (Throwable t = e; t != null; t = t.getCause()) {
                System.out.println("..." + t);
                if (t instanceof AccessControlException) {
                    ace = true;
                    break;
                }
            }
        }
        if (rogues.isEmpty() && ace) {
            System.out.println("PASSED: attempt to create ClassLoader was foiled");
        } else if (ace) {
            // This could indicate that we got an exception for some unrelated reason,
            // and that the problem does still exist.
            fail("Attempt to create ClassLoader failed, but exception was not AccessControlException");
        } else {
            fail("Successfully created ClassLoader");
        }
    }

    private static void fail(String msg) throws Exception {
        throw new Exception("FAILED: " + msg);
    }

    // This is a cheapo way of getting an implementation of MBeanServer.
    // Since we don't have any permissions, we can't call MBeanServerFactory.newMBeanServer().
    private static class NoddyIH implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println(method.getName() + ((args == null) ? "[]" : Arrays.toString(args)));
            if (method.getName().equals("getClassLoaderRepository")) {
                return Proxy.newProxyInstance(
                        myClassLoader, new Class<?>[] {ClassLoaderRepository.class}, this);
            }
            return null;
        }
    }
}
