package com.pillow.webviewjsplayer;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class youtubePlayerActivity extends Activity {
    private final static String TAG = "WebviewJSPlayer";
    private WebView mVideoView;
    private String mID;
    private WebChromeClient.CustomViewCallback mViewCallback;
    private FrameLayout mRootLayout;
    private View mCustomView;
    private WebChromeClient mWebChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_youtube_player);
        mRootLayout = (FrameLayout)findViewById(R.id.youtubeLayout);

        mVideoView = (WebView) findViewById(R.id.YouTubePlayerView);
        mVideoView.setFocusable(true);
        mVideoView.setClickable(true);
        mVideoView.requestFocus();
        WebSettings settings = mVideoView.getSettings();
        settings.setJavaScriptEnabled(true);
    //    settings.setUserAgentString("");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            settings.setMediaPlaybackRequiresUserGesture(false);

        mID = "bHQqvYy5KYo";

        mWebChromeClient = new WebChromeClient() {
            @Override
            public View getVideoLoadingProgressView() {
                ProgressBar pb = new ProgressBar(mVideoView.getContext());
                pb.setIndeterminate(true);
                return pb;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // if a view already exists then immediately terminate the new one
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                //  Add the custom view to its container.
                FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER = new
                        FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);

                mRootLayout.addView(view, COVER_SCREEN_GRAVITY_CENTER);
                mCustomView = view;
                mViewCallback = callback;

                // hide main browser view
                mVideoView.setVisibility(View.GONE);

                // Finally show the custom view container.
                mRootLayout.setVisibility(View.VISIBLE);
                //    mRootLayout.bringToFront();
            };

            @Override
            public void onHideCustomView() {
                if (mCustomView == null)
                    return;

                // Hide the custom view.
                mCustomView.setVisibility(View.GONE);
                // Remove the custom view from its container.
                mRootLayout.removeView(mCustomView);
                mCustomView = null;
                mRootLayout.setVisibility(View.GONE);
                mViewCallback.onCustomViewHidden();

                // Show the content view.
                mVideoView.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, consoleMessage.message() + " at line:" + consoleMessage.lineNumber());
                return true;
            }
        };
        mVideoView.setWebChromeClient(mWebChromeClient);
        // Set up the user interaction to manually show or hide the system UI.
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //    emulateClick();
            }
        });
//        String url = "http://www.youtube.com/embed/" + mID + "?fs=1&frameborder=0&autoplay=1";
//        mVideoView.loadUrl(url);
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
        mVideoView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                String id = "bHQqvYy5KYo";
                String cmd = "javascript:initYoutube('" + id + "')";
                mVideoView.loadUrl(cmd);
            }
        });

        mVideoView.loadUrl("file:///android_asset/youtube.html");
    }

    @Override
    protected void onPause() {
        super.onPause();

        mVideoView.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mVideoView.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //    Toast toast = Toast.makeText(getApplicationContext(), event.toString(), Toast.LENGTH_LONG);
        //    toast.show();
        if ((event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER ||
                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) &&
                event.getAction() == KeyEvent.ACTION_UP) {
//            emulateClick();
            //mVideoView.performClick();
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
