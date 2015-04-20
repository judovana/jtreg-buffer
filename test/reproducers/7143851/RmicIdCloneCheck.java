import java.io.IOException;
import java.io.StringWriter;

import org.omg.CORBA.portable.ObjectImpl;

import sun.rmi.rmic.IndentingWriter;
import sun.rmi.rmic.iiop.StubGenerator;

public class RmicIdCloneCheck {

    private static class CustomGenerator extends StubGenerator {

        public int testStubGenerator(boolean poa) throws IOException {
            StringWriter stringWriter = new StringWriter();
            IndentingWriter indentingWriter = new IndentingWriter(stringWriter);

            POATie = poa;
            write_tie__ids_method(indentingWriter);
            indentingWriter.flush();

            String generated = stringWriter.toString();

            if (!generated.contains(".clone()")) {
                System.out.println("FAIL");
                System.out.print(generated);
                return 1;
            } else {
                System.out.println("OK");
                return 0;
            }
        }

        public void testStubGenerator() throws IOException {
            int rv = 0;
            System.out.print("- POA: false - ");
            rv += testStubGenerator(false);
            System.out.print("- POA: true - ");
            rv += testStubGenerator(true);
            if (rv > 0)
                failed("StubGenerator generates code with no clone calls");
        }
    }

    private static int testRemoteObject(ObjectImpl object) {
        String[] ids = null;
        ids = object._ids();
        ids[0] = null;
        ids = object._ids();
        if (ids[0] == null) {
            System.out.println("FAIL");
            return 1;
        } else {
            System.out.println("OK");
            return 0;
        }
    }

    private static void testBuiltInClasses() {
        int rv = 0;
        System.out.print("- javax.management.remote.rmi._RMIServerImpl_Tie - ");
        rv += testRemoteObject(new javax.management.remote.rmi._RMIServerImpl_Tie());
        System.out.print("- javax.management.remote.rmi._RMIConnectionImpl_Tie - ");
        rv += testRemoteObject(new javax.management.remote.rmi._RMIConnectionImpl_Tie());
        if (rv > 0)
            failed("JDK class built with unfixed rmic");
    }

    private static void failed(String string) {
        System.out.println("\nTEST FAILED: " + string);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Testing JDK classes to see if they were built with fixed rmic\n"); 
        testBuiltInClasses();
        System.out.println("\n");

        System.out.println("Testing StubGenerator (note that this fails with certain IBM Java versions with fixed rmic)\n");
        new CustomGenerator().testStubGenerator();
        System.out.println();
    }

}
