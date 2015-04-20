import java.util.Properties;

public class RH1187252
{
  public static void main(String[] args)
  {
    Properties props = System.getProperties();
    String[] archProps = {"java.library.path",
              "sun.boot.library.path",
              "os.arch"};
    for (String prop : archProps)
      System.out.printf("%s = %s\n", prop,
            props.getProperty(prop));
  }
}

