/*
 * @test
 * @bug 6862968
 * @summary issue in huffman jpg 2
 *
 * @run main/othervm -Dqt.num=1000 -Dac.num=1000 -Ddc.num=1000 Test1
 */
public class Test {
    public static void main(String[] args) throws IOException {
		throw new RuntimeException("Should not be run. Test1 is expected to be run");
     }
}
