package com.appinforium.newthinktanktutorials.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appinforium.newthinktanktutorials.R;
import com.appinforium.newthinktanktutorials.data.AppDatabase;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class PlaylistsCursorAdapter extends CursorAdapter {

    LayoutInflater inflater;
    String[] loadingColors;
    int max;
    private int lastPosition = -1;
    private Context mContext;

    public PlaylistsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        inflater = LayoutInflater.from(context);
        loadingColors = context.getResources().getStringArray(R.array.thumbnail_loading_colors);
        max = loadingColors.length - 1;
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View itemLayout = inflater.inflate(R.layout.grid_item_playlists, null);

        ViewHolder holder = new ViewHolder();

        Random rand = new Random();
        int randomNum = rand.nextInt((max) + 1);

        ColorDrawable loadingColorDrawable = new ColorDrawable(Color.parseColor(loadingColors[randomNum]));

        Bitmap bitmap = Bitmap.createBitmap(320, 180, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        loadingColorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        loadingColorDrawable.draw(canvas);

        holder.thumbnailImageView = (ImageView) itemLayout.findViewById(R.id.gridItemPlaylistsThumbnailImageView);
        holder.thumbnailLoadingBitmap = bitmap;

        holder.titleTextView = (TextView) itemLayout.findViewById(R.id.gridItemPlaylistsTitleTextView);
        holder.itemCountTextView = (TextView) itemLayout.findViewById(R.id.gridItemPlaylistsItemCountTextView);

//        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.up_from_bottom);
//        itemLayout.startAnimation(animation);
//        lastPosition = position;

        itemLayout.setTag(holder);
        return itemLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

//        int position = cursor.getPosition();
//        if (position > lastPosition) {
//            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.up_from_bottom);
//            view.startAnimation(animation);
//            lastPosition = position;
//        }
        holder.titleTextView.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));

        int videoCount = cursor.getInt(cursor.getColumnIndex(AppDatabase.COL_ITEM_COUNT)) -
                cursor.getInt(cursor.getColumnIndex(AppDatabase.COL_ITEM_COUNT_OFFSET));

//        String videoCount = cursor.getString(cursor.getColumnIndex(AppDatabase.COL_ITEM_COUNT)) + " Videos";
        holder.itemCountTextView.setText(String.valueOf(videoCount) + " Videos");

        Picasso.with(context)
                .load(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_THUMBNAIL_URL)))
                .placeholder(new BitmapDrawable(holder.thumbnailLoadingBitmap))
                .error(R.drawable.no_thumbnail)
                .into(holder.thumbnailImageView);
    }

    private static class ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView itemCountTextView;
        Bitmap thumbnailLoadingBitmap;
    }
}
