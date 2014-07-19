package com.appinforium.newthinktanktutorials;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

public class VideoPlayerFragment extends YouTubePlayerFragment implements
        YouTubePlayer.OnInitializedListener,
        YouTubePlayer.OnFullscreenListener {

    private static final String DEBUG_TAG = "VideoPlayerFragment";
    public static final String VIDEO_ID = "VIDEO_ID_ARG";
    private String videoId;
    private YouTubePlayer player;

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        Log.d(DEBUG_TAG, "onInitializationSuccess");
        player = youTubePlayer;

//        int controlFlags = youTubePlayer.getFullscreenControlFlags();
//        controlFlags |= YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE;
//        youTubePlayer.setFullscreenControlFlags(controlFlags);
//        youTubePlayer.setOnFullscreenListener(this);
//        youTubePlayer.setFullscreen(true);
        youTubePlayer.setPlaybackEventListener(new OnPlaybackEventListener());
        youTubePlayer.setPlayerStateChangeListener(new OnPlayerStateChangeListener());

        if (!wasRestored && videoId != null) {
            youTubePlayer.loadVideo(videoId);
            youTubePlayer.play();

        }


        youTubePlayer.setFullscreen(true);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize(getResources().getString(R.string.youtube_api_key), this);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        videoId = bundle.getString(VIDEO_ID);
        return super.onCreateView(inflater, container, bundle);
    }

    @Override
    public void onFullscreen(boolean fullscreen) {


//        ViewGroup.LayoutParams playerParams = this.getView().getLayoutParams();
//        if (fullscreen) {
//            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        } else {
//            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//        }
    }

    private final class OnPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    }

    private final class OnPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    }
}
