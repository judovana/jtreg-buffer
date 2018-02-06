/*
 * @test MemoryAllocatorTest.java
 * @modules jdk.jartool/sun.tools.jar
 * @bug 6755943 6792554 7057857
 * @summary Checks any memory overruns in archive length.
 * @run main/othervm/timeout=1200 MemoryAllocatorTest
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class MemoryAllocatorTest {

    /*
     * The smallest possible pack file with 1 empty resource
     */
    static int[] magic = {
        0xCA, 0xFE, 0xD0, 0x0D
    };
    static int[] version_info = {
        0x07, // minor
        0x96  // major
    };
    static int[] option = {
        0x10
    };
    static int[] size_hi = {
        0x00
    };
    static int[] size_lo_ulong = {
        0xFF, 0xFC, 0xFC, 0xFC, 0xFC // ULONG_MAX 0xFFFFFFFF
    };
    static int[] size_lo_correct = {
        0x17
    };
    static int[] data = {
        0x00, 0xEC, 0xDA, 0xDE, 0xF8, 0x45, 0x01, 0x02,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x01, 0x31, 0x01, 0x00
    };
    // End of pack file data

    static final String JAVA_HOME = System.getProperty("java.home");

    static final boolean debug = Boolean.getBoolean("MemoryAllocatorTest.Debug");
    static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");
    static final boolean LINUX = System.getProperty("os.name").startsWith("Linux");
    static final boolean SIXTYFOUR_BIT = System.getProperty("sun.arch.data.model", "32").equals("64");
    static final private int NATIVE_EXPECTED_EXIT_CODE = (WINDOWS) ? -1 : 255;
    static final private int JAVA_EXPECTED_EXIT_CODE = 1;

    static final private String EXPECTED_ERROR_MESSAGE[] = {
        "Native allocation failed",
        "overflow detected",
        "archive header had incorrect size",
        "EOF reading band",
        "impossible archive size",
        "bad value count",
        "file too large"
};

    static int testExitValue = 0;

    static byte[] bytes(int[] a) {
        byte[] b = new byte[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) a[i];
        }
        return b;
    }

    static void createPackFile(boolean good, File packFile) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(packFile);
            fos.write(bytes(magic));
            fos.write(bytes(version_info));
            fos.write(bytes(option));
            fos.write(bytes(size_hi));
            if (good) {
                fos.write(bytes(size_lo_correct));
            } else {
                fos.write(bytes(size_lo_ulong));
            }
            fos.write(bytes(data));
        } finally {
            close(fos);
        }
    }

    static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /*
     * modify the packfile archive header's size_lo (LSB) by decrementing it and
     * returning the new value.
     */
    static int modifyPackFile(File packFile) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(packFile, "rws");
            int pos = magic.length + version_info.length + option.length + size_hi.length;
            raf.seek(pos);
            byte value = raf.readByte();
            raf.seek(pos);
            value--;
            raf.writeByte(value);
            return value & 0xFF;
        } finally {
            close(raf);
        }
    }

    static String getUnpack200Cmd() throws Exception {
        return getAjavaCmd("unpack200");
    }

    static String getJavaCmd() throws Exception {
        return getAjavaCmd("java");
    }

    static String getAjavaCmd(String cmdStr) throws Exception {
        File binDir = new File(JAVA_HOME, "bin");
        File unpack200File = WINDOWS
                ? new File(binDir, cmdStr + ".exe")
                : new File(binDir, cmdStr);

        String cmd = unpack200File.getAbsolutePath();
        if (!unpack200File.canExecute()) {
            throw new Exception("please check" +
                    cmd + " exists and is executable");
        }
        return cmd;
    }
    final static  String UNPACK_FN = "Unpack";


    static void createTestClass() throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(UNPACK_FN + ".java"));
        try {
            pw.println("import java.io.File;");
            pw.println("import java.io.FileOutputStream;");
            pw.println("import java.io.FileInputStream;");
            pw.println("import java.util.jar.JarOutputStream;");
            pw.println("import java.util.jar.Pack200;");

            pw.println("public final class " + UNPACK_FN + "  {");
            pw.println("public static void main(String args[]) throws Exception {");
            pw.println("FileOutputStream os = null;");
            pw.println("FileInputStream  is = null;");
            pw.println("try {");
            pw.println("Pack200.Unpacker u = Pack200.newUnpacker();");
            pw.println("File in = new File(args[0]);");
            pw.println("is = new FileInputStream(in);");
            pw.println("File out = new File(args[1]);");
            pw.println("os = new FileOutputStream(out) {");
            pw.println("public void write(int b) {");
            pw.println("}");
            pw.println("};");
            pw.println("u.unpack(is, new JarOutputStream(os));");
            pw.println("} finally {");
            pw.println("if (os != null) os.close();");
            pw.println("if (is != null) is.close();");
            pw.println("}");
            pw.println("}");
            pw.println("}");
        } finally {
            close(pw);
        }
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        final String javacCmds[] = {UNPACK_FN + ".java"};
        javac.run(null, null, null, javacCmds);
    }

    static TestResult runUnpack200(File packFile) throws Exception {
        if (!packFile.exists()) {
            throw new Exception("please check" + packFile + " exists");
        }
        return runUnpacker(getUnpack200Cmd(), packFile.getName(), "testout.jar");
    }

    static TestResult runJavaUnpack(File packFile) throws Exception {
        if (!packFile.exists()) {
            throw new Exception("please check" + packFile + " exists");
        }
        return runUnpacker(getJavaCmd(), "-cp", ".", UNPACK_FN, packFile.getName(), "testout.jar");
    }

    static TestResult runUnpacker(String... cmds) throws Exception {

        ArrayList<String> alist = new ArrayList<String>();
        ProcessBuilder pb =
                new ProcessBuilder(cmds);
        Map<String, String> env = pb.environment();
        pb.directory(new File("."));
        for (String x : cmds) {
            System.out.print(x + " ");
        }
        System.out.println("");
        int retval = 0;
        Process p = null;
        InputStreamReader ir = null;
        BufferedReader rd = null;
        InputStream is = null;
        try {
            pb.redirectErrorStream(true);
            p = pb.start();
            is = p.getInputStream();
            ir = new InputStreamReader(is);
            rd = new BufferedReader(ir, 8192);

            String in = rd.readLine();
            while (in != null) {
                alist.add(in);
                System.out.println(in);
                in = rd.readLine();
            }
            retval = p.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        } finally {
            close(rd);
            close(ir);
            close(is);
            if (p != null) p.destroy();
        }
        return new TestResult("", retval, alist);
    }

    static void deleteFile(File theFile) {
        if (theFile.exists() && !theFile.delete()) {
            Thread.dumpStack();
            throw new RuntimeException("could not delete: " + theFile.getAbsolutePath());
        }
        if (theFile.exists()) {
            Thread.dumpStack();
            throw new RuntimeException("file exists!!!, did delete work ?: " +
                    theFile.getAbsolutePath());
        }
    }

    static final FileFilter myFilter = new FileFilter() {
        public boolean accept(File pathname) {
            String name = pathname.getName();
            if (name.endsWith(".pack") || name.endsWith(".jar")) {
                return true;
            }
            return false;
        }
    };

    static void deleteFiles() {
        File cDir = new File(".");
        File[] scrFile = cDir.listFiles(myFilter);
        for (File f : scrFile) {
            System.out.println("deleting: " + f.getAbsolutePath());
            deleteFile(f);
        }
    }

    /*
     * tests large size_lo values, by first creating a ridiculously large pack
     * file archive size, and decrementing that value successively, to check for
     * overflow conditions.
     */
    static void runLargeTests() throws Exception {
        File packFile = new File("big.pack");
        // Create a good pack file and test if everything is ok
        createPackFile(true, packFile);
        TestResult tr = runUnpack200(packFile);
        tr.setDescription("unpack200: a good pack file");
        tr.checkPositive();
        tr.isOK();
        System.out.println(tr);
        if (testExitValue != 0) {
            throw new RuntimeException("Test ERROR");
        }

        // try the same with java unpacker
        tr = runJavaUnpack(packFile);
        tr.setDescription("unpack: a good pack file");
        tr.checkPositive();
        tr.isOK();
        System.out.println(tr);
        if (testExitValue != 0) {
            throw new RuntimeException("Test ERROR");
        }

        int value = 0;

        // create a bad pack file, with size being MAXINT
        createPackFile(false, packFile);

        // run the unpack200 unpacker
        tr = runUnpack200(packFile);
        tr.setDescription("unpack200: a wicked pack file");
        tr.contains(EXPECTED_ERROR_MESSAGE);
        tr.checkValue(NATIVE_EXPECTED_EXIT_CODE);
        // run the java unpacker now
        tr = runJavaUnpack(packFile);
        tr.setDescription("unpack: a wicked pack file");
        tr.contains(EXPECTED_ERROR_MESSAGE);
        tr.checkValue(JAVA_EXPECTED_EXIT_CODE);

        // Large values test
        System.out.println(tr);
        value = modifyPackFile(packFile);

        // continue creating bad pack files by modifying the specimen pack file.
        while (value >= 0xc0) {
            System.out.println("value: " + asHexString(value));
            tr.setDescription("unpack200: wicked value=" + asHexString(value));
            tr = runUnpack200(packFile);
            tr.contains(EXPECTED_ERROR_MESSAGE);
            tr.checkValue(NATIVE_EXPECTED_EXIT_CODE);

            System.out.println(tr);

            tr.setDescription("unpack: wicked value=" + asHexString(value));
            tr = runJavaUnpack(packFile);
            tr.contains(EXPECTED_ERROR_MESSAGE);
            tr.checkValue(JAVA_EXPECTED_EXIT_CODE);

            System.out.println(tr);
            value = modifyPackFile(packFile);
        }
    }

    /*
     * tests mutations in the lower regions of the size_lo, by first creating a
     * good pack file and decrementing the value successively.
     */
    static void runSmallTests() throws Exception {
          // small values test
         // create a good pack file to start with
        File packFile = new File("small.pack");
        createPackFile(true, packFile);
        TestResult tr = null;
        int value = modifyPackFile(packFile);
        while (value > 0) {
            System.out.println("value: " + asHexString(value));
            // run the unpack200 unpacker
            tr = runUnpack200(packFile);
            tr.setDescription("unpack200: wicked value=" + asHexString(value));
            tr.contains(EXPECTED_ERROR_MESSAGE);
            tr.checkValue(NATIVE_EXPECTED_EXIT_CODE);

            System.out.println(tr);

            // run the java unpacker now
            tr = runJavaUnpack(packFile);
            tr.setDescription("unpack: wicked value=" + asHexString(value));
            tr.contains(EXPECTED_ERROR_MESSAGE);
            tr.checkValue(JAVA_EXPECTED_EXIT_CODE);
            System.out.println(tr);

            value = modifyPackFile(packFile);
        }
    }

    static String asHexString(int value) {
        return "0x" + Integer.toHexString(value & 0xFF);
    }

    static void copyFile(File src, File target) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream  bos = null;
        try {
            fis = new FileInputStream(src);
            bis = new BufferedInputStream(fis);
            fos = new FileOutputStream(target);
            bos = new BufferedOutputStream(fos);
            byte[] buffer = new byte[8192];
            int n = bis.read(buffer);
            while (n > 0) {
                bos.write(buffer, 0, n);
                n = bis.read(buffer);
            }
        } finally {
            close(bos);
            close(fos);
            close(bis);
            close(fis);
        }
    }
    /*
     * These tests uses the unpacker on packfiles provided by external
     * contributors which cannot be recreated programmatically.
     */
    static void runBadPackTests() throws Exception {
       File jarFile = new File(System.getProperty("test.src", "."), "packfiles.jar");
       File tmpFile = new File("packfiles.jar");
       copyFile(jarFile, tmpFile);
       String[] jarCmd =  { "xvf", tmpFile.getName() };
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       PrintStream jarout = new PrintStream(baos);
       sun.tools.jar.Main jarTool = new sun.tools.jar.Main(jarout, System.err, "jar-tool");
       if (!jarTool.run(jarCmd)) {
           throw new RuntimeException("Error: could not extract archive");
       }
       TestResult tr = null;
       jarout.close();
       baos.flush();
       for (String x : baos.toString().split("\n")) {
           String line[] = x.split(":");
           if (line[0].trim().startsWith("inflated")) {
               String pfile = line[1].trim();
               tr = runUnpack200(new File(pfile));
               tr.setDescription("unpack200: " + pfile);
               tr.contains(EXPECTED_ERROR_MESSAGE);
               tr.checkValue(NATIVE_EXPECTED_EXIT_CODE);
               System.out.println(tr);

               tr = runJavaUnpack(new File(pfile));
               tr.setDescription("unpack: " + pfile);
               tr.contains(EXPECTED_ERROR_MESSAGE);
               tr.checkValue(JAVA_EXPECTED_EXIT_CODE);
               System.out.println(tr);
           }
       }
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        /*
         * jprt systems on windows and linux seem to have abundant memory
         * therefore can take a very long time to run, and even if it does
         * the error message is not accurate for us to discern if the test
         * passes successfully.
         */
        if (SIXTYFOUR_BIT && (LINUX || WINDOWS)) {
            System.out.println("Warning: Windows/Linux 64bit tests passes vacuously");
            return;
        }

        // Create the java unpacker
        createTestClass();
        runLargeTests();
        runSmallTests();
        runBadPackTests();
        deleteFiles(); // deletes transient files
        if (testExitValue != 0) {
            throw new Exception("Pack200 archive length tests(" +
                    testExitValue + ") failed ");
        } else {
            System.out.println("All tests pass");
        }
    }

    /*
     * A class to encapsulate the test results and stuff, with some ease
     * of use methods to check the test results.
     */
    static class TestResult {

        StringBuilder status;
        int exitValue;
        List<String> testOutput;
        String description;
        boolean isNPVersion;

        /*
         * The debug version builds of unpack200 call abort(3) which might set
         * an unexpected return value, therefore this test is to determine
         * if we are using a product or non-product build and check the
         * return value appropriately.
         */
        boolean isNonProductVersion() throws Exception {
            ArrayList<String> alist = new ArrayList<String>();
            ProcessBuilder pb = new ProcessBuilder(getUnpack200Cmd(), "--version");
            Map<String, String> env = pb.environment();
            System.out.println(new File(".").getAbsolutePath());
            pb.directory(new File("."));
            int retval = 0;
            Process p = null;
            BufferedReader rd = null;
            InputStreamReader ir = null;
            InputStream is = null;
            try {
                pb.redirectErrorStream(true);
                p = pb.start();
                is = p.getInputStream();
                ir = new InputStreamReader(is);
                rd = new BufferedReader(ir, 8192);
                String in = rd.readLine();
                while (in != null) {
                    alist.add(in);
                    System.out.println(in);
                    in = rd.readLine();
                }
                retval = p.waitFor();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex.getMessage());
            } finally {
                close(rd);
                close(ir);
                close(is);
                if (p != null) p.destroy();
            }
            for (String x : alist) {
                if (x.contains("non-product")) {
                    return true;
                }
            }
            return false;
        }


        public TestResult(String str, int rv, List<String> oList) throws Exception {
            status = new StringBuilder(str);
            exitValue = rv;
            testOutput = oList;
            isNPVersion = isNonProductVersion();
            if (isNPVersion) {
                System.err.println("Warning: exit values are not checked" +
                        " for non-product build");
            }
        }

        void setDescription(String description) {
            this.description = description;
        }

        void checkValue(int value) {
            if (isNPVersion) return;
            if (exitValue != value) {
                status =
                        status.append("  Error: test expected exit value " +
                        value + "got " + exitValue);
                testExitValue++;
            }
        }

        void checkNegative() {
            if (exitValue == 0) {
                status = status.append(
                        "  Error: test did not expect 0 exit value");

                testExitValue++;
            }
        }

        void checkPositive() {
            if (exitValue != 0) {
                status = status.append(
                        "  Error: test did not return 0 exit value");
                testExitValue++;
            }
        }

        boolean isOK() {
            return exitValue == 0;
        }

        boolean isZeroOutput() {
            if (!testOutput.isEmpty()) {
                status = status.append("  Error: No message from cmd please");
                testExitValue++;
                return false;
            }
            return true;
        }

        boolean isNotZeroOutput() {
            if (testOutput.isEmpty()) {
                status = status.append("  Error: Missing message");
                testExitValue++;
                return false;
            }
            return true;
        }

        public String toString() {
            if (debug) {
                for (String x : testOutput) {
                    status = status.append(x + "\n");
                }
            }
            if (description != null) {
                status.insert(0, description);
            }
            return status.append("exitValue = " + exitValue).toString();
        }

        boolean contains(String... strs) {
            for (String x : testOutput) {
                for (String y : strs) {
                    if (x.contains(y)) {
                        return true;
                    }
                }
            }
            status = status.append("   Error: expected strings not found");
            testExitValue++;
            return false;
        }
    }
}
