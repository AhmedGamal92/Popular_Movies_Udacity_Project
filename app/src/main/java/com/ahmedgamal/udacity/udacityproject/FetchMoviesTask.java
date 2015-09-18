package com.ahmedgamal.udacity.udacityproject;

import android.os.AsyncTask;

/**
 * Created by Ahmed Gamal on 9/14/15.
 */
public class FetchMoviesTask extends AsyncTask<Void, Void, Movie[]> {
    @Override
    protected Movie[] doInBackground(Void... params) {
        return new Movie[0];
    }
}
