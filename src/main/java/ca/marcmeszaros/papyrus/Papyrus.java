package ca.marcmeszaros.papyrus;

import android.app.Application;

import timber.log.Timber;

public class Papyrus extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
