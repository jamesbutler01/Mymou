package mymou.preferences;


import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import mymou.R;
import mymou.task.backend.CameraMain;
import mymou.task.backend.UtilsTask;

/**
 * The crop picker requires two separate fragments, one for the camera, and one for the crop overlay
 * This parent fragment simply loads up these two fragments
 */

public class PrefsActCamPicker extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_picker);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();


        // First disable all buttons and only enable if you detect that camera
        UtilsTask.toggleCue((Button) findViewById(R.id.buttExternalCam), false);
        UtilsTask.toggleCue((Button) findViewById(R.id.buttRearCam), false);
        UtilsTask.toggleCue((Button) findViewById(R.id.buttSelfieCamera), false);

        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        int cameraSelected = 1;


        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                switch (facing) {
                    case CameraCharacteristics.LENS_FACING_BACK:
                        UtilsTask.toggleCue((Button) findViewById(R.id.buttRearCam), true);
                        return;
                    case CameraCharacteristics.LENS_FACING_FRONT:
                        UtilsTask.toggleCue((Button) findViewById(R.id.buttSelfieCamera), true);
                        return;
                    case CameraCharacteristics.LENS_FACING_EXTERNAL:
                        UtilsTask.toggleCue((Button) findViewById(R.id.buttExternalCam), true);
                        return;

                }
            }

            } catch(CameraAccessException e){
                e.printStackTrace();
            }


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
