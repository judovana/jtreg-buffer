public class foo {

  public static void test(int n) {
    if (n == 0) return;
    System.out.println (n);
    test (n - 1);

  }

}
