/**
 * Mazes to be used with TaskDiscreteMaze
 *
 * Allows you to load the parameters of whichever map you'd like to use on the task
 *
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
        Log.d(TAG, "Loading map "+map_chosen);

        LoadImageList();
        Load_x_dimension();
        Load_y_dimension();
        Load_torus();
        Load_numNeighbours();
        Load_maxDistance();
    }

    public void LoadImageList() {

        int[] imageListMapOne = {
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
                R.drawable.aabap,};

        int[] imageListMapTwo = {
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

        int[][] maps = {imageListMapOne, imageListMapTwo};

        imageList = maps[map_chosen];
    }

    public void Load_x_dimension() {
        int[] x_dimensions = {4, 1};
        x_size = x_dimensions[map_chosen];
    }

    public void Load_y_dimension() {
        int[] y_dimensions = {4, 10};
        y_size = y_dimensions[map_chosen];
    }

    public void Load_torus() {
        boolean[] toruses = {true, false};
        torus = toruses[map_chosen];
    }

    public void Load_numNeighbours() {
        int[] numNeighbourss = {4, 2};
        numNeighbours = numNeighbourss[map_chosen];
    }

    public void Load_maxDistance() {
        int[] maxDistances = {4, 10};
        maxDistance = maxDistances[map_chosen];
    }

}

