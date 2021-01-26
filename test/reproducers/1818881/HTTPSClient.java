import java.net.URL;
 
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
 
public class HTTPSClient {
    // Disable the hostname verification for demo purpose
    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
    }
     
    public static void main(String[] args){
        try{
            int port = Integer.valueOf(System.getProperty("test.port", "9999"));
            URL url = new URL("https://127.0.0.1:"+port);
            HttpsURLConnection client = (HttpsURLConnection) url.openConnection();
            System.out.println("RETURN : "+client.getResponseCode());
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
