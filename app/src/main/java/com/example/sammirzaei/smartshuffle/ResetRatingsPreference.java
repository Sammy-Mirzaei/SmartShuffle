package com.example.sammirzaei.smartshuffle;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

/**
 * Created by sammirzaei on 5/24/2016.
 */
public class ResetRatingsPreference extends DialogPreference {

    public ResetRatingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);


        setDialogLayoutResource(R.layout.reset_ratings_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);


        setDialogIcon(null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK"
        if (positiveResult) {
            boolean result = MainActivity.resetRatings();
            if(result){
                Toast.makeText(this.getContext(), "done", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
