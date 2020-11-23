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

public class PrefsFragTaskStaticCue extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Resources r;

    public PrefsFragTaskStaticCue() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_trainingstaticcue, rootKey);
        r = getContext().getResources();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get size of screen
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Set x options based on screen width
        SeekBarPreferenceCustom seekBar = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_cuex));
        seekBar.setMax(size.x);
        int xval = sharedPrefs.getInt(getString(R.string.preftag_t_sc_cuex), size.x / 2);
        seekBar.setValue(xval);

        SeekBarPreferenceCustom seekBar3 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_cuextwo));
        seekBar3.setMax(size.x);
        seekBar3.setValue(xval);

        // Set y options based on screen height
        SeekBarPreferenceCustom seekBar2 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_cuey));
        seekBar2.setMax(size.y);
        int yval = sharedPrefs.getInt(getString(R.string.preftag_t_sc_cuey), size.y / 2);
        seekBar2.setValue(yval);

        SeekBarPreferenceCustom seekBar4 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_cueytwo));
        seekBar4.setMax(size.y);
        seekBar4.setValue(yval);

        // Set ontouchlisteners for the seekbars to allow users to manually input values
        SeekBarPreferenceCustom[] seekBarPreferences = new SeekBarPreferenceCustom[11];
        seekBarPreferences[0] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_cuex));
        seekBarPreferences[1] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_cuey));
        seekBarPreferences[2] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_sess_length));
        seekBarPreferences[3] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_bordersize));
        seekBarPreferences[4] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_minrew));
        seekBarPreferences[5] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_maxrew));
        seekBarPreferences[6] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_miniti));
        seekBarPreferences[7] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_maxiti));
        seekBarPreferences[8] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_sess_length));
        seekBarPreferences[9] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_cuextwo));
        seekBarPreferences[10] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_t_sc_cueytwo));
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
        if (sharedPrefs.getBoolean(getContext().getResources().getString(R.string.preftag_t_sc_stopsess), r.getBoolean(R.bool.default_t_sc_stopsess))) {
            findPreference(getContext().getResources().getString(R.string.preftag_t_sc_sess_length)).setVisible(true);
        }
        if (sharedPrefs.getBoolean(getContext().getResources().getString(R.string.preftag_t_sc_alternatecue), r.getBoolean(R.bool.default_t_sc_alternatecue))) {
            findPreference(getContext().getResources().getString(R.string.preftag_t_sc_cuextwo)).setVisible(true);
            findPreference(getContext().getResources().getString(R.string.preftag_t_sc_cueytwo)).setVisible(true);
        }

        // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getContext().getResources().getString(R.string.preftag_t_sc_stopsess))) {
            findPreference(getContext().getResources().getString(R.string.preftag_t_sc_sess_length)).setVisible(sharedPreferences.getBoolean(key, r.getBoolean(R.bool.default_t_sc_stopsess)));
        }
        if (key.equals(getContext().getResources().getString(R.string.preftag_t_sc_alternatecue))) {
            findPreference(getContext().getResources().getString(R.string.preftag_t_sc_cuextwo)).setVisible(sharedPreferences.getBoolean(key, r.getBoolean(R.bool.default_t_sc_alternatecue)));
            findPreference(getContext().getResources().getString(R.string.preftag_t_sc_cueytwo)).setVisible(sharedPreferences.getBoolean(key, r.getBoolean(R.bool.default_t_sc_alternatecue)));
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
