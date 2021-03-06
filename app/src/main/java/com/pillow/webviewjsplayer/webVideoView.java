package com.pillow.webviewjsplayer;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class webVideoView extends WebView {
    private static final String                 TAG = "WebVideoView";
    private WebSettings                         mWebSettings;
    private WebChromeClient                     mChromeClient;
    private VideoView                           mCustomVideoView;
    private WebChromeClient.CustomViewCallback  mViewCallback;

    private final String                        mEmbedUrl = "http://www.dailymotion.com/embed/video/%s?html=1&fullscreen=%s&app=%s&api=location";
    private final String                        mExtraUA = "; DailymotionEmbedSDK 1.0";
    private FrameLayout                         mVideoLayout;
    private boolean                             mIsFullscreen = false;
    private FrameLayout                         mRootLayout;
    private boolean                             mAllowAutomaticNativeFullscreen = true;
    private boolean mAutoPlay = false;
    public enum playerState {
        READY, PLAY, PLAYING, PAUSED, SEEKING, SEEKED, ENDED, ERROR
    }
    private playerState mState;
    private float mTimeSec = 0;

    public webVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public webVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public webVideoView(Context context) {
        super(context);
        init();
    }

    private void init(){

        //The topmost layout of the window where the actual VideoView will be added to
        mRootLayout = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();

        mWebSettings = getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setPluginState(WebSettings.PluginState.ON);
        mWebSettings.setUserAgentString(mWebSettings.getUserAgentString() + mExtraUA);
        if (Build.VERSION.SDK_INT >= 17) {
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        mChromeClient = new WebChromeClient(){

            /**
             * The view to be displayed while the fullscreen VideoView is buffering
             * @return the progress view
             */
            @Override
            public View getVideoLoadingProgressView() {
                ProgressBar pb = new ProgressBar(getContext());
                pb.setIndeterminate(true);
                return pb;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                ((Activity) getContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
                mIsFullscreen = true;
                mViewCallback = callback;
                if (view instanceof FrameLayout){
                    FrameLayout frame = (FrameLayout) view;
                    if (frame.getFocusedChild() instanceof VideoView){//We are in 2.3
                        VideoView video = (VideoView) frame.getFocusedChild();
                        frame.removeView(video);

                        setupVideoLayout(video);

                        mCustomVideoView = video;
                        mCustomVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                hideVideoView();
                            }
                        });


                    } else {//Handle 4.x

                        setupVideoLayout(view);

                    }
                }
            }

            @Override
            public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) // Only available in API level 14+
            {
                onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                hideVideoView();
            }

        };


        setWebChromeClient(mChromeClient);
        setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                if (uri.getScheme().equals("dmevent")) {
                    String event = uri.getQueryParameter("event");
                    if (event.equals("apiready")) {
                        if (mAutoPlay) {
                            callPlayerMethod("play", "");
                        }
                    } else if (event.equals("timeupdate")) {
                        String time = uri.getQueryParameter("time");
                        mTimeSec = Float.parseFloat(time);
                    } else if (event.equals("playing")) {
                        Log.d(TAG, uri.toString());
                        mState = playerState.PLAYING;
                    } else if (event.equals("pause")) {
                        Log.d(TAG, uri.toString());
                        mState = playerState.PAUSED;
                    } else if (event.equals("seeking")) {
                        Log.d(TAG, uri.toString());
                        mState = playerState.SEEKING;
                    } else if (event.equals("seeked")) {
                        Log.d(TAG, uri.toString());
                        mState = playerState.SEEKED;
                    } else if (event.equals("error")) {
                        Log.d(TAG, uri.toString());
                        mState = playerState.ERROR;
                    } else if (event.equals("ended")) {
                        Log.d(TAG, uri.toString());
                        mState = playerState.ENDED;
                        ((Activity) getContext()).finish();
                    }
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }
        });
    }

    public playerState getPlayerState() {
        return mState;
    }
    public float getTimeSec() {
        return mTimeSec;
    }
    public void callPlayerMethod(String method, String args) {
        String cmd = "javascript:player.api(\"" + method + "\"";
        if (!args.isEmpty())
            cmd += "," + args;
        cmd += ")";
        loadUrl(cmd);
    }
    public void setVideoId(String videoId){
        loadUrl(String.format(mEmbedUrl, videoId, mAllowAutomaticNativeFullscreen, getContext().getPackageName()));
    }

    public void setVideoId(String videoId, boolean autoPlay){
        mAutoPlay = autoPlay;
        loadUrl(String.format(mEmbedUrl, videoId, mAllowAutomaticNativeFullscreen, getContext().getPackageName()));
    }

    public void hideVideoView(){
        if(isFullscreen()){
            if(mCustomVideoView != null){
                mCustomVideoView.stopPlayback();
            }
            mRootLayout.removeView(mVideoLayout);
            mViewCallback.onCustomViewHidden();
            ((Activity) getContext()).setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            mIsFullscreen = false;
        }


    }

    private void setupVideoLayout(View video){

        /**
         * As we don't want the touch events to be processed by the underlying WebView, we do not set the WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE flag
         * But then we have to handle directly back press in our View to exit fullscreen.
         * Otherwise the back button will be handled by the topmost Window, id-est the player controller
         */
        mVideoLayout = new FrameLayout(getContext()){

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    hideVideoView();
                    return true;
                }

                return super.dispatchKeyEvent(event);
            }};

        mVideoLayout.setBackgroundResource(R.color.black);
        mVideoLayout.addView(video);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        mRootLayout.addView(mVideoLayout, lp);
        mIsFullscreen = true;
    }

    public boolean isFullscreen(){
        return mIsFullscreen;
    }

    public void handleBackPress(Activity activity) {
        if(isFullscreen()){
            hideVideoView();
        } else {
            loadUrl("");//Hack to stop video
            activity.finish();
        }
    }

    public void setAllowAutomaticNativeFullscreen(boolean allowAutomaticNativeFullscreen){
        mAllowAutomaticNativeFullscreen = allowAutomaticNativeFullscreen;
    }

    public boolean isAutoPlaying(){
        return mAutoPlay;
    }

    public void setAutoPlay(boolean autoPlay){
        mAutoPlay = autoPlay;
    }
}