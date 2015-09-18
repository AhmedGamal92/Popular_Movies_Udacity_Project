package com.ahmedgamal.udacity.udacityproject;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * The fragment that contains the movies grid.
 */
public class MoviesFragment extends Fragment {

    private GridView mGridView;
    private MoviesAdapter mMoviesAdapter;

    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mGridView = (GridView) view.findViewById(R.id.movies_grid);
        mMoviesAdapter = new MoviesAdapter(getActivity(), new ArrayList<Movie>());
        mGridView.setAdapter(mMoviesAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        new FetchMoviesTask().execute();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, Movie[]> {
        @Override
        protected Movie[] doInBackground(Void... params) {
            Movie[] movies = new Movie[3];
            movies[0] = new Movie("test1", "http://image.tmdb.org/t/p/w185/kqjL17yufvn9OVLyXYpvtyrFfak.jpg");
            movies[1] = new Movie("test2", "http://image.tmdb.org/t/p/w185/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg");
            movies[2] = new Movie("test3", "http://image.tmdb.org/t/p/w185/uXZYawqUsChGSj54wcuBtEdUJbh.jpg");
            return movies;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            if (movies != null && mMoviesAdapter != null) {
                mMoviesAdapter.clear();
                for (Movie movie : movies) {
                    mMoviesAdapter.add(movie);
                }
//                mMoviesAdapter.addAll(movies);
            }
        }
    }
}
