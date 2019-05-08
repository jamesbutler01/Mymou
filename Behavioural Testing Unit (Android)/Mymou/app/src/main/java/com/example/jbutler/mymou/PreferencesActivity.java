package com.example.jbutler.mymou;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        Fragment preferenceFragment = new PreferencesMenu();
        ft.add(R.id.container_, preferenceFragment);

        ft.commit();

    }
}
