package ca.marcmeszaros.papyrus;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

public class Papyrus extends Application {

    private static final String TAG = "Papyrus";
    private static Papyrus instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // print the version
        Log.i(TAG, String.format("+++ BUILD VERSION: %s(%d) +++", getVersionName(), getVersionCode()));

        // if we are in release mode
        if(!BuildConfig.DEBUG) {
            Log.i(TAG, "Starting in release mode.");
        } else {
            Log.i(TAG, "Starting in debug mode.");
        }
    }

    /**
     * Get the application context.
     *
     * @return the application {@link android.content.Context}
     */
    public static Context getContext() {
        return instance;
    }

    /**
     * Return the application's version code defined in the manifest file.
     *
     * @return int of the android application version code
     */
    public static int getVersionCode() {
        try {
            PackageInfo packageInfo = instance.getPackageManager().getPackageInfo(instance.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    /**
     * Return the applications version name defined in the manifest file.
     *
     * @return a string representing the version name
     */
    public static String getVersionName() {
        try {
            PackageInfo packageInfo = instance.getPackageManager().getPackageInfo(instance.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
