
/*
 @test runtest
 @bug 2036462
 @requires var.msys2.enabled == "false"
 @summary test whether this Test.java file is buildable as is
 @run shell runtest.sh
*/

class Test {
    public static void main(String[] args) throws Exception {
        sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS initArgs = new sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS();
        initArgs.flags = 0;
        sun.security.pkcs11.wrapper.PKCS11 pkcs11 = sun.security.pkcs11.wrapper.PKCS11.getInstance("/usr/lib64/opensc-pkcs11.so", "C_GetFunctionList", initArgs, false);

        long[] slotList = pkcs11.C_GetSlotList(true);
        for (long slot : slotList) {
            sun.security.pkcs11.wrapper.CK_TOKEN_INFO tokenInfo = pkcs11.C_GetTokenInfo(slot);
            System.out.println("Slot info:\n" + tokenInfo.toString());
        }
    }
}
