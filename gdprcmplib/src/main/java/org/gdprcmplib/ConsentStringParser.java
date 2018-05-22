package org.gdprcmplib;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a parser for the IAB consent string as specified in
 * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/
 * Draft_for_Public_Comment_Transparency%20%26%20Consent%20Framework%20-%20cookie%20and%20vendor%20list%20format%
 * 20specification%20v1.0a.pdf
 *
 *
 */

class ConsentStringParser {

    private static final String TAG = "ConsentStringParser";

    private static final int VENDOR_ENCODING_RANGE = 1;

    private static final int VERSION_BIT_OFFSET = 0;
    private static final int VERSION_BIT_SIZE = 6;
    private static final int CREATED_BIT_OFFSET = 6;
    private static final int CREATED_BIT_SIZE = 36;
    private static final int UPDATED_BIT_OFFSET = 42;
    private static final int UPDATED_BIT_SIZE = 36;
    private static final int CMP_ID_OFFSET = 78;
    private static final int CMP_ID_SIZE = 12;
    private static final int CMP_VERSION_OFFSET = 90;
    private static final int CMP_VERSION_SIZE = 12;
    private static final int CONSENT_SCREEN_SIZE_OFFSET = 102;
    private static final int CONSENT_SCREEN_SIZE = 6;
    private static final int CONSENT_LANGUAGE_OFFSET = 108;
    private static final int CONSENT_LANGUAGE_SIZE = 12;
    private static final int VENDOR_LIST_VERSION_OFFSET = 120;
    private static final int VENDOR_LIST_VERSION_SIZE = 12;
    private static final int PURPOSES_OFFSET = 132;
    private static final int PURPOSES_SIZE = 24;
    private static final int MAX_VENDOR_ID_OFFSET = 156;
    private static final int MAX_VENDOR_ID_SIZE = 16;
    private static final int ENCODING_TYPE_OFFSET = 172;
    private static final int ENCODING_TYPE_SIZE = 1;
    private static final int VENDOR_BITFIELD_OFFSET = 173;
    private static final int DEFAULT_CONSENT_OFFSET = 173;
    private static final int NUM_ENTRIES_OFFSET = 174;
    private static final int NUM_ENTRIES_SIZE = 12;
    private static final int RANGE_ENTRY_OFFSET = 186;
    private static final int VENDOR_ID_SIZE = 16;

    private String consentString;
    Bits bits;
    // fields contained in the consent string
    private int version;
    private long consentRecordCreated;
    private long consentRecordLastUpdated;
    private int cmpID;
    private int cmpVersion;
    private int consentScreenID;
    private String consentLanguage;
    private  int vendorListVersion;
    private int maxVendorId;
    private int vendorEncodingType;
    private final List<Boolean> allowedPurposes = new ArrayList<>();
    // only used when range entry is enabled
    private List<RangeEntry> rangeEntries;
    private boolean defaultConsent;

    private List<Integer> integerPurposes = null;

    private Map<Integer, GdprVendor> vendorsMap;
    private List<GdprVendor> vendors;
    private List<GdprPurpose> purposes;

    /**
     * Constructor.
     *
     * @param consentString
     *            (required). The binary user consent data encoded as url and filename safe base64 string
     *
     * @throws ParseException
     *             if the consent string cannot be parsed
     */
    public ConsentStringParser(String consentString) throws ParseException, Base64DecoderException {
        this(Base64.decodeWebSafe(consentString.getBytes()));
        this.consentString = consentString;
    }

