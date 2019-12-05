package mymou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.preference.PreferenceManager;

import mymou.Utils.PermissionManager;
import mymou.Utils.UtilsSystem;
import mymou.preferences.PreferencesManager;
import mymou.preferences.PrefsActCamPicker;
import mymou.task.backend.DataViewer;
import mymou.task.backend.RewardSystem;
import mymou.task.backend.TaskManager;
import mymou.task.backend.UtilsTask;
import mymou.preferences.PrefsActSystem;

public class MainMenu extends Activity {

    private static String TAG = "MyMouMainMenu";

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

        UtilsSystem.setBrightness(true, this);

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
        updateRewardText();
        rewardSystem.setCustomObjectListener(new RewardSystem.MyCustomObjectListener() {
            @Override
            public void onChangeListener() {
                updateRewardText();
            }
        });

    }

    private void updateRewardText() {
        TextView tv1 = findViewById(R.id.tvBluetooth);
        if (!preferencesManager.bluetooth) {
            tv1.setText("Disabled");
        } else if (rewardSystem.bluetoothConnection) {
            tv1.setText("Connected");
        } else if (!rewardSystem.bluetoothConnection) {
            tv1.setText("Disconnected");
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
        taskSelected = settings.getInt(key, 0);

        // Set up UI for currently selected task
        spinner.setSelection(taskSelected);
        checkTaskHasSettings();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {

                // Update task selected
                taskSelected = position;

                // Activate settings button if valid
                checkTaskHasSettings();

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

    private void checkTaskHasSettings() {
        TypedArray tasks_has_settings = getResources().obtainTypedArray(R.array.task_has_settings);
        boolean has_settings = Boolean.valueOf(tasks_has_settings.getBoolean(taskSelected, false));
        UtilsTask.toggleCue((Button) findViewById(R.id.buttonTaskSettings), has_settings);
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
        findViewById(R.id.buttonViewData).setOnClickListener(buttonClickListener);
        findViewById(R.id.info_button).setOnClickListener(buttonClickListener);

        // Disabled as in development
        findViewById(R.id.buttonViewData).setEnabled(false);

        // Radio groups (reward system controller)
        reward_chan = preferencesManager.default_rew_chan;
        RadioButton[] radioButtons = new RadioButton[preferencesManager.max_reward_channels];
        radioButtons[0] = findViewById(R.id.rb_chan0);
        radioButtons[1] = findViewById(R.id.rb_chan1);
        radioButtons[2] = findViewById(R.id.rb_chan2);
        radioButtons[3] = findViewById(R.id.rb_chan3);
        radioButtons[reward_chan].setChecked(true);

        for (int i=0; i<preferencesManager.max_reward_channels; i++) {
            boolean active = i >= preferencesManager.num_reward_chans ? false : true;
            UtilsTask.toggleView(radioButtons[i], active);
        }

        RadioGroup group = findViewById(R.id.rg_rewchanpicker);
        group.setOnCheckedChangeListener(checkedChangeListener);
        RadioGroup group2 = findViewById(R.id.rg_rewonoff);
        group2.setOnCheckedChangeListener(checkedChangeListener);

        // Reset text on start button in case they are returning from task
        Button startButton = findViewById(R.id.buttonStart);
        startButton.setText("START TASK");

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
            Log.d(TAG, "onClick: "+view.getId());
            switch (view.getId()) {
                case R.id.buttonStart:
                    startTask();
                    break;
                case R.id.buttonSettings:
                    Intent intent = new Intent(context, PrefsActSystem.class);
                    intent.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_menu_prefs));
                    startActivity(intent);
                    break;
                case R.id.buttonTaskSettings:
                    Intent intent2 = new Intent(context, PrefsActSystem.class);

                    // Load task specific settings
                    if (taskSelected < 5) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_t_one_settings));
                    } else if (taskSelected == 6) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_disc_maze_settings));
                    } else if (taskSelected == 7) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_odc_settings));
                    } else if (taskSelected == 8) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_od_settings));
                    } else if (taskSelected == 9) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_pr_settings));
                    } else if (taskSelected == 10) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_ea_settings));
                    } else if (taskSelected == 11) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_sr_settings));
                    } else if (taskSelected == 12) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_sl_settings));
                    } else if (taskSelected == 13) {
                        intent2.putExtra(getString(R.string.preftag_settings_to_load), getString(R.string.preftag_task_rdm_settings));
                    }

                    startActivity(intent2);
                    break;
                case R.id.buttonViewData:
                    Intent intent3 = new Intent(context, DataViewer.class);
                    startActivity(intent3);
                    break;
                case R.id.info_button:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
                    String[] descriptions = getResources().getStringArray(R.array.task_descriptions);
                    String[] names = getResources().getStringArray(R.array.available_tasks);
                    builder.setMessage(descriptions[taskSelected])
                            .setTitle(names[taskSelected]);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        preferencesManager = new PreferencesManager(this);
        initialiseLayoutParameters();
    }

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
