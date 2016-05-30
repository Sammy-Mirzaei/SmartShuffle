package com.example.sammirzaei.smartshuffle;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by sammirzaei on 4/13/2016.
 */
public class SaveData {

    public static ArrayList<Song> getSongsList( Context context, String filename){

        ArrayList<Song> list = new ArrayList<>();

        try {

            ObjectInputStream ois = new ObjectInputStream(context.openFileInput(filename));
            list = (ArrayList<Song>) ois.readObject(); // cast is needed.
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void storeSongsList(Context context, String filename, ArrayList<Song> list){

        //writing list of Songs to file
        FileOutputStream fos;
        ObjectOutputStream oos;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(list);
            oos.close();
            fos.close();
            System.out.println("writing done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
