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

public class PrefsFragTaskWald extends PreferenceFragmentCompat  {

    private String TAG="MymouPrefsFragTaskWald";

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_wald, rootKey);

        // Set ontouchlisteners for the seekbars to allow users to manually input values
        SeekBarPreferenceCustom[] seekBarPreferences = new SeekBarPreferenceCustom[15];
        seekBarPreferences[0] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_numcues));
        seekBarPreferences[1] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_startdelay));
        seekBarPreferences[2] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_probcuesdelay_low));
        seekBarPreferences[3] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_probcuesdelay_high));
        seekBarPreferences[4] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_gocuesize));
        seekBarPreferences[5] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_choicecuesize));
        seekBarPreferences[6] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_rewcuesize));
        seekBarPreferences[7] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_probcuexloc1));
        seekBarPreferences[8] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_probcuexloc2));
        seekBarPreferences[9] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_probcueyloc1));
        seekBarPreferences[10] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_probcueyloc2));
        seekBarPreferences[11] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_choicecuex1));
        seekBarPreferences[12] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_choicecuey1));
        seekBarPreferences[13] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_choicecuex2));
        seekBarPreferences[14] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_w_choicecuey2));
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
