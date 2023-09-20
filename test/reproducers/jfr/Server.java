import java.util.Random;

class Server {

  private static final Random rand = new Random();

  public static void main(String... timeout) throws Exception {
    for(int x = 0 ; x < Integer.parseInt(timeout[0]) ; x++) {
      for(int y = 0 ; y < 90 ; y++) {
         int n = rand.nextInt();
         Thread.sleep(10);
      }
    }
  }
}

