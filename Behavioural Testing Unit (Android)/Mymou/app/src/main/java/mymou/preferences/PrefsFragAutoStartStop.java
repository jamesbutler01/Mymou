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
        if(sharedPrefs.getBoolean("autostartswitch",false)) {
            Preference editTextPreference = findPreference("autostart");
            editTextPreference.setVisible(true);
        }
        if(sharedPrefs.getBoolean("autostopswitch",false)) {
            Preference editTextPreference = findPreference("autostop");
            editTextPreference.setVisible(true);
        }

       // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

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
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
