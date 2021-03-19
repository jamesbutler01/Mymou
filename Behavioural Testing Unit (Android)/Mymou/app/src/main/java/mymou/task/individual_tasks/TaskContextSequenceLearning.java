package mymou.task.individual_tasks;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;

import java.util.Random;

import mymou.R;
//import mymou.Utils.PlayCustomTone;
import mymou.Utils.SoundManager;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Context sequence learning
 */
public class TaskContextSequenceLearning extends Task {

    // Debug
    public static String TAG = "MymouTaskContextSequenceLearning";

    // Global task variables
    private static PreferencesManager prefManager;  // Load settings specified by experimenter
    private static Handler h0 = new Handler();  // Show object handler
    private static Handler h1 = new Handler();  // Hide object handler
    private long startTime, rtTime;

    private static Button cue1, cue2, choice_cue_i, choice_cue_a;

    private int sound_1, sound_2;
    private int[] currConfig;

    private int seqNr = 0;
    private int curr_context;

    private int maxVolume = 100;

    /**
     * Function called when task first loaded (before the UI is loaded)
     * Loads the UI components (cues, background etc)
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_context_sequence_learning, container, false);
    }

    /**
     *
     * Function called after the UI has been loaded
     * Once this is called you can then make any UI changes you want (moving cues around etc)
     *
     */

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        // currConfig holds the trial sequence that needs to be played out
        currConfig = returnTrialConfiguration();

        // Instantiate task objects with curr config
        assignObjects(currConfig);

        // assign the whole thing here programatically
        startMovie();

    }

    /**
     * Recursive function that manages the timing of the task
     * Flashes two bars onto the screen of different heights, then displays a blank screen, then
     * repeats until the desired number of bars have been displayed, at which points it asks subjects
     * to choose between the two options
     */

    private void startMovie() {

        // first define relevant bits for this trial

        if (seqNr == 0) {

            curr_context = currConfig[0];
            sound_1 = currConfig[2];
            sound_2 = currConfig[3];}

        else if (seqNr == 1){

            curr_context = currConfig[1];
            sound_1 = currConfig[4]; // 1 becomes 3
            sound_2 = currConfig[5];} // 2 becomes 4}

        else {

            endOfTrial(false, callback, prefManager);} // successful trial is false becausae we want the trial to end but subject not get any reward as they have already received it
            logEvent("This is the current configuration originally: " + currConfig[0] + currConfig[1], callback);
            logEvent("This is the current configuration: " + curr_context + sound_1 + sound_2, callback);

            // now we play out the relevant parts of the trial in sequence
            // first 'door' comes on
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {
                    turnOnCue();
                }
            }, 0);
    }

    private int[] returnTrialConfiguration()
    {
        // random number between 1 to 8 to correspond to all possible trial types
        int max = 2; // for now we constrain to only probe trials
        int min = 1;

        Random rand = new Random();
        int trialType = rand.nextInt((max - min) + 1) + min;

        // depending on random number, we generate a number of relevant things: col context 1, col context 2, and individual sounds
        // number convention is as follows. trialConfig = {context color 1, context color 2, sound 1, sound 2, sound 3, sound 4} - this removes any ambiguity
        // context color is either 1 (orange) or 2 (purple) - in line with the document
        // sound 1 2 3 4 correspond to (A, B, C, D).
        // so trialConfig = [1, 1, 1, 2, 3, 4]; corresponds orange background repeated throughout the trial with (A B), (C D) being played out
        // so trialConfig = [2, 1, 4, 1, 2, 3]; corresponds purple played for first chain and orange for second chain, with (D A), (B C) being played out

        // initialize code
        int[] trialConfig = new int[8];

        switch (trialType)
        // trialConfig returns trial configuration
        {
            case 1:
                trialConfig = new int[] {1, 1, 1, 2, 3, 4};
                break;
            case 2:
                trialConfig = new int[] {2, 2, 2, 1, 4, 3};
                break;
            // 1 and 2 are training / habituation trials
            case 3:
                trialConfig = new int[] {1, 2, 1, 2, 3, 4};
                break;
            case 4:
                trialConfig = new int[] {2, 1, 2, 1, 4, 3};
                break;
            case 5:
                trialConfig = new int[] {1, 1, 1, 2, 4, 3};
                break;
            case 6:
                trialConfig = new int[] {2, 2, 2, 1, 3, 4};
                break;
            case 7:
                trialConfig = new int[] {1, 2, 1, 2, 4, 3};
                break;
            case 8:
                trialConfig = new int[] {2, 1, 2, 1, 3, 4};
                break;
        }
        return trialConfig;
    }

    private void assignObjects(int[] trialConfig)
    {
        prefManager = new PreferencesManager(getContext());
        prefManager.ContextSequenceLearning();

        seqNr = 0;

        // generate the stuff

        choice_cue_i = UtilsTask.addColorCue(1, prefManager.csl_choice_col_i, getContext(), voidClickListener, getView().findViewById(R.id.parent_task_csl));
        choice_cue_a = UtilsTask.addColorCue(2, prefManager.csl_choice_col_a, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_csl));

        // Create cue
        cue1 = UtilsTask.addColorCue(3, prefManager.csl_col_context_1, getContext(), startClickListener, getView().findViewById(R.id.parent_task_csl));
        cue2 = UtilsTask.addColorCue(4, prefManager.csl_col_context_2, getContext(), startClickListener, getView().findViewById(R.id.parent_task_csl));

        // Figure out how big to make the cue
