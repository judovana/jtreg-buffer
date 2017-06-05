/*   Copyright (C) 2017 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */
package cryptotest.utils;

import cryptotest.Settings;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class AlgorithmTest {

    protected List<Exception> failedInits = new ArrayList<>();
    protected List<Exception> failedRuns = new ArrayList<>();
    protected int algorithmsSeen = 0;
    protected int testsCount = 0;
    protected Provider provider;
    protected Provider.Service service;
    protected String currentAlias;
    protected String currentTitle;
    private boolean run;

    public abstract String getTestedPart();

    protected abstract void checkAlgorithm() throws AlgorithmInstantiationException, AlgorithmRunException;

    protected List<String> createNames() {
        return Misc.createNames(service);
    }

    protected String generateTitle() {
        return Misc.generateTitle(testsCount, provider, service, currentAlias);

    }

    public TestResult doTest() {
        return mainLoop();
    }

    protected TestResult mainLoop() {
        if (run) {
            throw new RuntimeException("Thsi test already run. Make new instance");
        }
        System.out.println("running: " + this.getClass().getName());
        run = true;
        Provider[] providers = Security.getProviders();
        //dont convert to for loop, we need global field here
        for (int i = 0; i < providers.length; i++) {
            provider = providers[i];
            List<Provider.Service> sevices = new ArrayList<>(provider.getServices());
            //dont convert to for loop, we need global field here
            for (int j = 0; j < sevices.size(); j++) {
                service = sevices.get(j);
                //we can test each instance by its name or by its alias. Stills etup is doen only by name, as from aliases it si very hard to be guessed
                List<String> names = Misc.createNames(service);
                //dont convert to for loop, we need global field here
                for (int k = 0; k < names.size(); k++) {
                    algorithmsSeen++;
                    currentAlias = names.get(k);
                    try {
                        if (service.getType().equals(getTestedPart())) {
                            currentTitle = generateTitle();
                            System.out.println(currentTitle);
                            testsCount++;
                            checkAlgorithm();
                            System.out.println("Passed");
                        }
                    } catch (AlgorithmRunException ex) {
                        failedRuns.add(new Exception(currentTitle, ex));
                        System.out.println(ex);
                        System.out.println("failed to init: " + service.getAlgorithm() + "from " + provider);
                        System.out.println("Failed");
                        if (Settings.VerbositySettings.printStacks) {
                            System.err.println(currentTitle);
                            ex.printStackTrace();
                        }
                    } catch (AlgorithmInstantiationException ex) {
                        failedInits.add(new Exception(currentTitle, ex));
                        System.out.println(ex);
                        System.out.println("Failed to use: " + service.getAlgorithm() + " from " + provider);
                        System.out.println("Failed");
                        if (Settings.VerbositySettings.printStacks) {
                            System.err.println(currentTitle);
                            ex.printStackTrace();
                        }
                    }
                }

            }
        }
        int failed = (failedInits.size() + failedRuns.size());
        TestResult.AlgorithmTestResult r;
        if (failed == 0) {
            r = TestResult.AlgorithmTestResult.pass("All " + getTestedPart() + " passed", this.getClass(), testsCount, algorithmsSeen);
        } else {

            String expl = failed + " " + getTestedPart() + " failed\n";
            expl = expl + "** failed runs: " + failedRuns.size() + " **\n";
            for (Exception ex : failedRuns) {
                StringWriter stack = new StringWriter();
                ex.printStackTrace(new PrintWriter(stack));
                expl += stack.toString();
            }
            expl = expl + "** failed inits: " + failedInits.size() + " **\n";
            for (Exception ex : failedInits) {
                StringWriter stack = new StringWriter();
                ex.printStackTrace(new PrintWriter(stack));
                expl += stack.toString();
            }
            r = TestResult.AlgorithmTestResult.fail(expl, this.getClass(), testsCount, failed, algorithmsSeen);

        }
        return r;
    }

    public static void printResult(String s) {
        if (Settings.VerbositySettings.printResults) {
            System.out.println(s);
        }
    }

    public static void printResult(int i) {
        printResult("[" + i + "]");
    }

    public static void printResult(byte[] res) {
        printResult(Arrays.toString(res));
    }

}
