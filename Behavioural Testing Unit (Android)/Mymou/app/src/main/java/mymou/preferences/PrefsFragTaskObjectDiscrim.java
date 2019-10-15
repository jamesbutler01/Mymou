package mymou.preferences;


import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import mymou.R;

public class PrefsFragTaskObjectDiscrim extends PreferenceFragmentCompat  {

    public PrefsFragTaskObjectDiscrim() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_objectdiscrimcol, rootKey);

        // Adjust seekbars to the maximum number of cues selected
        PreferencesManager preferencesManager = new PreferencesManager(getContext());
        preferencesManager.ObjectDiscriminationCol();

        SeekBarPreferenceCustom seekBar = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_od_num_corr_cues));
        seekBar.setMin(1);
        seekBar.setMax(preferencesManager.objectdiscrim_num_incorr_options);

        SeekBarPreferenceCustom seekBar2 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_od_num_incorr_cues));
        seekBar2.setMin(1);
        seekBar2.setMax(preferencesManager.objectdiscrim_num_corr_options);

        SeekBarPreferenceCustom seekBar3 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_od_num_steps));
        seekBar3.setMin(1);
        seekBar3.setMax(10);

    }

}
