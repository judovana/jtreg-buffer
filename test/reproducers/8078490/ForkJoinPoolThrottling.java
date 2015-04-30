
 /*
 * @test
 * @bug 8078490
 * @summary  [concurrency-interest] Missed submissions in latest versions of Java ForkJoinPool
 * @requires jdk.version.major >= 7
 * @run main/timeout=0.5/othervm ForkJoinPoolThrottling
 */


import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ForkJoinPoolThrottling {
     public static void main(String[] args) throws Throwable {
         final ForkJoinPool e = new ForkJoinPool(1);
         final AtomicBoolean b = new AtomicBoolean();
         final Runnable setFalse = new Runnable() { public void run() {
             b.set(false);
         }};
         for (int i = 0; i < 100000; i++) {
             b.set(true);
             e.execute(setFalse);
             do {} while (b.get()); // spins forever here
         }
     }
} 
