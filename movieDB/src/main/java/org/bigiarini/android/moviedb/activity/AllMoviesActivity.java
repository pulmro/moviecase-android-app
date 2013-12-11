package org.bigiarini.android.moviedb.activity;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import org.bigiarini.android.moviedb.fragment.AllMoviesFragment;
import org.bigiarini.android.moviedb.fragment.MovieDetailFragment;
import org.bigiarini.android.moviedb.R;

public class AllMoviesActivity extends FragmentActivity implements AllMoviesFragment.AllMoviesListener {

    private static final String TAG_LOG = AllMoviesActivity.class.getName();

    /*private static final int DETAIL_ACTIVITY_REQUEST = 1;*/

    private long mCurrentId;

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allmovies);

        mTwoPane = (findViewById(R.id.right_anchor) != null);

        if (savedInstanceState == null) {
            final AllMoviesFragment fragment = new AllMoviesFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.anchor_point, fragment).commit();
        }

        if(mTwoPane) {
            MovieDetailFragment rightFragment = MovieDetailFragment.getMovieDetailFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.right_anchor, rightFragment).commit();
        }
        Log.d(TAG_LOG, "AllMoviesActivity created.");
    }

    @Override
    public void movieSelected(long id) {
        if(mTwoPane) {
            MovieDetailFragment rightFragment = MovieDetailFragment.getMovieDetailFragment(id);
            getSupportFragmentManager().beginTransaction().replace(R.id.right_anchor, rightFragment).commit();
            mCurrentId = id;
        }
        else {
            final Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.MOVIE_ID_EXTRA, id);
            startActivity(intent);
        }
    }

}