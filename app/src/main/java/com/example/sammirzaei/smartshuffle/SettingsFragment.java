package com.example.sammirzaei.smartshuffle;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.AttributeSet;
import android.widget.Toast;

import java.util.prefs.Preferences;

/**
 * Created by sammirzaei on 5/24/2016.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);

    }

    private class MyEditTextPreference extends DialogPreference{
        public MyEditTextPreference(Context context, AttributeSet attrs) {
            super(context, attrs);
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

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        //sort songs by
        if (key.matches(getString(R.string.key_song_sort_list))) {
            Toast.makeText(super.getActivity(), "app has to be restarted for this change to take place", Toast.LENGTH_SHORT).show();
            ListPreference listPreference = (ListPreference) findPreference(key);
            String value = listPreference.getEntries()[Integer.valueOf(sharedPreferences.getString(key, ""))].toString();
            listPreference.setSummary(value);
        }

        //other settings

    }

}
