package com.example.jbutler.mymou;


import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class PreferencesCompat extends PreferenceFragmentCompat {

   public static final String FRAGMENT_TAG = "my_preference_fragment";

    public PreferencesCompat() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
    }


    }
