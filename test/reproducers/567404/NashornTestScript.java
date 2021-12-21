/*
 * 567404 is real id, but jtreg needs 7 numbers bugs.... However, trick with zero works....
 * don't forget to include 0567404 as whole bug id
 * @test
 * @bug 0567404
 * @summary  verify nashorn minimal functionality
 * @requires jdk.version.major <= 11
 */

public class NashornTestScript {
    public static void main(String[] args) throws Exception {
        MainJavaScriptEngineRunner.main("JavaScript");
        MainJavaScriptEngineRunner.main("js");
    }
}

