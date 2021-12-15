/*
 * @test
 * @bug 1688571
 * @summary Tests new jap era, based on test from 1677511
 * @compile -encoding UTF-8 ReiwaNewEraTest.java
 * @run main/othervm ReiwaNewEraTest
 * @requires jdk.version.major >= 8
 */

import java.text.*;
import java.util.*;
import java.time.LocalDate;
import java.time.chrono.*;
import java.time.format.*;

public class ReiwaNewEraTest {

    public static void main(String... args) throws Exception {

        boolean fail = false;
        {
            // ### Traditional java.util.Date/Calendar API"
            String apiName = "Traditional java.util.Date/Calendar API";

            Locale locale = Locale.forLanguageTag("ja-JP-u-ca-japanese");
            // Locale locale = new Locale("ja", "JP", "JP");
            DateFormat fullKanjiFormat = new SimpleDateFormat("GGGGy年M月d日", locale);
            DateFormat shortFormat = new SimpleDateFormat("Gyy.MM.dd", locale);

            Date lastHeiseiDate = new Calendar.Builder().setDate(2019, Calendar.APRIL, 30).build().getTime();
            String lastHeiseiDateStringFull = fullKanjiFormat.format(lastHeiseiDate).toString();
            if (!lastHeiseiDateStringFull.equals("平成31年4月30日")) {
                System.err.printf("%s: expected %s, got %s\n", apiName, "平成31年4月30日", lastHeiseiDateStringFull);
                fail = true;
            }
            String lastHeiseiDateStringShort = shortFormat.format(lastHeiseiDate).toString();
            if (!lastHeiseiDateStringShort.equals("H31.04.30") && !lastHeiseiDateStringShort.equals("平成31.04.30")) {
                System.err.printf("%s: expected %s, got %s\n", apiName, "H31.04.30", lastHeiseiDateStringShort);
                fail = true;
            }

            Date firstReiwaDate = new Calendar.Builder().setDate(2019, Calendar.MAY, 1).build().getTime();
            String firstReiwaDateStringFull = fullKanjiFormat.format(firstReiwaDate).toString();
            if (!firstReiwaDateStringFull.equals("令和1年5月1日")) {
                System.err.printf("%s: expected %s, got %s\n", apiName, "令和1年5月1日", firstReiwaDateStringFull);
                fail = true;
            }
            String firstReiwaDateStringShort = shortFormat.format(firstReiwaDate).toString();
            if (!firstReiwaDateStringShort.equals("R01.05.01") && !firstReiwaDateStringShort.equals("令和01.05.01")) {
                System.err.printf("%s: expected %s, got %s\n", apiName, "R01.05.01", firstReiwaDateStringShort);
                fail = true;
            }
        }

        {
            // "### Java 8 Date and Time API"
            String apiName = "Java 8 Date and Time API";

            DateTimeFormatter fullKanjiFormat = DateTimeFormatter.ofPattern("GGGGy年M月d日", Locale.JAPAN);
            DateTimeFormatter shortFormat = DateTimeFormatter.ofPattern("GGGGGyy.MM.dd", Locale.JAPAN);

            JapaneseDate lastHeiseiDate = JapaneseDate.of(2019, 4, 30);
            String lastHeiseiDateStringFull = fullKanjiFormat.format(lastHeiseiDate).toString();
            if (!lastHeiseiDateStringFull.equals("平成31年4月30日")) {
                System.err.printf("%s: expected %s, got %s\n", apiName, "平成31年4月30日", lastHeiseiDateStringFull);
                fail = true;
            }
            String lastHeiseiDateStringShort = shortFormat.format(lastHeiseiDate).toString();
            if (!lastHeiseiDateStringShort.equals("H31.04.30")) {
                System.err.printf("%s: expected %s, got %s\n", apiName, "H31.04.30", lastHeiseiDateStringShort);
                fail = true;
            }

            JapaneseDate firstReiwaDate = JapaneseDate.of(2019, 5, 1);
            String firstReiwaDateStringFull = fullKanjiFormat.format(firstReiwaDate).toString();
            if (!firstReiwaDateStringFull.equals("令和1年5月1日")) {
                System.err.printf("%s: expected %s, got %s\n", apiName, "令和1年5月1日", firstReiwaDateStringFull);
                fail = true;
            }
            String firstReiwaDateStringShort = shortFormat.format(firstReiwaDate).toString();
            if (!firstReiwaDateStringShort.equals("R01.05.01")) {
                System.err.printf("%s: expected %s, got %s\n", apiName, "R01.05.01", firstReiwaDateStringShort);
                fail = true;
            }
        }

        if (fail) {
            System.exit(1);
        }
    }
}
