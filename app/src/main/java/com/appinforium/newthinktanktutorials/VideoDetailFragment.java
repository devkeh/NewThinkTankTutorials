package com.appinforium.newthinktanktutorials;

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;

public class VideoDetailFragment extends Fragment {

    private static final String DEBUG_TAG = "VideoDetailFragment";
    public static final String VIDEO_INDEX = "VIDEO_INDEX";

    private TextView videoTitleTextView;
    private TextView videoDescriptionTextView;
    private Button watchVideoButton;
    private long videoIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_detail, container, false);

        videoTitleTextView = (TextView) view.findViewById(R.id.videoTitleTextView);
        videoDescriptionTextView = (TextView) view.findViewById(R.id.videoDescriptionTextView);
        watchVideoButton = (Button) view.findViewById(R.id.watchVideoButton);

        Bundle args = getArguments();
        videoIndex = args.getLong(VIDEO_INDEX);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] projection = {AppDatabase.COL_ID, AppDatabase.COL_DESCRIPTION,
                AppDatabase.COL_TITLE, AppDatabase.COL_VIDEO_ID};

        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_VIDEOS, String.valueOf(videoIndex));

        Cursor cursor = getActivity().getContentResolver().query(content_uri,
                projection, null, null, null);

        if (cursor.moveToFirst()) {
            videoTitleTextView.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));
            videoDescriptionTextView.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_DESCRIPTION)));
        }
    }

}
