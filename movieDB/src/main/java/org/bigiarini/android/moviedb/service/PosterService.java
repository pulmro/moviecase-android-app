package org.bigiarini.android.moviedb.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.model.ServerModel;
import org.bigiarini.android.moviedb.net.PosterDownloaderTask;

/**
 * Created by emanuele on 11/11/13.
 */
public enum PosterService {

    INSTANCE;

    private static final String TAG_LOG = PosterService.class.getName();

    public static final boolean TINY = true;

    private LruCache<Long, Bitmap> mMemoryCache;


    PosterService() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory/8;

        Log.d("", "Poster cache size is: "+cacheSize);

        mMemoryCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

    }

    public void addBitmapToMemoryCache(Long id, Bitmap bitmap) {
        if (getBitmapFromCache(id) == null) {
            mMemoryCache.put(id, bitmap);
        }
    }

    public Bitmap getBitmapFromCache(final Long id) {
        return mMemoryCache.get(id);
    }

    public void loadPoster(final Long id, final ImageView imageView, Context context, ServerModel server) {
        loadPoster(id, imageView, context, server, false);
    }

    public void loadPoster(final Long id, final ImageView imageView, Context context, ServerModel server, boolean tiny) {
        Bitmap bitmap = getBitmapFromCache(id);
        if (bitmap != null) {
            Log.d(TAG_LOG, "Loading from cache. Poster id "+id+" Cache Size:"+mMemoryCache.size());
            Bitmap bbitmap = tiny ? Bitmap.createScaledBitmap(bitmap, 56, 76, false) : bitmap;
            final Long taggedId = (Long) imageView.getTag();
            if ( taggedId.equals(id) ) {
                imageView.setImageBitmap(bbitmap);
            }
            else {
                Log.d(TAG_LOG, "Found tag "+imageView.getTag()+" instead of "+id);
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_launcher));
            }
        }
        else {
            new PosterDownloaderTask(imageView, context, server, tiny).execute(id);
        }
    }

}
