package org.bigiarini.android.moviedb.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.service.PosterService;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by emanuele on 07/11/13.
 */
public class MovieAdapter extends BaseAdapter implements Filterable {

    // This is the reference to the model of the fragment/activity that uses this adapter
    private List<MovieModel> mStoredModel;
    // This is the copy of the model for internal use (filtering, etc.)
    private List<MovieModel> mModel;// = new LinkedList<MovieModel>();
    private Context context;
    private MovieFilter mFilter;
    private ServerModel mServer;
    private Drawable mDefaultPoster;

    private boolean mIsGoodDownloadingPosters = true;


    public MovieAdapter(List<MovieModel> model, Context context) {
        this.mModel = model;
        this.context = context;
        this.mServer = ServerModel.load(context);
        this.mDefaultPoster = context.getResources().getDrawable(R.drawable.ic_launcher);
        //this.mModel.addAll(model);
    }

    @Override
    public int getCount() {
        return mModel.size();
    }

    @Override
    public Object getItem(int position) {
        return mModel.get(position);
    }

    @Override
    public long getItemId(int position) {
        MovieModel movie = (MovieModel) mModel.get(position);
        return movie.id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        Activity activity = (Activity) context;
        boolean mTwoPane = activity.findViewById(R.id.right_anchor) != null;
        MovieViewHolder holder;
        if (view==null) {
            view = mTwoPane ? layoutInflater.inflate(R.layout.movie_list_item_two_pane, null) : layoutInflater.inflate(R.layout.movie_list_item, null);
            holder = new MovieViewHolder();
            holder.titleTextView = (TextView) view.findViewById(R.id.list_item_title);
            holder.directorTextView = (TextView) view.findViewById(R.id.list_item_director);
            holder.posterImageView = (ImageView) view.findViewById(R.id.tiny_poster_view);
            view.setTag(holder);
        }
        else {
            holder = (MovieViewHolder) view.getTag();
        }

        final MovieModel itemModel = (MovieModel) getItem(position);
        holder.titleTextView.setText(itemModel.title);
        holder.directorTextView.setText(itemModel.director);
        if (holder.posterImageView != null) {
            holder.posterImageView.setTag(itemModel.id);
            if (mIsGoodDownloadingPosters) {
                PosterService.INSTANCE.loadPoster(itemModel.id, holder.posterImageView, context, mServer, PosterService.TINY);
                //Log.d("Adapter", "Loaded poster id "+itemModel.id);
                holder.loadedPoster = true;
            }
            else {
                holder.posterImageView.setImageDrawable(mDefaultPoster);
                holder.loadedPoster = false;
            }
        }
        return view;
    }

    public void setPosterDownloadAllowed(final boolean flag) {
        mIsGoodDownloadingPosters = flag;
    }

    public void updateStoredData() {
        mModel.clear();
        mModel.addAll(mStoredModel);
    }

    @Override
    public Filter getFilter() {
        if (mFilter==null)
            mFilter = new MovieFilter();
        return mFilter;
    }

    private class MovieFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            /*if (constraint==null || constraint.length()==0) {
                results.values = mStoredModel;
                results.count = mStoredModel.size();
            }
            else {*/
                List<MovieModel> nMovieModel = new LinkedList<MovieModel>();


                for (MovieModel movie : mStoredModel) {
                    if( movie.title.toLowerCase().contains(constraint.toString().toLowerCase())
                            || movie.director.toLowerCase().contains(constraint.toString().toLowerCase()) ) {
                        nMovieModel.add(movie);
                        Log.d("adapter" , movie.title);
                    }
                }
                results.values = nMovieModel;
                results.count = nMovieModel.size();
            //}
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mModel = (List<MovieModel>) results.values;
            notifyDataSetChanged();

        }
    }
}
