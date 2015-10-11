package com.ahmedgamal.udacity.udacityproject.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class MoviesAuthenticatorService extends Service {

    private MoviesAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new MoviesAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
