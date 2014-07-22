package com.appinforium.newthinktanktutorials.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appinforium.newthinktanktutorials.R;
import com.appinforium.newthinktanktutorials.data.AppDatabase;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class VideosCursorAdapter extends CursorAdapter {
    private LayoutInflater inflater;
    private Context mContext;
    private String[] loadingColors;
    private int max;
    private int lastPosition = -1;

    public VideosCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        inflater = LayoutInflater.from(context);
        loadingColors = context.getResources().getStringArray(R.array.thumbnail_loading_colors);
        max = loadingColors.length - 1;
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View itemLayout = inflater.inflate(R.layout.grid_item_videos, null);

        ViewHolder holder = new ViewHolder();

        Random rand = new Random();
        int randomNum = rand.nextInt((max) + 1);

        ColorDrawable loadingColorDrawable = new ColorDrawable(Color.parseColor(loadingColors[randomNum]));

        Bitmap bitmap = Bitmap.createBitmap(320, 180, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        loadingColorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        loadingColorDrawable.draw(canvas);

        holder.thumbnailImageView = (ImageView) itemLayout.findViewById(R.id.gridItemVideoThumbnailImageView);
        holder.thumbnailLoadingBitmap = bitmap;
        holder.progressBar = (ProgressBar) itemLayout.findViewById(R.id.gridItemVideoProgressBar);

        holder.progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#b02224"), PorterDuff.Mode.MULTIPLY);

        holder.titleTextView = (TextView) itemLayout.findViewById(R.id.gridItemVideoTitleTextView);

        itemLayout.setTag(holder);
        return itemLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.titleTextView.setText(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));

        int playTime = cursor.getInt(cursor.getColumnIndex(AppDatabase.COL_PLAY_TIME));

        if (playTime > 0) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setMax(cursor.getInt(cursor.getColumnIndex(AppDatabase.COL_DURATION)));
            holder.progressBar.setProgress(playTime);
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }

        Picasso.with(context)
                .load(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_THUMBNAIL_URL)))
                .placeholder(new BitmapDrawable(holder.thumbnailLoadingBitmap))
                .error(R.drawable.no_thumbnail)
                .into(holder.thumbnailImageView);
    }

    private static class ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        Bitmap thumbnailLoadingBitmap;
        ProgressBar progressBar;
    }
}
