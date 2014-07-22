package com.appinforium.newthinktanktutorials.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.appinforium.newthinktanktutorials.R;
import com.appinforium.newthinktanktutorials.data.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ArticlesCursorAdapter extends CursorAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private int lastPosition = -1;

    public ArticlesCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        inflater = LayoutInflater.from(context);
        this.mContext = context;
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

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d LLL yyyy", Locale.US);
        String publishedDate = sdf.format(Date.parse(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_PUBLISHED_AT))));

        String description = Html.fromHtml(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_DESCRIPTION))).toString();

        holder.title.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));
        holder.description.setText(description);
        holder.publishedDate.setText("Posted on " + publishedDate);
    }

    private static class ViewHolder {
        TextView title;
        TextView description;
        TextView publishedDate;
    }
}
