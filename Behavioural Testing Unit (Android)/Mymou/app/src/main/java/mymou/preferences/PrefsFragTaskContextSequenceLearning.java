package mymou.preferences;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import mymou.R;
import mymou.Utils.PlayCustomTone;

public class PrefsFragTaskContextSequenceLearning extends PreferenceFragmentCompat  {

    private String TAG="MymouPrefsFragTaskContextSequenceLearning";

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_task_contextsequencelearning, rootKey);

        // Set ontouchlisteners for the seekbars to allow users to manually input values
        SeekBarPreferenceCustom[] seekBarPreferences = new SeekBarPreferenceCustom[8];
        seekBarPreferences[0] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_csl_tone_freqA));
        seekBarPreferences[1] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_csl_tone_freqB));
        seekBarPreferences[2] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_csl_tone_freqC));
        seekBarPreferences[3] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_csl_tone_freqD));
        seekBarPreferences[4] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_csl_tone_durA));
        seekBarPreferences[5] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_csl_tone_durB));
        seekBarPreferences[6] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_csl_tone_durC));
        seekBarPreferences[7] = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_csl_tone_durD));
        for (int i = 0; i < seekBarPreferences.length; i++) {
            final int i_final = i;
            Log.d("asd", "setting seekbar"+i_final);
            seekBarPreferences[i].setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Number dialog
                    Log.d("asd", "setting seekbar"+i_final);
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Input number");
                    final EditText input = new EditText(getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setRawInputType(Configuration.KEYBOARD_12KEY);
                    alert.setView(input);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {

                                int value = Integer.valueOf(input.getText().toString());
                                if (value < seekBarPreferences[i_final].getMax()) {
                                    seekBarPreferences[i_final].setValue(value);
                                } else {
                                    Toast.makeText(getContext(), "Value too high", Toast.LENGTH_LONG).show();
                                }

                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                    alert.show();
                    return false;
                }
            });

        }

        // Play tone buttons
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Resources r = getContext().getResources();

        Preference button = findPreference("playtonea");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int freq = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_freqA), r.getInteger(R.integer.default_csl_tone_freqA));
                int dur = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_durA), r.getInteger(R.integer.default_csl_tone_durA));
                playCustomTone(dur, freq);
                return true;
            }
        });
        Preference button2 = findPreference("playtoneb");
        button2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int freq = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_freqB), r.getInteger(R.integer.default_csl_tone_freqB));
                int dur = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_durB), r.getInteger(R.integer.default_csl_tone_durB));
                playCustomTone(dur, freq);
                return true;
            }
        });
        Preference button3 = findPreference("playtonec");
        button3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int freq = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_freqC), r.getInteger(R.integer.default_csl_tone_freqC));
                int dur = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_durC), r.getInteger(R.integer.default_csl_tone_durC));
                playCustomTone(dur, freq);
                return true;
            }
        });
        Preference button4 = findPreference("playtoned");
        button4.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int freq = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_freqD), r.getInteger(R.integer.default_csl_tone_freqD));
                int dur = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_durD), r.getInteger(R.integer.default_csl_tone_durD));
                playCustomTone(dur, freq);
                return true;
            }
        });

    }

    private PlayCustomTone playToneThread;
    private boolean isThreadRunning = false;
    private final Handler stopThread = new Handler();

    private void playCustomTone(int length, int freq) {
        Log.d(TAG, "Playing custom tone: " + freq + " Hz, " + length + " s");
        if (!isThreadRunning) {

            playToneThread = new PlayCustomTone(freq, length);
            playToneThread.start();
            isThreadRunning = true;

            stopThread.postDelayed(new Runnable() {
                @Override
                public void run() {

                    // Have to stop tone early to avoid clicking sound at end
                    if (playToneThread != null) {
                        playToneThread.stopTone();
                        playToneThread.interrupt();
                        playToneThread = null;
                        isThreadRunning = false;
                    }
                }
            }, length);
        }
    }



}
