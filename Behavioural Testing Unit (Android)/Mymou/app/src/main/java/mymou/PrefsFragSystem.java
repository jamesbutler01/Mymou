package mymou;


import android.content.SharedPreferences;
import android.os.Bundle;
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
            findPreference("croppicker_prefsfrag").setVisible(true);
        }
        if (sharedPrefs.getBoolean("bluetooth", false)) {
            findPreference(getString(R.string.preftag_num_rew_chans)).setVisible(true);
            findPreference(getString(R.string.preftag_default_rew_chan)).setVisible(true);
        }
        if (sharedPrefs.getBoolean("sound", false)) {
            findPreference("soundpicker_prefsfrag").setVisible(true);
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
