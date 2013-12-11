package org.bigiarini.android.moviedb.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.bigiarini.android.moviedb.model.MovieViewHolder;
import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.model.MovieAdapter;
import org.bigiarini.android.moviedb.model.MovieModel;
import org.bigiarini.android.moviedb.model.ServerModel;
import org.bigiarini.android.moviedb.service.MoviedbService;
import org.bigiarini.android.moviedb.service.PosterService;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by emanuele on 29/10/13.
 */
public class AllMoviesFragment extends ListFragment {

    private static final String TAG_LOG = AllMoviesFragment.class.getName();

    private AllMoviesListener mListener;
    private ServerModel mServer;
    private MovieAdapter mAdapter;
    private List<MovieModel> mModel = new LinkedList<MovieModel>();
    private boolean mTwoPane;
    private int currentPage = 1;
    private int totalPageCount = 0;

    private int mLatestScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    private ProgressDialog mProgressDialog;
    private EditText mFilterEdit;
    private TextView mEmptyTextView;

    public interface AllMoviesListener {
        void movieSelected(long id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mServer = ServerModel.load(getActivity());

        Log.d(TAG_LOG, "MovieListFragment created.");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AllMoviesListener) {
            mListener = (AllMoviesListener) activity;
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTwoPane = getActivity().findViewById(R.id.right_anchor) != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_list_movies, null);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Wait while loading movie list...");
        mFilterEdit = (EditText) getActivity().findViewById(R.id.filterEdit);

        /*
        mFilterEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mAdapter.getFilter().filter(s.toString());
            }
        });*/
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new MovieAdapter(mModel, getActivity());
        setListAdapter(mAdapter);
        mEmptyTextView = (TextView) getListView().getEmptyView();
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            private int visibleThreshold = 5;
            private int previousTotal = 0;
            private boolean loading = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //mAdapter.setPosterDownloadAllowed(scrollState != SCROLL_STATE_FLING);
                if (mLatestScrollState == SCROLL_STATE_FLING && scrollState != SCROLL_STATE_FLING) {
                    mAdapter.setPosterDownloadAllowed(false);
                    updatePosters();
                }
                else if (scrollState == SCROLL_STATE_FLING) {
                    mAdapter.setPosterDownloadAllowed(false);
                }
                if (scrollState == SCROLL_STATE_IDLE) {
                    mAdapter.setPosterDownloadAllowed(true);
                }
                mLatestScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        if (previousTotal != 0) {
                            currentPage++;
                        }
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount-visibleItemCount) <= (firstVisibleItem + visibleThreshold) && currentPage<totalPageCount) {
                    new AllMoviesAsyncTask().execute(currentPage+1);
                    loading = true;
                }
            }
        });
        Log.d(TAG_LOG, "MovieListFragment started.");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mModel.isEmpty()) {
            updateList();
        }
        Log.d(TAG_LOG, "MovieListFragment resumed.");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mListener.movieSelected(id);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_activity2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_update_list) {
            updateList();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateList() {
        AllMoviesAsyncTask task = new AllMoviesAsyncTask();
        task.execute(currentPage);
    }

    private void updatePosters() {
        ListView listView = getListView();
        final int count = listView.getChildCount();
        Log.d(TAG_LOG, "listView.getChildCount() -> "+count);
        for (int i = 0; i < count; i++) {
            final View view = listView.getChildAt(i);
            final MovieViewHolder holder = (MovieViewHolder) view.getTag();
            if (!holder.loadedPoster) {
                final Long id = (Long) holder.posterImageView.getTag();
                PosterService.INSTANCE.loadPoster(id, holder.posterImageView, getActivity(), mServer, true);
                Log.d(TAG_LOG, "Updated Poster id: "+id);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    private class AllMoviesAsyncTask extends AsyncTask<Integer, Void, MoviedbService.MovieTransferObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected MoviedbService.MovieTransferObject doInBackground(Integer... params) {
            final MoviedbService.MovieTransferObject result = MoviedbService.sMoviedbService.loadMovies(params[0], getActivity(), mServer);
            Log.d(TAG_LOG, "Loading page "+params[0]);
            return result;
        }

        @Override
        protected void onPostExecute(MoviedbService.MovieTransferObject movieTransferObject) {
            super.onPostExecute(movieTransferObject);
            //mModel.clear();
            if (!movieTransferObject.hasError()) {
                /*
                Load the detail for the first movie in the list if it's in two pane and loading first page.
                 */
                if (mTwoPane && mModel.isEmpty()) {
                    long first_detail_id = movieTransferObject.mData.get(0).id;
                    mListener.movieSelected(first_detail_id);
                }
                mModel.addAll(movieTransferObject.mData);
                totalPageCount = movieTransferObject.mCountPage;

            }
            else {
                mEmptyTextView.setText(movieTransferObject.errorString());
            }

            //mAdapter.updateStoredData();
            mAdapter.notifyDataSetChanged();
            mProgressDialog.dismiss();
        }
    }
}