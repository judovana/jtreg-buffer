
/*
 * Test delegated to shell to make the assert more direct
 * test
 * bug 1897602
 * summary Warnings when using ManagementFactory.getPlatformMBeanServer with -Xcheck:jni VM argument
 * run main/othervm -Xcheck:jni TestDiagnosticCommandImpl
 */

public class TestDiagnosticCommandImpl {
	public static void main(String[] args) {
		System.out.println(java.lang.management.ManagementFactory.getPlatformMBeanServer());
	}
}

