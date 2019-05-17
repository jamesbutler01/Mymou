package com.example.jbutler.mymou;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import androidx.fragment.app.DialogFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

public class PrefsFragCropPickerParent extends Fragment {

    public PrefsFragCropPickerParent() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_timepicker, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putBoolean("crop_picker", true);
        CameraMain fragment = new CameraMain();
        fragment.setArguments(bundle);
        PrefsFragCropPicker fragment2 = new PrefsFragCropPicker();
        fragmentTransaction.add(R.id.mainLayout, fragment, "camera_fragment");
        fragmentTransaction.add(R.id.mainLayout, fragment2, "crop_fragment");
        fragmentTransaction.commit();

    }


}
