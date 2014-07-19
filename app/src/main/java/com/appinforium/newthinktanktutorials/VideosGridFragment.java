package com.appinforium.newthinktanktutorials;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appinforium.newthinktanktutorials.adapter.VideosCursorAdapter;
import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;

public class VideosGridFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String DEBUG_TAG = "VideosGridFragment";
    public static final String NAME = DEBUG_TAG;
    public static final String PLAYLIST_ID = "PLAYLIST_ID_ARG";
    public static final String SELECTION = "SELECTION_ARG";
    public static final String SELECTION_ARGS = "SELECTION_ARGS_ARG";
    public static final String SORT_ORDER = "SORT_ORDER_ARG";
    public static final String EMPTY_MSG = "EMPTY_MSG_ARG";

    private String selection;
    private String[] selectionArgs;
    private String sortOrder;

    private GridView videosGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_videos_grid, container, false);

        videosGridView = (GridView) view.findViewById(R.id.videosGridView);

        videosGridView.setOnItemClickListener(this);

        TextView emptyTextView = (TextView) view.findViewById(R.id.empty);
        emptyTextView.setTextColor(Color.parseColor("#000000"));

        videosGridView.setEmptyView(emptyTextView);

        Bundle args = getArguments();
        selection = args.getString(SELECTION);
        selectionArgs = args.getStringArray(SELECTION_ARGS);
        sortOrder = args.getString(SORT_ORDER);
        String emptyMsg = args.getString(EMPTY_MSG);

        if (emptyMsg != null) {
            emptyTextView.setText(emptyMsg);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] projection = {AppDatabase.COL_ID, AppDatabase.COL_THUMBNAIL_URL,
                AppDatabase.COL_TITLE};

        Cursor cursor = getActivity().getContentResolver().query(AppDataContentProvider.CONTENT_URI_VIDEOS,
                projection, selection, selectionArgs, sortOrder);

        VideosCursorAdapter adapter = new VideosCursorAdapter(getActivity(), cursor, true);
        videosGridView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        OnVideoSelectedListener listener = (OnVideoSelectedListener) getActivity();
        listener.onVideoSelected(id);
    }


    public interface OnVideoSelectedListener {
        public void onVideoSelected(long id);
    }
}
