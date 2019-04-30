package com.example.jbutler.mymou;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.app.Fragment;

import java.util.Random;

// A basic object discrimination task showcasing the main features of the Mymou system:
// Uses facial recognition to deliver separate tasks to two different subjects
// Offers choice of rewards for successful trial completion

public class TaskExample extends Fragment
        implements View.OnClickListener {

    // Debug
    private static TextView textView;
    public static String TAG = "TaskExample";

         // Unique numbers assigned to each subject, used for facial recognition
    private static int monkO = 0, monkV = 1;

    // Task objects
    private static Button[] cues_O = new Button[2];  // List of all trial objects for Subject O
    private static Button[] cues_V = new Button[2];  // List of all trial objects for Subject V

    private static int ec_correctTrial = 1;
    private static int ec_incorrectTrial = 0;

    // Predetermined locations where cues can appear on screen, calculated by calculateCueLocations()
    private static int maxCueLocations = 8;  // Number of possible locations that cues can appear in
    private static int[] xLocs = new int[maxCueLocations];
    private static int[] yLocs = new int[maxCueLocations];

    // Random number generator
    private static Random r = new Random();

    // Static activity reference to refer to it in static contexts
    private static Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_example, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        activity = this.getActivity();

        assignObjects();

        setOnClickListeners();

        calculateCueLocations();

        randomiseCueLocations();

        disableAllCues();

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
    }


    private void assignObjects() {
        cues_O[0] = getView().findViewById(R.id.buttonCue1MonkO);
        cues_O[1] = getView().findViewById(R.id.buttonCue2MonkO);
        cues_V[0] = getView().findViewById(R.id.buttonCue1MonkV);
        cues_V[1] = getView().findViewById(R.id.buttonCue2MonkV);
        textView = getView().findViewById(R.id.tvLog);
    }



    private void setOnClickListenerLoop(Button[] buttons) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setOnClickListener(this);
        }
    }

     private void setOnClickListeners() {
         setOnClickListenerLoop(cues_O);
         setOnClickListenerLoop(cues_V);
    }


    @Override
    public void onClick(View view) {

        // Always disable all cues after a press as monkeys love to bash repeatedly
        disableAllCues();

        // Now decide what to do based on what button pressed
        switch (view.getId()) {
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
        }
    }


    private void incorrectOptionChosen() {
        endOfTrial(ec_incorrectTrial);
    }

    private void correctOptionChosen() {
        endOfTrial(ec_correctTrial);
    }

    private static void endOfTrial(int outcome) {
        // End trial and send outcome up to parent
    }

    private static void disableAllCues() {
        toggleTaskCues(-1, false);  // monkId not needed when switching off
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
        randomiseNoReplacement(cues_O);
        randomiseNoReplacement(cues_V);
    }


}
