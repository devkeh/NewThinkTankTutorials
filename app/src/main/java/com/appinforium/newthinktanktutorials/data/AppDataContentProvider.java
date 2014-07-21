package com.appinforium.newthinktanktutorials.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.sql.SQLException;

public class AppDataContentProvider extends ContentProvider {

    private static final String DEBUG_TAG = "AppDataContentProvider";
    private static final String AUTHORITY = "com.appinforium.newthinktanktutorials.data.AppDataContentProvider";

    public static final Uri CONTENT_URI_PLAYLISTS = Uri.parse("content://" + AUTHORITY + "/playlists");
    public static final Uri CONTENT_URI_VIDEOS = Uri.parse("content://" + AUTHORITY + "/videos");
    public static final Uri CONTENT_URI_ARTICLES = Uri.parse("content://" + AUTHORITY + "/articles");

    private static final int PLAYLISTS = 100;
    private static final int PLAYLIST_ID = 101;
    private static final int ARTICLES = 102;
    private static final int ARTICLE_ID = 103;
    private static final int VIDEOS = 104;
    private static final int VIDEO_ID = 105;

    private AppDatabase appDatabase;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // content://<AUTHORITY>/playlists
        uriMatcher.addURI(AUTHORITY, "playlists", PLAYLISTS);

        // content://<AUTHORITY>/playlists
        uriMatcher.addURI(AUTHORITY, "playlists/#", PLAYLIST_ID);

        // content://<AUTHORITY>/videos
        uriMatcher.addURI(AUTHORITY, "videos", VIDEOS);

        // content://<AUTHORITY>/videos/#
        uriMatcher.addURI(AUTHORITY, "videos/#", VIDEO_ID);

        // content://<AUTHORITY>/articles
        uriMatcher.addURI(AUTHORITY, "articles", ARTICLES);

        // content://<AUTHORITY>/articles/#
        uriMatcher.addURI(AUTHORITY, "articles/#", ARTICLE_ID);
    }

    @Override
    public boolean onCreate() {
        this.appDatabase = new AppDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = uriMatcher.match(uri);
        if (selection == null) {
            selection = "";
        }

        switch (uriType) {
            case PLAYLISTS:
                queryBuilder.setTables(AppDatabase.TABLE_PLAYLISTS);
                break;
            case PLAYLIST_ID:
                queryBuilder.setTables(AppDatabase.TABLE_PLAYLISTS);
                selection = selection + " _ID = " + uri.getLastPathSegment();
                break;
            case VIDEOS:
                queryBuilder.setTables(AppDatabase.TABLE_VIDEOS);
                break;
            case VIDEO_ID:
                queryBuilder.setTables(AppDatabase.TABLE_VIDEOS);
                selection = selection + " _ID = " + uri.getLastPathSegment();
                break;
            case ARTICLES:
                queryBuilder.setTables(AppDatabase.TABLE_ARTICLES);
                break;
            case ARTICLE_ID:
                queryBuilder.setTables(AppDatabase.TABLE_ARTICLES);
                selection = selection + " _ID = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown content Uri");
        }

        Cursor cursor = queryBuilder.query(appDatabase.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int uriType = uriMatcher.match(uri);
        final SQLiteDatabase db = appDatabase.getWritableDatabase();

        switch (uriType) {
            case PLAYLISTS:
                try {
                    long newID = db.insertOrThrow(AppDatabase.TABLE_PLAYLISTS, null, contentValues);
                    if (newID > 0) {
                        Uri newUri = ContentUris.withAppendedId(uri, newID);
                        getContext().getContentResolver().notifyChange(newUri, null);
//                        db.close();
                        return newUri;
                    } else {
                        throw new SQLException("Failed to insert row into " + uri);
                    }
                } catch (SQLiteConstraintException e) {
                    Log.d(DEBUG_TAG, "Ignoring constraint failure for playlists");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case VIDEOS:
                try {
                    long newID = db.insertOrThrow(AppDatabase.TABLE_VIDEOS, null, contentValues);
                    if (newID > 0) {
                        Uri newUri = ContentUris.withAppendedId(uri, newID);
                        getContext().getContentResolver().notifyChange(newUri, null);
                        return newUri;
                    } else {
                        throw new SQLException("Failed to insert row into " + uri);
                    }
                } catch (SQLiteConstraintException e) {
                    Log.d(DEBUG_TAG, "Ignoring constraint failure for videos");
                } catch (SQLException e) {
                    e.printStackTrace();;
                }
                break;
            case ARTICLES:
                try {
                    long newID = db.insertOrThrow(AppDatabase.TABLE_ARTICLES, null, contentValues);
                    if (newID > 0) {
                        Uri newUri = ContentUris.withAppendedId(uri, newID);
                        getContext().getContentResolver().notifyChange(newUri, null);
                        return newUri;
                    } else {
                        throw new SQLException("Failed to insert row into " + uri);
                    }
                } catch (SQLiteConstraintException e) {
                    Log.d(DEBUG_TAG, "Ignoring constraint failure for articles");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid URI for insert");
        }

        db.close();
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = appDatabase.getWritableDatabase();
        final SelectionBuilder builder = new SelectionBuilder();
        final int uriType = uriMatcher.match(uri);
        int count;
        switch (uriType) {
            case PLAYLIST_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(AppDatabase.TABLE_PLAYLISTS)
                        .where(AppDatabase.COL_ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, contentValues);
                break;
            case VIDEO_ID:
                count = builder.table(AppDatabase.TABLE_VIDEOS)
                        .where(AppDatabase.COL_ID + "=?", uri.getLastPathSegment())
                        .where(selection, selectionArgs)
                        .update(db, contentValues);
                break;
            case ARTICLE_ID:
                count = builder.table(AppDatabase.TABLE_ARTICLES)
                        .where(AppDatabase.COL_ID + "=?", uri.getLastPathSegment())
                        .where(selection, selectionArgs)
                        .update(db, contentValues);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }
}
