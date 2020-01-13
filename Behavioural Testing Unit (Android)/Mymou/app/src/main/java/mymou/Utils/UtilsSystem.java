package mymou.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import mymou.R;
import mymou.preferences.PreferencesManager;

import java.util.StringTokenizer;

public class UtilsSystem {
    // Debug
    public static String TAG = "MymouUtilsSystem";


    public static void setBrightness(boolean bool, Context context, PreferencesManager preferencesManager) {
        if (Settings.System.canWrite(context)) {
            int brightness;
            if (bool || !preferencesManager.dimscreen) {
                brightness = 255;
            } else {
                brightness = preferencesManager.dimscreenlevel;
            }
            ContentResolver cResolver = context.getContentResolver();
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
    }


    public static void setOnClickListenerLoop(Button[] buttons, View.OnClickListener view) {
        for (Button button : buttons) {
            button.setOnClickListener(view);
        }
    }

    public static String convertIntArrayToString(int[] list) {
//        String out = Arrays.toString(list);
        StringBuilder str = new StringBuilder();
        for (int s : list) {
            str.append(s).append(",");
        }
        String out = str.toString();
        return out;
    }

    public static int[] loadIntArray(String tag, SharedPreferences prefs, Context context) {
        Log.d(TAG, tag + "  " + getDefaultArr(tag, context));
        String savedString = prefs.getString(tag, getDefaultArr(tag, context));
        Log.d(TAG, "Loaded " + savedString + "from " + tag);
        StringTokenizer st = new StringTokenizer(savedString, ",");
        int n = st.countTokens();
        int[] savedList = new int[n];
        for (int i = 0; i < n; i++) {
            savedList[i] = Integer.parseInt(st.nextToken());
        }
        return savedList;
    }

    // Get default colour values for ColourPicker (as specified in Strings.xml)
    public static String getDefaultArr(String tag, Context context) {
        if (tag == context.getResources().getString(R.string.preftag_gocuecolors)) {
            return context.getResources().getString(R.string.default_gocue_colours);
        } else if (tag == context.getResources().getString(R.string.preftag_od_num_corr_cues)) {
            return context.getResources().getString(R.string.default_objdis_corr_colours);
        } else if (tag == context.getResources().getString(R.string.preftag_od_num_incorr_cues)) {
            return context.getResources().getString(R.string.default_objdis_incorr_colours);
        } else if (tag == "two_prev_cols_incorr" | tag == "two_prev_cols_corr") {
            return "doesn't matter what this default string is as this will only be called if there is a stored value";
        }
        new Exception("Invalid tag specified");
        return "";
    }

    // Calculate the position to put view in the centre of the screen
    public static Point getCropDefaultXandY(Activity activity, int camera_width) {
        int default_y = 200;
        // Get size of screen for centring views
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screen_width = size.x;
        int default_x = ((screen_width - camera_width) / 2);
        Point out = new Point(default_x, default_y);
        return out;
    }

    // Calculate how big to scale the camera view so that it fits neatly in the screen
    public static int getCropScale(Activity activity, int camera_width, int camera_height) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screen_width = size.x;
        int margin = 100;
        int x_scale = (screen_width - margin * 2) / camera_width;

        int screen_height = size.y / 2;
        int y_scale = screen_height / camera_height;

        int lowestscale = x_scale < y_scale ? x_scale : y_scale;

        return lowestscale;
    }

    public static void addGraph(GraphView graph, LineGraphSeries<DataPoint> series, String xlab, String ylab, int num_sessions) {
        graph.addSeries(series);

        GridLabelRenderer glr = graph.getGridLabelRenderer();
        glr.setTextSize(30);
        glr.setVerticalAxisTitle(ylab);
        glr.setHorizontalAxisTitle(xlab);
        glr.setHorizontalAxisTitleTextSize(35);
        glr.setVerticalAxisTitleTextSize(35);
        glr.setPadding(30);
        series.setColor(Color.WHITE);
        series.setThickness(5);
        series.setDataPointsRadius(10);
        series.setDrawDataPoints(true);

        // X limits
        double edge_buffer = 0.1;
        graph.getViewport().setMaxX(num_sessions - 1 + edge_buffer);
        graph.getViewport().setMinX(-edge_buffer);
        graph.getViewport().setXAxisBoundsManual(true);
        glr.setNumHorizontalLabels(num_sessions);

    }

    public static boolean[] getBooleanFalseArray(int n) {
        boolean[] out = new boolean[n];
        for (int i = 0; i < n; i++) {
            out[i] = false;
        }
        return out;
    }


}




