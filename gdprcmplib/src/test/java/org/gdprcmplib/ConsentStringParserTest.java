package org.gdprcmplib;

import org.junit.Test;

import java.text.ParseException;
import java.util.BitSet;
import java.util.Date;

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

    @Test
    public void testSTuff() throws Exception {
        byte[] bytes = new byte[30];
        int value = 22;
        int size = 6, startInclusive = 0;
        ConsentStringParser parser = new ConsentStringParser(bytes, false);
        parser.setInt(startInclusive, size, value);
        System.out.println("Does this match?: " + parser.getInt(startInclusive, size) + " == " + value);

        BitSet b = convert(100);
        System.out.println("Converted back: " + convert(b));
    }

    @Test
    public void testBitField() throws ParseException, Base64DecoderException {
        String consentString = "BN5lERiOMYEdiAOAWeFRAAYAAaAAptQ";

        ConsentStringParser consent = new ConsentStringParser(consentString);
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