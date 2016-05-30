package com.example.sammirzaei.smartshuffle;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by sammirzaei on 5/28/2016.
 */
public abstract class FileFetcher {

    private static final String FILENAME = MainActivity.FILENAME;

    protected static ArrayList<Song> getFromFile(String[] files, Context context){
        ArrayList<Song> result = new ArrayList<>();
        if(haveDataFile(files,FILENAME)){
            //ALREADY SAVED THE DATA, read content into songs array
            result = SaveData.getSongsList(context, FILENAME);
        }
        return result;
    }

    protected static ArrayList<Song> getFromDevice(Context context){
        ArrayList<Song> result = new ArrayList<>();
        //read media files and start making song instances from them
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String sortOrder = MediaStore.Audio.Media.ALBUM + " ASC";  //default value
        //here check the pref for "sort songs by" and display the appropriate list: Songs (Album)(0), Artist(1), Name(2)
        int sortPref = Integer.valueOf(MainActivity.sharedPref.getString(context.getString(R.string.key_song_sort_list), "0"));
        switch (sortPref){
            case 0:
                sortOrder = MediaStore.Audio.Media.ALBUM + " ASC";
                break;
            case 1:
                sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";
                break;
            case 2:
                sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
                break;
        }

        Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);
        if (cursor == null) {
            // query failed, handle error.
            //make a toast
            Toast.makeText(context, "query failed", Toast.LENGTH_SHORT).show();
        } else if (!cursor.moveToFirst()) {
            // no media on the device
            Toast.makeText(context, "No songs were found", Toast.LENGTH_SHORT).show();
        } else {
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int isMusicColumn = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int albumIDColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            do {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                String thisArtist = cursor.getString(artistColumn);
                int isMusic = cursor.getInt(isMusicColumn);
                double duration = cursor.getDouble(durationColumn);
                long albumID = cursor.getLong(albumIDColumn);
                // ...process entry...
                if(isMusic == 1){
                    Song song = new Song(thisTitle);
                    song.setSongID(thisId);
                    song.setArtist(thisArtist);
                    song.setDuration(duration);
                    song.setAlbumID(albumID);
                    result.add(song);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }

    //sets the importance of each song found on device with the importance found in the saved file.
    //the equals method for Song has been overridden to only check for Title and Artist being equal
    protected static ArrayList<Song> consolidateImportance(ArrayList<Song> fromDevice , ArrayList<Song> fromFile, Context context){
        ArrayList<Song> result;
        if(!fromFile.isEmpty()){
            for(Song song : fromDevice){
                if(fromFile.contains(song)){
                    song.setImportance(fromFile.get(fromFile.indexOf(song)).getImportance());
                }
            }
        }
        result = fromDevice;
        SaveData.storeSongsList(context, FILENAME, result);
        return result;
    }

    private static boolean haveDataFile(String[] files, String fileName){
        for(int i=0; i<files.length; i++){
            if(files[i].contentEquals(fileName)){
                return true;
            }
        }
        return false;
    }
}