    /**
     * Constructor
     *
     * @param bytes:
     *            the byte string encoding the user consent data
     * @throws ParseException
     *             when the consent string cannot be parsed
     */
    public ConsentStringParser(byte[] bytes) throws ParseException {
        this.bits = new Bits(bytes);
        // begin parsing

        this.version = bits.getInt(VERSION_BIT_OFFSET, VERSION_BIT_SIZE);
        this.consentRecordCreated = bits.getInstantFromEpochDemiseconds(CREATED_BIT_OFFSET, CREATED_BIT_SIZE);
        this.consentRecordLastUpdated = bits.getInstantFromEpochDemiseconds(UPDATED_BIT_OFFSET, UPDATED_BIT_SIZE);
        this.cmpID = bits.getInt(CMP_ID_OFFSET, CMP_ID_SIZE);
        this.cmpVersion = bits.getInt(CMP_VERSION_OFFSET, CMP_VERSION_SIZE);
        this.consentScreenID = bits.getInt(CONSENT_SCREEN_SIZE_OFFSET, CONSENT_SCREEN_SIZE);
        this.consentLanguage = bits.getSixBitString(CONSENT_LANGUAGE_OFFSET, CONSENT_LANGUAGE_SIZE);
        this.vendorListVersion = bits.getInt(VENDOR_LIST_VERSION_OFFSET, VENDOR_LIST_VERSION_SIZE);
        this.maxVendorId = bits.getInt(MAX_VENDOR_ID_OFFSET, MAX_VENDOR_ID_SIZE);
        this.vendorEncodingType = bits.getInt(ENCODING_TYPE_OFFSET, ENCODING_TYPE_SIZE);
        for (int i = PURPOSES_OFFSET, ii = PURPOSES_OFFSET + PURPOSES_SIZE; i < ii; i++) {
            allowedPurposes.add(bits.getBit(i));
        }
        if (vendorEncodingType == VENDOR_ENCODING_RANGE) {
            this.rangeEntries = new ArrayList<>();
            this.defaultConsent = bits.getBit(DEFAULT_CONSENT_OFFSET);
            int numEntries = bits.getInt(NUM_ENTRIES_OFFSET, NUM_ENTRIES_SIZE);
            int currentOffset = RANGE_ENTRY_OFFSET;
            for (int i = 0; i < numEntries; i++) {
                boolean range = bits.getBit(currentOffset);
                currentOffset++;
                if (range) {
                    int startVendorId = bits.getInt(currentOffset, VENDOR_ID_SIZE);
                    currentOffset += VENDOR_ID_SIZE;
                    int endVendorId = bits.getInt(currentOffset, VENDOR_ID_SIZE);
                    currentOffset += VENDOR_ID_SIZE;
                    rangeEntries.add(new RangeEntry(startVendorId, endVendorId));
                } else {
                    int vendorId = bits.getInt(currentOffset, VENDOR_ID_SIZE);
                    currentOffset += VENDOR_ID_SIZE;
                    rangeEntries.add(new RangeEntry(vendorId));
                }
            }
        }
    }

    /**
     * @return the string passes in the constructor.
     *
     */
    public String getConsentString() {
        return consentString;
    }

    /**
     * @return the time at which the consent record was created
     */
    public long getConsentRecordCreated() {
        return consentRecordCreated;
    }

    /**
     *
     * @return the time at which the cookie was last updated
     */
    public long getConsentRecordLastUpdated() {
        return consentRecordLastUpdated;
    }

    /**
     *
     * @return the version of the cookie format used in this consent string
     */
    public int getVersion() {
        return version;
    }

    /**
     *
     * @return the id of the consent management partner that created this consent string
     */
    public int getCmpId() {
        return cmpID;
    }

    /**
     * @return the version of the cmp being used
     */
    public int getCmpVersion() {
        return cmpVersion;
    }

    /**
     *
     * @return the id of the string through which the user gave consent in the CMP UI
     */
    public int getConsentScreen() {
        return consentScreenID;
    }

    /**
     * @return The two letter ISO639-1 language code in which the CMP asked for consent
     */
    public String getConsentLanguage() {
        return consentLanguage;
    }

    public int getMaxVendorId() {
        return maxVendorId;
    }

    /**
     *
     * @return a list of purpose id's which are permitted according to this consent string
     */
    public List<Integer> getAllowedPurposes() {
        if (integerPurposes != null) {
            return integerPurposes;
        }
        List<Integer> purposes = new ArrayList<Integer>();
        for (int i = 1, ii = allowedPurposes.size(); i <= ii; i++) {
            if (isPurposeAllowed(i)) {
                purposes.add(i);
            }
        }
        integerPurposes = purposes;
        return purposes;
    }

    /**
     *
     * @return the vendor list version which was used in creating this consent string
     */
    public int getVendorListVersion() {
        return vendorListVersion;
    }

    /**
     * @return a boolean describing the user consent status for a particular purpose. The lowest purpose ID is 1.
     */
    public boolean isPurposeAllowed(int purposeId) {
        if (purposeId < 1 || purposeId > allowedPurposes.size()) {
            return false;
        }
        return allowedPurposes.get(purposeId - 1);
    }

