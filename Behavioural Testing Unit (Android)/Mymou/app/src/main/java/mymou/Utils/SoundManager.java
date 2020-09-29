package mymou.Utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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

    private void playCustomTone() {
        double duration = (double) preferencesManager.tone_dur;                // seconds
        double freqOfTone = (double) preferencesManager.tone_freq;           // hz
        int sampleRate = 8000;              // a number
        Log.d(TAG, ""+preferencesManager.tone_dur+","+preferencesManager.tone_freq);

        double dnumSamples = duration * sampleRate;
        dnumSamples = Math.ceil(dnumSamples);
        int numSamples = (int) dnumSamples;
        double sample[] = new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];

        for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
            sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        int i = 0;
        int ramp = numSamples / 20;                                    // Amplitude ramp as a percent of sample count
        for (i = 0; i < ramp; ++i) {                                     // Ramp amplitude up (to avoid clicks)
            double dVal = sample[i];
            // Ramp up to maximum
            final short val = (short) ((dVal * 32767 * i / ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (i = i; i < numSamples - ramp; ++i) {                        // Max amplitude for most of the samples
            double dVal = sample[i];
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (i = i; i < numSamples; ++i) {                               // Ramp amplitude down
            double dVal = sample[i];
            // Ramp down to zero
            final short val = (short) ((dVal * 32767 * (numSamples - i) / ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        AudioTrack audioTrack = null;                                   // Get audio track
        try {
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();                                          // Play the track
            audioTrack.write(generatedSnd, 0, generatedSnd.length);     // Load the track
        } catch (Exception e) {
        }
        if (audioTrack != null) audioTrack.release();           // Track play done. Release track.
    }
}

