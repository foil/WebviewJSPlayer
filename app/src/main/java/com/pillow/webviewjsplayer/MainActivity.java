package com.pillow.webviewjsplayer;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private final static String TAG = "WebviewJSPlayer";
    private final static int SEEK_SHORT = 30;
//    private final static int SEEK_LONG = 30 * 10;
    private int mSeekStep = 0;
    private webVideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_youtube_player);
        mVideoView = ((webVideoView) findViewById(R.id.YouTubePlayerView));
        mVideoView.setVideoId("k4M8KMCoSkbyvxcVkP7");
        mVideoView.setAutoPlay(true);

//        String url =
//                "<iframe class=\"youtube-player\" "
//                        + "style=\"border: 0; width: 100%; height: 100%;"
//                        + "padding:0px; margin:0px\" "
//                        + "id=\"ytplayer\" type=\"text/html\" "
//                        + "src=\"http://www.youtube.com/embed/" + mID
//                        + "?fs=1?frameborder=0 " + "allowfullscreen autobuffer "
//                        + "controls onclick=\"this.play()\">\n" + "</iframe>\n";
//
//        mVideoView.loadData(url, "text/html", "utf-8");
//        mVideoView.loadUrl("file:///android_asset/youtube.html");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mVideoView.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mVideoView.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        mVideoView.handleBackPress(this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            mSeekStep = 0;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    webVideoView.playerState state = mVideoView.getPlayerState();
                    if (state == webVideoView.playerState.PLAYING)
                        mVideoView.callPlayerMethod("pause", "");
                    else //if (state == webVideoView.playerState.PAUSED)
                        //
                        mVideoView.callPlayerMethod("play", "");
                    break;//return true;
                default:
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (mVideoView.getPlayerState() != webVideoView.playerState.SEEKING) {
                        mSeekStep ++;
                        float sec = mVideoView.getTimeSec();
                        mVideoView.callPlayerMethod("seek",
                                Integer.toString((int) sec + mSeekStep * SEEK_SHORT));
                        Log.d(TAG, event.toString());
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (mVideoView.getPlayerState() != webVideoView.playerState.SEEKING) {
                        mSeekStep--;
                        float sec = mVideoView.getTimeSec();
                        mVideoView.callPlayerMethod("seek",
                                Integer.toString((int) sec + mSeekStep * SEEK_SHORT));
                        Log.d(TAG, event.toString());
                    }
                    return true;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
