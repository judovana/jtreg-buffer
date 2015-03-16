

import org.mozilla.javascript.NativeCall;

/**
 * Simple program to check if OpenJDK can see newer rhino classes 
 * (only works if rhino >= 1.7)
 */
public class UpstreamRhinoClassPathTest {
    /** Try a method that only exists in newer Rhino */
    public static void main(String args[]) {
        NativeCall call = null;
        call.getThisObj();
    }
}
