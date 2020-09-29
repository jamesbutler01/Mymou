package mymou.Utils;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import mymou.preferences.PreferencesManager;

/**
 * SoundManager used to create beeps for secondary reinforcement upon reward delivery
 * Only runs if preferenceManager.sound = true
 */


public class SoundManager {

    private String TAG = "MymouToneGenerator";
    private ToneGenerator toneGenerator;
    private static int tone_duration = 200;
    private PreferencesManager preferencesManager;
    private final Handler stopThread = new Handler();


    public SoundManager(PreferencesManager preferencesManagerInit) {
        preferencesManager = preferencesManagerInit;
        if (preferencesManager.sound) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        }
    }

    public void playTone() {
        if (preferencesManager.sound) {
            if (preferencesManager.custom_tone) {
                playCustomTone();
            } else {
                playSystemTone();
            }
        }
    }

    private void playSystemTone() {
        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            }
            toneGenerator.startTone(preferencesManager.sound_to_play, tone_duration);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGenerator != null) {
                        toneGenerator.release();
                        toneGenerator = null;
                    }
                }

            }, tone_duration);
        } catch (Exception e) {
            Log.d(TAG, "Exception while playing sound:" + e);
        }
    }
    private PlayCustomTone playToneThread;
    private boolean isThreadRunning = false;

    private void playCustomTone() {
        if (!isThreadRunning) {

            playToneThread = new PlayCustomTone(preferencesManager.tone_freq, preferencesManager.tone_dur);
            playToneThread.start();
            isThreadRunning = true;

            stopThread.postDelayed(new Runnable() {
                @Override public void run() {

                    // Have to stop tone early to avoid clicking sound at end
                    if (playToneThread != null) {
                        playToneThread.stopTone();
                        playToneThread.interrupt();
                        playToneThread = null;
                        isThreadRunning = false;
                    }
                }
            }, preferencesManager.tone_dur * 1000);
        }

    }



}

