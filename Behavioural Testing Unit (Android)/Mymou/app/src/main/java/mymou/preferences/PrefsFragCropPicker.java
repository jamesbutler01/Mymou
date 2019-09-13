package mymou.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.os.Bundle;

import android.widget.SeekBar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import android.widget.RelativeLayout.LayoutParams;
import mymou.MainMenu;
import mymou.R;
import mymou.Utils.UtilsSystem;
import org.w3c.dom.Text;

import java.io.File;

import static android.app.Activity.RESULT_OK;

public class PrefsFragCropPicker extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static String TAG = "MymouPrefsFragCropPicker";

    private int max_width_crop, crop_width, camera_width;
    private int max_height_crop, crop_height, camera_height;
    private int scale;
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
        camera_width = settings.getInt("camera_width", 320);
        camera_height = settings.getInt("camera_height", 240);
        scale = UtilsSystem.getCropScale(getActivity(), camera_width, camera_height);

        // Scale views
        camera_width *= scale;
        camera_height *= scale;
        for (int i = 0; i < crop_vals.length; i++) {
            crop_vals[i] = settings.getInt(crop_keys[i], 0) * scale;
        }

        default_position = UtilsSystem.getCropDefaultXandY(getActivity(), camera_width);

        max_height_crop = camera_height;
        max_width_crop = camera_width;

        seekbars[0] = view.findViewById(R.id.crop_top);
        seekbars[1] = view.findViewById(R.id.crop_bottom);
        seekbars[2] = view.findViewById(R.id.crop_left);
        seekbars[3] = view.findViewById(R.id.crop_right);

        for (int i = 0; i < crop_vals.length; i++) {
            if (i < 2) {
                seekbars[i].setMax(max_height_crop);
            } else {
                seekbars[i].setMax(max_width_crop);
            }
            seekbars[i].setProgress(crop_vals[i]);
        }

        // Add seekbarlistener last as otherwise configuring them will trigger the listener
        for (int i = 0; i < crop_vals.length; i++) {
            seekbars[i].setOnSeekBarChangeListener(this);
        }

        // Add onClickListener to exit button
        view.findViewById(R.id.butt_exitcroppicker).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Exit button pressed");
                getActivity().finish();
            }
        });

        // Add onClickListener to select image button
        view.findViewById(R.id.butt_selectimage).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Disabled as not functioning at the moment
        view.findViewById(R.id.butt_selectimage).setEnabled(false);
        view.findViewById(R.id.butt_selectimage).setVisibility(View.INVISIBLE);

        updateImage();

    }

    private static int SELECT_PICTURE = 111;

    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    // For returning selected image
    // TODO: Get onActivityResult working
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult2");
            if (requestCode == SELECT_PICTURE) {
                Log.d(TAG, "onActivityResult3");
                // Get the url from data
                Uri selectedImageUri = data.getData();

                if (null != selectedImageUri) {
                    // Get the image from the Uri
                    File imgFile = new File("/sdcard/Images/test_image.jpg");

                    if (imgFile.exists()) {

                        // Put image on layout
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        TextureView textureView = getView().findViewById(R.id.crop_picker_texture);
                        ImageView imageView = new ImageView(getContext());
                        imageView.setImageBitmap(myBitmap);

                        imageView.setLayoutParams(new RelativeLayout.LayoutParams(camera_width, camera_height));
                        LayoutParams lp = (LayoutParams) imageView.getLayoutParams();
                        textureView.setLayoutParams(lp);
                        textureView.setY(default_position.y);
                        textureView.setX(default_position.x);

                        mTextureView.bringToFront();
                    }
                }
            }
        }
    }


    // Make sure that left crop + right crop is less than total width, and same for the other dimension
    private boolean checkValidAdjustment() {
        boolean valid = true;
        if (seekbars[i_top].getProgress() + seekbars[i_bottom].getProgress() > camera_height - (camera_height * 0.05)) {
            valid = false;
        }
        if (seekbars[i_left].getProgress() + seekbars[i_right].getProgress() > camera_width - (camera_width * 0.05)) {
            valid = false;
        }
        return valid;
    }

    // Update red overlay on screen
    private void updateImage() {
        crop_height = camera_height - (seekbars[i_top].getProgress() + seekbars[i_bottom].getProgress());
        crop_width = camera_width - (seekbars[i_left].getProgress() + seekbars[i_right].getProgress());
        mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(crop_width, crop_height));
        LayoutParams lp = (LayoutParams) mTextureView.getLayoutParams();
        mTextureView.setLayoutParams(lp);
        mTextureView.setX(default_position.y + seekbars[i_left].getProgress());  // Shift to the right by left crop amount
        mTextureView.setY(default_position.x + seekbars[i_top].getProgress());
    }

    // Write settings to shared preferences
    private void saveSettings() {
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i < crop_vals.length; i++) {
            editor.putInt(crop_keys[i], seekbars[i].getProgress() / scale);
            crop_vals[i] = seekbars[i].getProgress();
        }
        editor.commit();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (checkValidAdjustment()) {
            updateImage();
            saveSettings();
        } else {
            // Reset previous values if they have adjusted seekbar too much
            for (int i = 0; i < crop_vals.length; i++) {
                seekbars[i].setProgress(crop_vals[i]);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}

