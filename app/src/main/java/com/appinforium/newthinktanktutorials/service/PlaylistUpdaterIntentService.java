package com.appinforium.newthinktanktutorials.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.appinforium.newthinktanktutorials.R;
import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PlaylistUpdaterIntentService extends IntentService {

    private static final String DEBUG_TAG = "PlaylistUpdaterIntentService";
    public static final String PLAYLIST_ID = "playlist_id_msg";
    public static final String NOTIFICATION = "com.appinforium.newthinktankcodingtutorials.service";
    public static final String RESULT = "result";
    private int result = Activity.RESULT_CANCELED;

    public PlaylistUpdaterIntentService() {
        super(DEBUG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String playlistId = intent.getStringExtra(PLAYLIST_ID);

        Log.d(DEBUG_TAG, "starting playlistId: " + playlistId);
        if (playlistId != null) {
            String strUrl = String.format(
                    "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=%s&playlistId=%s&key=%s",
                    getResources().getString(R.string.api_max_results),
                    playlistId, getResources().getString(R.string.youtube_api_key));

            if (!processRequest(strUrl)) {
                Log.e(DEBUG_TAG, "processRequest failed.");
                publishResult(Activity.RESULT_CANCELED);
            } else {
                publishResult(Activity.RESULT_OK);
            }
        }
    }

    private boolean processRequest(String strUrl) {
        boolean succeeded = true;
        boolean lastPage = true;
        String nextPageToken = null;
        URL apiUrl;

        do {

            try {

                if (nextPageToken != null) {
                    String tmpUrl = strUrl + "&pageToken=" + nextPageToken;
                    apiUrl = new URL(tmpUrl);
                } else {
                    apiUrl = new URL(strUrl);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                break;
            }

            JSONObject jsonObject = getJsonResponse(apiUrl);

            if (jsonObject != null) {
                try {

                    try {
                        nextPageToken = jsonObject.getString("nextPageToken");
                        lastPage = false;
                    } catch (JSONException e) {
                        nextPageToken = null;
                        lastPage = true;
                    }

                    JSONArray items = null;
                    items = jsonObject.getJSONArray("items");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);

                        JSONObject snippet = item.getJSONObject("snippet");

                        String title = snippet.getString("title");
                        String description = snippet.getString("description");
                        String publishedAt = snippet.getString("publishedAt");

                        String playlistId = snippet.getString("playlistId");
                        String position = snippet.getString("position");

                        JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                        JSONObject thumbnail = thumbnails.getJSONObject("medium");

                        String thumbnailUrl = thumbnail.getString("url");

                        JSONObject resourceId = snippet.getJSONObject("resourceId");

                        String videoId = resourceId.getString("videoId");
                        Log.d(DEBUG_TAG, "videoId " + videoId);

                        ContentValues videoData = new ContentValues();

                        videoData.put(AppDatabase.COL_TITLE, title);
                        videoData.put(AppDatabase.COL_DESCRIPTION, description);
                        videoData.put(AppDatabase.COL_PLAYLIST_ID, playlistId);
                        videoData.put(AppDatabase.COL_VIDEO_ID, videoId);
                        videoData.put(AppDatabase.COL_THUMBNAIL_URL, thumbnailUrl);
                        videoData.put(AppDatabase.COL_PUBLISHED_AT, publishedAt);
                        videoData.put(AppDatabase.COL_POSITION, Integer.valueOf(position));

//                        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_VIDEOS, playlistId);

                        getContentResolver().insert(AppDataContentProvider.CONTENT_URI_VIDEOS, videoData);

                    }

                } catch (JSONException e) {
                    Log.e(DEBUG_TAG, "JSONException", e);
                    succeeded = false;
                }

            }
        } while (!lastPage);

        return succeeded;
    }

    public static JSONObject getJsonResponse(URL apiUrl) {
        String response = null;

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            HttpGet httpGet = new HttpGet(String.valueOf(apiUrl));
            httpResponse = httpClient.execute(httpGet);

            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);

            return new JSONObject(response);

        } catch (ClientProtocolException e) {
            Log.w(DEBUG_TAG, "ClientProtocolException", e);
        } catch (IOException e) {
            Log.w(DEBUG_TAG, "IOException", e);
        } catch (JSONException e) {
            Log.w(DEBUG_TAG, "JSONException", e);
        }

        return null;
    }

    private void publishResult(int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }
}
