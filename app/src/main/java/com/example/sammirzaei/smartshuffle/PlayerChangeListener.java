package com.example.sammirzaei.smartshuffle;

import android.media.MediaPlayer;

/**
 * Created by sammirzaei on 11/3/2015.
 */
public class PlayerChangeListener implements MediaPlayer.OnPreparedListener{


    @Override
    public void onPrepared (MediaPlayer mp){

        mp.start();


    }


}
