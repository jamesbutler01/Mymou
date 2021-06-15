package mymou.preferences;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import mymou.R;

public class PrefsFragTaskColoredGrating extends PreferenceFragmentCompat  {

    private String TAG="MymouPrefsFragTaskColoredGrating";

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_coloredgrating, rootKey);

        // Set ontouchlisteners for the seekbars to allow users to manually input values
        SeekBarPreferenceCustom[] seekBarPreferences = new SeekBarPreferenceCustom[25];
        seekBarPreferences[0] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_rt_limit));
        seekBarPreferences[1] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_catchtrialfreq));
        seekBarPreferences[2] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_fixation_time_mi));
        seekBarPreferences[3] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_fixation_time_ma));
        seekBarPreferences[4] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_cue_time_mi));
        seekBarPreferences[5] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_cue_time_ma));
        seekBarPreferences[6] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_stimulus_time_mi));
        seekBarPreferences[7] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_stimulus_time_ma));
        seekBarPreferences[8] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_dim_time_mi));
        seekBarPreferences[9] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_dim_time_ma));
        seekBarPreferences[10] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_start_dim));
        seekBarPreferences[11] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_end_dim));
        seekBarPreferences[12] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_target_shape));
        seekBarPreferences[13] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_sizecolcue));
        seekBarPreferences[14] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_sizefixcue));
        seekBarPreferences[15] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_sizeindicatorcue));
        seekBarPreferences[16] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_red_x));
        seekBarPreferences[17] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_red_y));
        seekBarPreferences[18] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_green_x));
        seekBarPreferences[19] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_green_y));
        seekBarPreferences[20] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_blue_x));
        seekBarPreferences[21] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_blue_y));
        seekBarPreferences[22] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_sizestripes));
        seekBarPreferences[23] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_numstripes));
        seekBarPreferences[24] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_grateoffset));

        for (int i = 0; i < seekBarPreferences.length; i++) {
            final int i_final = i;
            seekBarPreferences[i].setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Number dialog
                    Log.d(TAG, "setting seekbar"+i_final);
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Input number");
                    final EditText input = new EditText(getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setRawInputType(Configuration.KEYBOARD_12KEY);
                    alert.setView(input);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {

                                int value = Integer.valueOf(input.getText().toString());
                                if (value < seekBarPreferences[i_final].getMax()) {
                                    seekBarPreferences[i_final].setValue(value);
                                } else {
                                    Toast.makeText(getContext(), "Value too high", Toast.LENGTH_LONG).show();
                                }

                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                    alert.show();
                    return false;
                }
            });

        }
    }

}
