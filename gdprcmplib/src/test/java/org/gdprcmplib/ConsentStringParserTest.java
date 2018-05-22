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


    @Test
    public void testSTuff() throws Exception {
        ConsentStringParser parser = new  ConsentStringParser(1, new Date().getTime(),
                new Date().getTime(), 20, 13, 4,
                "EN", 5);
        List<GdprPurpose> purposes = new ArrayList<>(5);
        for (int i=0;i < 5;i++) {
            GdprPurpose p = new GdprPurpose(i+1, "test purpose "+(i+1), "some description "+(i+1));
            p.setAllowed(true);
            purposes.add(p);
        }
        List<GdprVendor> vendors = new ArrayList<>(5);
        for (int i=0;i < 5;i++) {
            GdprVendor v = new GdprVendor(i+1, "test vendor "+(i+1), "http://privacy.com/html"+(i+1));
            v.setAllowed(true);
            vendors.add(v);
        }
        parser.setVendorEncodingType(0);
        parser.setPurposes(purposes);
        parser.setVendors(vendors);
        String consent = parser.getEncodedConsentString();
        System.out.println("fabulous consent string: "+consent);
        ConsentStringParser newParser = new ConsentStringParser(consent);
        System.out.println("create date: "+new Date(newParser.getConsentRecordCreated()));
        System.out.println("update date: "+new Date(newParser.getConsentRecordLastUpdated()));
        System.out.println("getVersion: "+newParser.getVersion());
        System.out.println("getCmpVersion: "+newParser.getCmpVersion());
        System.out.println("getCmpId: "+newParser.getCmpId());
        System.out.println("getConsentScreen: "+newParser.getConsentScreen());
        System.out.println("language: "+newParser.getConsentLanguage());
        System.out.println("vendor list version: "+newParser.getVendorListVersion());
        for (int i=0;i < 24;i++) {
            System.out.println("is purpose allowed... "+(i+1) + ": "+newParser.isPurposeAllowed(i+1));
        }
        System.out.println("max vendor size: "+newParser.getMaxVendorId());
    }

    @Test
    public void testBitField() throws ParseException, Base64DecoderException {
        String consentString = "BN5lERiOMYEdiAOAWeFRAAYAAaAAptQ";

        ConsentStringParser consent = new ConsentStringParser(consentString);
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