/**
 * some of the jtreg classes are pretty huge, with 30G some similarity search failed. Try:
 * java -Xmx60G  FindDupes  ./test/reproducers/1098399/GlyphBug.java /home/jvanek/git/jdk/test/jdk/java/lang/String/concat/ImplicitStringConcatShapes.java  --verbose 
 * with less mem. On 30 failed after minute. ON 60 passes in 6 minutes for me
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

public class FindDupes {

 public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.err.println("At least one argument necessary - directory/dir to comapre CWD agasint");
            System.err.println("Other understood args:");
            System.err.println("  minimal similarity in percent --min=NUMB");
            System.err.println(
                    "  minimal similarity in percent with whitechars removed" + "                                --minws=NUMB");
            System.err.println("  verbose mode                  --verbose");
            //implement if to slow reading all for ever - jsut hashmap <path, content>. contentn null for bianry
            //System.err.println("  cache mode                    --cachesrc");
            //System.err.println("  cache mode                    --cacheall");
            System.err.println("Note, that min/minws should be 0-100 inclusive. Bigger/higher will effectively exclude the method.");
            System.err.println("everything not `-` starting  is considered as dir/file which  the CWD/first file/dir should be "
                    + "compared against");
            throw new RuntimeException(" one ore more ergs expected, got zero");
        }
        File src = null;
        List<File> compares = new ArrayList<>(args.length + 1);
        double min = 80;
        double minws = 90;
        boolean verbose = true;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                arg = arg.replaceAll("^-+", "-");
                switch (arg) {
                    case "-min":
                        min = Double.parseDouble(arg.split(":")[1]);
                        break;
                    case "-minws":
                        minws = Double.parseDouble(arg.split(":")[1]);
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
        long counter = 0;
        long totalttoal = (long) (finalSrcs.size()) * total;
        for (Path from:  finalSrcs) {
            String content1 = null;
            try {
                content1 = Files.readString(from);
            } catch(Exception ex) {
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
                    System.err.println("Starting " +  toinfo.getAbsolutePath());
                }
                for (Path to: finalCompares.get(x)) {
                    counter++;
                    if (verbose) {
                        System.err.println(counter + "/" + totalttoal + " " + from.toFile().getAbsolutePath() + " x " + to.toFile().getAbsolutePath());
                    }
                    String content2 = null;
                    try {
                        content2 = Files.readString(to);
                    } catch(Exception ex) {
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
                    boolean compare1 = LevenshteinDistance.isDifferenceTolerable(content1, content2, min, verbose);
                    if (compare1) {
                        System.out.println(from.toFile().getAbsolutePath() + " == " + to.toFile().getAbsolutePath());
                    } else {
                        if (verbose) {
                            System.err.print(" similarity without spaces: ");
                        }
                        boolean compare2 = LevenshteinDistance.isDifferenceTolerable(
                                content1.replaceAll("\\s+", ""),
                                content2.replaceAll("\\s+", ""), minws,
                                verbose);
                        if (compare2) {
                            System.out.println(from.toFile().getAbsolutePath() + " == " + to.toFile().getAbsolutePath());
                        }
                    }
                }
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

            public static boolean isDifferenceTolerable(String s1, String s2, double samenessPercentage, boolean verbose) {
                return isDifferenceTolerableImpl(samenessPercentage, LevenshteinDistance.calculate(s1, s2),
                        Math.max(s1.length(), s2.length()), verbose);
            }

            public static boolean isDifferenceTolerableImpl(double samenessPercentage, int actualChanges, int totalSize,
                    boolean verbose) {
                double changesAllowed = (1.0 - samenessPercentage) * totalSize;
                if (verbose) {
                    System.err.println(100-((actualChanges*100)/totalSize)+"% <? "+samenessPercentage*100 + "%");
                }
                return actualChanges <= changesAllowed;
            }
        }

}
