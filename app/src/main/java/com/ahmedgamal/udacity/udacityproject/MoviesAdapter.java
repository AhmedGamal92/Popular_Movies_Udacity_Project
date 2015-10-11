package com.ahmedgamal.udacity.udacityproject;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.ahmedgamal.udacity.udacityproject.data.MoviesContract;
import com.squareup.picasso.Picasso;

/**
 * Adapter to manage movies grid view.
 */
public class MoviesAdapter extends CursorAdapter {

    private final LayoutInflater mLayoutInflater;

    public MoviesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.item_movie, parent, false);

        ViewHolder holder = new ViewHolder(view);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String posterUrl = String.format(Utility.POSTER_IMAGE_URL, cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_THUMBNAIL_URL)));
        Picasso.with(context).load(posterUrl).fit().placeholder(R.drawable.place_holder).into(holder.moviePosterImage);
    }


    static class ViewHolder {
        ImageView moviePosterImage;

        ViewHolder(View view){
            moviePosterImage = (ImageView) view.findViewById(R.id.movie_poster);
        }
    }
}
