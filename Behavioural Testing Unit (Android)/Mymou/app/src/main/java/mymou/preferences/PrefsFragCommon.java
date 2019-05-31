package mymou.preferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import mymou.R;

/**
 * Common preference fragment used to manage all preferences that have no special behaviour
 */

public class PrefsFragCommon extends PreferenceFragmentCompat {

    public PrefsFragCommon() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {

        String prefTag = getArguments().getString("pref_tag");

        if (prefTag ==getString(R.string.preftag_cue_settings)) {

            setPreferencesFromResource(R.xml.preferences_cues, rootKey);

        } else if (prefTag == getString(R.string.preftag_event_codes)) {

            setPreferencesFromResource(R.xml.preferences_eventcodes, rootKey);

        }

    }

}
