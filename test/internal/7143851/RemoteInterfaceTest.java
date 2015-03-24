import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterfaceTest extends Remote {
    public void test() throws RemoteException;
}