    private boolean findVendorIdInRange(int vendorId) {
        int limit = rangeEntries.size();
        if (limit == 0) {
            return false;
        }
        int index = limit / 2;
        while (index >= 0 && index < limit) {
            RangeEntry entry = rangeEntries.get(index);
            if (entry.containsVendorId(vendorId)) {
                return true;
            }
            if (index == 0 || index == limit - 1) {
                return false;
            }
            if (entry.idIsGreaterThanMax(vendorId)) {
                index = (index + ((limit - index) / 2));
            } else {
                index = index / 2;
            }
        }
        return false;
    }

    /**
     * @return a boolean describing if end-user has consented to a particular vendor. The lowest vendor ID is 1.
     *
     *         This method, along with {isPurposeAllowed} fully describes the user consent for a particular action
     *         by a given vendor.
     */
    public boolean isVendorAllowed(int vendorId) {
        if (vendorEncodingType == VENDOR_ENCODING_RANGE) {
            boolean present = findVendorIdInRange(vendorId);
            return present != defaultConsent;
        } else {
            return bits.getBit(VENDOR_BITFIELD_OFFSET + vendorId - 1);
        }
    }

    // static classes
    public static class RangeEntry {
        /**
         * This class corresponds to the RangeEntry field given in the consent string specification.
         */
        private final List<Integer> vendorIds = new ArrayList<Integer>();
        private final int maxVendorId;
        private final int minVendorId;

        public RangeEntry(int vendorId) {
            vendorIds.add(vendorId);
            this.maxVendorId = this.minVendorId = vendorId;
        }

        public RangeEntry(int startId, int endId) {
            this.maxVendorId = endId;
            this.minVendorId = startId;
            for (; startId <= endId; startId++) {
                vendorIds.add(startId);
            }
        }

        public boolean containsVendorId(int vendorId) {
            return vendorIds.indexOf(vendorId) >= 0;
        }

        public boolean idIsGreaterThanMax(int vendorId) {
            return vendorId > maxVendorId;
        }

        public boolean isIsLessThanMin(int vendorId) {
            return vendorId < minVendorId;
        }
    }

    // since java.util.BitSet is inappropriate to use here--as it reversed the bit order of the consent string--we
    // implement our own bitwise operations here.
    private static class Bits {
        // big endian
        private static final byte[] bytePows = { -128, 64, 32, 16, 8, 4, 2, 1 };
        private final byte[] bytes;

        public Bits(byte[] b) {
            this.bytes = b;
        }

        /**
         *
         * @param index:
         *            the nth number bit to get from the bit string
         * @return boolean bit, true if the bit is switched to 1, false otherwise
         */
        public boolean getBit(int index) {
            int byteIndex = index / 8;
            int bitExact = index % 8;
            byte b = bytes[byteIndex];
            return (b & bytePows[bitExact]) != 0;
        }

        /**
         * interprets n number of bits as a big endian int
         *
         * @param startInclusive:
         *            the nth to begin interpreting from
         * @param size:
         *            the number of bits to interpret
         * @return
         * @throws ParseException
         *             when the bits cannot fit in an int sized field
         */
        public int getInt(int startInclusive, int size) throws ParseException {
            if (size > Integer.SIZE) {
                throw new ParseException("can't fit bit range in int.", startInclusive);
            }
            int val = 0;
            int sigMask = 1;
            int sigIndex = size - 1;

            for (int i = 0; i < size; i++) {
                if (getBit(startInclusive + i)) {
                    val += (sigMask << sigIndex);
                }
                sigIndex--;
            }
            return val;
        }

        /**
         * interprets n bits as a big endian long
         *
         * @param startInclusive:
         *            the nth to begin interpreting from
         * @param size:the
         *            number of bits to interpret
         * @return
         * @throws ParseException
         *             when the bits cannot fit in an int sized field
         */
        public long getLong(int startInclusive, int size) throws ParseException {
            if (size > Long.SIZE) {
                throw new ParseException("can't fit bit range in long.", startInclusive);
            }
            long val = 0;
            long sigMask = 1;
            int sigIndex = size - 1;

            for (int i = 0; i < size; i++) {
                if (getBit(startInclusive + i)) {
                    val += (sigMask << sigIndex);
                }
                sigIndex--;
            }
            return val;
        }

