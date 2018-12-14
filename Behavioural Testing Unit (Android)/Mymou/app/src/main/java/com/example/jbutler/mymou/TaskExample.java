package com.example.jbutler.mymou;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

// A basic example task showcasing the main features of the Mymou system:
// Uses facial recognition to deliver seperate tasks to two different subjects
// Offers choice of rewards for successful trial completion

public class TaskExample extends Fragment
        implements View.OnClickListener {

    // Debug
    private static TextView textView;

    // Used to cover/disable task when required (e.g. no bluetooth connection)
    public View hideApplication;

    // Background colours
    private static View backgroundRed, backgroundPink;

    // Timeouts for wrong choices by subject
    private static int timeoutWrongGoCuePressed = 300;  // Timeout for now pressing their own Go cue
    private int timeoutWrongCueChosen = 1500;  // Timeout for getting the task wrong

    // Timer to reset task if subject stops halfway through a trial
    private static int maxTrialDuration = 10000;  // Milliseconds until task timeouts and resets
    private static int time = 0;  // Time from last press - used for idle timeout if it reaches maxTrialDuration
    private static boolean timerRunning;  // Signals if timer currently active

    // Unique numbers assigned to each subject, used for facial recognition
    private static int monkO = 0, monkV = 1;

    // Task objects
    private static Button cueGo_O, cueGo_V; // Go cues to start a trial
    private static Button[] cues_Reward = new Button[4];  // Reward cues for the different reward options
    private static Button[] cues_O = new Button[2];  // List of all trial objects for Subject O
    private static Button[] cues_V = new Button[2];  // List of all trial objects for Subject V

    // Reward
    static int rewardAmount = 1000;  // Duration (ms) that rewardSystem activated for

    // Predetermined locations where cues can appear on screen, calculated by calculateCueLocations()
    private static int maxCueLocations = 8;  // Number of possible locations that cues can appear in
    private static int[] xLocs = new int[maxCueLocations];
    private static int[] yLocs = new int[maxCueLocations];

    // Random number generator
    private static Random r = new Random();

    // Boolean to signal if task should be active or not (e.g. overnight it is set to true)
    public static boolean shutdown = false;

    // Aync handlers used to posting delayed task events
    private static Handler h0 = new Handler();  // Task timer
    private static Handler h1 = new Handler();  // Prepare for new trial
    private static Handler h2 = new Handler();  // Timeout go cues

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_example, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        assignObjects();

        setOnClickListeners();

        calculateCueLocations();

        TaskManager.setBrightness(255);

        disableAllCues();

        PrepareForNewTrial(0);

    }

    private void assignObjects() {
        backgroundRed = getView().findViewById(R.id.backgroundred);
        backgroundPink = getView().findViewById(R.id.backgroundpink);
        hideApplication = getView().findViewById(R.id.foregroundblack);
        cueGo_O = getView().findViewById(R.id.buttonGoMonkO);
        cueGo_V = getView().findViewById(R.id.buttonGoMonkV);
        cues_O[0] = getView().findViewById(R.id.buttonCue1MonkO);
        cues_O[1] = getView().findViewById(R.id.buttonCue2MonkO);
        cues_V[0] = getView().findViewById(R.id.buttonCue1MonkV);
        cues_V[1] = getView().findViewById(R.id.buttonCue2MonkV);
        cues_Reward[0]  = getView().findViewById(R.id.buttonRewardZero);
        cues_Reward[1]  = getView().findViewById(R.id.buttonRewardOne);
        cues_Reward[2]  = getView().findViewById(R.id.buttonRewardTwo);
        cues_Reward[3]  = getView().findViewById(R.id.buttonRewardThree);
        textView = getView().findViewById(R.id.tvLog);
    }

    // Make a predetermined list of the locations on the screen where cues can be placed
    private void calculateCueLocations() {
        int imageWidths = 175 + 175/2;
        int distanceFromCenter = imageWidths + 30; // Buffer between different task objects

        // Find centre of screen in pixels
        Display display = getActivity().getWindowManager().getDefaultDisplay();
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
         setOnClickListenerLoop(cues_O);
         setOnClickListenerLoop(cues_V);
         cueGo_O.setOnClickListener(this);
         cueGo_V.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        // Always disable all cues after a press as monkeys love to bash repeatedly
        disableAllCues();

        // Reset task timer (used for idle timeout and calculating reaction times if desired)
        time = 0;

        // Make screen bright
        TaskManager.setBrightness(255);

        // Now decide what to do based on what button pressed
        switch (view.getId()) {
            case R.id.buttonGoMonkO:
                checkMonkeyPressedTheirCue(monkO);
                break;
            case R.id.buttonGoMonkV:
                checkMonkeyPressedTheirCue(monkV);
                break;
            case R.id.buttonCue1MonkO:
                correctOptionChosen();
                break;
            case R.id.buttonCue1MonkV:
                incorrectOptionChosen();
                break;
            case R.id.buttonCue2MonkO:
                incorrectOptionChosen();
                break;
            case R.id.buttonCue2MonkV:
                correctOptionChosen();
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

    // Each monkey has it's own start cue. At start of each trial make sure the monkey pressed it's own cue using
    // the facial recognition
    private static void checkMonkeyPressedTheirCue(int monkId) {
        boolean correctCuePressed = TaskManager.checkMonkey(monkId);
        if (correctCuePressed) {  // If they clicked their specific cue
            startTrial(monkId);
        } else {
            TimeoutGoCues();
        }
    }

    // Wrong Go cue selected so give short timeout
    private static void TimeoutGoCues() {
        toggleBackground(backgroundRed, true);
        h2.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleGoCues(true);
                toggleBackground(backgroundRed, false);
            }
        }, timeoutWrongGoCuePressed);
    }

    private static void startTrial(int monkId) {
        logEvent("Trial started for monkey "+monkId);

        if(!timerRunning) {
            timer();
        }

        toggleTaskCues(monkId, true);
    }

    private void incorrectOptionChosen() {
        logEvent("Error stage: Incorrect cue chosen");
        toggleBackground(backgroundRed, true);
        endOfTrial(0, timeoutWrongCueChosen);
    }

    private void correctOptionChosen() {
        logEvent("Reward stage: Correct cue chosen");
        toggleBackground(backgroundPink, true);
        toggleButtonList(cues_Reward, true);
    }

    private void deliverReward(int juiceChoice) {
        logEvent("Delivering "+rewardAmount+"ms reward on channel "+juiceChoice);
        TaskManager.deliverReward(juiceChoice, rewardAmount);
        endOfTrial(1, rewardAmount + 500);
    }

    private static void endOfTrial(int outcome, int newTrialDelay) {
        TaskManager.commitTrialData(outcome);

        PrepareForNewTrial(newTrialDelay);
    }

    // This is just needed to show user on screen what is happening during the task
    // Normally just use TaskManager.logEvent()
    private static void logEvent(String log) {
        TaskManager.logEvent(log);
        textView.setText(log);
    }

    private static void disableAllCues() {
        toggleGoCues(false);
        toggleTaskCues(-1, false);  // monkId not needed when switching off
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

    private static void toggleTaskCues(int monkId, boolean status) {
        if (status) {
            if (monkId == monkO) {
                toggleButtonList(cues_O, status);
            } else {
                toggleButtonList(cues_V, status);
            }
        } else {
            // If switching off, just always switch off all cues
            toggleButtonList(cues_O, status);
            toggleButtonList(cues_V, status);
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

    private static void randomiseCueLocations() {
        // Place all trial objects in random locations
        randomiseNoReplacement(cues_Reward);
        randomiseNoReplacement(cues_O);
        randomiseNoReplacement(cues_V);
    }

    // Recursive function to track task time
    private static void timer() {
        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                time += 1000;
                if (time > maxTrialDuration) {
                    disableAllCues();
                    endOfTrial(7, 0);

                    //Decrease brightness while not in use
                    TaskManager.setBrightness(50);

                    time = 0;
                    timerRunning = false;
                } else {
                    timer();
                    timerRunning = true;
                }
            }
        }, 1000);
    }

    private void cancelHandlers() {
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
        h2.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelHandlers();
    }



}
