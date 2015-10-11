package com.ahmedgamal.udacity.udacityproject.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ahmedgamal.udacity.udacityproject.R;
import com.ahmedgamal.udacity.udacityproject.Utility;
import com.ahmedgamal.udacity.udacityproject.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = MoviesSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final String API_KEY_PARAM = "api_key";
    private static final String API_KEY = "6292699fe133d7e5efaa9166bdf08448";

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Performing sync");
        getDataFromMoviesApi();
    }

    private void getDataFromMoviesApi() {
        final String MOVIES_URL = "http://api.themoviedb.org/3/discover/movie?";
        final String SORT_PARAM = "sort_by";
        final String VOTE_COUNT_GTE_PARAM = "vote_count.gte";
        final String SORT_BY_MOST_POPULAR = "popularity.desc";
        final String SORT_BY_TOP_RATED = "vote_average.desc";

        String sortingOrder = Utility.getSortingOrder(getContext());
        String sortByQuery;
        if (sortingOrder.equals(getContext().getString(R.string.pref_sorting_rate)))
            sortByQuery = SORT_BY_TOP_RATED;
        else
            sortByQuery = SORT_BY_MOST_POPULAR; //Popular is the default sorting

        Uri builtUri = Uri.parse(MOVIES_URL).buildUpon()
                .appendQueryParameter(SORT_PARAM, sortByQuery)
                .appendQueryParameter(VOTE_COUNT_GTE_PARAM, Integer.toString(100)) // Movies with this low number of votes doesn't deserve to be here :)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        try {
            final String response = getResponse(builtUri);
            if (response != null)
                parseAndSyncDataFromResponse(response);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Nullable
    private String getResponse(Uri uri) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            if (buffer.length() == 0) {
                return null;
            }

            return buffer.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void parseAndSyncDataFromResponse(String response) throws JSONException {
        Log.i(TAG, "Parsing data for movies");
        final String JSON_KEY_RESULTS = "results";
        final String JSON_KEY_ID = "id";
        final String JSON_KEY_TITLE = "title";
        final String JSON_KEY_OVERVIEW = "overview";
        final String JSON_KEY_DATE = "release_date";
        final String JSON_KEY_POSTER = "poster_path";
        final String JSON_KEY_RATING = "vote_average";
        final String JSON_KEY_POPULARITY = "popularity";

        JSONObject object = new JSONObject(response);
        JSONArray resultsArray = object.getJSONArray(JSON_KEY_RESULTS);

        final int length = resultsArray.length();
        ContentValues[] contentValuesArray = new ContentValues[length];
        String[] moviesIds = new String[length];
        for (int i = 0; i < length; i++) {
            try {
                JSONObject movieObject = resultsArray.getJSONObject(i);
                String id = movieObject.getString(JSON_KEY_ID);
                String title = movieObject.getString(JSON_KEY_TITLE);
                String overview = movieObject.getString(JSON_KEY_OVERVIEW);
                String year = movieObject.getString(JSON_KEY_DATE).substring(0, 4);
                String posterPath = movieObject.getString(JSON_KEY_POSTER);
                double rating = movieObject.getDouble(JSON_KEY_RATING);
                double popularity = movieObject.getDouble(JSON_KEY_POPULARITY);

                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, id);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, title);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_DESCRIPTION, overview);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_YEAR, year);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_THUMBNAIL_URL, posterPath);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING, rating);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_POPULARITY, popularity);
                contentValuesArray[i] = contentValues;
                moviesIds[i] = id;
            } catch (Exception e){
                Log.e(TAG, e.getMessage(), e);
            }
        }

        getContext().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, contentValuesArray);

        syncMoviesTrailers(moviesIds);
        syncMoviesReviews(moviesIds);
        Log.i(TAG, "finished syncing");

        //TODO: Delete old records except favourites
    }

    private void syncMoviesTrailers(String[] moviesIds) throws JSONException {
        ArrayList<ContentValues> contentValuesArray = new ArrayList<>();
        for (String movieId : moviesIds) {
            if (movieId == null)
                continue;
            SystemClock.sleep(500); // To avoid getting file not found exception on heavy requesting on the api
            final String MOVIE_TRAILERS_URL = String.format("http://api.themoviedb.org/3/movie/%s/videos?", movieId);
            Uri builtUri = Uri.parse(MOVIE_TRAILERS_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .build();

            final String response = getResponse(builtUri);
            if (response == null) return;

            Log.i(TAG, "Parsing data for movies trailers");
            final String JSON_KEY_RESULTS = "results";
            final String JSON_KEY_ID = "id";
            final String JSON_KEY_KEY = "key";
            final String JSON_KEY_NAME = "name";
            final String JSON_KEY_TYPE = "type";
            final String JSON_KEY_SITE = "site";
            final String TYPE_TRAILER = "Trailer";
            final String SITE_YOUTUBE = "YouTube";
            final String YOUTUBE_VIDEO_URL = "http://www.youtube.com/watch?v=%s";

            JSONObject object = new JSONObject(response);
            JSONArray resultsArray = object.getJSONArray(JSON_KEY_RESULTS);

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject trailerObject = resultsArray.getJSONObject(i);
                String type = trailerObject.getString(JSON_KEY_TYPE);
                String site = trailerObject.getString(JSON_KEY_SITE);

                if (!type.equals(TYPE_TRAILER) || !site.equals(SITE_YOUTUBE)) continue;

                String id = trailerObject.getString(JSON_KEY_ID);
                String title = trailerObject.getString(JSON_KEY_NAME);
                String url = String.format(YOUTUBE_VIDEO_URL, trailerObject.getString(JSON_KEY_KEY));

                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MoviesTrailersEntry.COLUMN_MOVIE_ID, movieId);
                contentValues.put(MoviesContract.MoviesTrailersEntry.COLUMN_TRAILER_ID, id);
                contentValues.put(MoviesContract.MoviesTrailersEntry.COLUMN_TRAILER_TITLE, title);
                contentValues.put(MoviesContract.MoviesTrailersEntry.COLUMN_TRAILER_URL, url);

                contentValuesArray.add(contentValues);
            }
        }
        ContentValues[] cvArray = new ContentValues[contentValuesArray.size()];
        contentValuesArray.toArray(cvArray);
        getContext().getContentResolver().bulkInsert(MoviesContract.MoviesTrailersEntry.CONTENT_URI, cvArray);
    }

    private void syncMoviesReviews(String[] moviesIds) throws JSONException {
        ArrayList<ContentValues> contentValuesArray = new ArrayList<>();
        for (String movieId : moviesIds) {
            if (movieId == null)
                continue;
            SystemClock.sleep(500); // To avoid getting file not found exception on heavy requesting on the api
            final String MOVIE_TRAILERS_URL = String.format("http://api.themoviedb.org/3/movie/%s/reviews?", movieId);
            Uri builtUri = Uri.parse(MOVIE_TRAILERS_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .build();

            final String response = getResponse(builtUri);
            if (response == null) return;

            Log.i(TAG, "Parsing data for movies reviews");

            final String JSON_KEY_ID = "id";
            final String JSON_KEY_RESULTS = "results";
            final String JSON_KEY_AUTHOR = "author";
            final String JSON_KEY_CONTENT = "content";


            JSONObject object = new JSONObject(response);
            JSONArray resultsArray = object.getJSONArray(JSON_KEY_RESULTS);

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject reviewObject = resultsArray.getJSONObject(i);

                String id = reviewObject.getString(JSON_KEY_ID);
                String author = reviewObject.getString(JSON_KEY_AUTHOR);
                String content = reviewObject.getString(JSON_KEY_CONTENT);

                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MoviesReviewsEntry.COLUMN_MOVIE_ID, movieId);
                contentValues.put(MoviesContract.MoviesReviewsEntry.COLUMN_REVIEW_ID , id);
                contentValues.put(MoviesContract.MoviesReviewsEntry.COLUMN_REVIEW_AUTHOR, author);
                contentValues.put(MoviesContract.MoviesReviewsEntry.COLUMN_REVIEW_CONTENT, content);

                contentValuesArray.add(contentValues);
            }

        }
        ContentValues[] cvArray = new ContentValues[contentValuesArray.size()];
        contentValuesArray.toArray(cvArray);
        getContext().getContentResolver().bulkInsert(MoviesContract.MoviesReviewsEntry.CONTENT_URI, cvArray);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {

        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        syncImmediately(context);
    }

    private static void configurePeriodicSync(Context context, int syncInterval, int syncFlextime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, syncFlextime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