//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        Point screen_size = new Point();
//        display.getSize(screen_size);

        cue1.setX(600);
        cue1.setY(940);

        cue2.setX(600);
        cue2.setY(940);

        choice_cue_i.setX(600);
        choice_cue_i.setY(940);

        choice_cue_a.setX(600);
        choice_cue_a.setY(940);

        // turn off all cues
        UtilsTask.toggleCue(cue1, false);
        UtilsTask.toggleCue(cue2, false);
        UtilsTask.toggleCue(choice_cue_i, false);
        UtilsTask.toggleCue(choice_cue_a, false);

    }

    private void turnOnCue()
    { // turn on the cue here

        if (curr_context == 1) {
            logEvent("we are in case1 of cueon" + curr_context, callback);
            UtilsTask.toggleCue(cue1, true);
        }

        else if (curr_context == 2) {
            logEvent("we are in case2 of cueon" + curr_context, callback);
            UtilsTask.toggleCue(cue2, true);
        }
    }

    private void turnOffCue()
    {

        UtilsTask.toggleCue(cue1, false);
        UtilsTask.toggleCue(cue2, false);
    }

    private int[] soundMap(int soundNr)

    {
        int tone_dur = 1000;
        int tone_freq = 200;

        switch (soundNr)
        {
            case 1:
                tone_dur = 1000;
                tone_freq = prefManager.csl_tone_freqA; // these should probably be set as variables they can tweak
                break;
            case 2:
                tone_dur = 1000;
                tone_freq = prefManager.csl_tone_freqB;
                break;
            case 3:
                tone_dur = 1000;
                tone_freq = prefManager.csl_tone_freqC;
                break;
            case 4:
                tone_dur = 1000;
                tone_freq = prefManager.csl_tone_freqD;
                break;
        };

        // just return an array with 2 vec
        int[] soundConfig = {tone_dur, tone_freq};

        return soundConfig;

    }

    private void turnOnSound()
    {
        // sound1 corresponds to first sound in sequence / this can be any of the four sounds
        // sound2 corresponds to second sound in sequence

        // modify first sound
        int[] out = soundMap(sound_1);
//        prefManager.tone_freq = out[1];
        new SoundManager(prefManager).playTone();

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                // modify second sound
                int[] out = soundMap(sound_2);
//                prefManager.tone_freq = out[1];
                new SoundManager(prefManager).playTone();
            }
        }, prefManager.csl_tone_delay); // delay between sound 1 and 2

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                UtilsTask.toggleCue(choice_cue_i, false);

                logEvent("Choice phase enabled", callback);
                startTime = System.currentTimeMillis(); // this should be once the last tone is played
                UtilsTask.toggleCue(choice_cue_a, true);

            }
        }, prefManager.csl_tone_delay+100); // this delay denotes the differences when sound 2 stops playing and go cue turning green
    }


    /**
     * Called whenever a cue is pressed by a subject
     * So then loads the next appropriate stage of the task depending on what cue was selected
     */

    private View.OnClickListener voidClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // does nothing
        };
    };

    private View.OnClickListener startClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // removes cue and puts context color on
            turnOffCue();

            if (curr_context == 1) {
                getActivity().findViewById(R.id.background_main).setBackgroundColor(prefManager.csl_col_context_1);
            }
            else if (curr_context == 2) {
                getActivity().findViewById(R.id.background_main).setBackgroundColor(prefManager.csl_col_context_2);
            }

            // play sound 12
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UtilsTask.toggleCue(choice_cue_i, true);
                    turnOnSound();
                }
            }, prefManager.csl_onset_delay); // this is delay between context turning on and tones playing
        }
    };
    // this is our listened for the response that computes the reaction time and dictates strength of sound that comes out
    private View.OnClickListener responseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            rtTime = System.currentTimeMillis() - startTime;
            logEvent("This is the RT: " + rtTime + "ms", callback);

            // we need some rtTime that we subtract it from
            long rtBase = 450; // this is base reaction time; this should be a setting in prefmanager probably

            // turn off cue
            UtilsTask.toggleCue(choice_cue_a, false);

            // dictate sound strength
            double modifier;

            modifier = (rtTime - rtBase);

            double tmp = (double) modifier/ (double) rtBase;;

            double bound = 0.99;
            if (modifier > 0.99) {modifier = (double) bound;} // this is a temporary solution until they specify the exact equation they want for controlling the reward and sound strength
            else {modifier = tmp;};

            logEvent("This is modifier: " + modifier, callback);

            float rewardSize;

            rewardSize = 500 * (float) modifier; // reward size should be a setting in prefmanager as well
            int rewardSizeN = (int) rewardSize;

            // give reward
            logEvent("This is the reward size: " + rewardSizeN + "ml", callback);

            // this should have reward sound turned off; if the go cue for 2nd stage is pressed too quickly, 2nd two sounds dont appear yet because juice is still coming.
            // this will be fixed by proper timing

            prefManager.tone_strength = 0; // turn off for default sound from givereward from task and then turn back on for sound manager play tone
            callback.giveRewardFromTask_(rewardSizeN); // needs to be modified to reflect sound strength
            getActivity().findViewById(R.id.background_main).setBackgroundColor(prefManager.taskbackground);
            prefManager.tone_strength= maxVolume;

            // now sort out sound - we multiply everything by 100 such that no decimals get lost and we can convert between int / double etc.
            tmp = modifier*100;
            tmp = (int) tmp;
            int soundStrength = maxVolume*100 * (int) tmp; // multiplty max sound strength by tmp
            double soundTmp;
            soundTmp = (double) soundStrength / 100.0; // back to max 100

            // we now change the strength of the tone depending on the soundTmp
            // we will also want to change the sound of the correct sound here.
