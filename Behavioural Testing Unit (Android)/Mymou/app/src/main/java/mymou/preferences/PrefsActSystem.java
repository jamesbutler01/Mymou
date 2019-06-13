package mymou.preferences;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import mymou.R;

public class PrefsActSystem extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    String TAG = "MymouPrefsActSystem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        // Get which settings to load
        String settings_to_load = getIntent().getStringExtra(getString(R.string.preftag_settings_to_load));
        Log.d(TAG, settings_to_load);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment preferenceFragment = null;
        if (settings_to_load.equals(getString(R.string.preftag_system_settings))) {
            preferenceFragment = new PrefsFragSystem();
        } else if (settings_to_load.equals(getString(R.string.preftag_task_obj_disc_settings))) {
            preferenceFragment = new PrefsFragTaskObjectDiscrim();
        } else {
            new Exception("Invalid preferences specified");
        }
        ft.add(R.id.container_, preferenceFragment);
        ft.commit();


    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        Log.d(TAG, pref.getKey());
        if (pref.getKey().equals("croppicker_prefsfrag")) {
            Intent intent = new Intent(this, PrefsFragCropPickerParent.class);
            startActivity(intent);
                    return true;

        }
        // Instantiate the new Fragment
        Bundle args = pref.getExtras();
        args.putString("pref_tag", pref.getKey());

        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment(),
                args);
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("asdf", "onActivityResult_act_parent");
    }


    @Override
    public void onBackPressed() {
        boolean allowExit;
        // Don't let user exit if in colour picker fragment
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container_);
        if (f instanceof PrefsFragColourPicker) {
            PrefsFragColourPicker fragment = (PrefsFragColourPicker) getSupportFragmentManager().findFragmentById(R.id.container_);
            allowExit = fragment.onBackPressed();
        } else {
            allowExit = true;
        }
        if (allowExit) {
            super.onBackPressed();
        }
    }
}
