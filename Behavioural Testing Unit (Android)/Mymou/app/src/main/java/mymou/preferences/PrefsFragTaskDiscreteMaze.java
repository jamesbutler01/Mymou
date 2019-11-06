package mymou.preferences;


import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import mymou.R;

public class PrefsFragTaskDiscreteMaze extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener  {

    public PrefsFragTaskDiscreteMaze() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_discretemaze, rootKey);

        // Adjust seekbars to the maximum number of cues selected
        PreferencesManager preferencesManager = new PreferencesManager(getContext());
        preferencesManager.DiscreteMaze();

        SeekBarPreferenceCustom seekBar1 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_dm_min_start_distance));
        seekBar1.setMin(1);
        seekBar1.setMax(preferencesManager.dm_max_dist_in_map);


        SeekBarPreferenceCustom seekBar2 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_dm_max_start_distance));
        seekBar2.setMin(1);
        seekBar2.setMax(preferencesManager.dm_max_dist_in_map);

        SeekBarPreferenceCustom seekBar3 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_dm_num_extra_steps));
        seekBar3.setMin(0);
        seekBar3.setMax(10);

        // For these have to special loading as values are above 100
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        SeekBarPreferenceCustom seekBar4 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_dm_animation_duration));
        seekBar4.setMin(1);
        seekBar4.setMax(1000);
        int anim_dur = sharedPrefs.getInt(getString(R.string.preftag_dm_animation_dur_actual), getResources().getInteger(R.integer.default_dm_animation_duration));
        seekBar4.setValue(anim_dur);

        SeekBarPreferenceCustom seekBar5 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_dm_choice_delay));
        seekBar5.setMin(1);
        seekBar5.setMax(1000);
        int choice_delay = sharedPrefs.getInt(getString(R.string.preftag_dm_choice_delay_actual), getResources().getInteger(R.integer.default_dm_choice_delay));
        seekBar5.setValue(choice_delay);

        SeekBarPreferenceCustom seekBar6 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_dm_booster_amount));
        seekBar6.setMin(0);
        seekBar6.setMax(5000);
        int booster_amount = sharedPrefs.getInt(getString(R.string.preftag_dm_booster_amount_actual), getResources().getInteger(R.integer.default_dm_booster_amount));
        seekBar6.setValue(booster_amount);

        SeekBarPreferenceCustom seekBar7 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_dm_dist_to_target));
        seekBar7.setMin(0);
        seekBar7.setMax(3);

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

        @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Seekbars can't store values above 100, so store a secondary variable with the actual, scaled amount
        if (key.equals(getString(R.string.preftag_dm_animation_duration))) {
            int val = (int) sharedPreferences.getInt(key, getResources().getInteger(R.integer.default_dm_animation_duration));
            sharedPreferences.edit().putInt(getString(R.string.preftag_dm_animation_dur_actual), val).commit();
        }
        if (key.equals(getString(R.string.preftag_dm_choice_delay))) {
            int val = (int) sharedPreferences.getInt(key, getResources().getInteger(R.integer.default_dm_choice_delay));
            sharedPreferences.edit().putInt(getString(R.string.preftag_dm_choice_delay_actual), val).commit();
        }
        if (key.equals(getString(R.string.preftag_dm_booster_amount))) {
            int val = (int) sharedPreferences.getInt(key, getResources().getInteger(R.integer.default_dm_booster_amount));
            sharedPreferences.edit().putInt(getString(R.string.preftag_dm_booster_amount_actual), val).commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


}
