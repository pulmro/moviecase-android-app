package org.bigiarini.android.moviedb.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.bigiarini.android.moviedb.model.ServerModel;
import org.bigiarini.android.moviedb.service.MoviedbService;
import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.service.PosterService;

import java.lang.ref.WeakReference;

/**
 * Created by emanuele on 08/11/13.
 */
public class PosterDownloaderTask extends AsyncTask<Long, Void, Bitmap> {

    private final WeakReference<ImageView> mImageViewReference;
    private final Context mContext;
    private final ServerModel mServer;
    private final Long mId;
    private final boolean mTiny;

    public PosterDownloaderTask(ImageView imageView, Context context, ServerModel server, boolean tiny) {
        mImageViewReference = new WeakReference<ImageView>(imageView);
        mContext = context;
        mServer = server;
        mId = (Long) imageView.getTag();
        mTiny = tiny;
    }

    @Override
    protected void onPreExecute() {

        if (mImageViewReference != null) {
            ImageView imageView = mImageViewReference.get();
            if (imageView != null)
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
        }
    }

    @Override
    protected Bitmap doInBackground(Long... params) {
        Bitmap poster = MoviedbService.sMoviedbService.getPoster(mContext, mServer, params[0]);
        return poster;
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
        if (mImageViewReference != null && bitmap!=null) {
            PosterService.INSTANCE.addBitmapToMemoryCache(mId, bitmap);
            ImageView imageView = mImageViewReference.get();
            if (imageView!=null && imageView.getTag().equals(mId)) {
                Bitmap bbitmap = mTiny ? Bitmap.createScaledBitmap(bitmap, 56, 76, false) : bitmap;
                imageView.setImageBitmap(bbitmap);
            }
        }
    }
}
