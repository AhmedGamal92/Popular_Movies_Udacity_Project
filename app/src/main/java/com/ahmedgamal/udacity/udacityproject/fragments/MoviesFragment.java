package com.ahmedgamal.udacity.udacityproject.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.test.mock.MockContentProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ahmedgamal.udacity.udacityproject.MoviesAdapter;
import com.ahmedgamal.udacity.udacityproject.R;
import com.ahmedgamal.udacity.udacityproject.Utility;
import com.ahmedgamal.udacity.udacityproject.data.MoviesContract;
import com.ahmedgamal.udacity.udacityproject.sync.MoviesSyncAdapter;

/**
 * The fragment that contains the movies grid.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIES_LOADER = 0;
    private GridView mGridView;
    private MoviesAdapter mMoviesAdapter;
    private int mPosition = GridView.INVALID_POSITION;

    public interface Callback {
        void onItemSelected(Uri uri, String movieId);
    }

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
        mMoviesAdapter = new MoviesAdapter(getActivity(), null, 0);
        mGridView.setAdapter(mMoviesAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(MoviesContract.MoviesEntry.buildMovieUri(
                                    cursor.getLong(cursor.getColumnIndex(MoviesContract.MoviesEntry._ID))),
                                    cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID)));
                }
                mPosition = position;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String sorting = Utility.getSortingOrder(getActivity());
        String sortOrder = null;
        String selection = null;
        String[] selectionArgs = null;

        if (sorting.equals(getString(R.string.pref_sorting_rate)))
            sortOrder = MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING + " DESC";
        else if (sorting.equals(getString(R.string.pref_sorting_favourite))){
            selection = MoviesContract.MoviesEntry.COLUMN_MOVIE_IS_FAVOURITE + " = ? ";
            selectionArgs = new String[]{Integer.toString(1)};
        } else
            sortOrder = MoviesContract.MoviesEntry.COLUMN_MOVIE_POPULARITY + " DESC";

        return new CursorLoader(getActivity(),
                MoviesContract.MoviesEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviesAdapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }

    public void onSortingChanged(String newSorting){
        if (!newSorting.equals(getString(R.string.pref_sorting_favourite))) // No need to sync for favourites
            MoviesSyncAdapter.syncImmediately(getActivity());
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }
}