//            prefManager.tone_strength = (int) soundTmp;
//            prefManager.tone_freq = 50000;

            prefManager.tone_strength = maxVolume; // leave it like this for now
            new SoundManager(prefManager).playTone();

            // set back to max volume for sounds 1-4
//            prefManager.tone_strength = maxVolume;

            // play sound 12
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {

                    // startMovie for 2nd part
                    seqNr = seqNr + 1;

                    startMovie();

                    if (seqNr == 2) { // iti period
                        getActivity().findViewById(R.id.background_main).setBackgroundColor(prefManager.taskbackground);
                    };
                }
            }, (long) tmp + 1000); // this will be delay between reward delivery and next door coming on


        }
    };


    /**
     * onPause called whenever a task is paused, interrupted, or cancelled
     *
     * If task aborted for some reason (e.g. they did not respond quick enough), then cancel the handlers to stop the movie playing
     * This prevents task objects being loaded AFTER a trial has finished
     */
    @Override
    public void onPause() {
        super.onPause();
        super.onDestroy();
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
    }

    /**
     * This is static code repeated in each task that enables communication between the individual
     * task and the parent TaskManager.java which handles the backend utilities (reward delivery,
     * selfie processing, intertrial intervals, etc etc)
     */
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {this.callback = callback;}

}

