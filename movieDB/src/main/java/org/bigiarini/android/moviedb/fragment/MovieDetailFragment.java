package org.bigiarini.android.moviedb.fragment;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.model.MovieModel;
import org.bigiarini.android.moviedb.model.ServerModel;
import org.bigiarini.android.moviedb.service.MoviedbService;
import org.bigiarini.android.moviedb.service.PosterService;

/**
 * Created by emanuele on 30/10/13.
 */
public class MovieDetailFragment extends Fragment {

    private static final String MOVIE_KEY = "org.bigiarini.android.moviedb.key.MOVIE_KEY";
    private static final long NO_DETAIL = 0L;

    private TextView titleTextView;
    private TextView directorTextView;
    private TextView castTextView;
    private TextView runtimeTextView;
    private TextView overviewTextView;
    private ImageView posterImageView;
    private TextView errorTextView;

    private ServerModel mServer;
    private Long mID = NO_DETAIL;


    public static MovieDetailFragment getMovieDetailFragment(long id) {
        final MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putLong(MOVIE_KEY,id);
        fragment.setArguments(args);
        return fragment;
    }

    public static MovieDetailFragment getMovieDetailFragment() {
        return new MovieDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null && getArguments().containsKey(MOVIE_KEY)) {
            mID = getArguments().getLong(MOVIE_KEY);
        }
        mServer = ServerModel.load(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        titleTextView = (TextView) contentView.findViewById(R.id.titleTextView);
        directorTextView = (TextView) contentView.findViewById(R.id.directorTextView);
        castTextView = (TextView) contentView.findViewById(R.id.castTextView);
        runtimeTextView = (TextView) contentView.findViewById(R.id.runtimeTextView);
        overviewTextView = (TextView) contentView.findViewById(R.id.overviewTextView);
        posterImageView = (ImageView) contentView.findViewById(R.id.imageView);
        errorTextView = (TextView) contentView.findViewById(R.id.errorTextView);
        if (mID != NO_DETAIL) {
            errorTextView.setVisibility(View.INVISIBLE);
        }
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mID != NO_DETAIL) {
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute(mID);
            posterImageView.setTag(mID);
            PosterService.INSTANCE.loadPoster(mID, posterImageView, getActivity(), mServer);
        }
    }

    private class MovieAsyncTask extends AsyncTask<Long, Void, MovieModel> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            errorTextView.setText("Loading...");
            errorTextView.setVisibility(View.VISIBLE);
        }

        @Override
        protected MovieModel doInBackground(Long... params) {
            MovieModel movie = MoviedbService.sMoviedbService.getMovie(getActivity(), mServer, params[0]);
            return movie;
        }

        @Override
        protected void onPostExecute(MovieModel model) {
            super.onPostExecute(model);

            if (!model.hasError()) {
                titleTextView.setText(model.title);
                directorTextView.setText(model.director);
                castTextView.setText(model.cast);
                runtimeTextView.setText(getResources().getString(R.string.runtime_text_view, model.runtime));
                overviewTextView.setText(model.overview);
                errorTextView.setVisibility(View.INVISIBLE);
            }
            else {
                errorTextView.setText(model.error);
            }

        }
    }
}