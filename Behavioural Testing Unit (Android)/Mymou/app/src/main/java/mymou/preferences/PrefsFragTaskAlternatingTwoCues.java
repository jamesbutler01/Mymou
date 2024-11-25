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

public class PrefsFragTaskAlternatingTwoCues extends PreferenceFragmentCompat  {

    public PrefsFragTaskAlternatingTwoCues() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_alternating_two_cues, rootKey);

        SeekBarPreferenceCustom seekBar = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_atc_reward_duration));

        seekBar.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Number dialog
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
                            if (value < seekBar.getMax()) {
                                seekBar.setValue(value);
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
