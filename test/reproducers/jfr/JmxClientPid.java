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

import com.sun.tools.attach.VirtualMachine;

class JmxClientPid {


  public static void main(String[] pid) throws Exception {
    
    VirtualMachine vm = VirtualMachine.attach(pid[0]);
    String jmxUrl = vm.startLocalManagementAgent();
    JMXServiceURL url = new JMXServiceURL(jmxUrl);
    System.out.println(url);
    JMXConnector jmxConnector = JMXConnectorFactory.connect(url);
    MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
    ObjectName objectName = new ObjectName("jdk.management.jfr:type=FlightRecorder");
    FlightRecorderMXBean flightRecorder = JMX.newMXBeanProxy(mBeanServerConnection, objectName, FlightRecorderMXBean.class);

    
    long recordingId = flightRecorder.newRecording();
    Map<String,String> settings = new HashMap();
    //enabel all settings
    for (EventTypeInfo event : flightRecorder.getEventTypes()) {
       settings.put(event.getName()+"#enabled", "true");
    }
    flightRecorder.setRecordingSettings(recordingId, settings);
    flightRecorder.startRecording(recordingId);
    Thread.sleep(2000);
    flightRecorder.stopRecording(recordingId);

    //void copyTo​(long recordingId, String outputFile) maybe may be used instead of the stream work
    long streamId = flightRecorder.openStream(recordingId, null);
    File f = new File("remotePidFlight.jfr");
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
