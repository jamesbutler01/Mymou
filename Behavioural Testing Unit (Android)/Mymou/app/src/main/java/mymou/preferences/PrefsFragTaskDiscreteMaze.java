package mymou.preferences;


import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import mymou.R;

public class PrefsFragTaskDiscreteMaze extends PreferenceFragmentCompat  {

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

    }

}
