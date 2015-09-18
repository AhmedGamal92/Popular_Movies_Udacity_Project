package com.ahmedgamal.udacity.udacityproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter to manage movies grid view.
 */
public class MoviesAdapter extends ArrayAdapter<Movie> {

    private final LayoutInflater mLayoutInflater;

    public MoviesAdapter(Context context, ArrayList<Movie> movies) {
        super(context, 0, movies);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.item_movie, parent, false);
            initViews(holder, view);
            view.setTag(holder);
        } else
            holder = (ViewHolder) view.getTag();

        Movie movie = getItem(position);
        setViewsValues(holder, movie);
        return view;
    }

    private void setViewsValues(ViewHolder holder, Movie movie) {
        Picasso.with(getContext()).load(movie.getImageUrl()).placeholder(R.drawable.place_holder).fit().into(holder.moviePosterImage);
    }

    private void initViews(ViewHolder holder, View view) {
        holder.moviePosterImage = (ImageView) view.findViewById(R.id.movie_poster);
    }

    static class ViewHolder {
        ImageView moviePosterImage;
    }
}
