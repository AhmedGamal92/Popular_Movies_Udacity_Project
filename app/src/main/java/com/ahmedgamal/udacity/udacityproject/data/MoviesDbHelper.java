package com.ahmedgamal.udacity.udacityproject.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ahmedgamal.udacity.udacityproject.data.MoviesContract.MoviesEntry;
import com.ahmedgamal.udacity.udacityproject.data.MoviesContract.MoviesReviewsEntry;
import com.ahmedgamal.udacity.udacityproject.data.MoviesContract.MoviesTrailersEntry;

/**
 * Manages the movies database.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final String TAG = MoviesDbHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // For testing
//        super(context, context.getExternalFilesDir(null) + "/db/" + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "Creating " + DATABASE_NAME + " database");
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_THUMBNAIL_URL + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_YEAR + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_DURATION + " TEXT ," +
                MoviesEntry.COLUMN_MOVIE_DESCRIPTION + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_RATING + " REAL NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_POPULARITY + " REAL NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_IS_FAVOURITE + " INTEGER DEFAULT 0, " +

                " UNIQUE (" + MoviesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_MOVIES_TRAILERS_TABLE = "CREATE TABLE " + MoviesTrailersEntry.TABLE_NAME + " (" +
                MoviesTrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                MoviesTrailersEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MoviesTrailersEntry.COLUMN_TRAILER_ID + " TEXT NOT NULL, " +
                MoviesTrailersEntry.COLUMN_TRAILER_TITLE + " TEXT NOT NULL, " +
                MoviesTrailersEntry.COLUMN_TRAILER_URL + " TEXT NOT NULL, " +

                " UNIQUE (" + MoviesTrailersEntry.COLUMN_TRAILER_ID + ") ON CONFLICT IGNORE, " +

                " FOREIGN KEY (" + MoviesTrailersEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry.COLUMN_MOVIE_ID + "));";


        final String SQL_CREATE_MOVIES_REVIEWS_TABLE = "CREATE TABLE " + MoviesReviewsEntry.TABLE_NAME + " (" +
                MoviesReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                MoviesReviewsEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MoviesReviewsEntry.COLUMN_REVIEW_ID + " TEXT NOT NULL, " +
                MoviesReviewsEntry.COLUMN_REVIEW_AUTHOR + " TEXT NOT NULL, " +
                MoviesReviewsEntry.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL, " +

                " UNIQUE (" + MoviesReviewsEntry.COLUMN_REVIEW_ID + ") ON CONFLICT IGNORE, " +

                " FOREIGN KEY (" + MoviesTrailersEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry.COLUMN_MOVIE_ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TRAILERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesTrailersEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesReviewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
