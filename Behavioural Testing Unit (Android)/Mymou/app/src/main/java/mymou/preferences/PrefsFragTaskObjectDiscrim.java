package mymou.preferences;


import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import mymou.R;

public class PrefsFragTaskObjectDiscrim extends PreferenceFragmentCompat  {

    public PrefsFragTaskObjectDiscrim() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_objectdiscrim, rootKey);

        // Adjust seekbars to the maximum number of cues selected
        PreferencesManager preferencesManager = new PreferencesManager(getContext());
        preferencesManager.ObjectDiscrimination();

        SeekBarPreference seekBar = (SeekBarPreference) findPreference("two_num_corr_cues");
        seekBar.setMin(1);
        seekBar.setMax(preferencesManager.objectdiscrim_num_incorr);

        SeekBarPreference seekBar2 = (SeekBarPreference) findPreference("two_num_incorr_cues");
        seekBar2.setMin(1);
        seekBar2.setMax(preferencesManager.objectdiscrim_num_corr);

        SeekBarPreference seekBar3 = (SeekBarPreference) findPreference("two_num_steps");
        seekBar3.setMin(1);
        seekBar3.setMax(10);

    }

}
