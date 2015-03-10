
/**
 * @test
 * @bug 7082299
 * @run main/othervm AtomicRefObjectArray
 * @summary  AtomicReferenceArray should ensure that array is Object[]
 * @author Chris Hegarty
 */

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// Run in 'othervm' as it will crash the vm if the bug exists.
// Only reproduces on server vm. No specific vm arg passed to the test
// since test batches are run in both client or server.

public class AtomicRefObjectArray {
    
    static Object obj = new Item(1);
    static boolean canRunNow;
    static boolean failed;

    public static void main(String[] args) throws Exception {        
        // if any arg is passed just generate the serial data
        if (args.length != 0) {
            generateSerialData();
            return;
        }
        
        constructorTest();
        
        serialTest(JDK7SerialData, "JDK7");
        serialTest(JDK6SerialData, "JDK6");
        if (failed)
            throw new RuntimeException("Some tests failed, check output");
    }
    
    // Items to be referenced by AtomicReferenceArray
    static class Item implements Serializable {
        // value to use for trivial checking of serialized instance
        static final int FIELD_VALUE = 56;
        int intField;
        Item (int value) { intField = value; }
    }
    
    static void constructorTest() {
        // warm up the compiler, get it to compile access and create
        for (int i=0; i<100000; i++) {
            access(create());
        }
        
        obj = "foo";
        canRunNow = true;
        
        try {
            access(create());
        } catch (ClassCastException e) {
            // ok, this is what we expect
            // vm will crash if the test fails!
        }
    }

    static Item create() {
        AtomicReferenceArray atomicArray = new AtomicReferenceArray(new Item[1]);
        atomicArray.set(0, obj);
        return (Item)atomicArray.get(0);
    }

    static void access(Item item) {
        if (canRunNow) {
            System.out.println(item.intField);
            item.intField += 8;
            System.out.println("foo");
        }
    }
    
    // Verify consistency of deserializing different serial forms
    static void serialTest(byte[] serialData, String versionStr) {
        try {
            ObjectInputStream oin = new ObjectInputStream(
                    new ByteArrayInputStream(serialData));
        
            AtomicReferenceArray atomicArray = (AtomicReferenceArray)oin.readObject();
            oin.close();
        
            Item item = (Item) atomicArray.get(0);
            if (item.intField != Item.FIELD_VALUE) {
                System.out.println("Error checking " + versionStr +
                        " serial data, intField:" + item.intField +
                        ", expected :" + Item.FIELD_VALUE);
                failed = true;
            }
        } catch (Exception e) {
            System.out.println("Exception caught during deserialization " + versionStr);
            failed = true;
            e.printStackTrace();
        }
    }
    
    // Used to generate serialData included in this test (below).
    //
    // To generate the 7 version of the serial data this needs to be run with
    // JDK7 FCS, 7u1, or 7u2. Any JDK6 should be sufficient, but FCS was
    // used in this case.
    static void generateSerialData() throws IOException {
        Item[] itemArray = new Item[] { new Item(Item.FIELD_VALUE) };
        AtomicReferenceArray atomicArray = new AtomicReferenceArray(itemArray);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(atomicArray);
        oos.close();

        byte[] ba = bos.toByteArray();
        System.out.format("static final byte[] SerialData = {\n");
        for (int i=0; i<ba.length; i++) {
            System.out.format(" (byte)0x%02X", ba[i] );
            if (i != (ba.length-1))
                System.out.format(",");
            if (((i + 1) % 6) == 0)
                System.out.format("\n");
        }
        System.out.format(" };\n");
    }
    
