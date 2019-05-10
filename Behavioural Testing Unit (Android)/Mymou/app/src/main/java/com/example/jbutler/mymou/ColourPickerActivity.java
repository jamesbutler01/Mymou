package com.example.jbutler.mymou;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class ColourPickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new FragmentColourPicker();
        Bundle bundle = new Bundle();

        // Pass tag argument from parent activity to child fragment
        bundle.putString("pref_tag", getIntent().getStringExtra("pref_tag"));
        fragment.setArguments(bundle);
        ft.add(R.id.container_, fragment);
        ft.commit();

    }


}
