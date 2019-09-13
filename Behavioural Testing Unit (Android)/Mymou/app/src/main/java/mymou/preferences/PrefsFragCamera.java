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
import android.os.Bundle;
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

        // Load up camera to get resolutions available
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // Find selfie camera
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // Use the smallest available size.
                List sizes = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
                CharSequence[] resolutions = new CharSequence[sizes.size()];
                CharSequence[] ints = new CharSequence[sizes.size()];
                for (int i=0; i<sizes.size(); i++) {
                    Size size = (Size) sizes.get(i);
                    resolutions[i] = ""+size.getHeight()+"x"+size.getWidth();
                    ints[i] = ""+i;
                }
                ListPreference lp = (ListPreference)findPreference(getString(R.string.preftag_camera_resolution));
                lp.setEntries(resolutions);
                lp.setEntryValues(ints);

                // And set value of list to saved one
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                int default_size = sizes.size() - 1;
                String resolution_saved = settings.getString(getString(R.string.preftag_camera_resolution), ""+default_size);
                Log.d(TAG, "Setting resolution value to "+resolution_saved);
                int resolution_index = Integer.valueOf(resolution_saved);
                lp.setValueIndex(resolution_index);

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

}
