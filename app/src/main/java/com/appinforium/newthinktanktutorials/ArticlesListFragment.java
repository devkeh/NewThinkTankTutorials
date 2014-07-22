package com.appinforium.newthinktanktutorials;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.appinforium.newthinktanktutorials.adapter.ArticlesCursorAdapter;
import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;

public class ArticlesListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String DEBUG_TAG = "ArticlesListFragment";
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_articles_list, container, false);

        listView = (ListView) view.findViewById(R.id.articlesListView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] projection = {AppDatabase.COL_ID, AppDatabase.COL_DESCRIPTION,
            AppDatabase.COL_TITLE, AppDatabase.COL_PUBLISHED_AT};

        Cursor cursor = getActivity().getContentResolver().query(AppDataContentProvider.CONTENT_URI_ARTICLES,
                projection, null, null, null);

        ArticlesCursorAdapter adapter = new ArticlesCursorAdapter(getActivity(), cursor, true);

        listView.setDivider(null);

        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter);

        getActivity().setTitle("Articles");
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        OnArticleClickedListener listener = (OnArticleClickedListener) getActivity();
        listener.onArticleClicked(id);
    }

    public interface OnArticleClickedListener {
        public void onArticleClicked(long id);
    }
}
