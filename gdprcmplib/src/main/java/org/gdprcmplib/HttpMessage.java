package org.gdprcmplib;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

final class HttpMessage {

    private static final String TAG = "HttpMessage";

    private URL url = null;
    private boolean isSecure = false;
    private static volatile boolean sIsInitialized = false;
    private static final String UTF8 = "UTF-8";
    private static final int BUFFERED_READER_SIZE = 8192;
    private static final int HTTP_TIMEOUT = 15000;

    /**
     * Constructs a new HttpMessage that can be used to communicate with the
     * servlet at the specified URL.
     *
     * @param url the server resource (typically a servlet) with which to
     *            communicate
     */

    public HttpMessage(final String url) {
        try {
            this.url = new URL(url);
            if (url.startsWith("https://")) {
                isSecure = true;
            }
        } catch (final MalformedURLException e) {
        }
    }

    /**
     * Designed for performance; converts the returned data into an int. Of
     * course, the returned data better be an int!
     *
     * @return
     * @throws Exception
     */
    public int getInt() throws Exception {
        final URLConnection urlConnection = openConnection();
        assignCommonAttributesToHttpURLConnection(urlConnection, true, false);
        ((HttpURLConnection) urlConnection).setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "text/plain;charset=utf-8");
        final BufferedReader is = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()), 32);
        final int someInt = Integer.parseInt(is.readLine());
        is.close();
        return someInt;
    }

    /**
     * Could be receiving some potentially large returns, so make sure the
     * return is gzipped
     *
     * @return
     * @throws Exception
     */
    public String getString() throws Exception {
        final URLConnection urlConnection = openConnection();
        assignCommonAttributesToHttpURLConnection(urlConnection, true, false);
        ((HttpURLConnection) urlConnection).setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "text/plain;charset=utf-8");
        urlConnection.setRequestProperty("Accept-Encoding", "gzip");
        final InputStream is = urlConnection.getInputStream();
        final boolean isGzip = responseIsGzip(urlConnection);
        final InputStream ois = isGzip ? new GZIPInputStream(is) : is;
        MLog.d(TAG,"getString() isGzip? "+isGzip);
        final String string = getString(ois);
        is.close();
        return string;
    }

    public JSONObject getJSONObject() throws Exception {
        return new JSONObject(getString());
    }

    public byte[] getBytes() throws Exception {
        final URLConnection urlConnection = openConnection();
        assignCommonAttributesToHttpURLConnection(urlConnection, true, false);
        ((HttpURLConnection) urlConnection).setRequestMethod("GET");
        urlConnection.setRequestProperty("Content-Type", "application/octet-stream");
        final InputStream is = urlConnection.getInputStream();
        final byte[] bytes = getBytes(is);
        is.close();
        return bytes;
    }

    private byte[] getBytes(final InputStream is) throws IOException {

        int len = 0;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] buf = new byte[4096];
        while ((len = is.read(buf, 0, buf.length)) != -1)
            bos.write(buf, 0, len);
        return bos.toByteArray();
    }

    private URLConnection openConnection() throws IOException {
        initializeSSL();
        return isSecure ? openSecureConnection() : url.openConnection();
    }

    /**
     * To open secure connection, use this
     *
     * @return
     * @throws Exception
     */
    private URLConnection openSecureConnection() throws IOException {
        return (HttpsURLConnection) url.openConnection();
    }

    /**
     * Apparently, since this is not based on the HttpDefaultClient
     * implementation we do not need to collect the certificates from the
     * server.
     */
    private static void setDefaultHostnameVerifier() throws Exception {

        // Install the all-trusting trust manager:
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @SuppressWarnings("unused")
            public boolean isServerTrusted(X509Certificate[] certs) {
                return true;
            }

            @SuppressWarnings("unused")
            public boolean isClientTrusted(X509Certificate[] certs) {
                return true;
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
        }};

        final SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        //HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
    }

    private void assignCommonAttributesToHttpURLConnection(final URLConnection urlConnection, final boolean doInput, final boolean doOutput) {
        urlConnection.setUseCaches(false);

		/*
         * the following lines mess stuff up a lot especially in later versions
		 * of android ics
		 */
        if (doInput)
            urlConnection.setDoInput(true);

        if (doOutput)
            urlConnection.setDoOutput(true);

        urlConnection.setConnectTimeout(HTTP_TIMEOUT);
        urlConnection.setReadTimeout(HTTP_TIMEOUT);
    }

    private String getString(final InputStream is) throws Exception {

        final StringBuilder sb = new StringBuilder(BUFFERED_READER_SIZE);
        final byte buf[] = new byte[BUFFERED_READER_SIZE];
        int read;
        while ((read = is.read(buf)) != -1) {
            sb.append(new String(buf, 0, read, UTF8));
        }
        return sb.toString();
    }

    private boolean responseIsGzip(final URLConnection urlConnection) {

        final Map<String, List<String>> responseHeaders = (Map<String, List<String>>) urlConnection.getHeaderFields();
        if (responseHeaders != null) {
            final Iterator<String> i = responseHeaders.keySet().iterator();
            while (i.hasNext()) {
                final String key = i.next();
                // System.out.println("header key = " + key);
                final List<String> values = responseHeaders.get(key);
                for (final String value : values) {
                    // System.out.println("\t\theader value = " + value);
                    if (value.equals("gzip")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static void initializeSSL() {
        if (sIsInitialized) {
            return;
        }
        try {
            //setDefaultHostnameVerifier();
            sIsInitialized = true;
        } catch (final Exception e) {
            MLog.e(TAG, "", e);
        }
    }

}
