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
package cryptotest.tests;

import cryptotest.utils.AlgorithmInstantiationException;
import cryptotest.utils.AlgorithmRunException;
import cryptotest.utils.AlgorithmTest;
import cryptotest.utils.TestResult;

import java.security.*;
import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;

/*
 * IwishThisCouldBeAtTest
 */
public class AlgorithmParametersTests extends AlgorithmTest {

    public static void main(String[] args) {
        TestResult r = new AlgorithmParametersTests().mainLoop();
        System.out.println(r.getExplanation());
        System.out.println(r.toString());
        r.assertItself();
    }

    @Override
    protected void checkAlgorithm(Provider.Service service, String alias) throws
            AlgorithmInstantiationException, AlgorithmRunException {
        try {
            AlgorithmParameters c = AlgorithmParameters.getInstance(alias, service.getProvider());
            AlgorithmParameterSpec params = null;
            if (service.getAlgorithm().contains("DSA")) {
                params = new DSAParameterSpec(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
            } else if (service.getAlgorithm().contains("PBEWithHmacSHA") && service.getAlgorithm().contains("AES")) {
                //nedsbooth?!?!?
                //params = new IvParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
                params = new PBEParameterSpec(new byte[]{1, 2, 3, 4}, 10);
            } else if (service.getAlgorithm().contains("PBEWithHmacSHA")) {
                params = new IvParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
            } else if (service.getAlgorithm().contains("DiffieHellman")) {
                params = new DHParameterSpec(BigInteger.ONE, BigInteger.ONE);
            } else if (service.getAlgorithm().contains("PBE")) {
                params = new PBEParameterSpec(new byte[]{1, 2, 3, 4}, 10);
            } else if (service.getAlgorithm().contains("Blowfish") || service.getAlgorithm().contains("DES")) {
                params = new IvParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
            }

            c.init(params);
            printResult(c.getEncoded());
            AlgorithmParameters c2 = AlgorithmParameters.getInstance(alias, service.getProvider());
            c2.init(c.getEncoded());

        } catch (IOException | InvalidParameterSpecException ex) {
            throw new AlgorithmInstantiationException(ex);
        } catch (Exception ex) {
            throw new AlgorithmRunException(ex);
        }

    }

    @Override
    public String getTestedPart() {
        return "AlgorithmParameters";
    }

}
