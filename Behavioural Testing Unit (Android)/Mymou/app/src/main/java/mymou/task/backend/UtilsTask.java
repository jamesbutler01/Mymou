package mymou.task.backend;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import androidx.constraintlayout.widget.ConstraintLayout;
import mymou.preferences.PreferencesManager;

import java.util.Random;
import java.util.stream.IntStream;

public class UtilsTask {
   // Debug
    public static String TAG = "MymouUtils";

    // Make a list of the possible locations on the screen where cues can be placed
    public static Point[] getPossibleCueLocs(Activity activity) {
        PreferencesManager preferencesManager = new PreferencesManager(activity);
        int imageWidths = preferencesManager.cue_size;
        int border = preferencesManager.cue_spacing;  // Spacing between different task objects
        int totalImageSize = imageWidths + border;

        // Find centre of screen in pixels
        Display display =  activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        // Find possible locs along each dimension
        int[] xlocs = calculateLocs(screenWidth, totalImageSize);
        int[] ylocs = calculateLocs(screenHeight, totalImageSize);

        // Populate 1D output array with all possible locations
        Point[] locs = new Point[xlocs.length * ylocs.length];
        int i_loc = 0;
        for (int i_x = 0; i_x < xlocs.length; i_x++) {
            for (int i_y = 0; i_y < ylocs.length; i_y++) {
                locs[i_loc] = new Point(xlocs[i_x], ylocs[i_y]);
                i_loc += 1;
            }
        }

        return locs;

    }

    public static Button addCue(int id, int color, Context context, View.OnClickListener onClickListener, ConstraintLayout layout) {
//    public static Button addCue(int id, int color, Context context, ConstraintLayout layout) {
//        View.OnClickListener onClickListener = (View.OnClickListener) context;
        PreferencesManager preferencesManager = new PreferencesManager(context);
        Button button = new Button(context);
        button.setWidth(preferencesManager.cue_size);
        button.setHeight(preferencesManager.cue_size);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(preferencesManager.border_size, preferencesManager.border_colour);
        drawable.setColor(color);
        button.setBackgroundDrawable(drawable);
        button.setId(id);
        button.setOnClickListener(onClickListener);
        layout.addView(button);
        return button;
    }

    // Switches on a particular monkeys cues, and switches off other monkey's cues
    public static void toggleMonkeyCues(int monkId, Button[][] all_cues) {
        for (int i_monk = 0; i_monk < all_cues.length; i_monk++) {
            toggleCues(all_cues[i_monk], false);
        }
        toggleCues(all_cues[monkId], true);
    }

    // Iterates through a list of cues enabling/disabling all in list
    public static void toggleCues(Button[] buttons, boolean status) {
        for (int i = 0; i < buttons.length; i++) {
            UtilsTask.toggleCue(buttons[i], status);
        }
    }

    // Fully enable/disable individual cue
    public static void toggleCue(Button button, boolean status) {
        toggleView(button, status);
        button.setClickable(status);
    }


    public static void toggleView(View view, boolean status) {
        if (status) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
        view.setEnabled(status);
    }


    public static void randomlyPositionCues(View[] cues, Point[] locs) {
        // Make zero array tracking which locations have already been used
        int maxCueLocations = locs.length;
        int[] chosen = new int[maxCueLocations];

        Random r = new Random();

        // Loop through and place each cue
        int choice = r.nextInt(maxCueLocations);
        for (int i = 0; i < cues.length; i++) {
            while (chosen[choice] == 1) {
                choice = r.nextInt(maxCueLocations);
            }
            cues[i].setX(locs[choice].x);
            cues[i].setY(locs[choice].y);
            chosen[choice] = 1;
        }
    }

    private static int[] calculateLocs(int screenLength, int totalImageSize) {
        int num_locs = screenLength / totalImageSize; // floor division

        int[] locs = new int[num_locs];

        int offset = screenLength - (num_locs * totalImageSize);  // Centre images on screen
        offset = offset / 2;

        for (int i = 0; i < num_locs; i++) {
            locs[i] = offset + i * totalImageSize;
        }

        return locs;

    }

}
