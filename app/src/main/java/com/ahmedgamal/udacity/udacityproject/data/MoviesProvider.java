package com.ahmedgamal.udacity.udacityproject.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MoviesProvider extends ContentProvider {

    private MoviesDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int MOVIES = 900;
    static final int TRAILERS = 901;
    static final int REVIEWS = 902;
    static final int MOVIE_TRAILERS = 903;
    static final int MOVIE_REVIEWS = 904;
    static final int MOVIE_WITH_ID = 905;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_TRAILERS, TRAILERS);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_REVIEWS, REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_TRAILERS + "/*", MOVIE_TRAILERS);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES_REVIEWS + "/*", MOVIE_REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*", MOVIE_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor resultCursor;
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                resultCursor = db.query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_TRAILERS: // "trailers/*"
                resultCursor = db.query(
                        MoviesContract.MoviesTrailersEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_REVIEWS: // "reviews/*"
                resultCursor = db.query(
                        MoviesContract.MoviesReviewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_WITH_ID: // "movies/*"
                resultCursor = db.query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext()!= null)
            resultCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return resultCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case TRAILERS:
                return MoviesContract.MoviesTrailersEntry.CONTENT_TYPE;
            case REVIEWS:
                return MoviesContract.MoviesReviewsEntry.CONTENT_TYPE;
            case MOVIE_TRAILERS:
                return MoviesContract.MoviesTrailersEntry.CONTENT_TYPE;
            case MOVIE_REVIEWS:
                return MoviesContract.MoviesReviewsEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri resultUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    resultUri = MoviesContract.MoviesEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILERS: {
                long _id = db.insert(MoviesContract.MoviesTrailersEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    resultUri = MoviesContract.MoviesTrailersEntry.buildMovieTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long _id = db.insert(MoviesContract.MoviesReviewsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    resultUri = MoviesContract.MoviesReviewsEntry.buildMovieReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_TRAILERS:
                rowsDeleted = db.delete(MoviesContract.MoviesTrailersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_REVIEWS:
                rowsDeleted = db.delete(MoviesContract.MoviesReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0 && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIE_TRAILERS:
                rowsUpdated = db.update(MoviesContract.MoviesTrailersEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIE_REVIEWS:
                rowsUpdated = db.update(MoviesContract.MoviesReviewsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0 && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int resultCount = 0;
        switch (match) {
            case MOVIES: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        if (value == null) continue;
                        long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                            resultCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case TRAILERS: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        if (value == null) continue;
                        long _id = db.insert(MoviesContract.MoviesTrailersEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                            resultCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case REVIEWS: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        if (value == null) continue;
                        long _id = db.insert(MoviesContract.MoviesReviewsEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                            resultCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            default:
                return super.bulkInsert(uri, values);
        }
        if (getContext() != null && resultCount != 0)
                getContext().getContentResolver().notifyChange(uri, null);
        return resultCount;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
