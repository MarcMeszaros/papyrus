package ca.marcmeszaros.papyrus;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

public class Papyrus extends Application {

    private static final String TAG = "Papyrus";

    @Override
    public void onCreate() {
        super.onCreate();

        // if we are in release mode
        if(!BuildConfig.DEBUG) {
            Log.i(TAG, "Starting in release mode.");
        } else {
            Log.i(TAG, "Starting in debug mode.");
        }
    }
}
