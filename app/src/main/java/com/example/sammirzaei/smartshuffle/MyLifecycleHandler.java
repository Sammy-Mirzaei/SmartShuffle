package com.example.sammirzaei.smartshuffle;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Created by sammirzaei on 5/25/2016.
 */
public class MyLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    // I use four separate variables here. You can, of course, just use two and
    // increment/decrement them instead of using four and incrementing them all.
    /*
    private int resumed;
    private int paused;
    private int started;
    private int stopped;
    */
    private SharedPreferences sharedPref = null;
    private Boolean pauseOnMinPref = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        android.util.Log.w("test", "application is in foreground: " + (resumed > paused));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        android.util.Log.w("test", "application is visible: " + (started > stopped));
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
        pauseOnMinPref = sharedPref.getBoolean(activity.getString(R.string.key_minimize_pause),false);

        if(!isApplicationVisible() && pauseOnMinPref){
            MainActivity.onPauseClicked(null);
        }
    }

    // If you want a static function you can use to check if your application is
    // foreground/background, you can use the following:

    // Replace the four variables above with these four
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;

    // And these two public static functions
    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    public static boolean isApplicationInForeground() {
        return resumed > paused;
    }

}
