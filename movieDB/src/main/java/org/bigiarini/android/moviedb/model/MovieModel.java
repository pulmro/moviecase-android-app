package org.bigiarini.android.moviedb.model;

import android.text.TextUtils;

import org.json.JSONObject;

/**
 * Created by emanuele on 28/10/13.
 */
public final class MovieModel {

    public final long id;

    public final String title;
    public final String director;
    public final String cast;
    public final String overview;
    public final long runtime;
    public final long year;
    public final String imagefile;

    public String error;

    private MovieModel(final long id, final String title, final String director,
                       final String cast, final String overview, final long runtime,
                       final long year, final String imagefile) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.cast = cast;
        this.overview = overview;
        this.runtime = runtime;
        this.year = year;
        this.imagefile = imagefile;
    }

    private MovieModel(final String error) {
        this.error = error;
        id=-1;title =""; director=""; cast=""; overview=""; runtime=0; year=0; imagefile="";
    }

    public static MovieModel create(final long id, final String title, final String director,
                                    final String cast, final String overview, final long runtime,
                                    final long year, final String imagefile) {

        return new MovieModel(id, title, director, cast, overview, runtime, year, imagefile);
    }

    public static MovieModel fromJson(final JSONObject jsonObject) {
        if (jsonObject != null) {
            return new MovieModel(jsonObject.optLong("movieid"), jsonObject.optString("title"),
                    jsonObject.optString("director"), jsonObject.optString("cast", ""),
                    jsonObject.optString("overview", ""), jsonObject.optLong("runtime", 0),
                    jsonObject.optLong("year", 0), jsonObject.optString("imagefile", ""));
        }
        else return MovieModel.fromError("No movie");

    }

    public static MovieModel fromError(final String error) {
        return new MovieModel(error);
    }

    public boolean hasError() {
        return !TextUtils.isEmpty(error);
    }

}
