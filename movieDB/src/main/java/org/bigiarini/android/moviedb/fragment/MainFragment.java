package org.bigiarini.android.moviedb.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.model.MovieModel;
import org.bigiarini.android.moviedb.model.ServerModel;
import org.bigiarini.android.moviedb.service.MoviedbService;
import org.bigiarini.android.moviedb.service.PosterService;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by emanuele on 26/10/13.
 */
public class MainFragment extends Fragment {

    private static final String TAG_LOG = MainFragment.class.getName();

    private List<MovieModel> mRecentMovieModel = new LinkedList<MovieModel>();

    public interface MainActivityListener {

        void showAllMovies();

        void showRecentMovie(long id);

        void showSettings();
    }

    private MainActivityListener mActivityListener;

    private Button mAllMovieButton;
    private Button mFileViewButton;
    private Button mSettingsButton;
    private GridView mRecentGridView;

    private BaseAdapter mRecentAdapter;
    private ServerModel mServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (areSettingsEnabled()) {
            mServer = ServerModel.load(getActivity());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivityListener) {
            mActivityListener = (MainActivityListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View MainView = inflater.inflate(R.layout.fragment_main, null);

        mAllMovieButton = (Button) MainView.findViewById(R.id.all_movie_button);
        mFileViewButton = (Button) MainView.findViewById(R.id.file_view_button);
        mSettingsButton = (Button) MainView.findViewById(R.id.settings_button);

        mAllMovieButton.setEnabled(areSettingsEnabled());
        mFileViewButton.setEnabled(areSettingsEnabled());


        mAllMovieButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActivityListener != null) {
                    Log.d(TAG_LOG, "User wants to see the list of movies.");
                    mActivityListener.showAllMovies();
                }
            }
        });

        mSettingsButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivityListener != null) {
                    Log.d(TAG_LOG, "Calling Settings Activity");
                    mActivityListener.showSettings();
                }
            }
        });

        mRecentGridView = (GridView) MainView.findViewById(R.id.recent_movie_grid);

        mRecentAdapter = new BaseAdapter() {

            class Holder {
                TextView titleTextView;
                ImageView posterView;
            }

            @Override
            public int getCount() {
                return mRecentMovieModel.size();
            }

            @Override
            public Object getItem(int position) {
                return mRecentMovieModel.get(position);
            }

            @Override
            public long getItemId(int position) {
                return mRecentMovieModel.get(position).id;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Log.d(TAG_LOG, "getview position:"+position);
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                Holder holder;
                if (convertView==null) {
                    convertView = layoutInflater.inflate(R.layout.recent_movie_item, null);
                    holder = new Holder();
                    holder.titleTextView = (TextView) convertView.findViewById(R.id.recent_movie_title);
                    holder.posterView = (ImageView) convertView.findViewById(R.id.recent_movie_poster);
                    convertView.setTag(holder);
                }
                else {
                    holder = (Holder) convertView.getTag();
                }
                /*TextView titleTextView = (TextView) convertView.findViewById(R.id.recent_movie_title);
                ImageView posterView = (ImageView) convertView.findViewById(R.id.recent_movie_poster);*/

                final MovieModel itemModel = (MovieModel) getItem(position);

                holder.titleTextView.setText(itemModel.title);

                holder.posterView.setTag(itemModel.id);
                PosterService.INSTANCE.loadPoster(itemModel.id, holder.posterView, getActivity(), mServer);

                return convertView;
            }
        };

        mRecentGridView.setAdapter(mRecentAdapter);

        mRecentGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mActivityListener.showRecentMovie(id);
            }
        });

        return MainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRecentMovieModel.isEmpty() && mServer!=null) {
            RecentMoviesAsyncTask task = new RecentMoviesAsyncTask();
            task.execute();
        }
    }

    private boolean areSettingsEnabled() {
        ServerModel servermodel = ServerModel.load(getActivity());
        return servermodel!=null;
    }


    class RecentMoviesAsyncTask extends AsyncTask<Void, Void, MoviedbService.MovieTransferObject> {

        @Override
        protected MoviedbService.MovieTransferObject doInBackground(Void... params) {
            Log.d(TAG_LOG, "Loading recent movies.");
            return MoviedbService.sMoviedbService.loadRecents(getActivity(), mServer);
        }

        @Override
        protected void onPostExecute(MoviedbService.MovieTransferObject movieTransferObject) {
            super.onPostExecute(movieTransferObject);
            if (!movieTransferObject.mData.get(0).hasError()) {
                mRecentMovieModel.clear();
                mRecentMovieModel.addAll(movieTransferObject.mData);
            }
            mRecentAdapter.notifyDataSetChanged();
        }
    }

}