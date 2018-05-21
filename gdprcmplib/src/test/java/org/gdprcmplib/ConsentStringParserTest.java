package org.gdprcmplib;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConsentStringParserTest {

    public static BitSet convert(int value) {
        BitSet bits = new BitSet();
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

    public static int convert(BitSet bits) {
        int value = 0;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1 << i) : 0;
        }
        return value;
    }

    String encodeIntToBits(int number, int numBits) {
        String bitString = "";
        bitString = Integer.toString(Integer.parseInt(number+"", 10),2);
        int padding = numBits - bitString.length();
        bitString = padLeft(bitString, padding);
        if (bitString.length() > numBits) {
            bitString = bitString.substring(0, numBits);
        }
        return bitString;
    }

    String repeat(int count) {
        String padString = "";
        for (int i=0;i < count;i++) {
            padString += "0";
        }
        return padString;
    }

    String  padLeft(String string, int padding) {
        if (padding <= 0)
            return string;
        return repeat(Math.max(0, padding)) + string;
    }

    String padRight(String string, int padding) {
        if (padding <= 0)
            return string;
      return string + repeat(Math.max(0, padding));
    }

    @Test
    public void testSTuff() throws Exception {

        String s = encodeIntToBits(22, 12);
        System.out.println("["+s+"]");
        s = padLeft(s, 78);
        s = padRight(s, 200);

        String paddedBinaryValue = padRight(s, 7 - ((s.length() + 7) % 8));

        for (int i=0; i < s.length();i++) {
            System.out.println("paddedBinaryValue i: "+i + "   "+s.charAt(i));
        }

        List<Byte> list = new ArrayList<>();

        for (int i=0; i < paddedBinaryValue.length(); i += 8) {
            byte b = (byte)Character.toChars(Integer.parseInt(paddedBinaryValue.substring(i, i+8), 2))[0];
            list.add(b);
        }
        byte bytes[] = new byte[list.size()];
        for (int i=0;i < list.size();i++) {
            bytes[i] = list.get(i);
        }

        String finalString = Base64.encodeWebSafe(bytes, true);

        System.out.println(finalString);

        ConsentStringParser parser = new ConsentStringParser(finalString);
        System.out.println(parser.getCmpId());
        for (int i=0;i< parser.bits.length();i++) {
            System.out.println("asdf i: "+i + "   " + (parser.bits.getBit(i) ? "1" : "0"));
        }
    }

    @Test
    public void testBitField() throws ParseException, Base64DecoderException {
        String consentString = "BN5lERiOMYEdiAOAWeFRAAYAAaAAptQ";

        ConsentStringParser consent = new ConsentStringParser(consentString);
        System.out.println(consent.getCmpVersion());
        for (int i=0; i < consent.bits.length();i++) {
            //System.out.println("test i: "+i+ "  bit: "+(consent.bits.getBit(i)?"1":"0"));
        }
        assertEquals(14, consent.getCmpId());
        assertEquals(22, consent.getCmpVersion());
        assertEquals("FR", consent.getConsentLanguage());
        assertEquals(14924661858L * 100, consent.getConsentRecordCreated());
        assertEquals(15240021858L * 100, consent.getConsentRecordLastUpdated());
        assertEquals(5, consent.getAllowedPurposes().size());
        assertTrue(consent.isPurposeAllowed(2));
        assertFalse(consent.isPurposeAllowed(1));
        assertTrue(consent.isPurposeAllowed(21));
        assertTrue(consent.isVendorAllowed(1));
        assertTrue(consent.isVendorAllowed(5));
        assertTrue(consent.isVendorAllowed(7));
        assertTrue(consent.isVendorAllowed(9));
        assertFalse(consent.isVendorAllowed(0));
        assertFalse(consent.isVendorAllowed(10));
        assertEquals(consentString, consent.getConsentString());
    }

    @Test
    public void testRangeEntryNoConsent() throws ParseException, Base64DecoderException {
        String consentString = "BN5lERiOMYEdiAKAWXEND1HoSBE6CAFAApAMgBkIDIgM0AgOJxAnQA==";

        ConsentStringParser consent = new ConsentStringParser(consentString);
        System.out.println("version: "+consent.getVersion());
        assertEquals(10, consent.getCmpId());
        assertEquals(22, consent.getCmpVersion());
        assertEquals("EN", consent.getConsentLanguage());
        assertEquals(14924661858L * 100, consent.getConsentRecordCreated());

        System.out.println(new Date(consent.getConsentRecordCreated()));
        System.out.println(new Date(consent.getConsentRecordLastUpdated()));

        assertEquals(15240021858L * 100, consent.getConsentRecordLastUpdated());
        assertEquals(8, consent.getAllowedPurposes().size());
        assertTrue(consent.isPurposeAllowed(4));
        assertFalse(consent.isPurposeAllowed(1));
        assertTrue(consent.isPurposeAllowed(24));
        assertFalse(consent.isPurposeAllowed(25));
        assertFalse(consent.isPurposeAllowed(0));
        assertFalse(consent.isVendorAllowed(1));
        assertFalse(consent.isVendorAllowed(3));
        assertTrue(consent.isVendorAllowed(225));
        assertTrue(consent.isVendorAllowed(5000));
        assertTrue(consent.isVendorAllowed(515));
        assertFalse(consent.isVendorAllowed(0));
        assertFalse(consent.isVendorAllowed(3244));
        assertEquals(consentString, consent.getConsentString());

    }

    @Test
    public void testRangeEntryConsent() throws ParseException, Base64DecoderException {
        String consentString = "BONZt-1ONZt-1AHABBENAO-AAAAHCAEAASABmADYAOAAeA";
        ConsentStringParser consent = new ConsentStringParser(consentString);

        assertTrue(consent.isPurposeAllowed(1));
        assertTrue(consent.isPurposeAllowed(3));
        assertTrue(consent.isVendorAllowed(28));
        assertFalse(consent.isVendorAllowed(1));
        assertFalse(consent.isVendorAllowed(3));
        assertTrue(consent.isVendorAllowed(27));
    }

}