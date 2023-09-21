import java.io.*;
import java.util.*;
import com.sun.tools.attach.*;
import sun.tools.attach.HotSpotVirtualMachine;


class ClientCmdLike {

  private static final boolean debug = false;
  private static final Random rand = new Random();

  public static void main(String[] pid) throws Exception {
         Integer.parseInt(pid[0]);
         VirtualMachine vm = VirtualMachine.attach(pid[0]);
        // Cast to HotSpotVirtualMachine as this is an
        // implementation specific method.
        HotSpotVirtualMachine hvm = (HotSpotVirtualMachine) vm;
            String line="JFR.start duration=2s filename=cmdLikeFlight.jfr";
            try (InputStream in = hvm.executeJCmd(line);) {
                // read to EOF and just print output
                byte b[] = new byte[256];
                int n;
                boolean messagePrinted = false;
                do {
                    n = in.read(b);
                    if (n > 0) {
                        String s = new String(b, 0, n, "UTF-8");
                        System.out.print(s);
                        messagePrinted = true;
                    }
                } while (n > 0);
                if (!messagePrinted) {
                    System.out.println("Command executed successfully");
                }
            }
        vm.detach();
  }
 
}
