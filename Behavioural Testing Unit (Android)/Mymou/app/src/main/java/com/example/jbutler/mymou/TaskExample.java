package com.example.jbutler.mymou;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.app.Fragment;

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
    private static int currMonk;

    // Task objects
    private static Button[] cues_O = new Button[2];  // List of all trial objects for Subject O
    private static Button[] cues_V = new Button[2];  // List of all trial objects for Subject V

    private static int ec_correctTrial = 1;
    private static int ec_incorrectTrial = 0;

    // Predetermined locations where cues can appear on screen, calculated by calculateCueLocations()
    private static Point[] locs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_example, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        currMonk = getArguments().getInt("currMonk");

        assignObjects();

        setOnClickListeners();

        locs = new Utils().getPossibleCueLocs(getActivity());

        randomiseCueLocations();

        toggleTaskCues(currMonk, true);

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
        toggleTaskCues(-1, false);  // monkId not needed when switching off

         // Reset timer for idle timeout on each press
         ((TaskManager) getActivity()).resetTimer();

        // Now decide what to do based on what button pressed
        boolean successfulTrial = false;
        switch (view.getId()) {
            case R.id.buttonCue1MonkO:
                successfulTrial = true;
            case R.id.buttonCue2MonkV:
                successfulTrial = true;
            endOfTrial(successfulTrial);
        }
    }

    private void endOfTrial(boolean successfulTrial) {
        int outcome;
        if (successfulTrial) {
            outcome = ec_correctTrial;
        } else {
            outcome = ec_incorrectTrial;
        }
        // Send outcome up to parent
        ((TaskManager) getActivity()).trialEnded(outcome);
    }

    private static void toggleTaskCues(int monkId, boolean status) {
        // Switches on a particular monkeys cues, or switches off all cues
        if (status) {
            if (monkId == monkO) {
                Utils.toggleCues(cues_O, status);
                Utils.toggleCues(cues_V, !status);
            } else {
                Utils.toggleCues(cues_V, status);
                Utils.toggleCues(cues_O, !status);
            }
        } else {
            // If switching off, just always switch off all cues
            Utils.toggleCues(cues_O, status);
            Utils.toggleCues(cues_V, status);
        }
    }

    private static void randomiseCueLocations() {
        // Place all trial objects in random locations
        Utils.randomlyPositionCues(cues_O, locs);
        Utils.randomlyPositionCues(cues_V, locs);
    }


}
