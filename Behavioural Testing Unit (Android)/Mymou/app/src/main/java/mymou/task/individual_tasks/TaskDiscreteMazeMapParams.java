/**
 * Mazes to be used with TaskDiscreteMaze
 * <p>
 * Allows you to load the parameters of whichever map you'd like to use on the task
 */
package mymou.task.individual_tasks;

import android.util.Log;

import mymou.R;
import mymou.preferences.PreferencesManager;

public class TaskDiscreteMazeMapParams {

    private int map_chosen;
    private String TAG = "TaskDiscreteMazeMapParams";

    public int[] imageList;
    public int x_size;
    public int y_size;
    public boolean torus;
    public int numNeighbours;
    public int maxDistance;

    public TaskDiscreteMazeMapParams(PreferencesManager preferencesManager) {
        Log.d(TAG, "Loading map ");

        map_chosen = preferencesManager.dm_map_selected;
        Log.d(TAG, "Loading map " + map_chosen);

        LoadImageList();
        Load_x_dimension();
        Load_y_dimension();
        Load_torus();
        Load_numNeighbours();
        Load_maxDistance();
    }

    public void LoadImageList() {

        int[] imageListMapOne = {
                R.drawable.aaaaa,
                R.drawable.aaaab,
                R.drawable.aaaac,
                R.drawable.aaaad,
                R.drawable.aaaae,
                R.drawable.aaaaf,
                R.drawable.aaaag,
                R.drawable.aaaah,
                R.drawable.aaaai,
                R.drawable.aaaaj,
        };

        int[] imageListMapTwo = {
                R.drawable.aabaa,
                R.drawable.aabab,
                R.drawable.aabac,
                R.drawable.aabad,
                R.drawable.aabae,
                R.drawable.aabaf,
                R.drawable.aabag,
                R.drawable.aabah,
                R.drawable.aabai,
                R.drawable.aabaj,
                R.drawable.aabak,
                R.drawable.aabal,
                R.drawable.aabam,
                R.drawable.aaban,
                R.drawable.aabao,
                R.drawable.aabap,
        };

        int[] imageListMapThree = {
                R.drawable.aadaa,
                R.drawable.aadab,
                R.drawable.aadac,
                R.drawable.aadad,
                R.drawable.aadae,
                R.drawable.aadaf,
                R.drawable.aadag,
                R.drawable.aadah,
                R.drawable.aadai,
                R.drawable.aadaj,
                R.drawable.aadak,
                R.drawable.aadal,
                R.drawable.aadam,
                R.drawable.aadan,
                R.drawable.aadao,
                R.drawable.aadap,
                R.drawable.aadaq,
                R.drawable.aadar,
                R.drawable.aadas,
                R.drawable.aadat,
                R.drawable.aadau,
                R.drawable.aadav,
                R.drawable.aadaw,
                R.drawable.aadax,
                R.drawable.aaday,};

        int[] imageListMapFour = {
                R.drawable.aaeaa,
                R.drawable.aaeab,
                R.drawable.aaeac,
                R.drawable.aaead,
                R.drawable.aaeae,
                R.drawable.aaeaf,
                R.drawable.aaeag,
                R.drawable.aaeah,
                R.drawable.aaeai,
                R.drawable.aaeaj,
                R.drawable.aaeak,
                R.drawable.aaeal,
                R.drawable.aaeam,
                R.drawable.aaean,
                R.drawable.aaeao,
                R.drawable.aaeap,
                R.drawable.aaeaq,
                R.drawable.aaear,
                R.drawable.aaeas,
                R.drawable.aaeat,
                R.drawable.aaeau,
                R.drawable.aaeav,
                R.drawable.aaeaw,
                R.drawable.aaeax,
                R.drawable.aaeay,};


        int[][] maps = {imageListMapOne, imageListMapTwo, imageListMapThree, imageListMapFour};

        imageList = maps[map_chosen];
    }

    public void Load_x_dimension() {
        int[] x_dimensions = {1, 4, 5, 5};
        x_size = x_dimensions[map_chosen];
    }

    public void Load_y_dimension() {
        int[] y_dimensions = {10, 4, 5, 5};
        y_size = y_dimensions[map_chosen];
    }

    public void Load_torus() {
        boolean[] toruses = {false, true, true, true};
        torus = toruses[map_chosen];
    }

    public void Load_numNeighbours() {
        int[] numNeighbourss = {2, 4, 4, 4};
        numNeighbours = numNeighbourss[map_chosen];
    }

    public void Load_maxDistance() {
        int[] maxDistances = {10, 4, 4, 4};
        maxDistance = maxDistances[map_chosen];
    }

}

