package org.bigiarini.android.moviedb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;

import org.bigiarini.android.moviedb.fragment.MainFragment;
import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.model.ServerModel;

public class MainActivity extends FragmentActivity implements MainFragment.MainActivityListener {

    private static final String TAG_LOG = MainActivity.class.getName();

    private static final int SETTINGS_REQUEST_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        if (savedInstanceState==null) {
            final MainFragment fragment = new MainFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.anchor_point, fragment).commit();
        }
    }

    @Override
    public void showAllMovies() {
        final Intent intent = new Intent(this, AllMoviesActivity.class);
        startActivity(intent);
    }

    @Override
    public void showRecentMovie(long id) {
        final Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.MOVIE_ID_EXTRA, id);
        startActivity(intent);
    }

    @Override
    public void showSettings() {
        final Intent intent = new Intent(SettingsActivity.SETTINGS_ACTION);
        startActivityForResult(intent, SETTINGS_REQUEST_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST_ID) {
            switch (resultCode) {
                case RESULT_OK:
                    final ServerModel serverModel =  data.getParcelableExtra(SettingsActivity.SERVER_DATA_EXTRA);
                    Log.d(TAG_LOG, serverModel.getAddress()+":"+serverModel.getPort());
                    serverModel.save(this);
                    Button allMovieButton = (Button) findViewById(R.id.all_movie_button);
                    allMovieButton.setEnabled(true);
                    Button fileViewButton = (Button) findViewById(R.id.file_view_button);
                    fileViewButton.setEnabled(true);
            }

        }
    }
}
