package mymou;

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
import mymou.Utils.PermissionManager;
import mymou.preferences.PreferencesManager;
import mymou.preferences.PrefsFragCropPickerParent;
import mymou.task.backend.RewardSystem;
import mymou.task.backend.TaskManager;
import mymou.task.backend.UtilsTask;
import mymou.preferences.PrefsActSystem;

public class MainMenu extends Activity {

    private static String TAG = "MyMouMainMenu";

    // If true this automatically starts the task upon application startup (Speeds up debugging/testing)
    public static final boolean testingMode = false;

    private static PreferencesManager preferencesManager;
    private static RewardSystem rewardSystem;

    // Default channel to be activated by the pump
    private static int reward_chan;

    // The task to be loaded, set by the spinner
    private static int taskSelected = 2;

    // Tasks cannot run unless permissions have been granted
    private boolean permissions_granted = false;

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

        if (testingMode && permissions_granted) {
            startTask();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("asdf", "onActivityResult_act_mainmenu");
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
            tv1.setText("Connected");
        } else if (!preferencesManager.bluetooth) {
            tv1.setText("Disabled");
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
        if (taskSelected < 2) {
            UtilsTask.toggleCue(findViewById(R.id.buttonTaskSettings), false);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // Update task selected
                taskSelected = position;
                if (taskSelected < 2) {
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
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    private void checkIfCrashed() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("restart")) {
                //If crashed then restart task
                Log.d(TAG, "checkIfCrashed() restarted task");
                startTask();
            }
         }
    }


    private void initialiseLayoutParameters() {
        // Buttons
        findViewById(R.id.buttonStart).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttonSettings).setOnClickListener(buttonClickListener);
        findViewById(R.id.buttonTaskSettings).setOnClickListener(buttonClickListener);

        // Radio groups (reward system controller)
        reward_chan = preferencesManager.default_rew_chan;
        if (reward_chan == 0) {
            RadioButton radioButton = findViewById(R.id.rb_chan0);
            radioButton.setChecked(true);
        } else if (reward_chan == 1) {
            RadioButton radioButton = findViewById(R.id.rb_chan1);
            radioButton.setChecked(true);
        } else if (reward_chan == 2) {
            RadioButton radioButton = findViewById(R.id.rb_chan2);
            radioButton.setChecked(true);
        } else if (reward_chan == 3) {
            RadioButton radioButton = findViewById(R.id.rb_chan3);
            radioButton.setChecked(true);
        }
        RadioGroup group = findViewById(R.id.rg_rewchanpicker);
        group.setOnCheckedChangeListener(checkedChangeListener);
        RadioGroup group2 = findViewById(R.id.rg_rewonoff);
        group2.setOnCheckedChangeListener(checkedChangeListener);

    }

    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int id = group.getCheckedRadioButtonId();
            switch (id) {
                case R.id.rb_chan0:
                    reward_chan = 0;
                    break;
                case R.id.rb_chan1:
                    reward_chan = 1;
                    break;
                case R.id.rb_chan2:
                    reward_chan = 2;
                    break;
                case R.id.rb_chan3:
                    reward_chan = 3;
                    break;
                case R.id.rb_pumpon:
                    if (!rewardSystem.bluetoothConnection) {
                        Log.d(TAG, "Error: Bluetooth not connected");
                        Toast.makeText(MainMenu.this, "Error: Bluetooth not connected/enabled", Toast.LENGTH_LONG).show();
                        RadioButton radioButton = findViewById(R.id.rb_pumpoff);
                        radioButton.setChecked(true);
                        return;
                    } else {
                        rewardSystem.startChannel(reward_chan);
                    }
                    break;
                case R.id.rb_pumpoff:
                    rewardSystem.stopChannel(reward_chan);
                    break;
            }

            // And always update default reward channel in case they changed value
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(getString(R.string.preftag_default_rew_chan), reward_chan).commit();
        }
    };


    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.buttonStart:
                    startTask();
                    break;
                case R.id.buttonSettings:
                    Intent intent = new Intent(context, PrefsActSystem.class);
                    intent.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_system_settings));
                    startActivity(intent);
                    break;
                case R.id.buttonTaskSettings:
                    if (taskSelected == 2) {
                        Intent intent2 = new Intent(context, PrefsActSystem.class);
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_obj_disc_settings));
                        startActivity(intent2);
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        if (permissions_granted) {
            rewardSystem.quitBt();
        }
    }

    // TODO: Figure out how to move this to PermissionsManager
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions granted");
                checkPermissions();
            } else {
                Toast.makeText(this, "Permission denied, all permissions must be enabled before app can run", Toast.LENGTH_LONG).show();
            }
        }
    }


}