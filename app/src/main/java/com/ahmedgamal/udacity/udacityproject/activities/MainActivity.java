package com.ahmedgamal.udacity.udacityproject.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ahmedgamal.udacity.udacityproject.R;
import com.ahmedgamal.udacity.udacityproject.Utility;
import com.ahmedgamal.udacity.udacityproject.fragments.MovieDetailsFragment;
import com.ahmedgamal.udacity.udacityproject.fragments.MoviesFragment;
import com.ahmedgamal.udacity.udacityproject.sync.MoviesSyncAdapter;

public class MainActivity extends AppCompatActivity implements MoviesFragment.Callback {

    private static final String MOVIE_DETAILS_FRAGMENT_TAG = "mdf";
    private String mSortingOrder;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSortingOrder = Utility.getSortingOrder(this);

        if (findViewById(R.id.movie_details_container) != null)
            mTwoPane = true;

        MoviesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortingOrder = Utility.getSortingOrder(this);

        if (sortingOrder != null && !sortingOrder.equals(mSortingOrder)) {
            MoviesFragment moviesFragment = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movies_fragment);
            if (null != moviesFragment)
                moviesFragment.onSortingChanged(sortingOrder);

            if (mTwoPane)
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_details_container, new Fragment(), MOVIE_DETAILS_FRAGMENT_TAG)
                        .commit();
            mSortingOrder = sortingOrder;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri uri, String movieId) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailsFragment.DETAIL_URI, uri);
            arguments.putString(MovieDetailsFragment.MOVIE_ID, movieId);

            MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
            movieDetailsFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container, movieDetailsFragment, MOVIE_DETAILS_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailsActivity.class)
                    .setData(uri);
            intent.putExtra(MovieDetailsFragment.MOVIE_ID, movieId);
            startActivity(intent);
        }
    }
}
