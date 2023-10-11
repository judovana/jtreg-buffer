/*
 * Copyright (c) 2017, Red Hat, Inc. and/or its affiliates.
 * 
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
 * @modules jdk.crypto.ec/sun.security.ec
 * @bug 1486025
 * @summary ECC with NSS concurrency JVM crash
 * @run main/othervm/timeout=1600 Main
 * @author Martin Balao (mbalao@redhat.com)
 */

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.PrivateKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;


import sun.security.ec.SunEC;

public class Main {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(getNistP256Params());
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey priv = (PrivateKey) keyPair.getPrivate();

        byte[] messageBytes = "Hello".getBytes("UTF-8");        
        for (int i = 0; i < 10000; i++) {
            try {
                SunEC sunECProvider = new SunEC();
                Signature signer = Signature.getInstance("SHA256WithECDSA", sunECProvider);
                signer.initSign(priv);
                signer.update(messageBytes);
                signer.sign();
            } catch (Exception e) {
                // Keep going, next will be a JVM crash!
            }
        }
        
        System.out.println("TEST PASS - OK");
    }
 
    // Code from wycheproof project
    // https://github.com/google/wycheproof
    public static ECParameterSpec getNistP256Params() {
        return getNistCurveSpec(
            "115792089210356248762697446949407573530086143415290314195533631308867097853951",
            "115792089210356248762697446949407573529996955224135760342422259061068512044369",
            "5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b",
            "6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296",
            "4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5");
    }
    
    // Code from wycheproof project
    // https://github.com/google/wycheproof
    public static ECParameterSpec getNistCurveSpec(String decimalP, String decimalN, String hexB, 
            String hexGX, String hexGY) {
        final BigInteger p = new BigInteger(decimalP);
        final BigInteger n = new BigInteger(decimalN);
        final BigInteger three = new BigInteger("3");
        final BigInteger a = p.subtract(three);
        final BigInteger b = new BigInteger(hexB, 16);
        final BigInteger gx = new BigInteger(hexGX, 16);
        final BigInteger gy = new BigInteger(hexGY, 16);
        final int h = 1;
        ECFieldFp fp = new ECFieldFp(p);
        java.security.spec.EllipticCurve curveSpec = new java.security.spec.EllipticCurve(fp, a, b);
        ECPoint g = new ECPoint(gx, gy);
        ECParameterSpec ecSpec = new ECParameterSpec(curveSpec, g, n, h);
        return ecSpec;
    }
}
