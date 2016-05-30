package com.example.sammirzaei.smartshuffle;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedList;
import android.content.IntentFilter;


public class MainActivity extends ActionBarActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, SeekBar.OnSeekBarChangeListener, RatingBar.OnRatingBarChangeListener {

    //constants
    protected static final int SONG_ACTIVITY_INTENT_REQUEST_CODE = 80;
    protected static final int SONG_PICKED = 90;
    protected static final int MAKE_PLAY_VISIBLE = 50;
    protected static final int MAKE_PAUSE_VISIBLE = 60;
    protected static final int MAKE_ALL_DISABLE = 70;
    private static final int UPDATE_SEEKBAR = 1;
    private static final String TAG = "MainActivity";
    protected static final String FILENAME = "data_file";

    //media player and song variables
    protected static ArrayList<Song> songs;
    private static LinkedList<Integer> indexes;
    private static MediaPlayer mMediaPlayer;
    private PlayerChangeListener mChangeListener;       //should try to use this for state changes...
    private MusicIntentReceiver myReceiver;
    protected static Song currentSong = null;

    //view elements
    private static TextView songName = null;
    private static TextView artistName = null;
    private static TextView elapsedTextView = null;
    private static TextView durTextView = null;
    private static SeekBar seekBar = null;
    private static ImageView imgView = null;
    private static RatingBar ratingBar = null;

    //buttons
    private static ImageButton playButton;
    private static ImageButton pauseButton;
    private static ImageButton nextButton;
    private static ImageButton previousButton;

    //file operations
    protected static String[] files;

    //app context
    protected static Context context;

    //headset plug in check
    protected static boolean isHeadsetPlugged = false;

    //for reading preferences
    protected static SharedPreferences sharedPref = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //initialize the main songs array list and the indexes of songs that are played
        songs = new ArrayList<>();
        indexes = new LinkedList<>();

        //make sure the default prefs are set, false argument at the end does not change the prefs if they have been changed already
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //get app context and start looking for media files and make the songs list
        context = getApplicationContext();
        files = fileList();
        ArrayList<Song> fromFile = FileFetcher.getFromFile(files, context);
        ArrayList<Song> fromDevice = FileFetcher.getFromDevice(context);
        songs = FileFetcher.consolidateImportance(fromDevice,fromFile,context);

        initializeMediaPlayer();
        initializeUiElements();

