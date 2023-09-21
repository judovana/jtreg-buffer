import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;

import javax.management.*;
import javax.management.remote.*;

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import jdk.management.jfr.*;

import java.util.*;
import java.io.*;
class JmxClientPort {


  public static void main(String[] port) throws Exception {
    
    String s = "/jndi/rmi://localhost:" + port[0] + "/jmxrmi";
    JMXServiceURL url = new JMXServiceURL("rmi", "", 0, s);
    JMXConnector jmxConnector = JMXConnectorFactory.connect(url);
    MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
    ObjectName objectName = new ObjectName("jdk.management.jfr:type=FlightRecorder");
    FlightRecorderMXBean flightRecorder = JMX.newMXBeanProxy(mBeanServerConnection, objectName, FlightRecorderMXBean.class);

    
    long recordingId = flightRecorder.newRecording();
    Map<String,String> settings = new HashMap();
    //enabel all settings
    for (EventType event : FlightRecorder.getFlightRecorder().getEventTypes()) {
       settings.put(event.getName()+"#enabled", "true");
    }
    flightRecorder.setRecordingSettings(recordingId, settings);
    flightRecorder.startRecording(recordingId);
    Thread.sleep(2000);
    flightRecorder.stopRecording(recordingId);

    long streamId = flightRecorder.openStream(recordingId, null);
    File f = new File("remotePortFlight.jfr");
    try (var fos = new FileOutputStream(f); var bos = new BufferedOutputStream(fos)) {
      while (true) {
        byte[] data = flightRecorder.readStream(streamId);
        if (data == null) {
          bos.flush();
          break;
        }
        bos.write(data);
      }
    }
  }
}
