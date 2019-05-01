package com.example.jbutler.mymou;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.*;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class TaskManager extends Activity implements Thread.UncaughtExceptionHandler, View.OnClickListener {
    // Debug
    public static String TAG = "TaskManager";
    private static TextView textView;

    private static int taskId;  // Unique string prefixed to all log entries
    public static String TAG_FRAGMENT = "taskfrag";

    public static int faceRecogPrediction = -1;
    private static int monkeyButtonPressed = -1;
    private static boolean faceRecogRunning = false;
    private static int trialCounter = 0;
    public static RewardSystem rewardSystem;

    private static FaceRecog faceRecog;
    private static ArrayList<String> trialData;
    public static String photoTimestamp;
    private static Handler logHandler;
    private static HandlerThread logThread;
    private static FragmentManager fragmentManager;
    private static FragmentTransaction fragmentTransaction;
    private static Context mContext;
    private static Activity activity;

        // Unique numbers assigned to each subject, used for facial recognition
    private static int monkO = 0, monkV = 1;

      // Aync handlers used to posting delayed task events
    private static Handler h0 = new Handler();  // Task timer
    private static Handler h1 = new Handler();  // Prepare for new trial
    private static Handler h2 = new Handler();  // Timeout go cues

       // Predetermined locations where cues can appear on screen, calculated by calculateCueLocations()
    private static int maxCueLocations = 8;  // Number of possible locations that cues can appear in
    private static int[] xLocs = new int[maxCueLocations];
    private static int[] yLocs = new int[maxCueLocations];

      // Used to cover/disable task when required (e.g. no bluetooth connection)
    public static View hideApplicationView;

    // Background colours
    private static View backgroundRed, backgroundPink;

    // Timeouts for wrong choices by subject
    private static int timeoutWrongGoCuePressed = 300;  // Timeout for now pressing their own Go cue
    private static int timeoutErrorTrial = 1000;  // Timeout for getting the task wrong

    // Timer to reset task if subject stops halfway through a trial
    private static int maxTrialDuration = 10000;  // Milliseconds until task timeouts and resets
    private static int time = 0;  // Time from last press - used for idle timeout if it reaches maxTrialDuration
    private static boolean timerRunning;  // Signals if timer currently active

      // Event codes for data logging
    private static int ec_correctTrial = 1;
    private static int ec_incorrectTrial = 0;
    private static int ec_wrongGoCuePressed = 2;
    private static int ec_idletimeout = 3;

    // Task objects
    private static Button cueGo_O, cueGo_V; // Go cues to start a trial
    private static Button[] cues_Reward = new Button[4];  // Reward cues for the different reward options

     // Reward
    static int rewardAmount = 1000;  // Duration (ms) that rewardSystem activated for

    // Boolean to signal if task should be active or not (e.g. overnight it is set to true)
    public static boolean shutdown = false;

    // Random number generator
    private static Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        activity = this;
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        setContentView(R.layout.activity_all_tasks);

        assignObjects();
        setOnClickListeners();
        calculateCueLocations();
        randomiseCueLocations();
        disableAllCues();

        initaliseRewardSystem();
        loadCamera();

        loadtask();

        initialiseScreenSettings();


        if (MainMenu.useFaceRecognition) {
            // Load facerecog off the main thread as takes a while
            Thread t = new Thread(new Runnable() {
                public void run() {
                    faceRecog = new FaceRecog();
                }
            });
            t.start();

        }

        registerPowerReceivers();

        initialiseLogHandler();

        //only lock if we aren't in testing mode
        if (!MainMenu.testingMode) {
            this.startLockTask();
        }

//            // Enable this if you want task to automatically restart on crash
//            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//                @Override
//                public void uncaughtException(Thread thread, Throwable throwable) {
//                    logHandler.post(new CrashReport(throwable));
//                    quitBt();
//                    restartApp();
//                }
//            });


        setBrightness(true);



        PrepareForNewTrial(0);

    }

    private void loadtask() {
        Intent intent = getIntent();
        taskId = intent.getIntExtra("tasktoload", -1);
        if (taskId == -1) {
            new Exception("Invalid task specified");
        }

    }

    private void initialiseLogHandler() {
        logThread = new HandlerThread("LogBackground");
        logThread.start();
        logHandler = new Handler(logThread.getLooper());
    }

    private void initaliseRewardSystem() {
        boolean successfullyEstablished = false;
        rewardSystem.quitBt();
        rewardSystem = new RewardSystem(this);
        if (rewardSystem.bluetoothConnection | !MainMenu.useBluetooth) {
            successfullyEstablished = enableApp(true);
        }

        // Repeat if either couldn't connect or couldn't enable app
        if (!successfullyEstablished){
            Handler handlerOne = new Handler();
            handlerOne.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initaliseRewardSystem();
                }
            }, 5000);
        }
    }

    public static void startTrial(int monkId) {
        logEvent("Trial started for monkey "+monkId);

        if(!timerRunning) {
            timer();
        }

        Bundle bundle = new Bundle();
        bundle.putInt("currMonk", monkId);
        if (taskId == 0) {
            TaskExample fragment = new TaskExample();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.container, fragment, TAG_FRAGMENT);
        } else if (taskId == 1) {
             TaskFromPaper fragment = new TaskFromPaper();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.container, fragment, TAG_FRAGMENT);
        }

        fragmentTransaction.commit();
        fragmentTransaction = fragmentManager.beginTransaction();


    }

    private void loadCamera() {
        if (!MainMenu.useCamera) {
            return;
        }

        Log.d(TAG, "Loading camera fragment");
        fragmentTransaction = fragmentManager.beginTransaction();
        CameraMain cM = new CameraMain();
        fragmentTransaction.add(R.id.container, cM);
        fragmentTransaction.commit();
        fragmentTransaction = fragmentManager.beginTransaction();

    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        logHandler.post(new CrashReport(throwable));
        rewardSystem.quitBt();
        restartApp();
    }

    private void restartApp() {
        Intent intent=new Intent(getApplicationContext(), TaskManager.class);
        intent.putExtra("restart",true);
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);
        System.exit(2);
    }


    public static void deliverReward(int juiceChoice, int rewardAmount) {
        rewardSystem.activateChannel(juiceChoice, rewardAmount);
    }

    private void registerPowerReceivers() {
        IntentFilter unplugIntent = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        IntentFilter plugIntent = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        registerReceiver(powerPlugReceiver, plugIntent);
        registerReceiver(powerUnplugReceiver, unplugIntent);
    }

    private final BroadcastReceiver powerPlugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
                // Do something when power connected
                // enableApp(True);
            }
        }
    };

    private final BroadcastReceiver powerUnplugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                // Do something when power disconnected
                // enableApp(false);
            }
        }
    };


    public static void setFaceRecogPrediction(int[] intArray) {

        if (faceRecog != null) {

            faceRecogPrediction = faceRecog.idImage(intArray);

            if (faceRecogPrediction == monkeyButtonPressed) {

                // If monkey clicked it's designated button
                Log.d(TAG, "Monkey pressed correct cue");
                resultMonkeyPressedTheirCue(true);

            } else {

                // If monkey clicked wrong button
                Log.d(TAG, "Monkey pressed wrong cue");
                resultMonkeyPressedTheirCue(false);

            }

        } else {

            Log.d(TAG, "Error: FaceRecog not instantiated");

        }

        // Release facerecog as now ready to process another image
        faceRecogRunning = false;
    }

    public static void commitTrialData(int overallTrialOutcome) {
        if (dateHasChanged()) {
            trialCounter = 0;
        } else {
            trialCounter++;
        }
        if (trialData != null) {
            int length = trialData.size();
            for (int i = 0; i < length; i++) {
                String s = trialData.get(i);
                // Prefix variables that were constant throughout trial (trial result, which monkey, etc)
                s = taskId +"," + trialCounter +"," + faceRecogPrediction + "," + overallTrialOutcome + "," + s;
                logHandler.post(new LogEvent(s));
                Log.d(TAG, "commitTrialData: " + s);
            }
        }
    }

    private void initialiseScreenSettings() {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final View decorView = TaskManager.this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public static void shutdownLoop() {
        Log.d(TAG, "shutdownLoop() called");
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int AMPM = c.get(Calendar.AM_PM);
        if (hour >= 7 && AMPM == Calendar.PM) {
            enableApp(false);
            boolean restartNextDay = true;
            if(restartNextDay) {

                // Only restart on certain days of the week
                int day = c.get(Calendar.DAY_OF_WEEK);
                if (day == Calendar.THURSDAY | day == Calendar.FRIDAY | day == Calendar.SATURDAY) {
                    startupLoop();
                }

            }
        } else {
            Handler handlerOne = new Handler();
            handlerOne.postDelayed(new Runnable() {
                @Override
                public void run() {
                    shutdownLoop();
                }
            }, 60000);
        }
    }

    private static void startupLoop() {
        Log.d(TAG, "startupLoop() called");
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int AMPM = c.get(Calendar.AM_PM);
        if (hour >= 7 && AMPM == Calendar.AM) {
                enableApp(true);
                shutdownLoop();
        } else {
            Handler handlerOne = new Handler();
            handlerOne.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startupLoop();
                }
            }, 60000);
        }
    }

    public static boolean enableApp(boolean bool) {
        Log.d(TAG, "Enabling app"+bool);
        setBrightness(false);
        if (hideApplicationView != null) {
            if (bool) {
                hideApplicationView.setEnabled(false);
                hideApplicationView.setVisibility(View.INVISIBLE);
            } else {
                hideApplicationView.setEnabled(true);
                hideApplicationView.setVisibility(View.VISIBLE);
            }
            return true;
        } else {
            Log.d(TAG, "hideApplication object not instantiated");
            return false;
        }
    }

    public static void setBrightness(boolean bool) {
        if (Settings.System.canWrite(mContext)) {
            int brightness;
            if (bool) {
                brightness = 255;
            } else {
                brightness = 0;
            }
            ContentResolver cResolver = mContext.getContentResolver();
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
    }

    public static void logEvent(String data) {
        // Show (human) user on screen what is happening during the task
        textView.setText(data);

        // Store data for logging at end of trial
        String timestamp = MainMenu.folderManager.getTimestamp();
        String msg = TaskManager.photoTimestamp + "," + timestamp + "," + data;
        trialData.add(msg);
    }

    public static void resetTrialData() {
        trialData = new ArrayList<String>();
    }

    // Takes selfie and checks to see if it matches with which monkey it should be
    public static boolean checkMonkey(int monkId) {
        if (MainMenu.useFaceRecognition && faceRecogRunning) {
            // Previous face recog still running
            return false;
        }

        boolean photoTaken = takePhoto();

        if (photoTaken && MainMenu.useFaceRecognition) {
            // Photo taken, now we wait for faceRecog to return prediction
            faceRecogRunning = true;
            monkeyButtonPressed = monkId;
        }

        return photoTaken;

    }

    public static boolean takePhoto() {
        if (MainMenu.useCamera) {
            Log.d(TAG, "takePhoto() called");
            photoTimestamp = MainMenu.folderManager.getTimestamp();
            boolean photoTaken = CameraMain.captureStillPicture(photoTimestamp);
            return photoTaken;
        } else {
            return true;
        }
    }

    //Checks if todays date is the same as the last time function was called
    public static boolean dateHasChanged() {
        String todaysDate = MainMenu.folderManager.getBaseDate();
        SharedPreferences sharedPref = activity.getPreferences(mContext.MODE_PRIVATE);
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
        super.onDestroy();
        Log.d(TAG,"onDestroy() called");
        cancelHandlers();
        rewardSystem.quitBt();
        unregisterReceivers();
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

    private void unregisterReceivers() {
        try {
            unregisterReceiver(powerPlugReceiver);
            unregisterReceiver(powerUnplugReceiver);
        } catch(IllegalArgumentException e) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }



    private void assignObjects() {
        backgroundRed = findViewById(R.id.backgroundred);
        backgroundPink = findViewById(R.id.backgroundpink);
        hideApplicationView = findViewById(R.id.foregroundblack);
        cueGo_O = findViewById(R.id.buttonGoMonkO);
        cueGo_V = findViewById(R.id.buttonGoMonkV);
        cues_Reward[0]  = findViewById(R.id.buttonRewardZero);
        cues_Reward[1]  = findViewById(R.id.buttonRewardOne);
        cues_Reward[2]  = findViewById(R.id.buttonRewardTwo);
        cues_Reward[3]  = findViewById(R.id.buttonRewardThree);
        textView = findViewById(R.id.tvLog);
    }


    // Make a predetermined list of the locations on the screen where cues can be placed
    private void calculateCueLocations() {
        int imageWidths = 175 + 175/2;
        int distanceFromCenter = imageWidths + 30; // Buffer between different task objects

        // Find centre of screen in pixels
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int xCenter = screenWidth / 2;
        xCenter -= imageWidths / 2;
        int screenHeight = size.y;
        int yCenter = screenHeight / 2;;

        // Y locations
        yLocs[0] = yCenter - distanceFromCenter;
        yLocs[1] = yCenter;
        yLocs[2] = yCenter + distanceFromCenter;
        yLocs[3] = yCenter;
        yLocs[4] = yCenter + distanceFromCenter;
        yLocs[5] = yCenter - distanceFromCenter;
        yLocs[6] = yCenter + distanceFromCenter;
        yLocs[7] = yCenter - distanceFromCenter;

        // X locations
        xLocs[0] = xCenter;
        xLocs[1] = xCenter - distanceFromCenter;
        xLocs[2] = xCenter;
        xLocs[3] = xCenter + distanceFromCenter;
        xLocs[4] = xCenter - distanceFromCenter;
        xLocs[5] = xCenter - distanceFromCenter;
        xLocs[6] = xCenter + distanceFromCenter;
        xLocs[7] = xCenter + distanceFromCenter;

        // Go cues are static location so place them now
        cueGo_O.setX(xLocs[1]);
        cueGo_O.setY(yLocs[1]);
        cueGo_V.setX(xLocs[3]);
        cueGo_V.setY(yLocs[3]);
    }


    private void setOnClickListenerLoop(Button[] buttons) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setOnClickListener(this);
        }
    }


     private void setOnClickListeners() {
         setOnClickListenerLoop(cues_Reward);
         cueGo_O.setOnClickListener(this);
         cueGo_V.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        // Always disable all cues after a press as monkeys love to bash repeatedly
        disableAllCues();

        // Reset task timer (used for idle timeout and calculating reaction times if desired)
        resetTimer();

        // Make screen bright
        TaskManager.setBrightness(true);

        // Now decide what to do based on what button pressed
        switch (view.getId()) {
            case R.id.buttonGoMonkO:
                checkMonkeyPressedTheirCue(monkO);
                break;
            case R.id.buttonGoMonkV:
                checkMonkeyPressedTheirCue(monkV);
                break;
            case R.id.buttonRewardZero:
                deliverReward(0);
                break;
            case R.id.buttonRewardOne:
                deliverReward(1);
                break;
            case R.id.buttonRewardTwo:
                deliverReward(2);
                break;
            case R.id.buttonRewardThree:
                deliverReward(3);
                break;
        }
    }

     // Each monkey has it's own start cue. At start of each trial make sure the monkey pressed it's own cue using
    // the facial recognition
    private static void checkMonkeyPressedTheirCue(int monkId) {

        // Take selfie
        boolean photoTaken = TaskManager.checkMonkey(monkId);

        if (MainMenu.useFaceRecognition) {

            // Here's how to code task if using facial recognition
            if (photoTaken) {

                // If photo successfully taken then do nothing as wait for faceRecog to return prediction
                // TaskManager.setFaceRecogPrediction will ultimately call TaskExample.resultMonkeyPressedTheirCue
                logEvent("FaceRecog started..");

            } else {

                // Photo not taken as camera/faceRecog wasn't ready so reset go cues to let them press again
                toggleGoCues(true);

            }

        } else {

            // Here's how to code task if not using facial recognition

            if (photoTaken) {
                // If photo successfully taken then start the trial
                startTrial(monkId);
            } else {
                // Photo not taken as camera wasn't ready so reset go cues to let them press again
                toggleGoCues(true);
            }

        }

    }

    public static void resultMonkeyPressedTheirCue(boolean correctCuePressed) {

        // Have to put this on UI thread as it's called from faceRecog which is off main thread
        activity.runOnUiThread(new Runnable() {
            public void run()
            {
                if (correctCuePressed) {
                   startTrial(TaskManager.faceRecogPrediction);
                } else {
                    MonkeyPressedWrongGoCue();
                }
            }
        });
    }

    // Wrong Go cue selected so give short timeout
    public static void MonkeyPressedWrongGoCue() {
        logEvent("Monkey pressed wrong go cue..");
        TaskManager.commitTrialData(ec_wrongGoCuePressed);
        // Switch on red background
        toggleBackground(backgroundRed, true);

        // Switch off red background after certain delay
        h2.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleGoCues(true);
                toggleBackground(backgroundRed, false);
            }
        }, timeoutWrongGoCuePressed);
    }

    public void trialEnded(int result) {
        // Kill task
        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag(TAG_FRAGMENT)).commit();
        fragmentTransaction = fragmentManager.beginTransaction();

        if (result == ec_correctTrial) {
            correctOptionChosen();
        } else {
            incorrectOptionChosen(result);
        }
    }

    private void incorrectOptionChosen(int result) {
        logEvent("Feedback: Error trial");
        toggleBackground(backgroundRed, true);
        endOfTrial(result, timeoutErrorTrial);
    }

    private void correctOptionChosen() {
        logEvent("Correct trial: Reward choice");
        toggleBackground(backgroundPink, true);
        toggleButtonList(cues_Reward, true);
    }

    private void deliverReward(int juiceChoice) {
        logEvent("Delivering "+rewardAmount+"ms reward on channel "+juiceChoice);
        TaskManager.deliverReward(juiceChoice, rewardAmount);
        endOfTrial(ec_correctTrial, rewardAmount + 500);
    }


    private static void endOfTrial(int outcome, int newTrialDelay) {
        TaskManager.commitTrialData(outcome);

        PrepareForNewTrial(newTrialDelay);
    }


    private static void disableAllCues() {
        toggleGoCues(false);
        toggleButtonList(cues_Reward, false);
    }


    // Lots of toggles for task objects
    private static void toggleGoCues(boolean status) {
        toggleButton(cueGo_O, status);
        toggleButton(cueGo_V, status);
    }


    private static void toggleButtonList(Button[] buttons, boolean status) {
        for (int i = 0; i < buttons.length; i++) {
            toggleButton(buttons[i], status);
        }
    }

    private static void toggleButton(Button button, boolean status) {
        if (status) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.INVISIBLE);
        }
        button.setEnabled(status);
        button.setClickable(status);
    }

    private static void toggleBackground(View view, boolean status) {
        if (status) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
        view.setEnabled(status);
    }


    // Recursive function to track task time
    public void resetTimer() {
        time = 0;
    }

    private static void timer() {
        time += 1000;

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (time > maxTrialDuration) {
                    time = 0;
                    timerRunning = false;

                    idleTimeout();

                } else {
                    timer();
                    timerRunning = true;
                }
            }
        }, 1000);
    }

    private static void idleTimeout() {
        logEvent("Error stage: Idle timeout");
        disableAllCues();
        toggleBackground(backgroundRed, true);
        endOfTrial(ec_idletimeout, timeoutErrorTrial);  // TODO: Switch this to trialEnded (which is static)
        //trialEnded(ec_idletimeout);
    }

    private static void PrepareForNewTrial(int delay) {
    TaskManager.resetTrialData();

    h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                randomiseCueLocations();
                toggleBackground(backgroundRed, false);
                toggleBackground(backgroundPink, false);
                toggleGoCues(true);
                textView.setText("Initiation Stage");
            }
        }, delay);
    }


    private void cancelHandlers() {
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
        h2.removeCallbacksAndMessages(null);
    }

    private static void randomiseCueLocations() {
    // Place all trial objects in random locations
        randomiseNoReplacement(cues_Reward);
    }

        // Utility functions
    private static void randomiseNoReplacement(Button[] buttons) {
        int[] chosen = new int[maxCueLocations];
        for (int i = 0; i < maxCueLocations; i++) {
            chosen[i] = 0;
        }
        int choice = r.nextInt(maxCueLocations);
        for (int i = 0; i < buttons.length; i++) {
            while (chosen[choice] == 1) {
                choice = r.nextInt(maxCueLocations);
            }
            buttons[i].setX(xLocs[choice]);
            buttons[i].setY(yLocs[choice]);
            chosen[choice] = 1;
        }
    }


}