        //for detecting headset plug in and unplug
        myReceiver = new MusicIntentReceiver();
    }

    private void initializeMediaPlayer(){

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mChangeListener = new PlayerChangeListener();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    private void initializeUiElements(){

        elapsedTextView = (TextView)findViewById(R.id.elapsed_textView);
        durTextView = (TextView)findViewById(R.id.dur_textView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        imgView = (ImageView) findViewById(R.id.imageView);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(this);
        ratingBar.setRating(5);
        songName = (TextView)findViewById(R.id.song_name_textView);
        songName.setFocusable(true);
        artistName = (TextView)findViewById(R.id.artist_name_textView);
        imgView.setImageResource(R.drawable.music_note_blue);
        playButton = (ImageButton) findViewById(R.id.playButton);
        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        previousButton = (ImageButton) findViewById(R.id.previousButton);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        nextButton.setClickable(false);
        previousButton.setClickable(false);
    }

    protected static boolean resetRatings(){
        for (Song song: songs) {
            song.setImportance(Song.DEFAULT_IMPORTANCE);
        }
        SaveData.storeSongsList(context,FILENAME, songs);
        return true;
    }


    public void onStartClicked(View view){

        if(songs == null || songs.isEmpty()){
            //output no songs found
            Toast.makeText(getApplicationContext(), "no songs found", Toast.LENGTH_SHORT).show();
            mMediaPlayer = null;
            return;

        }else{
            if(currentSong != null){
                mMediaPlayer.start();
            }else{
                mMediaPlayer.start();
                Song song = randomSong();
                currentSong = song;
                playSong(currentSong);
            }
            updateSeekBar();
            switchUIButtons(MAKE_PAUSE_VISIBLE);
            //make next and previous clickable
            nextButton.setClickable(true);
            previousButton.setClickable(true);
        }
    }

    public static void onPauseClicked(View view){

        if(mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){

                switchUIButtons(MAKE_PLAY_VISIBLE);
                mMediaPlayer.pause();
            }
        }
    }

    public void onPreviousClicked(View view){

        if(indexes.size()>=2){
            indexes.removeLast();
            currentSong = songs.get(indexes.getLast());
            indexes.removeLast();
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
            playSong(currentSong);
        }
    }

    public void onNextClicked(View view){

        if(currentSong != null){
            //a song was playing and next was clicked during playback
            if(mMediaPlayer.isPlaying()){
                //next was clicked before song finished
                double current_position = mMediaPlayer.getCurrentPosition();
                currentSong.updateImportance(Song.SKIPPED, current_position);

            }else{
                //completed
                currentSong.updateImportance(Song.COMPLETION, 0);
            }
        }
        Song song = randomSong();
        currentSong = song;
        playSong(currentSong);
    }

    public static void switchUIButtons(int mode){

        switch(mode) {
            case MAKE_PLAY_VISIBLE:
                //make play clickable and show pause
                //make the pause button disappear
                playButton.setVisibility(View.VISIBLE);
                playButton.setClickable(true);
                //make the pause button appear
                pauseButton.setVisibility(View.INVISIBLE);
                pauseButton.setClickable(false);
                playButton.bringToFront();
                break;
            case MAKE_PAUSE_VISIBLE:
                //make play unclickable and show pause
                //make the play button disappear
                playButton.setVisibility(View.INVISIBLE);
                playButton.setClickable(false);
                //make the pause button appear
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setClickable(true);
                pauseButton.bringToFront();
                break;
            case MAKE_ALL_DISABLE:
                //make all buttons disabled
                playButton.setClickable(false);
                nextButton.setClickable(false);
                previousButton.setClickable(false);
                pauseButton.setClickable(false);
                pauseButton.setVisibility(View.INVISIBLE);
        }
    }

    protected void playSong(Song song){

        long id = song.getSongID();
        Uri contentUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        System.gc();

        try{
            if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mMediaPlayer.prepareAsync();

        }catch(Exception ex){
            ex.printStackTrace();
        }

        currentSong = song;
        updateScreenInfo(song);
        switchUIButtons(MAKE_PAUSE_VISIBLE);
        nextButton.setEnabled(true);
        currentSong.incrementPlayCount();
    }


    public void updateScreenInfo(Song song){

        if(song != null){

            songName.setText(song.getName());
            songName.setSelected(true);
            artistName.setText(song.getArtist());
            ratingBar.setRating((float)song.getImportance()*5);

            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, song.getAlbumId());
            imgView.setImageURI(uri);

            if(imgView.getDrawable() ==null) imgView.setImageResource(R.drawable.music_note_blue);

            int dur_minute = (int)(song.getDuration()/1000)/60;
            int dur_seconds = (int)(song.getDuration()/1000)%60;
            
            String durSeconds = (dur_seconds > 10) ? String.valueOf(dur_seconds) : "0"+ String.valueOf(dur_seconds);
            durTextView.setText(String.valueOf(dur_minute) + ":" + durSeconds);

        }else{

            songName.setText("");
            artistName.setText("");
            imgView.setImageResource(R.drawable.music_note_blue);
            ratingBar.setRating(5);
        }
    }

    private Song randomSong(){

        Song song = null;

        //check for loop option
        Boolean loopPref = sharedPref.getBoolean(getString(R.string.key_loop_switch),true);

        if(!loopPref && indexes.size()==songs.size()){
            //disable all butons and let user know songs have finished
            switchUIButtons(MAKE_ALL_DISABLE);
            Toast.makeText(getApplicationContext(), "all songs were played" , Toast.LENGTH_SHORT).show();
            return song;
        }

        if(songs.size() > 1){

            while(true){
                int index = (int)Math.floor(Math.random()*songs.size());
                if(!loopPref && indexes.contains(index)){
                    continue;
                }
                double num = Math.random();
                //kind of a bad way to do it, but at least Math.random() includes 0.0 and that's the lowest the ratings could be set so eventually one if picked
                //better way would be to get a rand from lowest available rating in the list to highest,
                //but that would include additional time searching through the whole list every time
                if(songs.get(index).getImportance() >= num){
                    return songs.get(index);
                }
            }
        }else if(songs.size() == 1){
            song = songs.get(0);
        }
        return song;
    }

    protected void updateSeekBar(){

        Thread t = new Thread(new Runnable() {
            public void run() {
                while (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {

                    int currentPosition = mMediaPlayer.getCurrentPosition();
                    Message msg = handler.obtainMessage();

                    if(currentPosition %100 <= 10){
                        msg.what = UPDATE_SEEKBAR;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
        //t.setPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
        t.start();
    }

    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==UPDATE_SEEKBAR ){

                int elapsedSeconds = mMediaPlayer.getCurrentPosition()/1000%60;
                String eSeconds = (elapsedSeconds < 10) ? "0"+String.valueOf(elapsedSeconds) : String.valueOf(elapsedSeconds);
                int elapsedMinutes = (mMediaPlayer.getCurrentPosition()/1000)/60;
                String eMinutes = String.valueOf(elapsedMinutes);
                String elapsed = eMinutes + ":" + eSeconds;
                elapsedTextView.setText(elapsed);
                seekBar.setMax(mMediaPlayer.getDuration());
                seekBar.setProgress(mMediaPlayer.getCurrentPosition());
            }
            super.handleMessage(msg);
        }
    };

    //headset detection receiver
    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Boolean headsetPref = sharedPref.getBoolean(getString(R.string.key_headset_pause),true);
                        if(isHeadsetPlugged && headsetPref){       //needed because app goes into this case after exiting other activities or when starting up
                            Log.d(TAG, "Headset is unplugged");
                            onPauseClicked(getCurrentFocus());
                            isHeadsetPlugged = false;
                            //Toast.makeText(getApplicationContext(), "UNPLUGGED", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        isHeadsetPlugged = true;
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }

    @Override
    public void onRatingChanged (RatingBar ratingBar, float rating, boolean fromUser){
        if(currentSong != null){
            currentSong.setImportance((double)(rating/5));
        }
    }

    @Override
    public void onCompletion (MediaPlayer mp){

        if(mMediaPlayer!= null &&mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        onNextClicked(this.getCurrentFocus());
    }

    @Override
    public void onPrepared (MediaPlayer mp){

        mp.start();
        updateSeekBar();
        updateScreenInfo(currentSong);
        int temp = songs.indexOf(currentSong);
        indexes.add(temp);
    }

    @Override
    protected void onStop(){

        super.onStop();
        SaveData.storeSongsList(getApplicationContext(), FILENAME, songs);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //need to register the headset event receiver
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
        if(mMediaPlayer!= null && currentSong!= null){
            updateScreenInfo(currentSong);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case SONG_ACTIVITY_INTENT_REQUEST_CODE:
                if (resultCode == SONG_PICKED) {
                    System.gc();
                    Bundle res = data.getExtras();
                    int result = res.getInt("POSITION");        //should put this in the res
                    //Toast.makeText(getApplicationContext(), "Position: " + result , Toast.LENGTH_SHORT).show();
                    playSong(songs.get(result));
                    Log.d("FIRST", "result:"+result);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {

        if(mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            onSettings();
            return true;
        }

        if (id == R.id.action_quit) {
            this.finish();
        }

        if (id == R.id.action_songs) {
            //bring up the songs list
            if(songs.isEmpty()){
                Toast.makeText(getApplicationContext(), "no songs found", Toast.LENGTH_SHORT).show();
            }else{
                Intent intent = new Intent(MainActivity.this, SongsActivity.class);
                startActivityForResult(intent, SONG_ACTIVITY_INTENT_REQUEST_CODE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        //in case I need to do something as soon as the seekbar is moved
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //on start touch, do something here
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mMediaPlayer != null && currentSong != null){
            int value = seekBar.getProgress();
            mMediaPlayer.seekTo(value);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
}
