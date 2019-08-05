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

        SeekBarPreference seekBar = (SeekBarPreference) findPreference(getString(R.string.preftag_dm_min_start_distance));
        seekBar.setMin(1);
        seekBar.setMax(preferencesManager.dm_max_dist_in_map);

        SeekBarPreference seekBar2 = (SeekBarPreference) findPreference(getString(R.string.preftag_dm_max_start_distance));
        seekBar2.setMin(1);
        seekBar2.setMax(preferencesManager.dm_max_dist_in_map);

    }

}
