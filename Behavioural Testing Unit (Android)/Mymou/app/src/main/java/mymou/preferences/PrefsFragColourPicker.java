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

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}


