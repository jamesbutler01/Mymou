package mymou.task.backend;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.*;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import mymou.*;
import mymou.Utils.*;
import mymou.preferences.PreferencesManager;
import mymou.task.individual_tasks.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class TaskManager extends FragmentActivity implements View.OnClickListener {


    // Debug
    public static String TAG = "MyMouTaskManager";
    private static TextView tvExplanation, tvErrors;  // Explanatory messages for demo mode, and any errors present


    private static int taskId;  // Unique string prefixed to all log entries
    public static String TAG_FRAGMENT_TASK = "taskfrag";
    public static String TAG_FRAGMENT_CAMERA = "camerafrag";

    public static int faceRecogPrediction = -1;
    private static int monkeyButtonPressed = -1;
    private static boolean faceRecogRunning = false;
    private static int trialCounter = 0;
    public static RewardSystem rewardSystem;
    private static int latestRewardChannel;

    private static PreferencesManager preferencesManager;
    private static FolderManager folderManager;
    private static FaceRecog faceRecog;
    private static ArrayList<String> trialData;
    public static String photoTimestamp;
    private static Handler logHandler;
    private static HandlerThread logThread;
    private static FragmentManager fragmentManager;
    private static FragmentTransaction fragmentTransaction;
    private static Context mContext;
    private static Activity activity;
    private static CameraMain cM;

    // Aync handlers used to posting delayed task events
    private static Handler h0 = new Handler();  // Task trial_timer
    private static Handler h1 = new Handler();  // Prepare for new trial
    private static Handler h2 = new Handler();  // Timeout go cues
    private static Handler h3 = new Handler();  // Daily timer

    // Predetermined locations where cues can appear on screen, calculated by UtilsTask.calculateCueLocations()
    private static Point[] possible_cue_locs;

    // Timeouts for wrong choices by subject
    private static int timeoutWrongGoCuePressed = 300;  // Timeout for not pressing their own Go cue

    // Timer to reset task if subject stops halfway through a trial
    private static int time = 0;  // Time from last press - used for idle timeout if it reaches maxTrialDuration
    private static boolean timerRunning;  // Signals if trial_timer currently active

    // Task objects
    private static Button[] cues_Go = new Button[8]; // Go cues to start a trial
    private static Button[] cues_Reward = new Button[4];  // Reward cues for the different reward options

    // Boolean to signal if task should be active or not (e.g. overnight it is set to true)
    public static boolean task_enabled = true;

    // Boolean to signal whether a trial is currently active on screen
    private static boolean trial_running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_tasks);

        // In case of crashes
        initialiseAutoRestartHandler();

        // Create ui Elements
        assignObjects();
        initialiseScreenSettings();

        // Load settings
        loadAndApplySettings();
        loadtask();

        // Now adjust UI elements depending on the settings
        disableExtraGoCues();
        disableExtraRewardCues();
        positionGoCues();
        setOnClickListeners();

        // Load back end functions
        loadCamera();
        initialiseLogHandler();
        dailyTimer(false);
        if (preferencesManager.facerecog) {
            // Load facerecog off the main thread as takes a while
            Thread t = new Thread(new Runnable() {
                public void run() {
                    faceRecog = new FaceRecog();
                    if (!faceRecog.instantiated_successfully) {
                        tvErrors.setText(faceRecog.error_message);
                    }
                }
            });
            t.start();
        }

        // Disable app for now
        disableAllCues();
        enableApp(false);

        // Only lock if we aren't in testing mode
        if (!preferencesManager.debug) {
            this.startLockTask();
        }

        // Normally the reward system handles this as it has to wait for bluetooth connection
        if (!preferencesManager.bluetooth) {
            tvErrors.setText(getResources().getStringArray(R.array.error_messages)[getResources().getInteger(R.integer.i_bt_disabled)]);
        } else if (!rewardSystem.bluetoothConnection) {
            tvErrors.setText(getResources().getStringArray(R.array.error_messages)[getResources().getInteger(R.integer.i_bt_couldnt_connect)]);
        }

        // Lastly, we connect to the reward system, which will then activate the task once it successfully connects to bluetooth
        initialiseRewardSystem();

    }

    private void initialiseAutoRestartHandler() {
        if (!preferencesManager.debug) {
            Log.d(TAG, "initialiseAutoRestartHandler");
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    Log.d(TAG, "Task crashed");
                    new CrashReport(throwable, mContext);
                    rewardSystem.quitBt();
                    restartApp();

                }
            });
        }
    }

    private void loadtask() {
        taskId = getIntent().getIntExtra("tasktoload", -1);

        // Load settings for task
        switch (taskId) {
            case 0:
                preferencesManager.TrainingTasks();
                break;
            case 1:
                preferencesManager.TrainingTasks();
                break;
            case 2:
                preferencesManager.TrainingTasks();
                break;
            case 3:
                preferencesManager.TrainingTasks();
                break;
            case 4:
                preferencesManager.TrainingTasks();
                break;
            case 5:
                break;
            case 6:
                preferencesManager.DiscreteMaze();
                break;
            case 7:
                preferencesManager.ObjectDiscriminationCol();
                break;
            case 8:
                preferencesManager.ObjectDiscrim();
                break;
            case 9:
                preferencesManager.ProgressiveRatio();
                break;
            case 10:
                preferencesManager.EvidenceAccum();
                break;
            case 11:
                preferencesManager.SpatialResponse();
                break;
            case 12:
                preferencesManager.SequentialLearning();
                break;
            default:
                Log.d(TAG, "No task specified");
                new Exception("No task specified");
        }

    }

    private void disableExtraGoCues() {
        // Disable go cues for extra monkeys
        Button[] cues_excluded = Arrays.copyOfRange(cues_Go, preferencesManager.num_monkeys, cues_Go.length);
        UtilsTask.toggleCues(cues_excluded, false);

        // Shorten list to number needed
        cues_Go = Arrays.copyOf(cues_Go, preferencesManager.num_monkeys);
    }

    private void disableExtraRewardCues() {
        // Disable go cues for extra monkeys
        Button[] cues_excluded = Arrays.copyOfRange(cues_Reward, preferencesManager.num_reward_chans, cues_Reward.length);
        UtilsTask.toggleCues(cues_excluded, false);

        // Shorten list to number needed
        cues_Reward = Arrays.copyOf(cues_Reward, preferencesManager.num_reward_chans);
    }

    private void initialiseLogHandler() {
        logThread = new HandlerThread("LogBackground");
        logThread.start();
        logHandler = new Handler(logThread.getLooper());
    }

    private void initialiseRewardSystem() {
        boolean successfullyEstablished = false;
        rewardSystem.quitBt();
        rewardSystem = new RewardSystem(this, this);
        if (rewardSystem.bluetoothConnection | !preferencesManager.bluetooth) {
            successfullyEstablished = enableApp(true);

        }

        // Repeat if either couldn't connect or couldn't enable app
        if (!successfullyEstablished) {
            Handler handlerOne = new Handler();
            handlerOne.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initialiseRewardSystem();
                }
            }, 5000);
        } else {
            tvErrors.setVisibility(View.INVISIBLE);

            // Register listener to disable tablet if bluetooth gets DC'ed
            rewardSystem.setCustomObjectListener(new RewardSystem.MyCustomObjectListener() {
                @Override
                public void onChangeListener() {
                    enableApp(rewardSystem.bluetoothConnection);
                }
            });

        }
    }


    public static void startTrial(int monkId) {
        if (!task_enabled) {
            return;
        }  // Abort if task currently disabled

        boolean valid_configuration = true;

        Task task = null;
        Bundle bundle = new Bundle();
        bundle.putInt("currMonk", monkId);
        bundle.putInt("numTrials", trialCounter);
        switch (taskId) {
            case 0:
                task = new TaskTrainingOneFullScreen();
                break;
            case 1:
                task = new TaskTrainingTwoShrinkingCue();
                break;
            case 2:
                task = new TaskTrainingThreeMovingCue();
                break;
            case 3:
                task = new TaskTrainingFourSmallMovingCue();
                break;
            case 4:
                task = new TaskTrainingFiveTwoStep();
                break;
            case 5:
                task = new TaskExample();
                break;
            case 6:
                task = new TaskDiscreteMaze();
                break;
            case 7:
                task = new TaskObjectDiscrimCol();

                // Check settings correct
                valid_configuration = preferencesManager.objectdiscrim_valid_config;

                break;
            case 8:
                task = new TaskObjectDiscrim();
                break;
            case 9:
                task = new TaskProgressiveRatio();
                break;
            case 10:
                task = new TaskEvidenceAccum();
                break;
            case 11:
                task = new TaskSpatialResponse();
                break;
            case 12:
                task = new TaskSequentialLearning();
                break;
            case 13:
                task = new TaskRandomDotMotion();
                break;
            default:
                new Exception("No valid task specified");
                break;
        }

        task.setFragInterfaceListener(new TaskInterface() {
            @Override
            public void resetTimer_() {
                resetTimer();
            }

            @Override
            public void trialEnded_(String outcome, double rew_scalar) {
                trialEnded(outcome, rew_scalar);
            }

            @Override
            public void logEvent_(String outcome) {
                logEvent(outcome, true);
            }

            @Override
            public void giveRewardFromTask_(int amount) {
                giveRewardFromTask(amount);
            }

            @Override
            public void takePhotoFromTask_() {
                takePhoto();
            }

            @Override
            public void setBrightnessFromTask_(boolean bool) {
                UtilsSystem.setBrightness(bool, mContext);
            }

            @Override
            public void commitTrialDataFromTask_(String overallTrialOutcome) {
                commitTrialData(overallTrialOutcome);
            }

        });

        task.setArguments(bundle);
        fragmentTransaction.add(R.id.task_container, task, TAG_FRAGMENT_TASK);

        if (!valid_configuration) {

            tvErrors.setText(preferencesManager.base_error_message + preferencesManager.objectdiscrim_errormessage);

        } else {

            if (!timerRunning) {
                trial_timer();
            } // Start task timer first (so will still timeout if task is disabled)

            logEvent(preferencesManager.ec_trial_started, false);
            updateTvExplanation("");
            trial_running = true;
            commitFragment();

        }

    }


    // Automatically restart static fragmentTransaction so it is always available to use
    private static void commitFragment() {
        try {
            fragmentTransaction.commit();
            fragmentTransaction = fragmentManager.beginTransaction();
        } catch (IllegalStateException e) {
            new CrashReport(e, mContext);
        }
    }

    private static void loadCamera() {
        if (!preferencesManager.camera) {
            return;
        }
        Log.d(TAG, "Loading camera fragment");
        cM = new CameraMain();
        fragmentTransaction.add(R.id.task_container, cM);
        commitFragment();
    }


    private void restartApp() {
        if (preferencesManager.restartoncrash) {
            Log.d(TAG, "Restarting task");
            Intent intent = new Intent(getApplicationContext(), TaskManager.class);
            intent.putExtra("restart", true);
            intent.putExtra("tasktoload", taskId);
            final PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, pendingIntent);
        }
        System.exit(0);
    }


    public static void setFaceRecogPrediction(int[] intArray) {

        if (faceRecog != null && faceRecog.instantiated_successfully) {

            faceRecogPrediction = faceRecog.idImage(intArray);

            if (faceRecogPrediction == monkeyButtonPressed) {

                // If monkey clicked it's designated menu_button
                Log.d(TAG, "Monkey pressed correct cue");
                resultMonkeyPressedTheirCue(true);

            } else {

                // If monkey clicked wrong menu_button
                Log.d(TAG, "Monkey pressed wrong cue");
                resultMonkeyPressedTheirCue(false);

            }

        } else {

            Log.d(TAG, "Error: FaceRecog not instantiated");

        }

        // Release facerecog as now ready to process another image
        faceRecogRunning = false;
    }

    public static void commitTrialData(String overallTrialOutcome) {
        if (trialData != null) {

            // Reset trial counter if we passed midnight
            if (dateHasChanged()) {

                trialCounter = 0;

            }

            // Append all static variables to each line of trial data and write to file
            int length = trialData.size();
            for (int i = 0; i < length; i++) {
                String s = trialData.get(i);
                // Prefix variables that were constant throughout trial (trial result, which monkey, etc)
                s = taskId + "," + trialCounter + "," + faceRecogPrediction + "," + overallTrialOutcome + "," + s;
                logHandler.post(new WriteDataToFile(s, mContext));
            }

            // Place photo in correct monkey's folder
            if (preferencesManager.facerecog) {
                // Find name of photo and old/new location
                String photo_name = folderManager.getBaseDate() + "_" + photoTimestamp + ".jpg";
                File original_photo = new File(folderManager.getImageFolder(), photo_name);
                File new_photo = new File(folderManager.getMonkeyFolder(faceRecogPrediction), photo_name);

                // Copy from originalphoto to newphoto
                boolean copy_successful = true;
                FileChannel inputStream = null;
                FileChannel outputStream = null;
                try {
                    inputStream = new FileInputStream(original_photo).getChannel();
                    outputStream = new FileOutputStream(new_photo).getChannel();
                    outputStream.transferFrom(inputStream, 0, inputStream.size());
                } catch (IOException e) {
                    copy_successful = false;
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException e) {
                            copy_successful = false;
                            e.printStackTrace();
                        }
                    }
                }

                // Erase original photo if copy successful
                if (copy_successful) {
                    original_photo.delete();
                }

            }

        }

        // And now clear the list ready for the next trial
        trialData = new ArrayList<String>();

        // Increment trial counter
        trialCounter++;

    }

    private void initialiseScreenSettings() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    // Recursive function to track time and switch app off when it hits a certain time
    public static void dailyTimer(boolean shutdown) {
        Log.d(TAG, "dailyTimer called");
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int min = c.get(Calendar.MINUTE);
        int AMPM = c.get(Calendar.AM_PM);
        if (AMPM == Calendar.PM) {
            hour += 12;
        }

        if (shutdown) {  // If shutdown and waiting to start up in the morning
            if (hour >= preferencesManager.autostart_hour && min > preferencesManager.autostart_min) {
                Log.d(TAG, "dailyTimer enabling app");
                enableApp(true);
                shutdown = false;
            }
        } else {  // If active and waiting to shutdown
            if (hour >= preferencesManager.autostop_hour && min > preferencesManager.autostop_min) {
                Log.d(TAG, "dailyTimer disabling app");
                enableApp(false);
                shutdown = true;
            }
        }

        final boolean shutdown_f = shutdown;
        h3.postDelayed(new Runnable() {
            @Override
            public void run() {
                dailyTimer(shutdown_f);
            }
        }, 60000);
    }

    public static boolean enableApp(boolean bool) {
        Log.d(TAG, "Enabling app" + bool);
        UtilsSystem.setBrightness(bool, mContext);

        View foregroundBlack = activity.findViewById(R.id.foregroundblack);
        if (foregroundBlack != null) {
            task_enabled = bool;
            foregroundBlack.bringToFront();
            activity.findViewById(R.id.tvError).bringToFront();
            UtilsTask.toggleView(foregroundBlack, !bool);  // This is inverted as foreground object disables app
            if (bool) {
                PrepareForNewTrial(0);
            } else {
                killTask();
            }
            return true;
        } else {
            Log.d(TAG, "foregroundBlack object not instantiated");
            return false;
        }
    }


    public static void logEvent(String data, boolean from_task) {
        Log.d(TAG, "logEvent: " + data);

        // Seperate task logs and manager logs into different columns
        String column = "";
        if (!from_task) {
            column = ",";
        }

        // Store data for logging at end of trial
        String timestamp = folderManager.getTimestamp();
        String msg = photoTimestamp + "," + timestamp + "," + column + data;
        trialData.add(msg);
    }

    // Takes selfie and checks to see if it matches with which monkey it should be
    public static boolean checkMonkey(int monkId) {
        if (preferencesManager.facerecog && faceRecogRunning) {
            // Previous face recog still running
            return false;
        }

        boolean photoTaken = takePhoto();

        if (photoTaken && preferencesManager.facerecog) {
            // Photo taken, now we wait for faceRecog to return prediction
            faceRecogRunning = true;
            monkeyButtonPressed = monkId;
        }

        return photoTaken;

    }

    public static boolean takePhoto() {
        if (preferencesManager.camera) {
            if (cM.camera_error) {
                // Kill camera fragment and restart it
                fragmentTransaction.remove(fragmentManager.findFragmentByTag(TAG_FRAGMENT_TASK));
                fragmentTransaction.remove(fragmentManager.findFragmentByTag(TAG_FRAGMENT_CAMERA));
                commitFragment();
                loadCamera();
                startTrial(-1);
            }
            Log.d(TAG, "takePhoto() called");
            photoTimestamp = folderManager.getTimestamp();
            boolean photoTaken = CameraMain.captureStillPicture(photoTimestamp);
            return photoTaken;
        } else {
            return true;
        }
    }

    //Checks if todays date is the same as the last time function was called
    public static boolean dateHasChanged() {
        String todaysDate = folderManager.getBaseDate();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String lastRecordedDate = sharedPref.getString("lastdate", "null");
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lastdate", todaysDate);
        editor.commit();
        if (todaysDate.equals(lastRecordedDate)) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        super.onDestroy();
        cancelHandlers();
        rewardSystem.quitBt();
        quitThreads();
        this.stopLockTask();
    }

    private void quitThreads() {
        try {
            logThread.quitSafely();
            logThread.join();
            logThread = null;
            logHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Allow user to exit task if testing mode is enabled
        if (preferencesManager.debug) {
            return super.onKeyDown(keyCode, event);
        } else {
            return false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private void loadAndApplySettings() {
        // Face recog
        if (preferencesManager.facerecog) {
            folderManager = new FolderManager(mContext, preferencesManager.num_monkeys);
        } else {
            folderManager = new FolderManager(mContext, 0);
        }

        // Colours
        findViewById(R.id.task_container).setBackgroundColor(preferencesManager.taskbackground);
        for (int i = 0; i < preferencesManager.num_monkeys; i++) {
            cues_Go[i].setBackgroundColor(preferencesManager.colours_gocues[i]);
        }

    }

    private void assignObjects() {
        // Global variables
        activity = this;
        mContext = getApplicationContext();
        preferencesManager = new PreferencesManager(this);
        possible_cue_locs = new UtilsTask().getPossibleCueLocs(this);
        trialData = new ArrayList<String>();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        latestRewardChannel = preferencesManager.default_rew_chan;

        // Layout views
        for (int i = 0; i < cues_Go.length; i++) {
            cues_Go[i] = UtilsTask.addColorCue(i, preferencesManager.colours_gocues[i], this, this, findViewById(R.id.task_container));
        }
        try {
            cues_Reward[0] = findViewById(R.id.buttonRewardZero);
            cues_Reward[1] = findViewById(R.id.buttonRewardOne);
            cues_Reward[2] = findViewById(R.id.buttonRewardTwo);
            cues_Reward[3] = findViewById(R.id.buttonRewardThree);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.exit(0);
        }
        tvExplanation = findViewById(R.id.tvLog);
        tvErrors = findViewById(R.id.tvError);
        UtilsTask.toggleView(tvExplanation, preferencesManager.debug);

    }

    private void setOnClickListeners() {
        activity.findViewById(R.id.foregroundblack).setOnClickListener(this);
        UtilsSystem.setOnClickListenerLoop(cues_Reward, this);
        UtilsSystem.setOnClickListenerLoop(cues_Go, this);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClickListener called for " + view.getId());
        if (!task_enabled) {
            return;
        }

        // Always disable all cues after a press as monkeys love to bash repeatedly
        disableAllCues();

        // Reset task trial_timer (used for idle timeout and calculating reaction times if desired)
        resetTimer();

        // Make screen bright
        UtilsSystem.setBrightness(true, mContext);

        // Now decide what to do based on what menu_button pressed
        switch (view.getId()) {
            case R.id.foregroundblack:
                // Absorb any touch events while disabled
                break;
            case R.id.buttonRewardZero:
                deliverReward(0, 1);
                break;
            case R.id.buttonRewardOne:
                deliverReward(1, 1);
                break;
            case R.id.buttonRewardTwo:
                deliverReward(2, 1);
                break;
            case R.id.buttonRewardThree:
                deliverReward(3, 1);
                break;
            default:
                // If it wasn't a reward cue it must be a go cue
                checkMonkeyPressedTheirCue(view.getId());
                break;
        }
    }

    // Each monkey has it's own start cue. At start of each trial make sure the monkey pressed it's own cue using
    // the facial recognition
    private static void checkMonkeyPressedTheirCue(int monkId) {

        // Take selfie
        boolean photoTaken = checkMonkey(monkId);

        if (!photoTaken) {

            // Photo not taken as camera/faceRecog wasn't ready so reset go cues to let them press again
            Log.d(TAG, "Error: Camera not ready!");
            UtilsTask.toggleCues(cues_Go, true);

        } else if (photoTaken) {

            if (preferencesManager.facerecog) {
                // If photo successfully taken then do nothing as wait for faceRecog to return prediction
                // setFaceRecogPrediction will ultimately call resultMonkeyPressedTheirCue
                Log.d(TAG, "Photo taken, waiting for faceRecog..");

            } else {

                // If photo successfully taken then start the trial
                startTrial(monkId);

            }

        }

    }

    public static void resultMonkeyPressedTheirCue(boolean correctCuePressed) {

        // Have to put this on UI thread as it's called from faceRecog which is off main thread
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (correctCuePressed) {
                    startTrial(faceRecogPrediction);
                } else {
                    MonkeyPressedWrongGoCue();
                }
            }
        });
    }

    // Wrong Go cue selected so give short timeout
    public static void MonkeyPressedWrongGoCue() {

        // Log the event
        logEvent(preferencesManager.ec_wrong_gocue_pressed, false);
        commitTrialData(preferencesManager.ec_wrong_gocue_pressed);

        // Switch on red background
        activity.findViewById(R.id.background_main).setBackgroundColor(preferencesManager.timeoutbackground);

        // Switch off red background after certain delay
        h2.postDelayed(new Runnable() {
            @Override
            public void run() {
                UtilsTask.toggleCues(cues_Go, true);
                activity.findViewById(R.id.background_main).setBackgroundColor(preferencesManager.taskbackground);
            }
        }, timeoutWrongGoCuePressed);
    }

    private static void trialEnded(String result, double rew_scalar) {

        killTask();

        if (result == preferencesManager.ec_correct_trial) {
            correctTrial(rew_scalar);
        } else {
            incorrectTrial(result);
        }
    }

    private static void killTask() {
        if (trial_running) {
            fragmentTransaction.remove(fragmentManager.findFragmentByTag(TAG_FRAGMENT_TASK));
            commitFragment();
            h0.removeCallbacksAndMessages(null);
            timerRunning = false;
            trial_running = false;
        }
    }

    private static void incorrectTrial(String result) {
        activity.findViewById(R.id.background_main).setBackgroundColor(preferencesManager.timeoutbackground);
        endOfTrial(result, preferencesManager.timeoutduration);
    }

    private static void correctTrial(double rew_scalar) {

        activity.findViewById(R.id.background_main).setBackgroundColor(preferencesManager.rewardbackground);

        // If only one reward channel, skip reward selection stage
        if (preferencesManager.num_reward_chans == 1) {
            deliverReward(preferencesManager.default_rew_chan, rew_scalar);
        } else {
            // Otherwise reveal reward cues
            UtilsTask.randomlyPositionCues(cues_Reward, possible_cue_locs);
            UtilsTask.toggleCues(cues_Reward, true);
            updateTvExplanation("Correct trial! Choose your reward");

        }
    }

    private static void giveRewardFromTask(int reward_duration) {
        new SoundManager(preferencesManager).playTone();
        rewardSystem.activateChannel(latestRewardChannel, reward_duration);
    }


    private static void deliverReward(int juiceChoice, double rew_scalar) {
        // Play tone
        new SoundManager(preferencesManager).playTone();

        latestRewardChannel = juiceChoice;

        double reward_duration_double = preferencesManager.rewardduration * rew_scalar;
        int reward_duration_int = (int) reward_duration_double;

        updateTvExplanation("Delivering reward of " + reward_duration_int + "ms on channel " + juiceChoice);

        rewardSystem.activateChannel(juiceChoice, reward_duration_int);

        endOfTrial(preferencesManager.ec_correct_trial, preferencesManager.rewardduration + 5);
    }


    private static void endOfTrial(String outcome, int newTrialDelay) {
        logEvent(outcome, false);

        commitTrialData(outcome);

        PrepareForNewTrial(newTrialDelay);
    }

    private static void updateTvExplanation(String message) {
        Log.d(TAG, message);
        if (preferencesManager.debug) {
            tvExplanation.setText(message);
        }
    }


    private static void disableAllCues() {
        UtilsTask.toggleCues(cues_Reward, false);
        UtilsTask.toggleCues(cues_Go, false);
    }

    // Go cues are in static location to make it easier for monkeys to press their own cue
    private void positionGoCues() {
        if (possible_cue_locs.length < preferencesManager.num_monkeys) {
            new Exception("Go cues too big, not enough room for number of monkeys specified." +
                    "\nPlease reduce the size of the go cues or the number of monkeys");
        }

        // If only one monkey then put start cue in middle of screen
        if (preferencesManager.num_monkeys == 1) {
            cues_Go[0].setX(possible_cue_locs[possible_cue_locs.length / 2].x);
            cues_Go[0].setY(possible_cue_locs[possible_cue_locs.length / 2].y);
            return;
        }

        // If multiple monkeys then tile the space evenly
        int pos;
        int step = 1;
        // If there's enough room then space the go cues around the screen
        if (possible_cue_locs.length > preferencesManager.num_monkeys * 2) {
            step *= 2;
        }
        for (int i_monk = 0; i_monk < preferencesManager.num_monkeys; i_monk++) {
            if (i_monk % 2 == 0) {
                pos = i_monk * step;
            } else {
                pos = possible_cue_locs.length - (i_monk * step);
            }
            cues_Go[i_monk].setX(possible_cue_locs[pos].x);
            cues_Go[i_monk].setY(possible_cue_locs[pos].y);
        }
    }

    private static void resetTimer() {
        Log.d(TAG, "resetTimer");
        UtilsSystem.setBrightness(true, mContext);
        time = 0;
    }

    // Recursive function to track task time
    private static void trial_timer() {
        Log.d(TAG, "trial_timer " + time + " (limit =" + preferencesManager.responseduration + ")");

        time += 1000;

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (time > preferencesManager.responseduration) {

                    Log.d(TAG, "timer Trial timeout " + time);
                    resetTimer();
                    timerRunning = false;

                    idleTimeout();

                } else {

                    trial_timer();
                    timerRunning = true;

                }
            }
        }, 1000);
    }

    private static void idleTimeout() {
        updateTvExplanation("Idle timeout");

        disableAllCues();
        try {
            activity.findViewById(R.id.background_main).setBackgroundColor(preferencesManager.timeoutbackground);
        } catch (NullPointerException e) {
            Log.d(TAG, "Couldn't find background");
        }

        trialEnded(preferencesManager.ec_trial_timeout, 0);
    }

    private static void PrepareForNewTrial(int delay) {
        UtilsSystem.setBrightness(true, mContext);

        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.findViewById(R.id.background_main).setBackgroundColor(preferencesManager.taskbackground);
                updateTvExplanation("Waiting for trial to be started");
                // Auto start next trial if skipping go cue
                if (preferencesManager.skip_go_cue) {
                    startTrial(-1);
                } else {
                    UtilsTask.toggleCues(cues_Go, true);
                }
            }
        }, delay);
    }


    private void cancelHandlers() {
        Log.d(TAG, "Cancel all handlers (timer etc)");
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
        h2.removeCallbacksAndMessages(null);
        h3.removeCallbacksAndMessages(null);
        timerRunning = false;

    }

}
