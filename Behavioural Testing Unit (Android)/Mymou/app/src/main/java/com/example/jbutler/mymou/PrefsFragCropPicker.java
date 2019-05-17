package com.example.jbutler.mymou;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.os.Bundle;
import android.app.Fragment;

import android.widget.SeekBar;
import androidx.preference.PreferenceManager;
import android.widget.RelativeLayout.LayoutParams;

public class PrefsFragCropPicker extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private int max_width_crop, width, camera_width;
    private int max_height_crop, height, camera_height;
    private Point default_position;
    private int i_top = 0, i_bottom = 1, i_left = 2, i_right = 3;
    private int[] crop_vals = new int[4];
    private String[] crop_keys = {"crop_top", "crop_bottom", "crop_left", "crop_right"};
    private SeekBar[] seekbars = new SeekBar[4];
    private SharedPreferences settings;
    View mTextureView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_prefs_frag_crop_picker, container, false);
    }


    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mTextureView = (View) view.findViewById(R.id.crop_picker_texture);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(10, Color.RED);
        drawable.setColor(Color.TRANSPARENT);
        mTextureView.setBackgroundDrawable(drawable);

        // Load settings
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        int scale = 5;
        camera_width = settings.getInt("camera_width", 176) * scale;
        camera_height = settings.getInt("camera_height", 144) * scale;
        for (int i = 0; i < crop_vals.length; i++) {
            crop_vals[i] = settings.getInt(crop_keys[i], 0) * scale;
        }

        default_position = UtilsSystem.getCropDefaultXandY(getActivity(), camera_width);

        max_height_crop = camera_height / 2;
        max_width_crop = camera_width / 2;
        Log.d("asdf", " max height:" + max_height_crop + " max width:" + max_width_crop);
        Log.d("asdf", " max height:" + max_height_crop + " max width:" + max_width_crop);

        seekbars[0] = view.findViewById(R.id.crop_top);
        seekbars[1] = view.findViewById(R.id.crop_bottom);
        seekbars[2] = view.findViewById(R.id.crop_left);
        seekbars[3] = view.findViewById(R.id.crop_right);

        for (int i = 0; i < crop_vals.length; i++) {
            seekbars[i].setOnSeekBarChangeListener(this);
            seekbars[i].setProgress(crop_vals[i]);
            if (i < 2) {
                seekbars[i].setMax(max_height_crop);
            } else {
                seekbars[i].setMax(max_width_crop);
            }
        }

        updateImage();

    }


    private void updateImage() {
        height = camera_height - (seekbars[i_top].getProgress() + seekbars[i_bottom].getProgress());
        width = camera_width - (seekbars[i_left].getProgress() + seekbars[i_right].getProgress());
        mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        LayoutParams lp = (LayoutParams) mTextureView.getLayoutParams();
        mTextureView.setLayoutParams(lp);
        mTextureView.setY(default_position.y + seekbars[i_top].getProgress());  // Shift to the right by left crop amount
        mTextureView.setX(default_position.x + seekbars[i_left].getProgress());  // Shift to the right by left crop amount
    }

    private void saveSettings() {
         SharedPreferences.Editor editor = settings.edit();
           for (int i = 0; i < crop_vals.length; i++) {
            editor.putInt(crop_keys[i], seekbars[i].getProgress());
            }
        editor.commit();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        updateImage();
         saveSettings();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}

