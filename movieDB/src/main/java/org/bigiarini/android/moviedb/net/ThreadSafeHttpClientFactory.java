package org.bigiarini.android.moviedb.net;

import android.util.Log;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

/**
 * Created by emanuele on 04/11/13.
 */
public enum ThreadSafeHttpClientFactory {

    INSTANCE;

    private static final String TAG_LOG = ThreadSafeHttpClientFactory.class.getName();

    private static final int TIMEOUT = 60000;

    private static final String HTTP_SCHEMA = "http";

    private static final int HTTP_PORT = 80;

    private HttpClient httpClient;

    private ThreadSafeHttpClientFactory() {
        httpClient = createHttpClient();
    }

    public HttpClient getThreadSafeHttpClient() {
        if (httpClient == null) {
            httpClient = createHttpClient();
        }
        return httpClient;
    }

    public void release() {
        httpClient = null;
    }

    private HttpClient createHttpClient() {
        HttpParams httpParams = new BasicHttpParams();
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, HTTP.DEFAULT_CONTENT_CHARSET);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        Scheme httpScheme = new Scheme(HTTP_SCHEMA, PlainSocketFactory.getSocketFactory(), HTTP_PORT);
        schemeRegistry.register(httpScheme);

        ClientConnectionManager tsConnManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        HttpClient tmpClient = new DefaultHttpClient(tsConnManager, httpParams);
        HttpConnectionParams.setSoTimeout(tmpClient.getParams(), TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(tmpClient.getParams(), TIMEOUT);
        addUserAgent(tmpClient);
        return tmpClient;
    }

    private void addUserAgent(HttpClient client) {
        String userAgent = System.getProperty("http.agent");
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    }
}
