package com.ahmedgamal.udacity.udacityproject.data;

import android.test.AndroidTestCase;

/**
 * Created by Ahmed Gamal on 10/6/15.
 */
public class TestDB extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        // delete database before every test
        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
    }


}
