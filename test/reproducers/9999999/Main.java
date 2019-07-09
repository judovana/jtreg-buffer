/*
 * Copyright (c) 2019, Red Hat, Inc.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 9999999
 * @summary JDK-8218854 FontMetrics.getMaxAdvance may be less than the maximum FontMetrics.charWidth
 * @requires jdk.version.major >= 8
 * @requires os.arch != "aarch64"
 * @run main/othervm Main
 */

import java.awt.*;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        FontMetrics fm;
        Container container = new Container();
        int[] width;
        Font f = Font.createFont(Font.TRUETYPE_FONT,
                new File(System.getProperty("test.src", "src")
                + File.separator + "Z003-MediumItalic.otf"));
        f = f.deriveFont(Font.BOLD | Font.ITALIC, 1);
        fm = container.getFontMetrics(f);
        int maxWidth = fm.getMaxAdvance();
        if (maxWidth != -1) {
            System.out.println("fm.charWidth(37): " + fm.charWidth(37));
            width = fm.getWidths();
            System.out.println("width.length: " + width.length);
            for (int j = 0; j < width.length; j++) {
                System.out.println("width[ " + j + "]: " + width[j]);
            }
            for (int j = 0; j < width.length; j++) {
                if (width[j] > maxWidth) {
                    throw new Exception(
                            "FAILED: getMaxAdvance: is not max. " +
                                    "component: " + container.toString() +
                                    " for font: " + f.toString() +
                                    " getMaxAdvance(): " + maxWidth +
                                    " getWidths()[" + j + "]: " + width[j]);
                }
            }
        }
        System.out.println("OK - TEST PASSED");
    }
}
