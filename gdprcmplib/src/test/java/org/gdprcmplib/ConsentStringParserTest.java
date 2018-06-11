package org.gdprcmplib;

import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConsentStringParserTest {

    List<GdprPurpose> getNewPurposes(boolean isAllow) {
        final int numTestPurposes = 12;
        List<GdprPurpose> purposes = new ArrayList<>(numTestPurposes);
        for (int i=0;i < numTestPurposes;i++) {
            GdprPurpose p = new GdprPurpose(i+1, "test purpose "+(i+1), "some description "+(i+1));
            p.setAllowed(isAllow);
            purposes.add(p);
        }
        return purposes;
    }

    List<GdprVendor> getNewVendors() {
        final int numTestVendors = 120;
        List<GdprVendor> vendors = new ArrayList<>(numTestVendors);
        for (int i=0;i < numTestVendors;i++) {
            GdprVendor v = new GdprVendor(i+1, "test vendor "+(i+1), "http://privacy.com/html"+(i+1));
            v.setAllowed(true);
            vendors.add(v);
        }
        return vendors;
    }

    String decode(String enc) throws Exception {
        StringBuilder dec = new StringBuilder();
        byte[] out = Base64.decodeWebSafe(enc);
        for (int j=0,i=out.length-1;i >= 0;i--,j++) {
            dec.append((char)(out[i]+65));
        }
        System.out.println("out "+dec.toString());
        return dec.toString();
    }

    @Test
    public void testMyString() throws Exception {
        String s = "BOOWgK2OOWgK2ABABBENAf____AACABgACArIA==";
        ConsentStringParser consentStringParser = new ConsentStringParser(s);
        System.out.println("getVersion "+ consentStringParser.getVersion());
        System.out.println("getCmpVersion "+ consentStringParser.getCmpVersion());
        System.out.println("getAllowedPurposes "+ consentStringParser.getAllowedPurposes());
        System.out.println("getCmpId "+ consentStringParser.getCmpId());
        System.out.println("getConsentLanguage "+ consentStringParser.getConsentLanguage());
        System.out.println("getConsentRecordCreated "+ consentStringParser.getConsentRecordCreated());
        System.out.println("getConsentRecordLastUpdated "+ consentStringParser.getConsentRecordLastUpdated());
        System.out.println("getConsentScreen "+ consentStringParser.getConsentScreen());
        System.out.println("getMaxVendorId "+ consentStringParser.getMaxVendorId());
        System.out.println("getRangeEntries "+ consentStringParser.getRangeEntries());
        System.out.println("getDefaultConsent "+ consentStringParser.getDefaultConsent());
        System.out.println("range "+ consentStringParser.getRangeEntries().get(0).getMinVendorId() + " " + consentStringParser.getRangeEntries().get(0).getMaxVendorId());
        System.out.println("getVendorEncodingType "+ consentStringParser.getVendorEncodingType());
        System.out.println("getVendorListVersion "+ consentStringParser.getVendorListVersion());
        System.out.println("is vendor 100 allowed "+ consentStringParser.isVendorAllowed(100));
    }

    String encode(String string) throws Exception {
        byte bytes[] = new byte[string.length()];
        for (int j=0,i=string.length()-1;i >= 0;i--,j++) {
            bytes[j] = (byte)(string.charAt(i) - 65);
        }
        String enc = Base64.encodeWebSafe(bytes,false);
        System.out.println("key "+enc);
        return enc;
    }

    @Test
    public void testEncodePackageName() throws Exception {

        String packageName = "org.gdprcmp";
        int arr[] = ConsentStringParser.arr;
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<arr.length;i++) {
            sb.append((char)(arr[i]+50));
        }
        System.out.println(sb.toString());
        String enc = encode(packageName);
        System.out.println("encoded: "+enc);
        System.out.println("decoded: "+decode(enc));
    }

    @Test
    public void testMyStuff() throws Exception {
        final long created = 14924661858L * 100;
        final long updated = 15240021858L * 100;
        ConsentStringParser parser = new  ConsentStringParser(1, created,
                updated, 20, 13, 4,
                "EN", 5);
        parser.setVendorEncodingType(0);
        List<GdprPurpose> purposes = getNewPurposes(true);
        List<GdprVendor> vendors = getNewVendors();
        parser.setPurposes(purposes);
        parser.setVendors(vendors);
        String consent = parser.getEncodedConsentString();
        System.out.println("fabulous rangeConsent string: "+consent);
        ConsentStringParser newParser = new ConsentStringParser(consent);
        assertEquals(created, newParser.getConsentRecordCreated());
        assertEquals(updated, newParser.getConsentRecordLastUpdated());
        assertEquals(1, newParser.getVersion());
        assertEquals(20, newParser.getCmpId());
        assertEquals(13, newParser.getCmpVersion());
        assertEquals(4, newParser.getConsentScreen());
        assertEquals("EN", newParser.getConsentLanguage());
        assertEquals(5, newParser.getVendorListVersion());
        for (int i=0;i < purposes.size();i++) {
            assertEquals(purposes.get(i).isAllowed(), newParser.isPurposeAllowed(i+1));
        }
        for (int i=0;i < vendors.size();i++) {
            assertEquals(vendors.get(i).isAllowed(), newParser.isVendorAllowed(i+1));
        }
        assertEquals(vendors.get(vendors.size()-1).getId(), newParser.getMaxVendorId());
        assertEquals(true, parser.isPurposeAllowed(1));

        parser.setPurposes(getNewPurposes(false));
        assertEquals(false, parser.isPurposeAllowed(1));

    }

    @Test
    public void testBitField() throws ParseException, Base64DecoderException, Exception {
        final String consentString = "BN5lERiOMYEdiAOAWeFRAAYAAaAAptQ";
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

        String myConsentString = consent.getEncodedConsentString();
        consent = new ConsentStringParser(myConsentString);
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
        assertEquals(myConsentString, consent.getConsentString());
    }

    @Test
    public void testRangeEntryNoConsent() throws ParseException, Base64DecoderException, Exception {
        final String consentString = "BN5lERiOMYEdiAKAWXEND1HoSBE6CAFAApAMgBkIDIgM0AgOJxAnQA==";
        ConsentStringParser consent = new ConsentStringParser(consentString);
        assertEquals(10, consent.getCmpId());
        assertEquals(22, consent.getCmpVersion());
        assertEquals("EN", consent.getConsentLanguage());
        assertEquals(14924661858L * 100, consent.getConsentRecordCreated());
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
        String myConsentString = consent.getEncodedConsentString();
        consent = new ConsentStringParser(myConsentString);
        assertEquals(10, consent.getCmpId());
        assertEquals(22, consent.getCmpVersion());
        assertEquals("EN", consent.getConsentLanguage());
        assertEquals(14924661858L * 100, consent.getConsentRecordCreated());
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
        assertEquals(myConsentString, consent.getConsentString());
    }

    @Test
    public void testRangeEntryConsent() throws ParseException, Base64DecoderException, Exception {
        final String consentString = "BONZt-1ONZt-1AHABBENAO-AAAAHCAEAASABmADYAOAAeA";
        ConsentStringParser consent = new ConsentStringParser(consentString);
        assertTrue(consent.isPurposeAllowed(1));
        assertTrue(consent.isPurposeAllowed(3));
        assertTrue(consent.isVendorAllowed(28));
        assertFalse(consent.isVendorAllowed(1));
        assertFalse(consent.isVendorAllowed(3));
        assertTrue(consent.isVendorAllowed(27));
        String myConsentString = consent.getEncodedConsentString();
        consent = new ConsentStringParser(myConsentString);
        assertTrue(consent.isPurposeAllowed(1));
        assertTrue(consent.isPurposeAllowed(3));
        assertTrue(consent.isVendorAllowed(28));
        assertFalse(consent.isVendorAllowed(1));
        assertFalse(consent.isVendorAllowed(3));
        assertTrue(consent.isVendorAllowed(27));
        assertEquals(myConsentString, consent.getConsentString());
    }

}