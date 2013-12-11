package org.bigiarini.android.moviedb.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;

import org.bigiarini.android.moviedb.R;

/**
 * Created by emanuele on 09/10/13.
 */
public class SplashActivity extends Activity {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = SplashActivity.class.getName();

    private static final long MIN_WAIT_INTERVAL = 1500L;
    private static final long MAX_WAIT_INTERVAL = 1500L;
    private static final int GO_AHEAD_WHAT = 1;

    private long mStartTime;
    private boolean mIsDone;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_AHEAD_WHAT:
                    long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                    if (elapsedTime >= MIN_WAIT_INTERVAL && !mIsDone) {
                        mIsDone = true;
                        goAhead();
                    }
                    break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStartTime = SystemClock.uptimeMillis();
        final Message goAheadMessage = mHandler.obtainMessage(GO_AHEAD_WHAT);
        mHandler.sendMessageAtTime(goAheadMessage, mStartTime + MAX_WAIT_INTERVAL);
    }

    private void goAhead() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}