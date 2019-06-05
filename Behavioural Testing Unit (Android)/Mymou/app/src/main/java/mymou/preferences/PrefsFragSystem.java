package mymou.preferences;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import mymou.R;

public class PrefsFragSystem extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String TAG = "MymouPrefsFragSystem";

    public PrefsFragSystem() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_system, rootKey);
        Log.d(TAG, "onCreatePreferences");

        checkConditionalPrefs();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "oncreateview");

        SeekBarPreference rewardDuration = (SeekBarPreference) findPreference(getString(R.string.preftag_rewardduration));
        rewardDuration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Number dialog
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
                            if (value < rewardDuration.getMax()) {
                                rewardDuration.setValue(value);
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

        SeekBarPreference sb_rewardchan= (SeekBarPreference) findPreference(getString(R.string.preftag_num_rew_chans));
        sb_rewardchan.setMin(1);

        // Set onchange listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    private void checkConditionalPrefs() {
        // Get sharedpreferences
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        // Only show crop photos setting if crop photos enabled
        findPreference("croppicker_prefsfrag").setVisible(sharedPrefs.getBoolean("crop_photos", false));
        findPreference("soundpicker_prefsfrag").setVisible(sharedPrefs.getBoolean("sound", false));
    }

    @Override
    public void onDestroyView() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("asdf", "onActivityResult_prefsfragsystem");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        checkConditionalPrefs();
    }
}
