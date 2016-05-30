package com.example.sammirzaei.smartshuffle;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by sammirzaei on 4/21/2016.
 */
public class Pop extends Activity implements RatingBar.OnRatingBarChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupwindow);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int length = (int)((width < height ? width : height)*0.6);
        getWindow().setLayout(length, length);
        getWindow().setLayout(850, 850);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.popLayout);
        layout.getWidth();

        Toast.makeText(getApplicationContext(), String.valueOf(layout.getWidth()) , Toast.LENGTH_SHORT).show();

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(this);

        float rating = 5;
        if(MainActivity.currentSong != null){
            rating = (float)(MainActivity.currentSong.getImportance()*5);
        }
        ratingBar.setRating(rating);

    }


    @Override
    public void onRatingChanged (RatingBar ratingBar, float rating, boolean fromUser){
        if(MainActivity.currentSong != null){
            MainActivity.currentSong.setImportance((double)(rating/5));
        }

    }
}
