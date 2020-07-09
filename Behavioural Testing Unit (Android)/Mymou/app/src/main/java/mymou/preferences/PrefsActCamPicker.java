package mymou.preferences;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import org.w3c.dom.Text;

import mymou.CameraExternal;
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

    private static String[] messages = {
            "Currently selected: Rear camera",
            "Currently selected: Selfie camera",
            "Currently selected: External camera",};
    PreferencesManager preferencesManager;
    private AlertDialog mDialog;


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

        

        // Finally, actually load camera fragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("crop_picker", true);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (preferencesManager.camera_to_use != getApplicationContext().getResources().getInteger(R.integer.TAG_CAMERA_EXTERNAL)) {
            CameraMain fragment = new CameraMain();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.layout_camerapicker, fragment, "camera_fragment");
        } else {
            CameraExternal fragment = new CameraExternal();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.layout_camerapicker, fragment, "camera_fragment");
        }
        fragmentTransaction.commit();

    }


    private void showResolutionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        View rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_dialog_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_dialog);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResolutionList());
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String resolution = (String) adapterView.getItemAtPosition(position);
                String[] tmp = resolution.split("x");
                if (tmp != null && tmp.length >= 2) {
                    int widht = Integer.valueOf(tmp[0]);
                    int height = Integer.valueOf(tmp[1]);
                }
                mDialog.dismiss();
            }
        });

        builder.setView(rootView);
        mDialog = builder.create();
        mDialog.show();
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