    // --- Generated data ---
    static final byte[] JDK7SerialData = {
        (byte)0xAC, (byte)0xED, (byte)0x00, (byte)0x05, (byte)0x73, (byte)0x72,
        (byte)0x00, (byte)0x30, (byte)0x6A, (byte)0x61, (byte)0x76, (byte)0x61,
        (byte)0x2E, (byte)0x75, (byte)0x74, (byte)0x69, (byte)0x6C, (byte)0x2E,
        (byte)0x63, (byte)0x6F, (byte)0x6E, (byte)0x63, (byte)0x75, (byte)0x72,
        (byte)0x72, (byte)0x65, (byte)0x6E, (byte)0x74, (byte)0x2E, (byte)0x61,
        (byte)0x74, (byte)0x6F, (byte)0x6D, (byte)0x69, (byte)0x63, (byte)0x2E,
        (byte)0x41, (byte)0x74, (byte)0x6F, (byte)0x6D, (byte)0x69, (byte)0x63,
        (byte)0x52, (byte)0x65, (byte)0x66, (byte)0x65, (byte)0x72, (byte)0x65,
        (byte)0x6E, (byte)0x63, (byte)0x65, (byte)0x41, (byte)0x72, (byte)0x72,
        (byte)0x61, (byte)0x79, (byte)0xA9, (byte)0xD2, (byte)0xDE, (byte)0xA1,
        (byte)0xBE, (byte)0x65, (byte)0x60, (byte)0x0C, (byte)0x02, (byte)0x00,
        (byte)0x01, (byte)0x5B, (byte)0x00, (byte)0x05, (byte)0x61, (byte)0x72,
        (byte)0x72, (byte)0x61, (byte)0x79, (byte)0x74, (byte)0x00, (byte)0x13,
        (byte)0x5B, (byte)0x4C, (byte)0x6A, (byte)0x61, (byte)0x76, (byte)0x61,
        (byte)0x2F, (byte)0x6C, (byte)0x61, (byte)0x6E, (byte)0x67, (byte)0x2F,
        (byte)0x4F, (byte)0x62, (byte)0x6A, (byte)0x65, (byte)0x63, (byte)0x74,
        (byte)0x3B, (byte)0x78, (byte)0x70, (byte)0x75, (byte)0x72, (byte)0x00,
        (byte)0x1C, (byte)0x5B, (byte)0x4C, (byte)0x41, (byte)0x74, (byte)0x6F,
        (byte)0x6D, (byte)0x69, (byte)0x63, (byte)0x52, (byte)0x65, (byte)0x66,
        (byte)0x4F, (byte)0x62, (byte)0x6A, (byte)0x65, (byte)0x63, (byte)0x74,
        (byte)0x41, (byte)0x72, (byte)0x72, (byte)0x61, (byte)0x79, (byte)0x24,
        (byte)0x49, (byte)0x74, (byte)0x65, (byte)0x6D, (byte)0x3B, (byte)0xCA,
        (byte)0x16, (byte)0xDB, (byte)0xC9, (byte)0xF2, (byte)0xD8, (byte)0xE1,
        (byte)0x7B, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x73, (byte)0x72,
        (byte)0x00, (byte)0x19, (byte)0x41, (byte)0x74, (byte)0x6F, (byte)0x6D,
        (byte)0x69, (byte)0x63, (byte)0x52, (byte)0x65, (byte)0x66, (byte)0x4F,
        (byte)0x62, (byte)0x6A, (byte)0x65, (byte)0x63, (byte)0x74, (byte)0x41,
        (byte)0x72, (byte)0x72, (byte)0x61, (byte)0x79, (byte)0x24, (byte)0x49,
        (byte)0x74, (byte)0x65, (byte)0x6D, (byte)0xE2, (byte)0x34, (byte)0xAA,
        (byte)0x39, (byte)0x2D, (byte)0xD0, (byte)0xE3, (byte)0x54, (byte)0x02,
        (byte)0x00, (byte)0x01, (byte)0x49, (byte)0x00, (byte)0x08, (byte)0x69,
        (byte)0x6E, (byte)0x74, (byte)0x46, (byte)0x69, (byte)0x65, (byte)0x6C,
        (byte)0x64, (byte)0x78, (byte)0x70, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x38 };
    