        /**
         * returns an {@link Instant} derived from interpreting the given interval on the bit string as long
         * representing the number of demiseconds from the unix epoch
         *
         * @param startInclusive:
         *            the bit from which to begin interpreting
         * @param size:
         *            the number of bits to interpret
         * @return
         * @throws ParseException
         *             when the number of bits requested cannot fit in a long
         */
        public long getInstantFromEpochDemiseconds(int startInclusive, int size) throws ParseException {
            long epochDemi = getLong(startInclusive, size);
            //return Instant.ofEpochMilli(epochDemi * 100);
            return epochDemi * 100;
        }

        /**
         * @return the number of bits in the bit string
         *
         */
        public int length() {
            return bytes.length * 8;
        }

        /**
         * This method interprets the given interval in the bit string as a series of six bit characters, where 0=A and
         * 26=Z
         *
         * @param startInclusive:
         *            the nth bit in the bitstring from which to start the interpretation
         * @param size:
         *            the number of bits to include in the string
         * @return the string given by the above interpretation
         * @throws ParseException
         *             when the requested interval is not a multiple of six
         */
        public String getSixBitString(int startInclusive, int size) throws ParseException {
            if (size % 6 != 0) {
                throw new ParseException("string bit length must be multiple of six", startInclusive);
            }
            int charNum = size / 6;
            StringBuilder val = new StringBuilder();
            for (int i = 0; i < charNum; i++) {
                int charCode = getInt(startInclusive + (i * 6), 6) + 65;
                val.append((char) charCode);
            }
            return val.toString().toUpperCase();

        }

        /**
         *
         * @return a string representation of the byte array passed in the constructor. for example, a byte array of [4]
         *         yields a String of "0100"
         */
        public String getBinaryString() {
            StringBuilder s = new StringBuilder();
            int i = 0;
            int ii = length();
            for (; i < ii; i++) {
                if (getBit(i)) {
                    s.append("1");
                } else {
                    s.append("0");
                }
            }
            return s.toString();
        }
    }

