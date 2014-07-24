package com.appinforium.newthinktanktutorials;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.appinforium.newthinktanktutorials.adapter.PlaylistsCursorAdapter;
import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;

public class PlaylistsGridFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String DEBUG_TAG = "PlaylistsGridFragment";
    public static final String NAME = DEBUG_TAG;
    private GridView playlistsGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_playlists_grid, container, false);

        playlistsGridView = (GridView) view.findViewById(R.id.playlistsGridView);

        playlistsGridView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] projection = {AppDatabase.COL_ID, AppDatabase.COL_THUMBNAIL_URL,
                AppDatabase.COL_TITLE, AppDatabase.COL_ITEM_COUNT, AppDatabase.COL_ITEM_COUNT_OFFSET};

        Cursor cursor = getActivity().getContentResolver().query(AppDataContentProvider.CONTENT_URI_PLAYLISTS,
                projection, null, null, null);

        PlaylistsCursorAdapter adapter = new PlaylistsCursorAdapter(getActivity(), cursor, true);
        playlistsGridView.setAdapter(adapter);
        getActivity().setTitle(getResources().getString(R.string.app_name));

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        OnPlaylistSelectedListener listener = (OnPlaylistSelectedListener) getActivity();
        listener.onPlaylistSelected(id);

    }


    public interface OnPlaylistSelectedListener {
        public void onPlaylistSelected(long id);
    }

}
