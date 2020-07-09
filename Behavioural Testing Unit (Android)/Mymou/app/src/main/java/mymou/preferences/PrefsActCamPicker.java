package mymou.preferences;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

import mymou.CameraExternal;
import mymou.R;
import mymou.task.backend.CameraInterface;
import mymou.task.backend.CameraMain;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

import static java.security.AccessController.getContext;

/**
 * The crop picker requires two separate fragments, one for the camera, and one for the crop overlay
 * This parent fragment simply loads up these two fragments
 */

public class PrefsActCamPicker extends FragmentActivity {

    private static String TAG = "PrefsActCamPicker";

    private static String[] messages = {
            "Currently selected: Selfie camera",
            "Currently selected: Rear camera",
            "Currently selected: External camera",};
    PreferencesManager preferencesManager;
    private AlertDialog mDialog;
    private CameraMain cameraMain;
    private CameraExternal cameraExternal;
    private List<String> resolutions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_picker);

        preferencesManager = new PreferencesManager(this);

        findViewById(R.id.buttExternalCam).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttRearCam).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttSelfieCamera).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttPickResolution).setOnClickListener(buttonClickListener);

        preferencesManager = new PreferencesManager(this);
        TextView tv = findViewById(R.id.tv_camera_to_use);
        tv.setText(messages[preferencesManager.camera_to_use]);
        Log.d(TAG, "Camera to use"+preferencesManager.camera_to_use+" "+messages[preferencesManager.camera_to_use]);

        // Actually load camera fragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("crop_picker", true);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (preferencesManager.camera_to_use != getApplicationContext().getResources().getInteger(R.integer.TAG_CAMERA_EXTERNAL)) {
            cameraMain = new CameraMain();
            cameraMain.setArguments(bundle);
            cameraMain.setFragInterfaceListener(new CameraInterface() {
                @Override
                public void CameraLoaded() {
                    updateResolution();
                }
            });
            fragmentTransaction.add(R.id.layout_camerapicker, cameraMain, "camera_fragment");
        } else {
            cameraExternal = new CameraExternal();
            cameraExternal.setArguments(bundle);
            cameraExternal.setFragInterfaceListener(new CameraInterface() {
                                                        @Override
                                                        public void CameraLoaded() {
                                                            updateResolution();
                                                        }
                                                    });
            fragmentTransaction.add(R.id.layout_camerapicker, cameraExternal, "camera_fragment");
        }
        fragmentTransaction.commit();

        }


    private void updateResolution() {
        // Figure out which resolutions to load
        if (cameraMain != null && cameraMain.resolutions != null) {
            resolutions = cameraMain.resolutions;
        }else if (cameraExternal != null && cameraExternal.resolutions != null) {
            resolutions = cameraExternal.resolutions;
        } else {
            Toast.makeText(getApplicationContext(), "Waiting for camera to load resolutions..", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set string informing user what resolution currently selected
        
    }

    private void showResolutionListDialog() {

        CharSequence[] resolutionsChar = new CharSequence[resolutions.size()];
        for (int i = 0; i < resolutions.size(); i++) {
                        resolutionsChar[i] = resolutions.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(PrefsActCamPicker.this, R.style.Theme_AppCompat_Dialog);
        builder.setTitle("Choose resolution");
        builder.setItems(resolutionsChar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                TextView tv = findViewById(R.id.tv_resolution_to_use);
                tv.setText("Resolution: "+resolutions.get(item));

                // Store the value
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String[] prefTags = { getString(R.string.preftag_camera_resolution_front),
                        getString(R.string.preftag_camera_resolution_rear),
                        getString(R.string.preftag_camera_resolution_ext)};
                String prefTag = prefTags[preferencesManager.camera_to_use];
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(prefTag, item);
                editor.commit();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: " + view.getId());
            switch (view.getId()) {
                case R.id.buttRearCam:
                    switch_camera(getApplicationContext().getResources().getInteger(R.integer.TAG_CAMERA_REAR));
                    break;
                case R.id.buttSelfieCamera:
                    switch_camera(getApplicationContext().getResources().getInteger(R.integer.TAG_CAMERA_FRONT));
                    break;
                case R.id.buttExternalCam:
                    switch_camera(getApplicationContext().getResources().getInteger(R.integer.TAG_CAMERA_EXTERNAL));
                    break;
                case R.id.buttPickResolution:
                    showResolutionListDialog();
                    break;
            }
        }
    };

    private void switch_camera(int id) {
        // Check they have actually changed the choice
        if (id != preferencesManager.camera_to_use) {
            // Save new choice
            Log.d(TAG, "Camera saved: " + preferencesManager.camera_to_use);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(getString(R.string.preftag_camera_to_use), id);
            editor.apply();

            // Restart this activity with the new camera selected
            super.onBackPressed();
            Intent intent = new Intent(this, PrefsActCamPicker.class);
            startActivity(intent);
        }
    }


}

