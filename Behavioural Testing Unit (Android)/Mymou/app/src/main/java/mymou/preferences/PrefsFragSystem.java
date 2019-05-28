package mymou.preferences;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import mymou.R;

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

        Log.d("ASDF", "checking prefs "+sharedPrefs.getBoolean("bluetooth", false));

        // Only show crop photos setting if crop photos enabled
        findPreference("croppicker_prefsfrag").setVisible(sharedPrefs.getBoolean("crop_photos", false));
        findPreference(getString(R.string.preftag_num_rew_chans)).setVisible(sharedPrefs.getBoolean("bluetooth", false));
        findPreference(getString(R.string.preftag_default_rew_chan)).setVisible(sharedPrefs.getBoolean("bluetooth", false));
        findPreference("soundpicker_prefsfrag").setVisible(sharedPrefs.getBoolean("sound", false));
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
