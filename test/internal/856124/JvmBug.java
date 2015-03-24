import java.util.Arrays;

/*
 * 856124  is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
 * dont forget to include 0856124  as whole bug id
 * @test
 * @bug 0856124 
 * @summary  JVM heap memory disclosure
 * @run main/othervm JvmBug
 */

public class JvmBug 
{
   public static void main(String[] args) {
        int[] a;
        int n = 0;
        for (int i = 0; i < 100000000; ++i) {
            a = new int[10];
            for (int f : a)
                if (f != 0)
                    throw new RuntimeException("Array just after allocation: "+  Arrays.toString(a));
            Arrays.fill(a, 0);
            for (int j = 0; j < a.length; ++j)
                a[j] = (n - j)*i;
            for (int f : a)
                n += f;
        }
        System.out.println(n);
    }
}
