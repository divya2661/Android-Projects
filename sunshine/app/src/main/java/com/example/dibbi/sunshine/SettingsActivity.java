package com.example.dibbi.sunshine;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SettingsActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getFragmentManager().beginTransaction().replace(android.R.id.content, new settingfragment()).commit();

    }


    public static class settingfragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            bindpreferenceToValue(findPreference(getString(R.string.pref_location_key)));
        }

        private void bindpreferenceToValue(Preference preference) {

            preference.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) this);

            onPreferenceChange(preference,PreferenceManager
                                            .getDefaultSharedPreferences(preference.getContext())
                                            .getString(preference.getKey(),""));

        }

        /**
         * Called when a Preference has been changed by the user. This is
         * called before the state of the Preference is about to be updated and
         * before the state is persisted.
         *
         * @param preference The changed Preference.
         * @param newValue   The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            String stringValue = newValue.toString();

            Log.e("string","string value: " + stringValue);

            if(preference instanceof ListPreference){

                ListPreference listpreference = (ListPreference) preference;
                int prefIndex = listpreference.findIndexOfValue(stringValue);
                Log.e("prefIndex: ","prefIndex: " +prefIndex);
                if(prefIndex>=0){
                    preference.setSummary(listpreference.getEntries()[prefIndex]);
                }
            }
            else{
                preference.setSummary(stringValue);
            }
            return true;
        }
    }




}







