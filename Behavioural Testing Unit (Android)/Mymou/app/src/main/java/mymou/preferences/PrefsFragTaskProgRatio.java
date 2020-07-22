package mymou.preferences;


import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import mymou.R;

public class PrefsFragTaskProgRatio extends PreferenceFragmentCompat {

    public PrefsFragTaskProgRatio() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_prog_ratio, rootKey);

        // For these have to special loading as values are above 100
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get size of screen
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Set x options based on screen width
        SeekBarPreferenceCustom seekBar = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_pr_cuex));
        seekBar.setMax(size.x);
        int xval = sharedPrefs.getInt(getString(R.string.preftag_pr_cuex), size.x/2);
        seekBar.setValue(xval);

        // Set y options based on screen height
        SeekBarPreferenceCustom seekBar2 = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_pr_cuey));
        seekBar2.setMax(size.y);
        int yval = sharedPrefs.getInt(getString(R.string.preftag_pr_cuey), size.y/2);
        seekBar2.setValue(yval);

    }



}
