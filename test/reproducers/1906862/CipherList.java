import java.net.URL;
import java.io.*;
import javax.net.ssl.*;
import java.security.NoSuchAlgorithmException;

/*
 * @test
 * @bug 1906862
 * @summary failing on listing policies in default mode
 * @run main CipherList
 */

public class CipherList
{
  public static void main(String[] args)
  throws Exception
  {
    int i;
    String[] cipherSuites = getCipherList();

    for (i=0;i<cipherSuites.length;i++)
    {
      System.out.println(cipherSuites[i]);
    }
  }

  public static String[] getCipherList() throws NoSuchAlgorithmException{
    SSLContext context = SSLContext.getDefault();
    SSLSocketFactory sf = context.getSocketFactory();
    return sf.getDefaultCipherSuites();
  }
}

