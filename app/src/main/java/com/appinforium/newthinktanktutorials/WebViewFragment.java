package com.appinforium.newthinktanktutorials;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewFragment extends Fragment {

    public static final String WEB_URL = "WEB_URL_ARG";
    private static final String DEBUG_TAG = "WebViewFragment";
    public static final String BROADCAST_ACTION = "com.appinforium.newthinktanktutorials.webviewfragment";
    public static final String STATUS = "loading_status";
    public static int LOADING = 1;
    public static int FINISHED = 0;

    private String webUrl;

    private WebView webView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        getActivity().getWindow().requestFeature(Window.FEATURE_PROGRESS);
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);

        webView = (WebView) view.findViewById(R.id.fragmentWebView);
        Bundle args = getArguments();
        webUrl = args.getString(WEB_URL);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        webView.getSettings().setJavaScriptEnabled(true);


        final Activity activity = getActivity();


//        webView.setWebChromeClient(new WebChromeClient() {
//            public void onProgressChanged(WebView view, int progress) {
//                // Activities and WebViews measure progress with different scales.
//                // The progress meter will automatically disappear when we reach 100%
//                Log.d(DEBUG_TAG, "progress: " + progress);
//                activity.setProgress(progress * 1000);
//            }
//        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(DEBUG_TAG, "onPageStarted called");
                sendStatusBroadcast(LOADING);
//                listener.OnWebViewLoading(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                Log.d(DEBUG_TAG, "onPageFinished called");
                sendStatusBroadcast(FINISHED);
//                listener.OnWebViewLoading(false);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        if (webUrl != null) {
            webView.loadUrl(webUrl);
        }
    }

    private void sendStatusBroadcast(int status) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION);
        intent.putExtra(STATUS, status);
        getActivity().sendBroadcast(intent);
    }


}
