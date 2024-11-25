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

public class PrefsFragTaskSocialVideo extends PreferenceFragmentCompat  {

    private String TAG="MymouPrefsFragTaskSocialVideo";

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_social_video, rootKey);

        // Set ontouchlisteners for the seekbars to allow users to manually input values
        SeekBarPreferenceCustom[] seekBarPreferences = new SeekBarPreferenceCustom[11];
        seekBarPreferences[0] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_iti));
        seekBarPreferences[1] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_rew_duration));
        seekBarPreferences[2] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_n_movie_repeats_allowed));
        seekBarPreferences[3] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_timeout_duration_mins));
        seekBarPreferences[4] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_pos1_x));
        seekBarPreferences[5] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_pos1_y));
        seekBarPreferences[6] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_pos2_x));
        seekBarPreferences[7] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_pos2_y));
        seekBarPreferences[8] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_video_height));
        seekBarPreferences[9] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_video_width));
        seekBarPreferences[10] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_sv_cue_size));

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
