package com.example.sammirzaei.smartshuffle;

import android.app.ListActivity;
import android.content.Intent;
import android.preference.ListPreference;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SongsActivity extends ListActivity {

    private  Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        System.gc();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        //for future implementation, check pref on the fly and update the list
        //here check the pref for "sort songs by" and display the appropriate list: Songs (Album)(0), Artist(1), Name(2)
        //int sortPref = MainActivity.sharedPref.getInt(getString(R.string.key_song_sort_list), 0);
        SongsAdapter adapter = new SongsAdapter(this, this, MainActivity.songs);
        setListAdapter(adapter);
        intent = this.getIntent();

        //single click on an item in the list
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                returnResult(position);

            }
        });


        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText( getApplicationContext(),"Long Click in position " + position , Toast.LENGTH_LONG).show();
                // Return true to consume the click event. In this case the
                // onListItemClick listener is not called anymore.
                return true;
            }
        });
    }

    protected void returnResult(int position){

        intent.putExtra("POSITION", position);
        this.setResult(MainActivity.SONG_PICKED, intent);
        finish();
    }

}
