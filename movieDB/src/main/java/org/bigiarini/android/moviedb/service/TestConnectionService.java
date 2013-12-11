package org.bigiarini.android.moviedb.service;

import android.content.Context;
import android.text.TextUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.bigiarini.android.moviedb.MovieDbApplication;
import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.model.ServerModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by emanuele on 04/11/13.
 */
public final class TestConnectionService {

    private static final String TAG_LOG = TestConnectionService.class.getName();

    private static final String KO_RESULT = "KO";
    private static final String MOVIEDB_SERVICE = "MovieDb";

    private static TestConnectionService instance;

    public synchronized static TestConnectionService get() {
        if (instance==null) {
            instance = new TestConnectionService();
        }
        return instance;
    }

    private ResponseHandler<ServerModel.ServerState> mResponseHandler =
            new ResponseHandler<ServerModel.ServerState>() {
                @Override
                public ServerModel.ServerState handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    ServerModel.ServerState response;
                    InputStream content = httpResponse.getEntity().getContent();
                    byte[] buffer = new byte[1024];
                    int numRead = 0;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while ((numRead=content.read(buffer))!=-1) {
                        baos.write(buffer, 0, numRead);
                    }
                    content.close();
                    try {
                        JSONObject resultAsJson = new JSONObject(new String(baos.toByteArray()));
                        final String result = resultAsJson.optString("result", KO_RESULT);
                        if (KO_RESULT.equals(result)) {
                            response = ServerModel.createState(-1).withMessage(resultAsJson.optString("error"));
                        }
                        else {
                            final String service = resultAsJson.optString("service");
                            if (MOVIEDB_SERVICE.equals(service)) {
                                response = ServerModel.createState(1).withVersion(resultAsJson.optString("version"));
                            }
                            else {
                                response = ServerModel.createState(-1).withMessage("Are you sure this is a MovieDb server?");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        response = ServerModel.createState(-1).withMessage("JSON error: " + e.getMessage());
                    }
                    return response;
                }
            };

    public ServerModel testServer(final Context context, final String address, final int port) {
        ServerModel server = ServerModel.create(address).withPort(port);
        ServerModel.ServerState state = null;
        final String testUrl = context.getResources().getString(R.string.server_test_url, address, port );
        HttpClient httpClient = MovieDbApplication.getThreadSafeHttpClient();
        HttpGet request = new HttpGet(testUrl);
        try {
            state = httpClient.execute(request, mResponseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            state = ServerModel.createState(-1).withMessage("Connection error: " + e.getMessage());
        }
        server.setState(state);
        return server;
    }
}
