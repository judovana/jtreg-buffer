import java.util.Arrays;

/*
 * 856124  is real id, but jtreg needs 7 numbers bugs.... Howewer, trick with zero works....
 * dont forget to include 0856124  as whole bug id
 * @test
 * @bug 0856124 
 * @summary  JVM heap memory disclosure
 * @run main/othervm JvmBugOld
 */

public class JvmBugOld
{
   public static void main(String[] args) {
        int[] a;
        int n = 0;
        for (int i = 0; i < 100000000; ++i) {
            a = new int[10];
            for (int z = 0; z < 10; z++) {
		int f = a[z];
                if (f != 0)
                    throw new RuntimeException("Array just after allocation: "+ a[0] + "," + a[1] + "," + a[2] + "," + a[3] + "," + a[4] + "," + a[5] + "," + a[6] + "," + a[7] + "," + a[8] + "," + a[9]);
            }
	Arrays.fill(a, 0);
            for (int j = 0; j < a.length; ++j)
                a[j] = (n - j)*i;
	    for (int y = 0; y < 10; y++) {
		n += a[y];
                }
        }
        System.out.println(n);
    }
}
