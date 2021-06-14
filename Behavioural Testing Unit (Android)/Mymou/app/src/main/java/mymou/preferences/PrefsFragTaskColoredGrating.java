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
//        SeekBarPreferenceCustom[] seekBarPreferences = new SeekBarPreferenceCustom[7];
//        seekBarPreferences[0] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_min_radius));
//        seekBarPreferences[1] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_max_radius));
//        seekBarPreferences[2] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_colgrat_initial_reward));

//        for (int i = 0; i < seekBarPreferences.length; i++) {
//            final int i_final = i;
//            seekBarPreferences[i].setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    // Number dialog
//                    Log.d(TAG, "setting seekbar"+i_final);
//                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
//                    alert.setTitle("Input number");
//                    final EditText input = new EditText(getContext());
//                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
//                    input.setRawInputType(Configuration.KEYBOARD_12KEY);
//                    alert.setView(input);
//                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            try {
//
//                                int value = Integer.valueOf(input.getText().toString());
//                                if (value < seekBarPreferences[i_final].getMax()) {
//                                    seekBarPreferences[i_final].setValue(value);
//                                } else {
//                                    Toast.makeText(getContext(), "Value too high", Toast.LENGTH_LONG).show();
//                                }

//                            } catch (NumberFormatException e) {
//                                Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_LONG).show();
//                            }

//                        }
//                    });
//                    alert.show();
//                    return false;
//                }
//            });

//        }
    }

}
