package com.example.jbutler.mymou;


import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class PrefsFragSystem extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public PrefsFragSystem() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_system, rootKey);

        checkConditionalPrefs();

        // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    private void checkConditionalPrefs() {
        // Get sharedpreferences
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        // Only show crop photos setting if crop photos enabled
        if (sharedPrefs.getBoolean("crop_photos", false)) {
            Preference editTextPreference = findPreference("croppicker_prefsfrag");
            editTextPreference.setVisible(true);
        }
        if (sharedPrefs.getBoolean("bluetooth", false)) {
            Preference editTextPreference = findPreference(getString(R.string.preftag_num_rew_chans));
            editTextPreference.setVisible(true);
        }
        if (sharedPrefs.getBoolean("sound", false)) {
            Preference editTextPreference = findPreference("soundpicker_prefsfrag");
            editTextPreference.setVisible(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        checkConditionalPrefs();
    }
}
