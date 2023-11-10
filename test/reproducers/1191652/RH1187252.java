/*
 * @test
 * @bug 1191652
 * @author lzachar@redhat.com
 * @requires os.family != "windows"
 * @summary   -OpenJDK on RHEL7.1 / PPC64 LE says that arch is ppc64, not ppc64le, messing up Maven and other tools (java) OpenJDK has to report os.arch = ppc64le on ppc64le. Otherwise there is no way to distinguish between ppc64le/be
 * @run shell runtest.sh
 */


import java.util.Properties;

public class RH1187252
{
  public static void main(String[] args)
  {
    Properties props = System.getProperties();
    String[] archProps = {
              "java.library.path",
              "sun.boot.library.path",
              "os.arch"};
    for (String prop : archProps) {
      System.out.printf("%s = %s\n", prop,props.getProperty(prop));
    }
  }
}

