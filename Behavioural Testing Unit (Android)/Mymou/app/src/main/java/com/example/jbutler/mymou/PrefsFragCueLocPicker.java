package com.example.jbutler.mymou;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * Used for user to select location of a particular cue
 */

public class PrefsFragCueLocPicker extends Fragment {

    View cue;
    int cueTag=0;

    public PrefsFragCueLocPicker() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_cuelock_picker, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        cue = UtilsTask.addCue(cueTag, 1,  getContext(), getView().findViewById(R.id.layout_cue_loc_picker));

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Load camera fragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("crop_picker", true);
        CameraMain fragment = new CameraMain();
        fragment.setArguments(bundle);

        // Load crop picker fragment
        PrefsFragCropPicker fragment2 = new PrefsFragCropPicker();

        // Commit fragments
        fragmentTransaction.add(R.id.layout_croppicker, fragment, "camera_fragment");
        fragmentTransaction.add(R.id.layout_croppicker, fragment2, "crop_fragment");
        fragmentTransaction.commit();

    }


}
