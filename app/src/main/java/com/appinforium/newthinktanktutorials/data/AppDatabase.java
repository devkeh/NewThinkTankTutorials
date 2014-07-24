package com.appinforium.newthinktanktutorials.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AppDatabase extends SQLiteOpenHelper {

    // Database parameters
    private static final String DEBUG_TAG = "AppDatabase";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "app_data";

    // Tables
    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String TABLE_VIDEOS = "videos";
    public static final String TABLE_ARTICLES = "articles";
    public static final String TABLE_ROUTING_PLAYLIST_VIDEO = "routing_playlist_video";

    // Columns
    public static final String COL_ID = "_id";
    public static final String COL_PLAYLIST_ID = "playlist_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_VIDEO_ID = "video_id";
    public static final String COL_PUBLISHED_AT = "published_at";
    public static final String COL_THUMBNAIL_URL = "thumbnail_url";
    public static final String COL_POSITION = "position";
    public static final String COL_ITEM_COUNT = "item_count";
    public static final String COL_ITEM_COUNT_OFFSET = "item_count_offset";
    public static final String COL_BOOKMARKED = "bookmarked";
    public static final String COL_PLAY_TIME = "play_time";
    public static final String COL_DURATION = "duration";
    public static final String COL_ARTICLE_URL = "article_url";
    public static final String COL_ID_VIDEOS = "id_videos" ;
    public static final String COL_ID_PLAYLISTS = "id_playlists";

    // Table constructor SQL Statements
    private static final String CREATE_TABLE_PLAYLISTS = "CREATE TABLE " + TABLE_PLAYLISTS
            + " (" + COL_ID + " integer primary key autoincrement, "
            + COL_TITLE + " text not null, "
            + COL_PLAYLIST_ID + " text UNIQUE not null, "
            + COL_DESCRIPTION + " text, "
            + COL_PUBLISHED_AT + " datetime not null, "
            + COL_ITEM_COUNT + " integer, "
            + COL_ITEM_COUNT_OFFSET + " integer not null default 0, "
            + COL_THUMBNAIL_URL + " text);";

    private static final String CREATE_TABLE_VIDEOS = "CREATE TABLE " + TABLE_VIDEOS
            + " (" + COL_ID + " integer primary key autoincrement, "
            + COL_TITLE + " text not null, "
            + COL_VIDEO_ID + " text UNIQUE not null, "
            + COL_PLAYLIST_ID + " text not null, "
            + COL_DESCRIPTION + " text not null, "
            + COL_THUMBNAIL_URL + " text not null, "
            + COL_PUBLISHED_AT + " datetime not null, "
            + COL_BOOKMARKED + " int not null default 0, "
            + COL_PLAY_TIME + " int not null default 0, "
            + COL_DURATION + " int not null default 0, "
            + COL_POSITION + " integer not null);";

    private static final String CREATE_TABLE_ARTICLES = "CREATE TABLE " + TABLE_ARTICLES
            + " (" + COL_ID + " integer primary key autoincrement, "
            + COL_TITLE + " text not null, "
            + COL_DESCRIPTION + " text not null, "
            + COL_ARTICLE_URL + " text UNIQUE not null, "
            + COL_PUBLISHED_AT + " datetime not null);";

    private static final String CREATE_TABLE_ROUTING_PLAYLIST_VIDEO = "CREATE TABLE " + TABLE_ROUTING_PLAYLIST_VIDEO
            + " (" + COL_ID + " integer primary key autoincrement, "
            + COL_POSITION + " integer not null, "
            + COL_ID_VIDEOS + " integer not null, "
            + COL_ID_PLAYLISTS + " integer not null, "
            + "FOREIGN KEY(" + COL_ID_VIDEOS + ") REFERENCES " + TABLE_VIDEOS + "(" + COL_ID + "), "
            + "FOREIGN KEY(" + COL_ID_PLAYLISTS + ") REFERENCES " + TABLE_PLAYLISTS + "(" + COL_ID + "));";
//            + FOREIGN KEY(places_id) REFERENCES Places(_id)

    public AppDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    // Called when the database is created for the very first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(DEBUG_TAG, "Creating playlists table");
        db.execSQL(CREATE_TABLE_PLAYLISTS);
        Log.d(DEBUG_TAG, "Creating videos table");
        db.execSQL(CREATE_TABLE_VIDEOS);
        Log.d(DEBUG_TAG, "Creating articles table");
        db.execSQL(CREATE_TABLE_ARTICLES);
        Log.d(DEBUG_TAG, "Creating routing playlists to videos table");
        db.execSQL(CREATE_TABLE_ROUTING_PLAYLIST_VIDEO);
    }

    // Called when DB_VERSION is incremented
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DEBUG_TAG, "Upgrading database. Existing contents will be lost. ["
                + oldVersion + "]->[" + newVersion + "]");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        Log.d(DEBUG_TAG, "Dropped playlists table");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEOS);
        Log.d(DEBUG_TAG, "Dropped videos table");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLES);
        Log.d(DEBUG_TAG, "Dropped articles table");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTING_PLAYLIST_VIDEO);
        Log.d(DEBUG_TAG, "Dropped routing playlists to videos table");
        onCreate(db);
    }
}
