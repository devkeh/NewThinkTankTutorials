package com.appinforium.newthinktanktutorials.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.appinforium.newthinktanktutorials.R;
import com.appinforium.newthinktanktutorials.data.AppDatabase;

public class ArticlesCursorAdapter extends CursorAdapter {

    LayoutInflater inflater;

    public ArticlesCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        View itemLayout = inflater.inflate(R.layout.list_item_articles, null);

        ViewHolder holder = new ViewHolder();

        holder.title = (TextView) itemLayout.findViewById(R.id.listItemArticleTitleTextView);
        holder.description = (TextView) itemLayout.findViewById(R.id.listItemArticleDescriptionTextView);
        holder.publishedDate = (TextView) itemLayout.findViewById(R.id.listItemArticlePublishedDateTextView);

        itemLayout.setTag(holder);
        return itemLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.title.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));
        holder.description.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_DESCRIPTION)));
        holder.publishedDate.setText("Posted on " + cursor.getString(cursor.getColumnIndex(AppDatabase.COL_PUBLISHED_AT)));
    }

    private static class ViewHolder {
        TextView title;
        TextView description;
        TextView publishedDate;
    }
}
