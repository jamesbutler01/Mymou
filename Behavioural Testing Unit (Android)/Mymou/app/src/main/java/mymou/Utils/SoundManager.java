package mymou.Utils;

import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import mymou.R;
import mymou.preferences.PreferencesManager;

/**
 * SoundManager used to create beeps for secondary reinforcement upon reward delivery
 * Only runs if preferenceManager.sound = true
 */


public class SoundManager {

    private String TAG = "MymouSoundManager";
    private ToneGenerator toneGenerator;
    private PreferencesManager preferencesManager;
    private final Handler stopThread = new Handler();
    private Resources r;
    private int tone_type=-1;

    public SoundManager(PreferencesManager preferencesManagerInit) {
        preferencesManager = preferencesManagerInit;
        if (preferencesManager.sound) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        }

        // Predefine this so we're not querying preferences each time we play the tone
        r = preferencesManager.mContext.getResources();
        if (preferencesManager.tone_type.equals(r.getString(R.string.preftag_custom_tone))) {
            tone_type = 0;
        } else if (preferencesManager.tone_type.equals(r.getString(R.string.preftag_load_tone))) {
            tone_type = 1;
        } else {
            tone_type = 2;
        }
        Log.d(TAG, "Loaded tone_type "+ preferencesManager.tone_type);
        Log.d(TAG, "Sound boolean = "+ preferencesManager.sound);
    }

    public void playTone() {
        if (preferencesManager.sound) {
            if (tone_type==0) {
                playCustomTone();
            } else if (tone_type==1) {
                playSavedTone();
            } else {
                playSystemTone();
            }
        }
    }

    private void playSavedTone() {
        Log.d(TAG, "Playing saved tone: "+preferencesManager.tone_filename);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String fileName = preferencesManager.tone_filename;

                    // Check for file extension and add if missing
                    String ext = fileName.substring(fileName.length()-4, fileName.length());
                    if (!ext.equals(".wav")) {
                        fileName += ".wav";
                    }

                    File yourWavFile = new File(Environment.getExternalStorageDirectory() + "/Mymou", fileName);
                    FileInputStream fis = new FileInputStream(yourWavFile);
                    int minBufferSize = AudioTrack.getMinBufferSize(16_000,
                            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 16_000,
                            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                            minBufferSize, AudioTrack.MODE_STREAM);

                    int i = 0;
                    byte[] music = null;
                    try {
                        music = new byte[512];
                        at.play();

                        while ((i = fis.read(music)) != -1)
                            at.write(music, 0, i);

                    } catch (IOException e) {
                        e.printStackTrace();
                        preferencesManager.activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(preferencesManager.mContext, "Error! Failed to load WAV file.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    at.stop();
                    at.release();
                } catch (IOException ex) {
                    preferencesManager.activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(preferencesManager.mContext, "Error! Failed to load WAV file.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ex.printStackTrace();
                }
            }
        }).start();
    }


    private void playSystemTone() {
        Log.d(TAG, "Playing system tone: "+preferencesManager.sound_to_play);

        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            }
            toneGenerator.startTone(preferencesManager.sound_to_play, 200);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGenerator != null) {
                        toneGenerator.release();
                        toneGenerator = null;
                    }
                }

            }, 200);
        } catch (Exception e) {
            Log.d(TAG, "Exception while playing sound:" + e);
        }
    }
    private PlayCustomTone playToneThread;
    private boolean isThreadRunning = false;

    private void playCustomTone() {
        Log.d(TAG, "Playing custom tone: "+preferencesManager.tone_freq+" Hz, "+preferencesManager.tone_dur+" s");
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
            }, preferencesManager.tone_dur);
        }

    }



}

