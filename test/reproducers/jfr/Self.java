import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import jdk.management.jfr.*;

import java.util.*;
import java.io.*;
class Self {

  private static final boolean debug = false;
  private static final Random rand = new Random();

  public static void main(String[] timeout) throws Exception {
    
    if (false) {
      System.out.println("Inspecting FlightRecorderMXBeann:");
      ObjectName objectName = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
      MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
      MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(objectName);
      for (MBeanAttributeInfo attributeInfo : mBeanInfo.getAttributes()) {
        Object value = platformMBeanServer.getAttribute(objectName, attributeInfo.getName());
        System.out.println(attributeInfo.getName() + " = " + value);
      }
    }

    FlightRecorderMXBean flightRecorder = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
    if (debug) {
      //flightRecorder.getEventTypes() returns same items, only under different type, all are disabled by default
      System.out.println("All available events:");
      for (EventType event : FlightRecorder.getFlightRecorder().getEventTypes()) {
        System.out.println(event.getName());
        for (ValueDescriptor value: event.getFields()) {
          System.out.println("     " + value.getName());
        }
      }
    }
    
    long recordingId = flightRecorder.newRecording();
    Map<String,String> settings = new HashMap();
    //enabel all settings
    for (EventType event : FlightRecorder.getFlightRecorder().getEventTypes()) {
       settings.put(event.getName()+"#enabled", "true");
    }
    flightRecorder.setRecordingSettings(recordingId, settings);
    //saving via options's"destinations"  works fine too, I had used "more java liek saving"
    //Map<String,String> options = new HashMap();
    //options.put("destination", "flightSelf.jfr");
    //flightRecorder.setRecordingOptions​(recordingId, options);
    flightRecorder.startRecording(recordingId);
    //maybe just call Server.java?
    for(int x = 0 ; x < Integer.parseInt(timeout[0]) ; x++) {
      for(int y = 0 ; y < 90 ; y++) {
         int n = rand.nextInt();
         Thread.sleep(10);
      }
    }
    flightRecorder.stopRecording(recordingId);

    long streamId = flightRecorder.openStream(recordingId, null);
    File f = new File("flightSelf.jfr");
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
