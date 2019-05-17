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

        // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        final SeekBarPreference seekBar = (SeekBarPreference) findPreference("num_monkeys"); seekBar.setMin(1);

        // Only show times if auto start/stop is enabled
        if(sharedPrefs.getBoolean("autostartswitch",false)) {
            Preference editTextPreference = findPreference("autostart");
            editTextPreference.setVisible(true);
        }
        if(sharedPrefs.getBoolean("autostopswitch",false)) {
            Preference editTextPreference = findPreference("autostop");
            editTextPreference.setVisible(true);
        }
        if(sharedPrefs.getBoolean("crop_photos",false)) {
            Preference editTextPreference = findPreference("croppicker_prefsfrag");
            editTextPreference.setVisible(true);
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("autostartswitch")) {
            Preference editTextPreference = findPreference("autostart");
            editTextPreference.setVisible(sharedPreferences.getBoolean("autostartswitch",false));
        }
        if (key.equals("autostopswitch")) {
            Preference editTextPreference = findPreference("autostop");
            editTextPreference.setVisible(sharedPreferences.getBoolean("autostopswitch",false));
        }
        if (key.equals("crop_photos")) {
            Preference editTextPreference = findPreference("croppicker_prefsfrag");
            editTextPreference.setVisible(sharedPreferences.getBoolean("crop_photos",false));
        }
    }
}
