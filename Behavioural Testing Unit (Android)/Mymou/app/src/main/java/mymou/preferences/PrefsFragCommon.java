package mymou.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;

import mymou.R;
import mymou.task.backend.TaskManager;

/**
 * Common preference fragment used to manage all preferences that have no special behaviour
 */

public class PrefsFragCommon extends PreferenceFragmentCompat {

    public static String TAG = "MyMouPrefsFragCommon";


    public PrefsFragCommon() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {

        String prefTag = getArguments().getString("pref_tag");

        if (prefTag == null) {
            prefTag = getArguments().getString(getString(R.string.preftag_settings_to_load));
        }

        Log.d(TAG, "Loading settings for " + prefTag);

        if (prefTag.equals(getString(R.string.preftag_cue_settings))) {

            setPreferencesFromResource(R.xml.preferences_cues, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_event_codes))) {

            setPreferencesFromResource(R.xml.preferences_eventcodes, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_system_settings))) {

            setPreferencesFromResource(R.xml.preferences_system, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_task_sr_settings))) {

            setPreferencesFromResource(R.xml.preferences_task_spatialresponse, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_task_od_settings))) {

            setPreferencesFromResource(R.xml.preferences_task_objectdiscrim, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_bluetooth_strobes))) {

            setPreferencesFromResource(R.xml.preferences_bluetooth_strobes, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_task_sl_settings))) {

            setPreferencesFromResource(R.xml.preferences_task_sequential_learning, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_task_rdm_settings))) {

            setPreferencesFromResource(R.xml.preferences_task_random_dot_motion, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_task_dvs_settings))) {

            setPreferencesFromResource(R.xml.preferences_task_discrete_value_space, rootKey);

        } else if (prefTag.equals(getString(R.string.preftag_task_dvs_settings))) {

            Intent intent = new Intent(getActivity(), TaskManager.class);
            startActivity(intent);

        } else {

            new Exception("Invalid pref tag");

        }

    }

}
