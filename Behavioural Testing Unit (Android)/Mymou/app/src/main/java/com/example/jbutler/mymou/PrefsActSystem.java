package com.example.jbutler.mymou;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class PrefsActSystem extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment preferenceFragment = new PrefsFragSystem();
        ft.add(R.id.container_, preferenceFragment);
        ft.commit();

    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
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
