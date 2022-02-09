/*
 * @test
 * @bug 8218546
 * @requires jdk.version.major >= 11
 * @summary test if jdk can connect to google in not exactly standart way; https://github.com/openjdk/jdk11u/commit/ef5fff53ef1b047c2fca47047fe743689cc2734f
 * @run main ConnectGoogle
 * @author dontaed@severin.by
 */


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import static java.net.http.HttpClient.Redirect;
import static java.net.http.HttpClient.Version;
import static java.net.http.HttpResponse.BodyHandlers;
import static java.net.http.HttpResponse.BodySubscribers;

public class ConnectGoogle	 {
    public static void main(String args[]) throws Exception {
        HttpClient httpClient = HttpClient.newBuilder()
                       .version(Version.HTTP_2)  // this is the default
                       .followRedirects(Redirect.NORMAL)
                       .build();

        HttpRequest request = HttpRequest.newBuilder()
                       .uri(URI.create("https://google.com"))
                       .timeout(Duration.ofSeconds(10))
                       .build();

        BodyHandler<byte[]> bh0 = rsp -> {
            return BodySubscribers.ofByteArray();
        };

        var res = httpClient.sendAsync(request, bh0)
                  .thenAccept(response -> {
                               System.err.println("version:" + response.version());
                               System.err.println("status code: " + response.statusCode());
                               System.err.println("headers: " + response.headers());
                               System.err.println("body: " + response.body());
                          });

        try {
            res.get();
            System.err.println("SUCCESS");
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("FAILED");
            System.exit(-1);
        }
    }
}

