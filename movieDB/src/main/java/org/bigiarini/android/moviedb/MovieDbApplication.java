package org.bigiarini.android.moviedb;

import android.app.Application;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bigiarini.android.moviedb.net.ThreadSafeHttpClientFactory;

/**
 * Created by emanuele on 04/11/13.
 */
public class MovieDbApplication extends Application {

    public static HttpClient getThreadSafeHttpClient() {
        return ThreadSafeHttpClientFactory.INSTANCE.getThreadSafeHttpClient();
    }

    public static HttpClient getHttpClient() {
        return new DefaultHttpClient();
    }

    public static void releaseThreadSafeHttpClient() {
        ThreadSafeHttpClientFactory.INSTANCE.release();
    }

}
