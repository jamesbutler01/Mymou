package com.example.jbutler.mymou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.preference.PreferenceManager;

public class MainMenu extends Activity  {

    private static String TAG = "MyMouMainMenu";

    // If true this automatically starts the task upon application startup (Speeds up debugging/testing)
    public static final boolean testingMode = false;

    // Disable bluetooth and RewardSystem connectivity here
    private static PreferencesManager preferencesManager;

    private static RewardSystem rewardSystem; //TODO this is flagged as a memory leak (Context classes should not be in static fields)

    // The task to be loaded, set by the spinner
    private static int taskSelected = 2;

    // Tasks cannot run unless permissions have been granted
    private boolean permissions_granted=false;

    private Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Retrieve settings
        preferencesManager = new PreferencesManager(this);

        initialiseLayoutParameters();

        checkPermissions();

        checkIfCrashed();

        initialiseRewardSystem();

        initialiseSpinner();

        if(testingMode && permissions_granted) {
            startTask();
        }

    }

    private void checkPermissions() {
        if (new PermissionManager(this, this).checkPermissions()) {
            permissions_granted = true;
        }
    }

    private void startTask() {
        // Task can only start if all permissions granted
        if (!permissions_granted) {
            checkPermissions();
            return;
        }
        Button startButton = findViewById(R.id.buttonStart);
        startButton.setText("Loading ...");

        Log.d(TAG, "Starting TaskManager as Intent...");

        rewardSystem.quitBt();  // Reconnect from next activity

        Intent intent = new Intent(this, TaskManager.class);
        intent.putExtra("tasktoload", taskSelected);
        startActivity(intent);
    }

    private void initialiseRewardSystem() {
        rewardSystem = new RewardSystem(this, this);
        TextView tv1 = findViewById(R.id.tvBluetooth);
        if (rewardSystem.bluetoothConnection) {
            tv1.setText("Bluetooth status: Connected");
        } else if (!preferencesManager.bluetooth) {
            tv1.setText("Bluetooth status: Disabled");
        }
    }

    // This is the dropdown menu to select task to load
    private void initialiseSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerTaskMenu);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.available_tasks, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Find previously selected task and set spinner to this position
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String key = "task_selected";
        int prev_task_selected = settings.getInt(key, 0);
        taskSelected = prev_task_selected;
        spinner.setSelection(taskSelected);
        if(taskSelected < 2) {
            UtilsTask.toggleCue(findViewById(R.id.buttonTaskSettings), false);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // Update task selected
                taskSelected = position;
                if(taskSelected < 2) {
                    UtilsTask.toggleCue(findViewById(R.id.buttonTaskSettings), false);
                } else {
                    UtilsTask.toggleCue(findViewById(R.id.buttonTaskSettings), true);
                }

                // Store for future reference
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(key, position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) { }
        });

    }

    private void checkIfCrashed() {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if (extras.getBoolean("restart")) {
                //If crashed then restart task
                Log.d(TAG, "checkIfCrashed() restarted task");
                startTask();
            }
        }
    }


    private void initialiseLayoutParameters() {
        findViewById(R.id.buttonStart).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttonSettings).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttonTaskSettings).setOnClickListener(buttonClickListener);
        initialiseToggleButtons();
    }

    private void initialiseToggleButtons() {
        CompoundButton.OnCheckedChangeListener multiListener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (!rewardSystem.bluetoothConnection) {
                    Log.d(TAG, "Error: Bluetooth not connected");
                    Toast.makeText(MainMenu.this, "Error: Bluetooth not connected/enabled", Toast.LENGTH_LONG).show();
                    v.setChecked(false);
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
                case R.id.buttonSettings:
                    Intent intent = new Intent(context, PrefsActSystem.class);
                    startActivity(intent);
                    break;
                case R.id.buttonTaskSettings:
                    if (taskSelected == 2) {
                        Intent intent2 = new Intent(context, PrefsActTaskObjectDiscrim.class);
                        startActivity(intent2);
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy() called");
        if(permissions_granted) {
            rewardSystem.quitBt();
        }
    }

    // TODO: Figure out how to move this to PermissionsManager
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions granted");
                checkPermissions();
            } else {
                Toast.makeText(this, "Permission denied, all permissions must be enabled before app can run", Toast.LENGTH_LONG).show();
            }
        }
    }


}