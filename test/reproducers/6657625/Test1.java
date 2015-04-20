/*
 * @test
 * @bug 6657625
 * @summary RmfFileReader/StandardMidiFileWriter.types are public mutable statics (findbugs)
 * @run compile/fail Test1.java
*/

public class Test1 {
    public static void main(String[] args) {
        int[] a = com.sun.media.sound.StandardMidiFileWriter.types;
    }
}
