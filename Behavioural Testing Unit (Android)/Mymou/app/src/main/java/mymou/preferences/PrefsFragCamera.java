package mymou.preferences;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.jiangdg.usbcamera.UVCCameraHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mymou.R;

public class PrefsFragCamera extends PreferenceFragmentCompat {

    private String TAG = "MymouPrefsFragCamera";

    public PrefsFragCamera() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_camera, rootKey);
        Log.d(TAG, "onCreatePreferences");

        // Build resolution list for camera
        PreferencesManager preferencesManager  = new PreferencesManager(getContext());
        String prefTag = "";
        switch (preferencesManager.camera_to_use) {
            case CameraCharacteristics.LENS_FACING_BACK:
                prefTag = getString(R.string.preftag_camera_resolution_rear);
                findPreference(getString(R.string.preftag_camera_resolution_front)).setEnabled(false);
                findPreference(getString(R.string.preftag_camera_resolution_front)).setVisible(false);
                break;
            case CameraCharacteristics.LENS_FACING_FRONT:
                prefTag = getString(R.string.preftag_camera_resolution_front);
                findPreference(getString(R.string.preftag_camera_resolution_rear)).setEnabled(false);
                findPreference(getString(R.string.preftag_camera_resolution_rear)).setVisible(false);
                break;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                prefTag = getString(R.string.preftag_camera_resolution_ext);
                findPreference(getString(R.string.preftag_camera_resolution_front)).setEnabled(false);
                findPreference(getString(R.string.preftag_camera_resolution_rear)).setEnabled(false);
                findPreference(getString(R.string.preftag_camera_resolution_front)).setVisible(false);
                findPreference(getString(R.string.preftag_camera_resolution_rear)).setVisible(false);
                break;
        }

        if (preferencesManager.camera_to_use == getContext().getResources().getInteger(R.integer.TAG_CAMERA_EXTERNAL)) {
            return;
        }

        // Load up camera to get resolutions available
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] all_camera_ids = manager.getCameraIdList();
            String cameraId = all_camera_ids[preferencesManager.camera_to_use];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // Build list of resolutions to present to user
            List sizes = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
            CharSequence[] resolutions = new CharSequence[sizes.size()];
            CharSequence[] ints = new CharSequence[sizes.size()];
            for (int i = 0; i < sizes.size(); i++) {
                Size size = (Size) sizes.get(i);
                resolutions[i] = "" + size.getHeight() + "x" + size.getWidth();
                ints[i] = "" + i;
            }
            // Update list
            ListPreference lp = (ListPreference) findPreference(prefTag);
            lp.setEntries(resolutions);
            lp.setEntryValues(ints);

            // And set value of list to saved one
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
            int default_size = resolutions.length - 1;
            String resolution_saved = settings.getString(prefTag, ""+default_size);
            Log.d(TAG, "Setting resolution value to "+resolution_saved+ ",default="+default_size+" for "+prefTag);
            int resolution_index = Integer.valueOf(resolution_saved);
            lp.setValueIndex(resolution_index);

        } catch (CameraAccessException e) {

            e.printStackTrace();
        }

    }

}
