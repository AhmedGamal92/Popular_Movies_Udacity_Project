package com.ahmedgamal.udacity.udacityproject.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ahmedgamal.udacity.udacityproject.R;
import com.ahmedgamal.udacity.udacityproject.Utility;
import com.ahmedgamal.udacity.udacityproject.data.MoviesContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Ahmed Gamal on 10/9/15.
 */
public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DETAIL_URI = "URI";
    public static final String MOVIE_ID = "MID";
    private static final int MOVIE_DETAILS_LOADER = 1;
    private static final int MOVIE_Trailers_LOADER = 2;
    private static final int MOVIE_Reviews_LOADER = 3;
    private static final String SHARE_HASHTAG = " #Popular_Movies";

    private Uri mUri;
    private ImageView mPosterIV;
    private TextView mTitleTV;
    private TextView mYearTV;
    private TextView mRatingTV;
    private ImageView mFavouriteIV;
    private ListView mTrailersList;
    private ListView mReviewsList;
    private TextView mDescriptionTv;
    private SimpleCursorAdapter mTrailersCursorAdapter;
    private SimpleCursorAdapter mReviewsCursorAdapter;
    private String mMovieId;
    private ShareActionProvider mShareActionProvider;

    private String mFirstTrailer;

    public MovieDetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DETAIL_URI);
            mMovieId = arguments.getString(MOVIE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        mPosterIV = (ImageView) view.findViewById(R.id.movie_poster);
        mTitleTV = (TextView) view.findViewById(R.id.movie_title);
        mYearTV = (TextView) view.findViewById(R.id.movie_year);
        mRatingTV = (TextView) view.findViewById(R.id.movie_rating);
        mDescriptionTv = (TextView) view.findViewById(R.id.movie_description);
        mFavouriteIV = (ImageView) view.findViewById(R.id.movie_favourite);
        mTrailersList = (ListView) view.findViewById(R.id.movie_trailers_list);
        mReviewsList = (ListView) view.findViewById(R.id.movie_reviews_list);

        mTrailersCursorAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.trailer_item,
                null,
                new String[]{MoviesContract.MoviesTrailersEntry.COLUMN_TRAILER_TITLE},
                new int[]{R.id.trailer_title},
                0);
        mTrailersList.setAdapter(mTrailersCursorAdapter);
        mTrailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                final String url = cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesTrailersEntry.COLUMN_TRAILER_URL));

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

        mReviewsCursorAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.review_item,
                null,
                new String[]{MoviesContract.MoviesReviewsEntry.COLUMN_REVIEW_AUTHOR, MoviesContract.MoviesReviewsEntry.COLUMN_REVIEW_CONTENT},
                new int[]{R.id.review_author, R.id.review_content},
                0);
        mReviewsList.setAdapter(mReviewsCursorAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAILS_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_Trailers_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_Reviews_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIE_DETAILS_LOADER:
                if (null != mUri) {
                    return new CursorLoader(
                            getActivity(),
                            mUri,
                            null,
                            MoviesContract.MoviesEntry._ID + " = ?",
                            new String[]{Long.toString(ContentUris.parseId(mUri))},
                            null);
                }
                break;
            case MOVIE_Trailers_LOADER:
                if (null != mUri && null != mMovieId) {
                    return new CursorLoader(
                            getActivity(),
                            MoviesContract.MoviesTrailersEntry.buildMovieTrailersWithMovieIdUri(mMovieId),
                            null,
                            MoviesContract.MoviesTrailersEntry.COLUMN_MOVIE_ID + " = ? ",
                            new String[]{mMovieId},
                            null);
                }
                break;
            case MOVIE_Reviews_LOADER:
                if (null != mUri && null != mMovieId) {
                    return new CursorLoader(
                            getActivity(),
                            MoviesContract.MoviesReviewsEntry.buildMovieReviewsWithMovieIdUri(mMovieId),
                            null,
                            MoviesContract.MoviesReviewsEntry.COLUMN_MOVIE_ID + " = ? ",
                            new String[]{mMovieId},
                            null);
                }
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader == null || data == null) return;

        switch (loader.getId()) {
            case MOVIE_DETAILS_LOADER:
                if (data.moveToFirst())
                    fillMovieDetails(data);
                break;
            case MOVIE_Trailers_LOADER:
                if (data.moveToFirst()) {
                    mFirstTrailer = String.format("%s : %s", data.getString(data.getColumnIndex(MoviesContract.MoviesTrailersEntry.COLUMN_TRAILER_TITLE)),
                            data.getString(data.getColumnIndex(MoviesContract.MoviesTrailersEntry.COLUMN_TRAILER_URL)));
                    if (mShareActionProvider != null)
                        mShareActionProvider.setShareIntent(createShareTrailerIntent());
                }

                mTrailersCursorAdapter.swapCursor(data);
                Utility.setListViewHeightBasedOnChildren(mTrailersList);
                break;
            case MOVIE_Reviews_LOADER:
                mReviewsCursorAdapter.swapCursor(data);
                Utility.setListViewHeightBasedOnChildren(mReviewsList);
                break;
        }
    }

    private void fillMovieDetails(final Cursor cursor) {
        Picasso.with(getActivity()).
                load(String.format(Utility.POSTER_IMAGE_URL, cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_THUMBNAIL_URL)))).
                fit().placeholder(R.drawable.place_holder).
                into(mPosterIV);
        mTitleTV.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE)));
        mYearTV.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_YEAR)));
        Double rating = cursor.getDouble(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING));
        mRatingTV.setText(String.format("%.1f/10", rating));
        mDescriptionTv.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_DESCRIPTION)));
        final boolean isFavourite = cursor.getInt(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_IS_FAVOURITE)) == 1;
        mFavouriteIV.setImageResource(isFavourite ? R.drawable.favourite : R.drawable.not_favourite);
        mFavouriteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_IS_FAVOURITE, !isFavourite);
                getActivity().getContentResolver().update(
                        MoviesContract.MoviesEntry.CONTENT_URI,
                        values,
                        MoviesContract.MoviesEntry._ID + " = ? ",
                        new String[]{Long.toString(cursor.getLong(cursor.getColumnIndex(MoviesContract.MoviesEntry._ID)))}
                );
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        final int id = loader.getId();
        switch (id) {
            case MOVIE_Trailers_LOADER:
                mTrailersCursorAdapter.swapCursor(null);
                break;
            case MOVIE_Reviews_LOADER:
                mReviewsCursorAdapter.swapCursor(null);
                break;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_details_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mFirstTrailer != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFirstTrailer + SHARE_HASHTAG);
        return shareIntent;
    }
}
