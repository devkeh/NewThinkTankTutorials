package com.appinforium.newthinktanktutorials;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;
import com.squareup.picasso.Picasso;

public class VideoDetailFragment extends Fragment {

    private static final String DEBUG_TAG = "VideoDetailFragment";
    public static final String VIDEO_INDEX = "VIDEO_INDEX";

    private TextView videoTitleTextView;
    private TextView videoDescriptionTextView;
    private ImageView videoThumbnailImageView;
    private String videoId;
    private long videoIndex;
    private MenuItem actionBookmark;
    private boolean isBookmarked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_detail, container, false);

        videoTitleTextView = (TextView) view.findViewById(R.id.videoTitleTextView);
        videoDescriptionTextView = (TextView) view.findViewById(R.id.videoDescriptionTextView);
        videoThumbnailImageView = (ImageView) view.findViewById(R.id.videoDetailThumbnailImageView);

        Bundle args = getArguments();
        videoIndex = args.getLong(VIDEO_INDEX);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] projection = {AppDatabase.COL_ID, AppDatabase.COL_DESCRIPTION, AppDatabase.COL_BOOKMARKED,
                AppDatabase.COL_TITLE, AppDatabase.COL_VIDEO_ID, AppDatabase.COL_THUMBNAIL_URL};

        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_VIDEOS, String.valueOf(videoIndex));

        Cursor cursor = getActivity().getContentResolver().query(content_uri,
                projection, null, null, null);

        if (cursor.moveToFirst()) {
            videoTitleTextView.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));
            videoDescriptionTextView.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_DESCRIPTION)));
            Picasso.with(getActivity().getApplicationContext())
                    .load(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_THUMBNAIL_URL)))
                    .placeholder(R.drawable.no_thumbnail)
                    .error(R.drawable.no_thumbnail)
                    .into(videoThumbnailImageView);

            videoId = cursor.getString(cursor.getColumnIndex(AppDatabase.COL_VIDEO_ID));

            if (cursor.getInt(cursor.getColumnIndex(AppDatabase.COL_BOOKMARKED)) > 0) {
                isBookmarked = true;
            }
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.video_detail, menu);
        actionBookmark = menu.findItem(R.id.action_bookmark_video);
        if (isBookmarked) {
            actionBookmark.setIcon(R.drawable.ic_action_remove_bookmark);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_play_video) {
            OnWatchVideoClickedListener listener = (OnWatchVideoClickedListener) getActivity();
            listener.onWatchVideoClicked(videoId);
            return true;
        }
        if (id == R.id.action_bookmark_video) {
            Log.d(DEBUG_TAG, "action_bookmark_video");
            OnBookmarkVideoClickedListener listener = (OnBookmarkVideoClickedListener) getActivity();
            listener.onBookmarkVideoClicked(videoIndex, !isBookmarked);
            if (!isBookmarked) {
                actionBookmark.setIcon(R.drawable.ic_action_remove_bookmark);
            } else {
                actionBookmark.setIcon(R.drawable.ic_action_add_bookmark);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public interface OnWatchVideoClickedListener {
        public void onWatchVideoClicked(String videoId);
    }

    public interface OnBookmarkVideoClickedListener {
        public void onBookmarkVideoClicked(long videoIndex, boolean bookmarked);
    }
}