    /**
     *
     * BEGIN WRITING TO CONSENT STRING
     */
    String encodeIntToBits(long number, int numBits) {

        String bitString = "";
        bitString = Long.toString(Long.parseLong(number+"", 10),2);

        // Pad the string if not filling all bits
        if (numBits >= bitString.length()) {
            bitString = padLeft(bitString, numBits - bitString.length());
        }

        // Truncate the string if longer than the number of bits
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

    String padLeft(String string, int padding) {
        if (padding <= 0)
            return string;
        return repeat(Math.max(0, padding)) + string;
    }

    String padRight(String string, int padding) {
        if (padding <= 0)
            return string;
        return string + repeat(Math.max(0, padding));
    }

    String encodeLetterToBits(Character letter, int numBits) {
        return encodeIntToBits(Character.toUpperCase(letter) - 65, numBits);
    }

    String encodeLanguageToBits(String language, int numBits) {
        return encodeLetterToBits(Character.valueOf(language.charAt(0)), numBits/2)
                + encodeLetterToBits(Character.valueOf(language.charAt(1)), numBits / 2);
    }

    String encodePurposesToBits() {
        StringBuilder purposesBits = new StringBuilder("000000000000000000000000"); //24
        if (purposes != null) {
            for (int i=0; i < purposes.size();i++) {
                if (purposes.get(i).isAllowed()) {
                    purposesBits.setCharAt(i, '1');
                }
            }
        }
        return purposesBits.toString();
    }

    public ConsentStringParser(int version, long createdDate, long updatedDate, int cmpId,
                               int cmpVersion, int cmpScreenNumber, String language2DigitCode,
                               int vendorListVersion) {
        this.version = version;
        this.consentRecordCreated = createdDate/100;
        this.consentRecordLastUpdated = updatedDate/100;
        this.cmpID = cmpId;
        this.cmpVersion = cmpVersion;
        this.consentScreenID = cmpScreenNumber;
        this.consentLanguage = language2DigitCode;
        this.vendorListVersion = vendorListVersion;
    }

    /*
     * https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/Consent%20string%20and%20vendor%20list%20formats%20v1.1%20Final.md
     */
    public String getEncodedConsentString() throws Exception {

        consentRecordLastUpdated = new Date().getTime() / 100;
        String bitString = encodeIntToBits(version, 6); //Incremented when consent string format changes
        bitString += encodeIntToBits(consentRecordCreated, 36); //created
        bitString += encodeIntToBits(consentRecordLastUpdated, 36); //last updated
        bitString += encodeIntToBits(cmpID, 12); //cmpID
        bitString += encodeIntToBits(cmpVersion, 12); //cmpVersion
        bitString += encodeIntToBits(consentScreenID, 6); //screen number in CMP where consent given
        bitString += encodeLanguageToBits(consentLanguage, 12); //2-digit language code CMP asked for content in
        bitString += encodeIntToBits(vendorListVersion, 12); //vendor list version used in most recent consent string update
        bitString += encodePurposesToBits();  //24 purposes
        bitString += encodeIntToBits(maxVendorId, 16); //max vendor id
        bitString += encodeIntToBits(vendorEncodingType, 1);
        if (vendorEncodingType == VENDOR_ENCODING_RANGE) {
            bitString += defaultConsent ? "1" : "0";
            bitString += encodeIntToBits(rangeEntries.size(), 12);
            for (int i=0;i < rangeEntries.size();i++) {
                RangeEntry rangeEntry = rangeEntries.get(i);
                if (rangeEntry.vendorIds.size() == 1) {
                    bitString += "0"; //single vendor id
                    bitString += encodeIntToBits(rangeEntry.vendorIds.get(0), 16); //single vendor id
                    bitString += encodeIntToBits(0, 16);
                    bitString += encodeIntToBits(0, 16);
                } else {
                    bitString += "1";  //vendor id range
                    bitString += encodeIntToBits(0, 16);
                    bitString += encodeIntToBits(rangeEntry.minVendorId, 16);  //start vendor id
                    bitString += encodeIntToBits(rangeEntry.maxVendorId, 16);  //end vendor id
                }
            }
        } else {
            for (int i=1;i <= maxVendorId;i++) {
                if (vendorsMap.containsKey(i)) {
                    bitString += (vendorsMap.get(i).isAllowed() ? "1" : "0");
                } else {
                    bitString += "0";
                }
            }
        }

        System.out.println("bitString: " + bitString);

        String paddedBinaryValue = padRight(bitString, 7 - ((bitString.length() + 7) % 8));
        for (int i=0; i < bitString.length();i++) {
            //System.out.println("paddedBinaryValue i: "+i + "   "+bitString.charAt(i));
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
//        System.out.println(finalString);
//        ConsentStringParser parser = new ConsentStringParser(finalString);
//        System.out.println("create date: "+new Date(parser.getConsentRecordCreated()));
//        System.out.println("update date: "+new Date(parser.getConsentRecordLastUpdated()));
//        System.out.println("getVersion: "+parser.getVersion());
//        System.out.println("getCmpVersion: "+parser.getCmpVersion());
//        System.out.println("getCmpId: "+parser.getCmpId());
//        System.out.println("getConsentScreen: "+parser.getConsentScreen());
//        System.out.println("language: "+parser.getConsentLanguage());
//        System.out.println("vendor list version: "+parser.getVendorListVersion());
//        for (int i=0;i < 24;i++) {
//            System.out.println("is purpose allowed... "+(i+1) + ": "+parser.isPurposeAllowed(i+1));
//        }
//        System.out.println("max vendor size: "+parser.getMaxVendorId());
        return finalString;
    }

    /**
     *
     * @param vendorEncodingType 1 = range, 0: bitfield
     */
    public void setVendorEncodingType(int vendorEncodingType) {
        this.vendorEncodingType = vendorEncodingType;
    }

    public void addRangeEntry(RangeEntry rangeEntry) {
        if (rangeEntries == null) {
            rangeEntries = new ArrayList<>();
        }
        rangeEntries.add(rangeEntry);
    }

    public List<RangeEntry> getRangeEntries() {
        return rangeEntries;
    }

    public void setVendors(List<GdprVendor> vendors) {
        this.vendors = vendors;
        this.vendorsMap = new HashMap<>(this.vendors.size());
        Collections.sort(this.vendors);
        maxVendorId = this.vendors.get(this.vendors.size()-1).getId();
        for (int i=0;i<this.vendors.size();i++) {
            GdprVendor vendor = this.vendors.get(i);
            this.vendorsMap.put(vendor.getId(),vendor);
        }
    }

    public void setPurposes(List<GdprPurpose> purposes) {
        allowedPurposes.clear();
        this.purposes = purposes;
        Collections.sort(purposes);
        for (int i=0;i < purposes.size();i++) {
            GdprPurpose purpose = purposes.get(i);
            allowedPurposes.add(purpose.isAllowed());
        }
    }

    public void setDefaultConsent(boolean defaultConsent) {
        this.defaultConsent = defaultConsent;
    }
}