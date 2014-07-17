package com.appinforium.newthinktanktutorials.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
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

public class PlaylistsUpdaterIntentService extends IntentService {

    private static final String DEBUG_TAG = "PlaylistsUpdaterIntentService";
    public static final String CHANNEL_ID = "MSG_CHANNEL_ID";
    public static final String NOTIFICATION = "com.appinforium.newthinktanktutorials.service";
    public static final String RESULT = "result";

    public PlaylistsUpdaterIntentService() {
        super(DEBUG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String channelId = intent.getStringExtra(CHANNEL_ID);
        if (channelId != null) {
            String strUrl = String.format(
                    "https://www.googleapis.com/youtube/v3/playlists?part=snippet&maxResults=%s&channelId=%s&key=%s",
                    getResources().getString(R.string.api_max_results),
                    channelId, getResources().getString(R.string.youtube_api_key));

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
        String[] allowed_playlist_ids = getResources().getStringArray(R.array.allowed_playlist_ids);

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

                        String playlistId = item.getString("id");

                        JSONObject snippet = item.getJSONObject("snippet");

                        String title = snippet.getString("title");
                        String description = snippet.getString("description");
                        String publishedAt = snippet.getString("publishedAt");

                        JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                        JSONObject thumbnail = thumbnails.getJSONObject("medium");

                        String thumbnailUrl = thumbnail.getString("url");

                        ContentValues contentValues = new ContentValues();

                        contentValues.put(AppDatabase.COL_TITLE, title);
                        contentValues.put(AppDatabase.COL_DESCRIPTION, description);
                        contentValues.put(AppDatabase.COL_PLAYLIST_ID, playlistId);
                        contentValues.put(AppDatabase.COL_THUMBNAIL_URL, thumbnailUrl);
                        contentValues.put(AppDatabase.COL_PUBLISHED_AT, publishedAt);

//                        for (String playlist_id : allowed_playlist_ids) {
//                            if (playlist_id.equals(playlistId)) {
                                getContentResolver().insert(AppDataContentProvider.CONTENT_URI_PLAYLISTS,
                                        contentValues);
//                            }
//                        }

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
