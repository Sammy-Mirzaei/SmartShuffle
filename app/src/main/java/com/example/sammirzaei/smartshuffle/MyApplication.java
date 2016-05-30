package com.example.sammirzaei.smartshuffle;

import android.app.Application;

/**
 * Created by sammirzaei on 5/25/2016.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.
        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
    }

}
