package mymou.preferences;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import org.w3c.dom.Text;

import mymou.R;
import mymou.task.backend.CameraMain;
import mymou.task.backend.UtilsTask;

import static java.security.AccessController.getContext;

/**
 * The crop picker requires two separate fragments, one for the camera, and one for the crop overlay
 * This parent fragment simply loads up these two fragments
 */

public class PrefsActCamPicker extends FragmentActivity {

    private static String TAG = "PrefsActCamPicker";

    private static int[] i_camera = new int[3];
    private static String[] messages = {
            "Currently selected: Rear camera",
            "Currently selected: Selfie camera",
            "Currently selected: External camera",};
    CameraMain fragment;
    PreferencesManager preferencesManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_picker);

        preferencesManager = new PreferencesManager(this);

        findViewById(R.id.buttExternalCam).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttRearCam).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttSelfieCamera).setOnClickListener(buttonClickListener);

        // First disable all buttons and only enable if you detect that camera
        UtilsTask.toggleCue((Button) findViewById(R.id.buttExternalCam), false);
        UtilsTask.toggleCue((Button) findViewById(R.id.buttRearCam), false);
        UtilsTask.toggleCue((Button) findViewById(R.id.buttSelfieCamera), false);

        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);

        int cameraSelected = 1;
        try {
            String[] ids = manager.getCameraIdList();
            for (int i_cam = 0; i_cam < ids.length; i_cam++) {
                String cameraId = ids[i_cam];
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                Log.d(TAG, "Facing: " + facing);
                switch (facing) {
                    case CameraCharacteristics.LENS_FACING_BACK:
                        UtilsTask.toggleCue((Button) findViewById(R.id.buttRearCam), true);
                        i_camera[0] = i_cam;
                        break;
                    case CameraCharacteristics.LENS_FACING_FRONT:
                        UtilsTask.toggleCue((Button) findViewById(R.id.buttSelfieCamera), true);
                        i_camera[1] = i_cam;
                        break;
                    case CameraCharacteristics.LENS_FACING_EXTERNAL:
                        UtilsTask.toggleCue((Button) findViewById(R.id.buttExternalCam), true);
                        i_camera[2] = i_cam;
                        break;
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        preferencesManager = new PreferencesManager(this);
        TextView tv = findViewById(R.id.tv_camera_to_use);
        tv.setText(messages[preferencesManager.camera_to_use]);

        // Load camera fragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("crop_picker", true);
        fragment = new CameraMain();
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.layout_camerapicker, fragment, "camera_fragment");
        fragmentTransaction.addToBackStack("frag");
        fragmentTransaction.commit();
    }


    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: " + view.getId());
            switch (view.getId()) {
                case R.id.buttRearCam:
                    switch_camera(0);
                    break;
                case R.id.buttSelfieCamera:
                    switch_camera(1);
                    break;
                case R.id.buttExternalCam:
                    switch_camera(2);
                    break;
            }
        }
    };

    private void switch_camera(int id) {
        // Save new choice
        Log.d(TAG, "Camera saved: " + preferencesManager.camera_to_use);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(getString(R.string.preftag_camera_to_use), id);
        editor.apply();

        // Remove fragment and restart the activity
        getSupportFragmentManager().popBackStack();
        Intent intent = new Intent(this, PrefsActCamPicker.class);
        startActivity(intent);
    }


}
