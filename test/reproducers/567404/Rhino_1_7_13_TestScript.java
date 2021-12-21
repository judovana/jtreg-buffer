/*
 * 567404 is real id, but jtreg needs 7 numbers bugs.... However, trick with zero works....
 * don't forget to include 0567404 as whole bug id
 * @test
 * @bug 0567404
 * @summary  verify rhino minimal functionality
 * @library rhino-engine-1.7.13.jar rhino-runtime-1.7.13.jar
 * @compile MainJavaScriptEngineRunner.java Rhino_1_7_13_TestScript.java
 * @run main/othervm Rhino_1_7_13_TestScript
 * @requires jdk.version.major <= 11
 */

public class Rhino_1_7_13_TestScript {
    public static void main(String[] args) throws Exception {
        MainJavaScriptEngineRunner.main("js");
        MainJavaScriptEngineRunner.main("rhino");
    }
}
