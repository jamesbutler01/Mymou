package mymou.preferences;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.view.Display;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import mymou.R;

public class PrefsFragTaskPassiveReward extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Resources r;

    public PrefsFragTaskPassiveReward() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_prog_ratio, rootKey);
        r = getContext().getResources();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Set ontouchlisteners for the seekbars to allow users to manually input values
        SeekBarPreferenceCustom[] seekBarPreferences = new SeekBarPreferenceCustom[8];
        seekBarPreferences[0] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_pass_minrew));
        seekBarPreferences[1] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_pass_maxrew));
        seekBarPreferences[2] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_pass_miniti));
        seekBarPreferences[3] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_pass_maxiti));
        seekBarPreferences[4] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_pass_sess_length));
        for (int i = 0; i < seekBarPreferences.length; i++) {
            final int i_final = i;
            seekBarPreferences[i].setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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

        // Conditional settings
        if (sharedPrefs.getBoolean(getContext().getResources().getString(R.string.preftag_pass_stopsess), r.getBoolean(R.bool.default_pass_stopsess))) {
             findPreference(getContext().getResources().getString(R.string.preftag_pass_sess_length)).setVisible(true);
        }

        // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

   @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getContext().getResources().getString(R.string.preftag_pass_stopsess))) {
             findPreference(getContext().getResources().getString(R.string.preftag_pass_sess_length)).setVisible(sharedPreferences.getBoolean(key, r.getBoolean(R.bool.default_pass_stopsess)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
