package org.bigiarini.android.moviedb.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.bigiarini.android.moviedb.MovieDbApplication;
import org.bigiarini.android.moviedb.model.MovieModel;
import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.model.ServerModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by emanuele on 28/10/13.
 */
public abstract class MoviedbService {

    private static final String TAG_LOG = MoviedbService.class.getName();

    private static final String KO_RESULT = "KO";


    public static final class MovieTransferObject {

        private boolean mError;

        /**
         * The page index of this set of data
         */
        public final int mPage;
        /**
         * The total number of page of data has the server
         */
        public final int mCountPage;
        /**
         * The set of data in this page
         */
        public final List<MovieModel> mData;

        public MovieTransferObject(final List<MovieModel> data, final int page, final int countPage, final boolean hasError) {
            this.mData = data;
            this.mPage = page;
            this.mCountPage = countPage;
            this.mError = hasError;
        }

        public static MovieTransferObject create(final List<MovieModel> data, final int page, final int countPage) {
            return  new MovieTransferObject(data, page, countPage, false);
        }

        public static MovieTransferObject createFromError(final String errorString) {
            LinkedList<MovieModel> errorList = new LinkedList<MovieModel>();
            errorList.add(MovieModel.fromError(errorString));
            return new MovieTransferObject(errorList, -1, 0, true);
        }

        public boolean hasError() {
            return mError;
        }

        public String errorString() {
            if (hasError()) {
                return mData.get(0).error;
            }
            else return null;
        }
    }

    public abstract MovieTransferObject loadMovies(final int page, final Context context, final ServerModel server);

    public abstract MovieTransferObject loadRecents(final Context context, final ServerModel server);

    public abstract MovieModel getMovie(final Context context, final ServerModel server, final long id);

    public abstract Bitmap getPoster(final Context context, final ServerModel server, final long id);



    public static final MoviedbService sMoviedbService = new SmartMoviedbService();

    private static class SmartMoviedbService extends MoviedbService {

        private static final int RECENT_MOVIES = -1;

        private ResponseHandler<MovieModel> movieModelResponseHandler =
                new ResponseHandler<MovieModel>() {

                    @Override
                    public MovieModel handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                        MovieModel movie = null;
                        ByteArrayOutputStream baos = responseToByteArray(httpResponse);
                        if (baos.size()==0) {
                            Log.d(TAG_LOG, "Empty response?!");
                        }
                        try {
                            JSONObject resultAsJson = new JSONObject(new String(baos.toByteArray()));
                            final String result =  resultAsJson.optString("result", KO_RESULT);
                            if (KO_RESULT.equals(result)) {
                                movie = MovieModel.fromError(resultAsJson.optString("error"));
                            }
                            else {
                                JSONObject movieAsJson = resultAsJson.optJSONObject("response");
                                movie = MovieModel.fromJson(movieAsJson);
                            }
                        }
                        catch (JSONException e) {
                            Log.d(TAG_LOG, new String(baos.toByteArray()));
                            e.printStackTrace();
                            movie = MovieModel.fromError("JSON error: "+e.getMessage());
                        }
                        return movie;
                    }
                };

        private ResponseHandler<MovieTransferObject> movieListResponseHandler =
                new ResponseHandler<MovieTransferObject>() {
                    @Override
                    public MovieTransferObject handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                        final List<MovieModel> data = new LinkedList<MovieModel>();
                        int currentPage=-1;
                        int countPage=0;
                        ByteArrayOutputStream baos = responseToByteArray(httpResponse);
                        Log.d(TAG_LOG, "Response code "+httpResponse.getStatusLine().getStatusCode());
                        try {
                            JSONObject resultAsJson = new JSONObject(new String(baos.toByteArray()));
                            final String result = resultAsJson.optString("result", KO_RESULT);
                            if (KO_RESULT.equals(result)) {
                                data.add(MovieModel.fromError(resultAsJson.optString("error")));
                            }
                            else {
                                JSONArray jsonArray = resultAsJson.optJSONArray("response");
                                for (int i=0; i<jsonArray.length(); i++) {
                                    MovieModel movie = MovieModel.fromJson(jsonArray.optJSONObject(i));
                                    data.add(movie);
                                }
                                currentPage = resultAsJson.optInt("current_page");
                                countPage = resultAsJson.optInt("total_count_page");
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            data.add(MovieModel.fromError("JSON error: " + e.getMessage()));
                        }
                        return MovieTransferObject.create(data, currentPage, countPage);
                    }
                };

        private ResponseHandler<Bitmap> posterResponseHandler =
                new ResponseHandler<Bitmap>() {
                    @Override
                    public Bitmap handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                        ByteArrayOutputStream baos = responseToByteArray(httpResponse);
                        if (baos.size()==0) {
                            Log.d(TAG_LOG, "Empty poster response?!");
                        }
                        byte[] buffer = baos.toByteArray();
                        return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                    }
                };


        private ByteArrayOutputStream responseToByteArray(final HttpResponse httpResponse) throws IOException {
            InputStream content = httpResponse.getEntity().getContent();
            Log.d(TAG_LOG, "Response status "+httpResponse.getStatusLine().getStatusCode()+" "+httpResponse.getStatusLine().getReasonPhrase());
            byte[] buffer = new byte[1024];
            int numRead = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((numRead=content.read(buffer))!=-1) {
                baos.write(buffer, 0, numRead);
            }
            content.close();
            httpResponse.getEntity().consumeContent();
            return baos;
        }

        @Override
        public MovieTransferObject loadMovies(int page, Context context, ServerModel server) {
            MovieTransferObject movieTransferObject;
            final String movieListUrl;
            if (page == RECENT_MOVIES) {
                movieListUrl = context.getString(R.string.get_recent_movies, server.getAddress(), server.getPort());
            }
            else {
                movieListUrl = context.getResources().getString(R.string.get_movie_list_url, server.getAddress(), server.getPort(), page);
            }
            HttpClient httpClient = MovieDbApplication.getThreadSafeHttpClient();
            HttpGet request = new HttpGet(movieListUrl);
            try {
                movieTransferObject = httpClient.execute(request, movieListResponseHandler);
            }
            catch (IOException e) {
                e.printStackTrace();
                movieTransferObject = MovieTransferObject.createFromError("Connection error: " + e.getMessage());
            }
            return movieTransferObject;
        }

        @Override
        public MovieTransferObject loadRecents(Context context, ServerModel server) {
            return loadMovies(RECENT_MOVIES, context, server);
        }

        @Override
        public MovieModel getMovie(final Context context, final ServerModel server,  long id) {
            MovieModel movie;
            final String movieUrl = context.getResources().getString(R.string.get_movie_url, server.getAddress(), server.getPort(), id);
            HttpClient httpClient = MovieDbApplication.getThreadSafeHttpClient();
            HttpGet request = new HttpGet(movieUrl);
            try {
                movie = httpClient.execute(request, movieModelResponseHandler);
            }
            catch (IOException e) {
                e.printStackTrace();
                movie = MovieModel.fromError("Connection error: "+e.getMessage());
            }
            return movie;
        }

        @Override
        public Bitmap getPoster(Context context, ServerModel server, long id) {
            Bitmap poster = null;
            final String movieUrl = context.getResources().getString(R.string.get_poster_url, server.getAddress(), server.getPort(), id);
            HttpClient httpClient = MovieDbApplication.getThreadSafeHttpClient();
            HttpGet request = new HttpGet(movieUrl);
            try {
                poster = httpClient.execute(request, posterResponseHandler);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return poster;
        }
    }

}
