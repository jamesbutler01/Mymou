package mymou.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.preference.*;
import mymou.R;
import mymou.Utils.UtilsSystem;

import java.util.stream.IntStream;

public class PrefsFragColourPicker extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String TAG = "MymouColourPicker";
    int[] coloursChosen;
    String currPrefTag;
    Context mContext;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_empty, rootKey);

        // Load parameters
        mContext = getActivity();
        currPrefTag = getArguments().getString("pref_tag");
        String[] colornames = getResources().getStringArray(R.array.colournames);
        int num_colours = colornames.length;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        coloursChosen = UtilsSystem.loadIntArray(currPrefTag, sharedPrefs, mContext);

        int[] goCueColours = UtilsSystem.loadIntArray(getString(R.string.preftag_gocuecolors), sharedPrefs, mContext);
        // TODO handle this iteratively
        String TAG_othercues;
        if (currPrefTag == getString(R.string.preftag_od_corr_cols)) {
            TAG_othercues = getString(R.string.preftag_od_incorr_cols);
        } else {
            TAG_othercues = getString(R.string.preftag_od_corr_cols);
        }
        int[] otherCues = UtilsSystem.loadIntArray(TAG_othercues, sharedPrefs, mContext);

        // Add preferences
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(mContext);
        setPreferenceScreen(preferenceScreen);
        TypedValue themeTypedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(mContext, themeTypedValue.resourceId);
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        preferenceCategory.setTitle("Choose possible colours for stimuli");
        getPreferenceScreen().addPreference(preferenceCategory);

        // Now iterate through colours and add them as checkboxes
        for (int i=0; i < num_colours; i++) {
            // Exclude colours used by go cues
            if (currPrefTag != getString(R.string.preftag_gocuecolors)) {
                if (goCueColours[i] == 1 | otherCues[i] == 1) {
                    coloursChosen[i] = 0;
                    continue;
                }
            }
            CheckBoxPreference checkBoxPreference = new CheckBoxPreference(contextThemeWrapper);
            checkBoxPreference.setTitle(colornames[i]);
            checkBoxPreference.setKey(String.valueOf(i));
            boolean bool = coloursChosen[i] == 1;
            sharedPrefs.edit().putBoolean(String.valueOf(i), bool).commit();
            if (coloursChosen[i] == 0) {
                checkBoxPreference.setChecked(false);
            } else {
                checkBoxPreference.setChecked(true);
            }
            preferenceCategory.addPreference(checkBoxPreference);
        }

        // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    public boolean onBackPressed() {
        // First check they've selected at least one colour
         if (IntStream.of(coloursChosen).sum() == 0) {
             Toast.makeText(mContext, "Please select at least one cue colour", Toast.LENGTH_LONG).show();
             return false;
         }

        // Only let them exit if they have selected enough colours for the number of monkeys specified
        if (currPrefTag == getString(R.string.preftag_gocuecolors)) {
            if (IntStream.of(coloursChosen).sum() != new PreferencesManager(mContext).num_monkeys) {
                Toast.makeText(mContext, "Please select one cue for each monkey \n(To select fewer Go cues you first need to decrease the number of monkeys setting)", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        // On exit unregister shared preference change listener
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, key+" preference changed");
        try {
            Integer.valueOf(key);
        } catch (NumberFormatException e) {
            return;
        }

        // Update colorschosen array with new choice
        coloursChosen[Integer.valueOf(key)] = sharedPreferences.getBoolean(key, false) ? 1 : 0;

        // Store in shared preferences
        Log.d(TAG, "Writing "+UtilsSystem.convertIntArrayToString(coloursChosen)+"to ID: "+currPrefTag);
        sharedPreferences.edit().putString(currPrefTag, UtilsSystem.convertIntArrayToString(coloursChosen)).commit();

        // Go cues have special behaviour:
        // 1: Can only select as many go cues as there are number of monkey using the device
        // 2: Must remove chosen option from the cues used in the task
        if (currPrefTag == mContext.getString(R.string.preftag_gocuecolors)) {

            // Can only select option if they have specified enough monkeys using the device
            int num_selected = IntStream.of(coloursChosen).sum();
            if (num_selected > new PreferencesManager(mContext).num_monkeys) {
                sharedPreferences.edit().putBoolean(key, false).commit();
                CheckBoxPreference editTextPreference = (CheckBoxPreference) findPreference(key);
                editTextPreference.setChecked(false);
                Toast.makeText(mContext, "Too many options chosen - please increase number of monkeys setting if you wish to select more go cues", Toast.LENGTH_LONG).show();
            }

            // List of other options to remove the colour from
            String[] otherColorChoices = {mContext.getString(R.string.preftag_od_corr_cols),
                    mContext.getString(R.string.preftag_od_incorr_cols)};

            for (String other_tag : otherColorChoices) {
                // Load the other options
                int[] coloursTemp = UtilsSystem.loadIntArray(other_tag, sharedPreferences, mContext);

                // Set to 0
                coloursTemp[Integer.valueOf(key)] = 0;

                // Save array
                sharedPreferences.edit().putString(other_tag, UtilsSystem.convertIntArrayToString(coloursTemp)).commit();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}


