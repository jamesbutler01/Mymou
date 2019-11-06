package mymou.preferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import mymou.R;

public class PrefsFragTaskEvidenceAccum extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_evidenceaccum, rootKey);

        SeekBarPreferenceCustom seekBar1 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_ea_num_steps));
        seekBar1.setMin(1);
        seekBar1.setMax(10);

        SeekBarPreferenceCustom seekBar2 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_ea_distance));
        seekBar2.setMin(1);
        seekBar2.setMax(5);

        SeekBarPreferenceCustom seekBar3 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_ea_variance));
        seekBar3.setMin(1);
        seekBar3.setMax(3);

    }





}
