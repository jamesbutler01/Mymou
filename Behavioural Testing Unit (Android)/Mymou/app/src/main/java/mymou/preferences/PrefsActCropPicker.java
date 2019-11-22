package mymou.preferences;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import mymou.task.backend.CameraMain;
import mymou.R;

/**
 * The crop picker requires two separate fragments, one for the camera, and one for the crop overlay
 * This parent fragment simply loads up these two fragments
 */

public class PrefsActCropPicker extends FragmentActivity {

    public static String TAG = "PrefsActCropPicker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_picker);
        Log.d(TAG, "Loading activity");

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

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
