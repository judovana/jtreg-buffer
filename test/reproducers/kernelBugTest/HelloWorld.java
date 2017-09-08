import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

public class HelloWorld implements Daemon {

    @Override
    public void init(DaemonContext dc) throws Exception {
        System.out.println("initializing ...");
    }

    @Override
    public void start() throws Exception {
        System.out.println("starting ...");
    }

    @Override
    public void stop() throws Exception {
        System.out.println("stopping ...");
    }

    @Override
    public void destroy() {
        System.out.println("destroying ...");
    }

}