package org.bigiarini.android.moviedb.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.bigiarini.android.moviedb.fragment.MovieDetailFragment;
import org.bigiarini.android.moviedb.R;

/**
 * Created by emanuele on 30/10/13.
 */
public class MovieDetailActivity extends FragmentActivity {

    public static final String MOVIE_ID_EXTRA = "org.bigiarini.android.moviedb.extra.MOVIE_ID_EXTRA";

    private long mMovieId;

    private boolean mTwoPane;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        mMovieId = getIntent().getLongExtra(MOVIE_ID_EXTRA, 0);

        mTwoPane = findViewById(R.id.right_anchor) != null;

        if (mTwoPane) {
            finish();
        }

        if (savedInstanceState == null) {
            final MovieDetailFragment fragment = MovieDetailFragment.getMovieDetailFragment(mMovieId);
            getSupportFragmentManager().beginTransaction().add(R.id.anchor_point,fragment).commit();
        }
    }
}