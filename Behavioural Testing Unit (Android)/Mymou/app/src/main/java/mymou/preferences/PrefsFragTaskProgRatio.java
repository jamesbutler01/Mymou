package mymou.preferences;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import mymou.R;

public class PrefsFragTaskProgRatio extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public PrefsFragTaskProgRatio() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_prog_ratio, rootKey);

        // For these have to special loading as values are above 100
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        SeekBarPreferenceCustom seekBar4 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_pr_animation_duration));
        seekBar4.setMin(1);
        seekBar4.setMax(1000);
        int anim_dur = sharedPrefs.getInt(getString(R.string.preftag_pr_animation_dur_actual), getResources().getInteger(R.integer.default_pr_animation_duration));
        seekBar4.setValue(anim_dur);

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

        @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Seekbars can't store values above 100, so store a secondary variable with the actual, scaled amount
        if (key.equals(getString(R.string.preftag_pr_animation_duration))) {
            int val = (int) sharedPreferences.getInt(key, getResources().getInteger(R.integer.default_pr_animation_duration));
            sharedPreferences.edit().putInt(getString(R.string.preftag_pr_animation_dur_actual), val).commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }



}
