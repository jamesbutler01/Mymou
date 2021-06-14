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
        if (settings_to_load.equals(getString(R.string.preftag_menu_prefs))) {
            preferenceFragment = new PrefsFragMenu();
        } else if (settings_to_load.equals(getString(R.string.preftag_task_odc_settings))) {
            preferenceFragment = new PrefsFragTaskObjectDiscrim();
        } else if (settings_to_load.equals(getString(R.string.preftag_task_disc_maze_settings))) {
            preferenceFragment = new PrefsFragTaskDiscreteMaze();
        } else if (settings_to_load.equals(getString(R.string.preftag_task_t_one_settings))) {
            preferenceFragment = new PrefsFragTaskTrainingOne();
        } else if (settings_to_load.equals(getString(R.string.preftag_task_pr_settings))) {
            preferenceFragment = new PrefsFragTaskProgRatio();
        } else if (settings_to_load.equals(getString(R.string.preftag_task_t_sc_settings))) {
            preferenceFragment = new PrefsFragTaskStaticCue();
        } else if (settings_to_load.equals(getString(R.string.preftag_task_pass_settings))) {
            preferenceFragment = new PrefsFragTaskPassiveReward();
        } else if (settings_to_load.equals(getString(R.string.preftag_task_ea_settings))) {
            preferenceFragment = new PrefsFragTaskEvidenceAccum();
        }else if (settings_to_load.equals(getString(R.string.preftag_task_csl_settings))) {
            preferenceFragment = new PrefsFragTaskContextSequenceLearning();
        }else if (settings_to_load.equals(getString(R.string.preftag_task_wald_settings))) {
            preferenceFragment = new PrefsFragTaskWald();
        }else if (settings_to_load.equals(getString(R.string.preftag_task_colgrat_settings))) {
            preferenceFragment = new PrefsFragTaskColoredGrating();
        } else {
            // Default behaviour
            preferenceFragment = new PrefsFragCommon();
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.preftag_settings_to_load), settings_to_load);
            preferenceFragment.setArguments(bundle);
        }
        ft.add(R.id.container_, preferenceFragment);
        ft.commit();

    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        Log.d(TAG, pref.getKey());
        if (pref.getKey().equals(getString(R.string.preftag_crop_picker))) {
            Intent intent = new Intent(this, PrefsActCropPicker.class);
            startActivity(intent);
            return true;
        }
        if (pref.getKey().equals(getString(R.string.preftag_cam_picker))) {
            Intent intent = new Intent(this, PrefsActCamPicker.class);
            startActivity(intent);
            return true;
        }
        // Instantiate the new Fragment
        Bundle args = pref.getExtras();
        args.putString("pref_tag", pref.getKey());

        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(),pref.getFragment());
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
    }

}
