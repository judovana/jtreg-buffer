/*
 * @test
 * @bug 2021263
 * @summary - problem can happen when internal security classes are created by reflection
 */

public class Pkcs11Issue {
    public static void main(String[] args) throws Exception {
        Class.forName("sun.security.pkcs11.SunPKCS11");
    }
}


