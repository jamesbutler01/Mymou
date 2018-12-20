package com.example.jbutler.mymou;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainMenu extends Activity  {

    // If true this automatically starts the task upon application startup
    // Speeds up debugging/testing
    public static final boolean testingMode = true;

    // Can disable bluetooth and RewardSystem connectivity here
    public static final boolean useBluetooth = false;

    // Can disable facial recognition here
    // To use faceRecog must have the weights for the ANN (wo.txt, wi.txt, meanAndVar.txt) present in the Mymou folder
    public static final boolean useFaceRecog = false;

    public static RewardSystem rewardSystem;

    //Permission variables
    private boolean permissions = false;
    String[] permissionCodes = {
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_SETTINGS,
    };
    private Button[] permissionButtons = new Button[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        initialiseLayoutParameters();

        checkPermissions();

        checkIfCrashed();

        initaliseRewardSystem();

        if(testingMode && permissions) {
            startTask();
        }
    }

    private void startTask() {
        Button startButton = findViewById(R.id.buttonStart);
        startButton.setText("Loading..");
        rewardSystem.quitBt();  // Reconnect from next activity
        Intent intent = new Intent(this, TaskManager.class);
        startActivity(intent);
    }

    private void initaliseRewardSystem() {
        rewardSystem = new RewardSystem(this);
        TextView tv1 = findViewById(R.id.tvBluetooth);
        if (rewardSystem.bluetoothConnection) {
            tv1.setText("Bluetooth status: Connected");
        } else if (!useBluetooth) {
            tv1.setText("Bluetooth status: Disabled");
        }
    }

    private void checkIfCrashed() {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if (extras.getBoolean("restart")) {
                //If crashed then restart task
                startTask();
            }
        }
    }

    private void checkPermissions() {
        // Check if all permissions granted
        boolean permissionFlag = true;
        for (int i = 0; i < permissionCodes.length; i++){
            if(!checkPermissionNested(i)) {
                permissionFlag = false;
                break;
            }
        }
        if(permissionFlag) {
            View layout = findViewById(R.id.layoutCoveringUi);
            layout.setVisibility(View.INVISIBLE);
            permissions = true;
        }
    }

    private boolean checkPermissionNested(int i_perm) {
        final String permissionItem = permissionCodes[i_perm];
        int hasPermission=-1;
        if (i_perm<5) {
            hasPermission = checkSelfPermission(permissionItem);
        } else {
            if (Settings.System.canWrite(this)) {
                hasPermission = PackageManager.PERMISSION_GRANTED;
            }
        }
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(permissionItem)) {
                Toast.makeText(this, "All permissions must be enabled before app can run", Toast.LENGTH_LONG).show();
                requestPermissionLocal(i_perm);
                return false;
            }
            requestPermissionLocal(i_perm);
            return false;
        } else {
            permissionButtons[i_perm].setText("Granted");
            return true;
        }
    }

    private void requestPermissionLocal(int i_perm){
        if (i_perm==5) {  // This one is handled differently
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        } else {
            requestPermissions(new String[] {permissionCodes[i_perm]},123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission enabled", Toast.LENGTH_SHORT).show();
                checkPermissions();
            } else {
                // Permission Denied
                Toast.makeText(this, "Permission denied, all permissions must be enabled before app can run", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initialiseLayoutParameters() {
        //Permission buttons
        permissionButtons[0] = findViewById(R.id.permbuttonCamera);
        permissionButtons[1] = findViewById(R.id.permbuttonWrite);
        permissionButtons[2] = findViewById(R.id.permbuttonBt0);
        permissionButtons[3] = findViewById(R.id.permbuttonBt1);
        permissionButtons[4] = findViewById(R.id.permbuttonBt2);
        permissionButtons[5] = findViewById(R.id.permbuttonSettings);

        findViewById(R.id.mainPermButton).setOnClickListener(buttonClickListener);
        for (int i = 0; i < permissionButtons.length; i++) {
            permissionButtons[i].setOnClickListener(buttonClickListener);
        }
        findViewById(R.id.buttonStart).setOnClickListener(buttonClickListener);

        initialiseToggleButtons();
    }

    private void initialiseToggleButtons() {
        CompoundButton.OnCheckedChangeListener multiListener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (!rewardSystem.bluetoothConnection) {
                    Log.d("MainMenu", "Error: Bluetooth not connected");
                    return;
                }
                int chan = -1;
                switch (v.getId()){
                    case R.id.chanZeroButt:
                        chan = 0;
                        break;
                    case R.id.chanOneButt:
                        chan = 1;
                        break;
                    case R.id.chanTwoButt:
                        chan = 2;
                        break;
                    case R.id.chanThreeButt:
                        chan = 3;
                        break;
                }
                if (isChecked) {
                    rewardSystem.startChannel(chan);
                } else {
                    rewardSystem.stopChannel(chan);
                }
            }
        };

        ((ToggleButton)  findViewById(R.id.chanZeroButt)).setOnCheckedChangeListener(multiListener);
        ((ToggleButton)  findViewById(R.id.chanOneButt)).setOnCheckedChangeListener(multiListener);
        ((ToggleButton)  findViewById(R.id.chanTwoButt)).setOnCheckedChangeListener(multiListener);
        ((ToggleButton)  findViewById(R.id.chanThreeButt)).setOnCheckedChangeListener(multiListener);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.buttonStart:
                    startTask();
                    break;
                case R.id.mainPermButton:
                    checkPermissions();
                    break;
                case R.id.permbuttonCamera:
                    checkPermissionNested(0);
                    break;
                case R.id.permbuttonWrite:
                    checkPermissionNested(1);
                    break;
                case R.id.permbuttonBt0:
                    checkPermissionNested(2);
                    break;
                case R.id.permbuttonBt1:
                    checkPermissionNested(3);
                    break;
                case R.id.permbuttonBt2:
                    checkPermissionNested(4);
                    break;
                case R.id.permbuttonSettings:
                    checkPermissionNested(5);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("stop","stopped");
        if(permissions) {
            rewardSystem.quitBt();
        }
    }

}
