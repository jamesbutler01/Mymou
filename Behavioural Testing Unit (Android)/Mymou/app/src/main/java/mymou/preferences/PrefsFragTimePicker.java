package mymou.preferences;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import mymou.R;

public class PrefsFragTimePicker extends DialogFragment {

    private TimePicker timePicker;
    private String key;

    public PrefsFragTimePicker() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_timepicker, container, false);

    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        key = getArguments().getString("pref_tag");

        timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        // Set timepicker to currently selected time
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        int default_hour;
        if (key == "autostart") {
            default_hour = getResources().getInteger(R.integer.default_autostart_hour);
        } else {
            default_hour = getResources().getInteger(R.integer.default_autostop_hour);
        }
        int hour = settings.getInt(key+"_hour", default_hour);
        int min = settings.getInt(key+"_min", 0);
        timePicker.setHour(hour);
        timePicker.setMinute(min);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(key+"_hour", hourOfDay);
                editor.putInt(key+"_min", minute);
                editor.commit();
            }
        });

    }


}
