package mymou.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.preference.*;
import mymou.R;
import mymou.Utils.SoundManager;

/**
 * Preference Fragment for user to select secondary reinforcer sound
 * Currently hard-coded to give them all possible android options (98 in total)
 */


public class PrefsFragSoundPicker extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String TAG = "MymouColourPicker";
    private Context mContext;
    private SoundManager soundManager;
    private static int num_sounds = 98;
    private CheckBoxPreference[] checkBoxPreferences = new CheckBoxPreference[num_sounds];


    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_empty, rootKey);

        // Load parameters
        mContext = getActivity();

        soundManager = new SoundManager(new PreferencesManager(mContext));

        // Add preferences
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(mContext);
        setPreferenceScreen(preferenceScreen);
        TypedValue themeTypedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(mContext, themeTypedValue.resourceId);
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        preferenceCategory.setTitle("Choose sound used for secondary reinforcement");
        getPreferenceScreen().addPreference(preferenceCategory);

        // Now iterate through sounds and add them as checkboxes
        for (int i = 0; i < num_sounds; i++) {
            checkBoxPreferences[i] = new CheckBoxPreference(contextThemeWrapper);
            checkBoxPreferences[i].setTitle("Sound " + i);
            checkBoxPreferences[i].setKey(String.valueOf(i));
            checkBoxPreferences[i].setChecked(false);
            preferenceCategory.addPreference(checkBoxPreferences[i]);

        }

        // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, key + " preference changed");
        try {
            Integer.valueOf(key);
        } catch (NumberFormatException e) {
            return;
        }

        // Store tone
        sharedPreferences.edit().putInt(getString(R.string.preftag_sound_to_play), Integer.valueOf(key)).commit();

        // Play tone for the user
        new SoundManager(new PreferencesManager(mContext)).playTone();

        // Switch off other sounds so they can only ever pick one
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);  // Disable listener to stop recursion
        for (int i = 0; i < num_sounds; i++) {
            if (i != Integer.valueOf(key)) {
                checkBoxPreferences[i].setChecked(false);
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);  // Re-enable listener
    }

        @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}



