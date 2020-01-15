package mymou.preferences;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import mymou.R;

public class PrefsFragAutoStartStop extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public PrefsFragAutoStartStop() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_auto_start_stop, rootKey);

        // Get sharedpreferences
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        // Only show times if auto start/stop is enabled
        if (sharedPrefs.getBoolean(getContext().getResources().getString(R.string.preftag_autostart), false)) {
            Preference editTextPreference = findPreference(getContext().getResources().getString(R.string.preftag_autostarttimepicker));
            editTextPreference.setVisible(true);
        }
        if (sharedPrefs.getBoolean(getContext().getResources().getString(R.string.preftag_autostop), false)) {
            Preference editTextPreference = findPreference(getContext().getResources().getString(R.string.preftag_autostoptimepicker));
            editTextPreference.setVisible(true);
        }

        // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getContext().getResources().getString(R.string.preftag_autostart))) {
            Preference editTextPreference = findPreference(getContext().getResources().getString(R.string.preftag_autostarttimepicker));
            editTextPreference.setVisible(sharedPreferences.getBoolean(key, false));
        }
        if (key.equals(getContext().getResources().getString(R.string.preftag_autostop))) {
            Preference editTextPreference = findPreference(getContext().getResources().getString(R.string.preftag_autostoptimepicker));
            editTextPreference.setVisible(sharedPreferences.getBoolean(key, false));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
