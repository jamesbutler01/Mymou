package mymou.preferences;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import mymou.task.backend.CameraInterface;
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

        // Only configured for selfie camera
        PreferencesManager preferencesManager = new PreferencesManager(this);
        if (preferencesManager.camera_to_use != getApplicationContext().getResources().getInteger(R.integer.TAG_CAMERA_FRONT)) {
            Toast.makeText(getApplicationContext(), "You can only crop selfie photos using the settings UI. Select the selfie camera if you wish to use this function", Toast.LENGTH_LONG).show();
            super.onBackPressed();
            return;
        }

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        // Load camera fragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("crop_picker", true);
        CameraMain fragment = new CameraMain();
        fragment.setFragInterfaceListener(new CameraInterface() {
            @Override
            public void CameraLoaded() {
                Log.d(TAG, "Camera loaded");  // do nothing
            }
        });
        fragment.setArguments(bundle);

        // Load crop picker fragment
        PrefsFragCropPicker fragment2 = new PrefsFragCropPicker();

        // Commit fragments
        fragmentTransaction.add(R.id.layout_croppicker, fragment, "camera_fragment");
        fragmentTransaction.add(R.id.layout_croppicker, fragment2, "crop_fragment");
        fragmentTransaction.commit();
    }


}
