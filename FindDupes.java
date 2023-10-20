/**
 * TODO - for big files, repalce current 2D-array fast solution, by recursive slow solution :D
 * some of the jtreg classes are pretty huge, with 30G some similarity search failed. Try:
 * java -Xmx46G  FindDupes  ./test/reproducers/1098399/GlyphBug.java /home/jvanek/git/jdk/test/jdk/java/lang/String/concat/ImplicitStringConcatShapes.java  --verbose 
 * the 46GB of ram is ok for default 100kb limit on files. And already those takes about 5-8 minutes to comapre
 * I had tried also (ImplicitStringConcatShapes have 17000lines):
 * java -Xmx60G  FindDupes  /home/jvanek/git/jdk/test/jdk/java/lang/String/concat/ImplicitStringConcatShapes.java  /home/jvanek/git/jdk/test/jdk/java/lang/String/concat/ImplicitStringConcatShapes.java  --verbose 
 * but it faiks after few seconds...
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FindDupes {

 public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.err.println("At least one argument necessary - directory/dir to comapre CWD agasint");
            System.err.println("Other understood args:");
            System.err.println("  minimal similarity in percent --min=NUMB");
            System.err.println("  minimal similarity in percent with whitechars removed"
                             + "                                --minws=NUMB");
            System.err.println("  file path filter regex        --fitler=EXPRES");
            System.err.println("  verbose mode                  --verbose");
            System.err.println("  maximum filesize in KB        --maxsize=NUMBER");
            System.err.println("    Default is 100 (100kb), which eats about 46GB ram and taks 5-8 minutes. Biggrer files ");
            System.err.println("Note, that min/minws should be 0-100 inclusive. Bigger/higher will effectively exclude the method.");
            System.err.println("everything not `-` starting  is considered as dir/file which  the CWD/first file/dir should be compared against");
            throw new RuntimeException(" one ore more args expected, got zero");
        }
        File src = null;
        List<File> compares = new ArrayList<>(args.length + 1);
        double min = 80;
        double minws = 90;
        boolean verbose = false;
        long maxsize = 100*1024;    //100kb
        Pattern filter = Pattern.compile(".*");
        for (String arg : args) {
            if (arg.startsWith("-")) {
                arg = arg.replaceAll("^-+", "-");
                String aarg = arg.replaceAll("=.*", "");
                switch (aarg) {
                    case "-min":
                        min = Double.parseDouble(arg.split("=")[1]);
                        break;
                    case "-minws":
                        minws = Double.parseDouble(arg.split("=")[1]);
                        break;
                    case "-maxsize":
                        maxsize = Integer.parseInt(arg.split("=")[1])*1024;
                        break;
                    case "-filter":
                        filter = Pattern.compile(arg.split("=")[1]);
                        break;
                    case "-verbose":
                        verbose = true;
                        break;
                    default:
                        throw new RuntimeException("Uknown arg: " + arg);
                }
            } else {
                compares.add(new File(arg));
                if (!compares.get(compares.size() - 1).exists()) {
                    throw new RuntimeException(arg + "  do not exists: " + compares.get(compares.size() - 1).getAbsolutePath());
                }
            }
        }
        if (compares.size() == 0) {
            throw new RuntimeException("Nothing to compare against! Add some dirs/files, or run without args for help.");
        }
        if (compares.size() > 1) {
            src = compares.get(0);
            compares.remove(0);
        } else {
            src = new File("").getAbsoluteFile();
        }

        List<List<Path>> finalCompares = new ArrayList<>(compares.size());
        long total = 0;
        List<Path> finalSrcs = new ArrayList<>();
        if (verbose) {
            System.err.println("min: " + min);
            System.err.println("minws: " + minws);
            System.err.println("maxsize: " + maxsize);
        }
        for (int i = 0; i < compares.size(); i++) {
            File comp = compares.get(i);
            if (verbose) {
                System.err.print(comp.getAbsolutePath());
            }
            List<Path> finalCmpItem = new ArrayList<>();
            Files.walkFileTree(comp.toPath(), new FilesToListVisitor(finalCmpItem));
            finalCompares.add(finalCmpItem);
            total += finalCmpItem.size();
            if (verbose) {
                System.err.print(" (" + finalCmpItem.size() + ")");
                if (i == compares.size() - 1) {
                    System.err.println("");
                } else {
                    System.err.print(", ");
                }
            }
        }

        if (verbose) {
            System.err.print("Will compare against: " + src.getAbsolutePath());
        }
        Files.walkFileTree(src.toPath(), new FilesToListVisitor(finalSrcs));
        if (verbose) {
            System.err.println(" (" + finalSrcs.size() + ")");
        }
        min = min / 100d;
        minws = minws / 100d;
        int[] maxmin = new int[]{0};
        int[] maxminws = new int[]{0};
        long counter = 0;
        long hits = 0;
        int skips = 0;
        long totalttoal = (long) (finalSrcs.size()) * total;
        Date started=new Date();
        for (Path from:  finalSrcs) {
            if (verbose) {
                System.err.println("Started " + from.toFile().getAbsolutePath());
            }
            long localhits = 0;
            int[] lmaxmin = new int[]{0};
            int[] lmaxminws = new int[]{0};
            int lskips = 0;
            String content1 = null;
            try {
                if (from.toFile().length() > maxsize) {
                    throw new IOException("File too big,  limit is " + maxsize);
                }
                if (!filter.matcher(from.toFile().getAbsolutePath()).matches()) {
                    throw new IOException("File do not match fitlering " + filter.toString());
                }
                content1 = Files.readString(from);
            } catch(Exception ex) {
                skips++;
                if (verbose) {
                    System.err.println("skipping " + from.toFile().getAbsolutePath() + " binary? " + ex.getMessage());
                }
            }
            if (content1 == null) {
                continue;
            }
            for (int x = 0; x < compares.size(); x++) {
                File toinfo = compares.get(x);
                if (verbose) {
                    System.err.println("Starting " +  toinfo.getAbsolutePath() + " hits: "+localhits+"|"+hits);
                }
                for (Path to: finalCompares.get(x)) {
                    counter++;
                    if (verbose) {
                        System.err.println(counter + "/" + totalttoal + " " + eta(started, counter, totalttoal) + " " + from.toFile().getAbsolutePath() + " x " + to.toFile().getAbsolutePath());
                    }
                    String content2 = null;
                    try {
                        if (to.toFile().length() > maxsize) {
                            throw new IOException("File too big,  limit is " + maxsize);
                        }
                        if (!filter.matcher(to.toFile().getAbsolutePath()).matches()) {
                            throw new IOException("File do not match fitlering " + filter.toString());
                        }
                        content2 = Files.readString(to);
                    } catch(Exception ex) {
                        lskips++;
                        if (verbose) {
                            System.err.println("skipping " + to.toFile().getAbsolutePath() + " binary? " + ex.getMessage());
                        }
                    }
                    if (content2 == null) {
                        continue;
                    }
                    if (verbose) {
                        System.err.print(" similarity: ");
                    }
                    boolean compare1 = LevenshteinDistance.isDifferenceTolerable(content1, content2, min, verbose, lmaxmin);
                    if (lmaxmin[0] > maxmin[0]) {
                        maxmin[0]=lmaxmin[0];
                    }
                    if (compare1) {
                        hits++;
                        localhits++;
                        System.out.println(from.toFile().getAbsolutePath() + " == " + to.toFile().getAbsolutePath());
                        System.err.println(from.toFile().getAbsolutePath() + " == " + to.toFile().getAbsolutePath() + " hits: "+localhits+"|"+hits);
                    } else {
                        if (verbose) {
                            System.err.print(" similarity without spaces: ");
                        }
                        boolean compare2 = LevenshteinDistance.isDifferenceTolerable(
                                content1.replaceAll("\\s+", ""),
                                content2.replaceAll("\\s+", ""),
                                minws,verbose, lmaxminws);
                        if (lmaxminws[0] > maxminws[0]) {
                            maxminws[0]=lmaxminws[0];
                        }
                        if (compare2) {
                            hits++;
                            localhits++;
                            System.out.println(from.toFile().getAbsolutePath() + " == " + to.toFile().getAbsolutePath());
                            System.err.println(from.toFile().getAbsolutePath() + " == " + to.toFile().getAbsolutePath() + " hits: "+localhits+"|"+hits);
                        }
                    }
                }
            }
            if (verbose) {
                System.err.println("    Finished:"  + from.toFile().getAbsolutePath());
                System.err.println("        hits:"  + localhits);
                System.err.println("   bestmatch:"  + lmaxmin);
                System.err.println(" bestmatchws:"  + lmaxminws);
                System.err.println("       skips:"  + lskips);
                System.err.println("                   hits total: "  + hits);
                System.err.println("                  skips total: "  + skips);
                System.err.println("   bestmatch all over session: "  + maxmin);
                System.err.println(" bestmatchws all over session: "  + maxminws);
                System.err.println(" bestmatchws all over session: "  + maxminws);
            }
        }
    }

        private static class FilesToListVisitor implements FileVisitor<Path>  {
            private final List<Path> list;

            public FilesToListVisitor(List<Path> list) {
                this.list = list;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                list.add(path);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        }

        public static final class LevenshteinDistance {
            /**
             * Calculates the Levenshtein distance between two strings.<br/>
             * Uses a 2D array to represent individual changes, therefore the time complexity is quadratic
             * (in reference to the strings' length).
             *
             * @param str1 the first string
             * @param str2 the second string
             * @return an integer representing the amount of atomic changes between {@code str1} and {@code str2}
             */
            public static int calculate(String str1, String str2) {
                int[][] matrix = new int[str1.length() + 1][str2.length() + 1];

                for (int i = 0; i <= str1.length(); i++) {
                    for (int j = 0; j <= str2.length(); j++) {
                        if (i == 0) { // distance between "" and str2 == how long str2 is
                            matrix[i][j] = j;
                        } else if (j == 0) { // distance between str1 and "" == how long str1 is
                            matrix[i][j] = i;
                        } else {
                            int substitution = matrix[i - 1][j - 1] + substitutionCost(str1.charAt(i - 1), str2.charAt(j - 1));
                            int insertion = matrix[i][j - 1] + 1;
                            int deletion = matrix[i - 1][j] + 1;

                            matrix[i][j] = min3(substitution, insertion, deletion);
                        }
                    }
                }

                return matrix[str1.length()][str2.length()]; // result is in the bottom-right corner
            }

            private static int substitutionCost(char a, char b) {
                return (a == b) ? 0 : 1;
            }

            private static int min3(int a, int b, int c) {
                return Math.min(a, Math.min(b, c));
            }

            public static boolean isDifferenceTolerable(String s1, String s2, double samenessPercentage,
                    boolean verbose, int[] recorder) {
                return isDifferenceTolerableImpl(samenessPercentage, LevenshteinDistance.calculate(s1, s2),
                        Math.max(s1.length(), s2.length()), verbose, recorder);
            }

            public static boolean isDifferenceTolerableImpl(double samenessPercentage, int actualChanges, int totalSize,
                    boolean verbose, int[] recorder) {
                double changesAllowed = (1.0 - samenessPercentage) * totalSize;
                int percent=100-((actualChanges*100)/totalSize);
                if (percent>recorder[0]) {
                    recorder[0]=percent;
                }
                if (verbose) {
                    System.err.println(percent+"% <? "+samenessPercentage*100 + "% (max:" + recorder[0] + ")");
                }
                return actualChanges <= changesAllowed;
            }
        }

        public static String eta(Date start, long counter, long total)  {
            Date now = new Date();
            long tookTime = now.getTime() - start.getTime();
            double deta = (double)total/(double)counter*(double)tookTime;
            return "(run " + tookTime/1000/60+"m/eta " + (int)(deta/1000/60)+"m)";
        }
}
