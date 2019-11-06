package mymou.preferences;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import mymou.R;

public class PrefsFragReward extends PreferenceFragmentCompat {

    private String TAG = "MymouPrefsFragReward";

    public PrefsFragReward() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_reward, rootKey);
        Log.d(TAG, "onCreatePreferences");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "oncreateview");

        // Number input dialog for reward duration preference
        SeekBarPreferenceCustom rewardDuration = (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_rewardduration));
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

        SeekBarPreferenceCustom sb_rewardchan= (SeekBarPreferenceCustom) findPreference(getString(R.string.preftag_num_rew_chans));
        sb_rewardchan.setMin(1);

        super.onViewCreated(view, savedInstanceState);
    }

}
