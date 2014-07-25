package com.appinforium.newthinktanktutorials.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ArticlesUpdaterIntentService extends IntentService {

    private static final String DEBUG_TAG = "ArticlesUpdaterIntentService";
    public static final String BROADCAST_ACTION = "com.appinforium.newthinktankcodingtutorials.articleupdater";
    public static final String STATUS = "status";
    public static final int LOADING = 1;
    public static final int FINISHED = 0;


    public ArticlesUpdaterIntentService() {
        super(DEBUG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String title = null;
        String pubDate = null;
        String articleUrl = null;
        String description = null;

        sendStatusBroadcast(LOADING);

        try {
            URL url = new URL("http://www.newthinktank.com/feed");

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();

            // We will get the XML from an input stream
            xpp.setInput(getInputStream(url), "UTF_8");

        /* We will parse the XML content looking for the "<title>" tag which appears inside the "<item>" tag.
         * However, we should take in consideration that the rss feed name also is enclosed in a "<title>" tag.
         * As we know, every feed begins with these lines: "<channel><title>Feed_Name</title>...."
         * so we should skip the "<title>" tag which is a child of "<channel>" tag,
         * and take in consideration only "<title>" tag which is a child of "<item>"
         *
         * In order to achieve this, we will make use of a boolean variable.
         */
            boolean insideItem = false;

            // Returns the type of current event: START_TAG, END_TAG, etc..
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = true;
                    } else if (xpp.getName().equalsIgnoreCase("title")) {
                        if (insideItem)
                            title = xpp.nextText();
                    } else if (xpp.getName().equalsIgnoreCase("link")) {
                        if (insideItem)
                            articleUrl = xpp.nextText();
                    } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                        if (insideItem)
                            pubDate = xpp.nextText();
                    } else if (xpp.getName().equalsIgnoreCase("description")) {
                        if (insideItem)
                            description = xpp.nextText();
                    }
                }else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                    insideItem=false;

//                    Log.d(DEBUG_TAG, "title: " + title);
//                    Log.d(DEBUG_TAG, "description: " + description);
//                    Log.d(DEBUG_TAG, "pubDate: " + pubDate);
//                    Log.d(DEBUG_TAG, "Link: " + articleUrl);

                    ContentValues contentValues = new ContentValues();

                    contentValues.put(AppDatabase.COL_TITLE, title);
                    contentValues.put(AppDatabase.COL_ARTICLE_URL, articleUrl);
                    contentValues.put(AppDatabase.COL_PUBLISHED_AT, pubDate);
                    contentValues.put(AppDatabase.COL_DESCRIPTION, description);

                    String selection = AppDatabase.COL_ARTICLE_URL + " = ?";
                    String[] selectionArgs = {articleUrl};
                    String[] projection = {AppDatabase.COL_ID};

                    Cursor cursor = getContentResolver().query(AppDataContentProvider.CONTENT_URI_ARTICLES,
                            projection, selection, selectionArgs, null);

                    if (cursor.moveToFirst()) {
                        Log.d(DEBUG_TAG, "Updating article: " + title);
                        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_ARTICLES,
                                cursor.getString(cursor.getColumnIndex(AppDatabase.COL_ID)));
                        getContentResolver().update(content_uri, contentValues, null, null);
                    } else {
                        Log.d(DEBUG_TAG, "Inserting article: " + title);
                        getContentResolver().insert(AppDataContentProvider.CONTENT_URI_ARTICLES, contentValues);
                    }

                    cursor.close();
                }

                eventType = xpp.next(); //move to next element
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendStatusBroadcast(FINISHED);
    }

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    private void sendStatusBroadcast(int status) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION);
        intent.putExtra(STATUS, status);
        sendBroadcast(intent);
    }
}
