package com.example.jbutler.mymou;


import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

public class PrefsFragSystem extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public PrefsFragSystem() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_system, rootKey);

        // Get sharedpreferences
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());


        // Only show crop photos setting if crop photos enabled
        if(sharedPrefs.getBoolean("crop_photos",false)) {
            Preference editTextPreference = findPreference("croppicker_prefsfrag");
            editTextPreference.setVisible(true);
        }
        if(sharedPrefs.getBoolean("sound",false)) {
            Preference editTextPreference = findPreference("soundpicker_prefsfrag");
            editTextPreference.setVisible(true);
        }

         // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("crop_photos")) {
            Preference editTextPreference = findPreference("croppicker_prefsfrag");
            editTextPreference.setVisible(sharedPreferences.getBoolean("crop_photos",false));
        }
        if (key.equals("sound")) {
            Preference editTextPreference = findPreference("soundpicker_prefsfrag");
            editTextPreference.setVisible(sharedPreferences.getBoolean("sound",false));
        }
    }
}
