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


    public SoundManager(PreferencesManager preferencesManagerInit) {
        preferencesManager = preferencesManagerInit;
        if (preferencesManager.sound) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        }
    }

    public void playTone() {
        if (preferencesManager.sound) {
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
    }

}