    static final byte[] JDK6SerialData = {
        (byte)0xAC, (byte)0xED, (byte)0x00, (byte)0x05, (byte)0x73, (byte)0x72,
        (byte)0x00, (byte)0x30, (byte)0x6A, (byte)0x61, (byte)0x76, (byte)0x61,
        (byte)0x2E, (byte)0x75, (byte)0x74, (byte)0x69, (byte)0x6C, (byte)0x2E,
        (byte)0x63, (byte)0x6F, (byte)0x6E, (byte)0x63, (byte)0x75, (byte)0x72,
        (byte)0x72, (byte)0x65, (byte)0x6E, (byte)0x74, (byte)0x2E, (byte)0x61,
        (byte)0x74, (byte)0x6F, (byte)0x6D, (byte)0x69, (byte)0x63, (byte)0x2E,
        (byte)0x41, (byte)0x74, (byte)0x6F, (byte)0x6D, (byte)0x69, (byte)0x63,
        (byte)0x52, (byte)0x65, (byte)0x66, (byte)0x65, (byte)0x72, (byte)0x65,
        (byte)0x6E, (byte)0x63, (byte)0x65, (byte)0x41, (byte)0x72, (byte)0x72,
        (byte)0x61, (byte)0x79, (byte)0xA9, (byte)0xD2, (byte)0xDE, (byte)0xA1,
        (byte)0xBE, (byte)0x65, (byte)0x60, (byte)0x0C, (byte)0x02, (byte)0x00,
        (byte)0x01, (byte)0x5B, (byte)0x00, (byte)0x05, (byte)0x61, (byte)0x72,
        (byte)0x72, (byte)0x61, (byte)0x79, (byte)0x74, (byte)0x00, (byte)0x13,
        (byte)0x5B, (byte)0x4C, (byte)0x6A, (byte)0x61, (byte)0x76, (byte)0x61,
        (byte)0x2F, (byte)0x6C, (byte)0x61, (byte)0x6E, (byte)0x67, (byte)0x2F,
        (byte)0x4F, (byte)0x62, (byte)0x6A, (byte)0x65, (byte)0x63, (byte)0x74,
        (byte)0x3B, (byte)0x78, (byte)0x70, (byte)0x75, (byte)0x72, (byte)0x00,
        (byte)0x13, (byte)0x5B, (byte)0x4C, (byte)0x6A, (byte)0x61, (byte)0x76,
        (byte)0x61, (byte)0x2E, (byte)0x6C, (byte)0x61, (byte)0x6E, (byte)0x67,
        (byte)0x2E, (byte)0x4F, (byte)0x62, (byte)0x6A, (byte)0x65, (byte)0x63,
        (byte)0x74, (byte)0x3B, (byte)0x90, (byte)0xCE, (byte)0x58, (byte)0x9F,
        (byte)0x10, (byte)0x73, (byte)0x29, (byte)0x6C, (byte)0x02, (byte)0x00,
        (byte)0x00, (byte)0x78, (byte)0x70, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x01, (byte)0x73, (byte)0x72, (byte)0x00, (byte)0x19, (byte)0x41,
        (byte)0x74, (byte)0x6F, (byte)0x6D, (byte)0x69, (byte)0x63, (byte)0x52,
        (byte)0x65, (byte)0x66, (byte)0x4F, (byte)0x62, (byte)0x6A, (byte)0x65,
        (byte)0x63, (byte)0x74, (byte)0x41, (byte)0x72, (byte)0x72, (byte)0x61,
        (byte)0x79, (byte)0x24, (byte)0x49, (byte)0x74, (byte)0x65, (byte)0x6D,
        (byte)0xE2, (byte)0x34, (byte)0xAA, (byte)0x39, (byte)0x2D, (byte)0xD0,
        (byte)0xE3, (byte)0x54, (byte)0x02, (byte)0x00, (byte)0x01, (byte)0x49,
        (byte)0x00, (byte)0x08, (byte)0x69, (byte)0x6E, (byte)0x74, (byte)0x46,
        (byte)0x69, (byte)0x65, (byte)0x6C, (byte)0x64, (byte)0x78, (byte)0x70,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x38 };
        
}
