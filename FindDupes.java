/**
 *
 *
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

 private static final String DEFAULT_COMMENTS="\\s*[/*\\*#].*";
 private static final String DEFAULT_EXCLUDES=".*/(Main|Test|Test1|Test2|PURPOSE|TEST.ROOT|A)\\.java$";

 public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.err.println("At least one argument necessary - directory/dir to comapre CWD agasint");
            System.err.println("Other understood args:");
            System.err.println("  will comapre only if filenames are same/similar\n"
                             + "                                --names=false/true/icase/NUMBER/");
            System.err.println("    false - not used, comapre all. true filenames must be same before comaprison. icase - like true, only9 case insensitive. NUMBER - names must be similar (percentages) ");
            System.err.println("  minimal similarity in percent --min=NUMB");
            System.err.println("  minimal similarity in percent with whitechars removed\n"
                             + "                                --minws=NUMB");
            System.err.println("Note, that min/minws should be 0-100 inclusive. Bigger/higher will effectively exclude the method.");
            System.err.println("  file path filter regex        --fitler=EXPRES");
            System.err.println("  sources blackslist            --exclude=EXPRES");
            System.err.println("    exclude matching source files form run. Eg \""+DEFAULT_EXCLUDES+"\"");
            System.err.println("  remove comment lines          --eraser[=EXPRES]");
            System.err.println("    if enabled, default is " + DEFAULT_COMMENTS);
            System.err.println("  verbose mode                  --verbose");
            System.err.println("  maximum filesize in KB        --maxsize=NUMBER");
            System.err.println("    Default is 100 (100kb), which eats about 46GB ram and taks 5-8 minutes. Biggrer files ");
             System.err.println("  maximum filesize diff ratio  --maxratio=DOUBLE");
            System.err.println("    Default is 10. Unless target/n < source < target*N then comparison will be skipped. Happens after comment removal");
            System.err.println("everything not `-` starting  is considered as dir/file which  the CWD/first file/dir should be compared against");
            throw new RuntimeException(" one ore more args expected, got zero");
        }
        File src = null;
        List<File> compares = new ArrayList<>(args.length + 1);
        double names = -100;  //-100 off, --10  same, -1 sameignorecase, 0-100 levenstain
        double min = 80;
        double minws = 90;
        double maxratio = 10;
        boolean verbose = false;
        long maxsize = 100*1024;    //100kb
        Pattern eraser = null;
        Pattern filter = Pattern.compile(".*");
        Pattern blacklist = null;
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
                    case "-exclude":
                        if (arg.contains("=")) {
                            blacklist = Pattern.compile(arg.split("=")[1]);
                        } else {
                            blacklist = Pattern.compile(DEFAULT_EXCLUDES);
                        }
                        break;
                    case "-eraser":
                        if (arg.contains("=")) {
                            eraser = Pattern.compile(arg.split("=")[1]);
                        } else {
                            eraser = Pattern.compile(DEFAULT_COMMENTS);
                        }
                        break;
                    case "-names":
                        String value=arg.split("=")[1];
                        if (value.equals("false")) {
                            names=-100d;
                        } else if (value.equals("true")) {
                            names=-10d;
                        } else if (value.equals("icase")) {
                            names=-1d;
                        } else {
                            names = Double.parseDouble(value);
                        }
                        break;
                    case "-maxratio":
                        maxratio = Double.parseDouble(arg.split("=")[1]);
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
            System.err.println("names: " + names);
            System.err.println("maxratio: " + maxratio);
            System.err.println("eraser: " + eraser);
            System.err.println("filter: " + filter);
            System.err.println("blacklist: " + blacklist);
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
                if (blacklist !=null && blacklist.matcher(from.toFile().getAbsolutePath()).matches()) {
                    throw new IOException("Input is excluded by " + blacklist.toString());
                }
                content1 = Files.readString(from);
                if (eraser!=null) {
                    Matcher matcher = eraser.matcher(content1);
                    content1 = matcher.replaceAll("");
                }
            } catch(Exception ex) {
                skips++;
                if (verbose) {
                    System.err.println("skipping " + from.toFile().getAbsolutePath() + " from binary? " + ex.getMessage());
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
                        if (names > -50d && names < -5d) {
                            //-10 same
                            if (!from.getFileName().toString().equals(to.getFileName().toString())) {
                                throw new IOException("filenames are different");
                            }
                        } else if (names > -5d && names < 0d) {
                            //-1 sameignorecase
                            if (!from.getFileName().toString().equalsIgnoreCase(to.getFileName().toString())) {
                                throw new IOException("filenames are case-insensitive different");
                            }
                        } else if (names > 0d) {
                            //0-100 levenstain
                            boolean filenamecompare = LevenshteinDistance.isDifferenceTolerable(from.getFileName().toString(), to.getFileName().toString(), names/100d, verbose, null);
                            if (!filenamecompare) {
                                throw new IOException("only similar filenames are supposed to be comapred by content. Limit is " + names);
                            }
                        }
                        content2 = Files.readString(to);
                        if (eraser!=null) {
                            Matcher matcher = eraser.matcher(content2);
                            content2 = matcher.replaceAll("");
                        }
                        if ((double)(content2.length())/maxratio > (double)(content1.length())) {
                            throw new IOException("content2 to big -  " + content2.length() + ">>"+content1.length());
                        }
                        if ((double)(content2.length())*maxratio < (double)(content1.length())) {
                            throw new IOException("content2 to small -  " + content2.length() + "<<"+content1.length());
                        }
                    } catch(Exception ex) {
                        lskips++;
                        if (verbose) {
                            System.err.println("skipping " + to.toFile().getAbsolutePath() + " to binary? " + ex.getMessage());
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
                System.err.println("   bestmatch:"  + lmaxmin[0]);
                System.err.println(" bestmatchws:"  + lmaxminws[0]);
                System.err.println("       skips:"  + lskips);
                System.err.println("                   hits total: "  + hits);
                System.err.println("                  skips total: "  + skips);
                System.err.println("   bestmatch all over session: "  + maxmin[0]);
                System.err.println(" bestmatchws all over session: "  + maxminws[0]);
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
                if (recorder!=null && percent>recorder[0]) {
                    recorder[0]=percent;
                }
                if (verbose) {
                    if (recorder!=null) {
                        System.err.println(percent+"% ?> "+samenessPercentage*100 + "% (max:" + recorder[0] + ")");
                    } else {
                        System.err.println(percent+"% ?> "+samenessPercentage*100);
                    }
                }
                boolean result = actualChanges <= changesAllowed;
                if (recorder!=null) {
                    if (result) {
                        System.out.println("  " + percent+"%: ");
                    }
                    if (verbose){
                        System.err.println("  " + percent+"%: ");
                    }
                } else if (verbose) {
                    //nasty hack
                    System.err.println(" name similarity " + percent+"%: ");
                }
                return result;
            }
        }

        public static String eta(Date start, long counter, long total)  {
            Date now = new Date();
            long tookTime = now.getTime() - start.getTime();
            double deta = (double)total/(double)counter*(double)tookTime;
            return "(run " + tookTime/1000/60+"m/eta " + (int)(deta/1000/60)+"m)";
        }
}
