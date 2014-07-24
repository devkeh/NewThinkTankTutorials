package com.appinforium.newthinktanktutorials.adapter;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
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
import java.util.ArrayList;

public class NewThinkTankSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String DEBUG_TAG = "NewThinkTankSyncAdapter";
    private Context context;
    private ContentProviderClient provider;
    private NotificationManager notificationManager;

    private ArrayList<String> notificationStrings = new ArrayList<String>();

    public NewThinkTankSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onPerformSync(Account account, Bundle settingsBundle, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(DEBUG_TAG, "onPerformSync has been called");
        this.provider = provider;







        ArrayList<Bundle> updatePlaylistVideos = new ArrayList<Bundle>();

        String strUrl = String.format(
                "https://www.googleapis.com/youtube/v3/playlists?part=contentDetails,+snippet&maxResults=%s&channelId=%s&key=%s",
                context.getResources().getString(R.string.api_max_results),
                context.getResources().getString(R.string.channel_id),
                context.getResources().getString(R.string.youtube_api_key));

        ArrayList<JSONObject> playlistItems = getYoutubeJSONItems(strUrl);

        Log.d(DEBUG_TAG, "Got " + playlistItems.size() + " playlists");

        for (JSONObject item : playlistItems) {
            try {

                String playlistId = item.getString("id");

                JSONObject contentDetails = item.getJSONObject("contentDetails");
                int itemCount = contentDetails.getInt("itemCount");

                JSONObject snippet = item.getJSONObject("snippet");

                String title = snippet.getString("title");
                String description = snippet.getString("description");
                String publishedAt = snippet.getString("publishedAt");

                JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                JSONObject thumbnail = thumbnails.getJSONObject("medium");

                String thumbnailUrl = thumbnail.getString("url");

                String[] projection = {AppDatabase.COL_ID, AppDatabase.COL_PLAYLIST_ID,
                    AppDatabase.COL_ITEM_COUNT_OFFSET};
                String selection = AppDatabase.COL_PLAYLIST_ID + " = ? ";
                String[] selectionArgs = {playlistId};
                Cursor cursor = provider.query(AppDataContentProvider.CONTENT_URI_PLAYLISTS,
                        projection, selection, selectionArgs, null);

                ContentValues playlistValues = new ContentValues();

                playlistValues.put(AppDatabase.COL_TITLE, title);
                playlistValues.put(AppDatabase.COL_DESCRIPTION, description);
                playlistValues.put(AppDatabase.COL_PLAYLIST_ID, playlistId);
                playlistValues.put(AppDatabase.COL_THUMBNAIL_URL, thumbnailUrl);
                playlistValues.put(AppDatabase.COL_PUBLISHED_AT, publishedAt);
                playlistValues.put(AppDatabase.COL_ITEM_COUNT, itemCount);

                Boolean succeeded;

                String id_playlist;

                if (cursor.moveToFirst()) {

                    id_playlist = cursor.getString(cursor.getColumnIndex(AppDatabase.COL_ID));

                    // check if we need to push a playlist update.
                    String[] projection2 = {AppDatabase.COL_ID};
                    Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_PLAYLIST_VIDEOS, id_playlist);
                    Cursor cursor2 = provider.query(content_uri, projection2, null, null, null);

                    if (itemCount > cursor2.getCount() + cursor.getInt(cursor.getColumnIndex(AppDatabase.COL_ITEM_COUNT_OFFSET))) {
                        Log.d(DEBUG_TAG, "itemCount: " + itemCount + " databaseCount: " + cursor2.getCount());
                        Log.d(DEBUG_TAG, "Updating playlist_id: " + playlistId);
//                        succeeded = updatePlaylist(playlistId, id_playlist, itemCount);
                        Bundle bundle = new Bundle();
                        bundle.putString("playlistId", playlistId);
                        bundle.putString("id_playlist", id_playlist);
                        bundle.putInt("itemCount", itemCount);
                        updatePlaylistVideos.add(bundle);
                    }

                    cursor2.close();
                    Log.d(DEBUG_TAG, "Updating playlist_id: " + playlistId);
                    content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_PLAYLISTS,
                            cursor.getString(cursor.getColumnIndex(AppDatabase.COL_ID)));
                    provider.update(content_uri, playlistValues, null, null);

                } else {
                    Log.d(DEBUG_TAG, "Inserting playlist_id: " + playlistId);
                    Uri result_uri = provider.insert(AppDataContentProvider.CONTENT_URI_PLAYLISTS, playlistValues);
                    id_playlist = result_uri.getLastPathSegment();
//                    succeeded = updatePlaylist(playlistId, id_playlist, itemCount);

                    Bundle bundle = new Bundle();
                    bundle.putString("playlistId", playlistId);
                    bundle.putString("id_playlist", id_playlist);
                    bundle.putInt("itemCount", itemCount);
                    updatePlaylistVideos.add(bundle);
                }

                cursor.close();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }


        // now update individual playlists if any
        for (Bundle bundle : updatePlaylistVideos) {
            updatePlaylist(bundle.getString("playlistId"), bundle.getString("id_playlist"), bundle.getInt("itemCount"));
        }



        // now push a notification if new videos were posted.
        String nTitle;
        String nContentText;
        if (notificationStrings.size() > 0) {
            if (notificationStrings.size() == 1) {
                nTitle = "New Think Tank has posted a new video";
                nContentText =  notificationStrings.get(0);
            } else {
                nTitle = "New Think Tank has posted " + notificationStrings.size() + " new videos";
                nContentText = notificationStrings.get(0) + " ...";
            }

            Notification n = new Notification.Builder(this.context)
                .setContentTitle(nTitle)
                .setContentText(nContentText)
                .setSmallIcon(R.drawable.ic_launcher).getNotification();

            notificationManager.notify(0, n);
        }
    }

    private boolean updatePlaylist(String playlistId, String id_playlist, int itemCount) {
        boolean succeeded = true;

        String strUrl = String.format(
                "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet,+status&maxResults=%s&playlistId=%s&key=%s",
                context.getResources().getString(R.string.api_max_results),
                playlistId, context.getResources().getString(R.string.youtube_api_key));

        ArrayList<JSONObject> videoItems = getYoutubeJSONItems(strUrl);

        Log.d(DEBUG_TAG, "Got " + videoItems.size() + " videos");

        int privateCounter = 0;

        for (JSONObject item : videoItems) {
            try {

                JSONObject status = item.getJSONObject("status");
                String privacyStatus = status.getString("privacyStatus");

                if (privacyStatus.equalsIgnoreCase("public")) {
                    JSONObject snippet = item.getJSONObject("snippet");

                    String title = snippet.getString("title");
                    String description = snippet.getString("description");
                    String publishedAt = snippet.getString("publishedAt");

                    String position = snippet.getString("position");

                    JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                    JSONObject thumbnail = thumbnails.getJSONObject("medium");

                    String thumbnailUrl = thumbnail.getString("url");

                    JSONObject resourceId = snippet.getJSONObject("resourceId");

                    String videoId = resourceId.getString("videoId");

                    ContentValues videoData = new ContentValues();

                    videoData.put(AppDatabase.COL_TITLE, title);
                    videoData.put(AppDatabase.COL_DESCRIPTION, description);
                    videoData.put(AppDatabase.COL_PLAYLIST_ID, playlistId);
                    videoData.put(AppDatabase.COL_VIDEO_ID, videoId);
                    videoData.put(AppDatabase.COL_THUMBNAIL_URL, thumbnailUrl);
                    videoData.put(AppDatabase.COL_PUBLISHED_AT, publishedAt);
                    videoData.put(AppDatabase.COL_POSITION, Integer.valueOf(position));

                    String selection = "video_id = ?";
                    String[] selectionArgs = {videoId};
                    String[] projection = {AppDatabase.COL_ID};

                    Cursor cursor = provider.query(AppDataContentProvider.CONTENT_URI_VIDEOS,
                            projection, selection, selectionArgs, null);

                    String id_video;

                    if (cursor.moveToFirst()) {
//                    Log.d(DEBUG_TAG, "Updating video_id: " + videoId);
                        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_VIDEOS,
                                cursor.getString(cursor.getColumnIndex(AppDatabase.COL_ID)));
                        provider.update(content_uri, videoData, null, null);

                        id_video = cursor.getString(cursor.getColumnIndex(AppDatabase.COL_ID));
//                    Log.d(DEBUG_TAG, "count: " + count);
                    } else {
//                    Log.d(DEBUG_TAG, "Inserting video_id: " + videoId);
                        Uri result_uri = provider.insert(AppDataContentProvider.CONTENT_URI_VIDEOS, videoData);
                        id_video = result_uri.getLastPathSegment();
                        notificationStrings.add(title);
                    }

                    // query if we have this in the jointable
                    selection = AppDatabase.COL_ID_VIDEOS + " = ? AND " + AppDatabase.COL_ID_PLAYLISTS + " = ?";
                    String[] selectionArgs2 = {id_video, id_playlist};
                    cursor = provider.query(AppDataContentProvider.CONTENT_URI_PLAYLIST_VIDEOS, projection,
                            selection, selectionArgs2, null);
                    if (!cursor.moveToFirst()) {
                        ContentValues jointableValues = new ContentValues();
                        jointableValues.put(AppDatabase.COL_ID_VIDEOS, id_video);
                        jointableValues.put(AppDatabase.COL_ID_PLAYLISTS, id_playlist);
                        jointableValues.put(AppDatabase.COL_POSITION, position);
                        provider.insert(AppDataContentProvider.CONTENT_URI_PLAYLIST_VIDEOS, jointableValues);
                    }


                    cursor.close();
                } else {
                    privateCounter += 1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                succeeded = false;
            } catch (RemoteException e) {
                e.printStackTrace();
                succeeded = false;
            }
        }

        if (succeeded) {
            try {
                // find out if we had duplicates and save the offset value on playlists entry.
                String[] projection = {AppDatabase.COL_ID};
                Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_PLAYLIST_VIDEOS, id_playlist);
                Cursor cursor = provider.query(content_uri, projection, null, null, null);

                if (itemCount > cursor.getCount()) {
                    Log.d(DEBUG_TAG, "Applying offset of " + (itemCount - cursor.getCount() - privateCounter) + " due to duplicate entries");
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppDatabase.COL_ITEM_COUNT_OFFSET, (itemCount - cursor.getCount()));
                    content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_PLAYLISTS, id_playlist);
                    provider.update(content_uri, contentValues, null, null);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                succeeded = false;
            }
        }
        return succeeded;
    }

    private ArrayList<JSONObject> getYoutubeJSONItems(String strUrl) {

        boolean lastPage = true;
        String nextPageToken = null;
        URL apiUrl;

        ArrayList<JSONObject> resultItems = new ArrayList<JSONObject>();

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
                        resultItems.add(items.getJSONObject(i));
                    }

                } catch (JSONException e) {
                    Log.e(DEBUG_TAG, "JSONException", e);
                }

            }
        } while (!lastPage);

        return resultItems;

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
}
