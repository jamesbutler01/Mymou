package mymou.task.backend;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.room.Room;

import mymou.R;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import mymou.Utils.UtilsSystem;
import mymou.database.MymouDatabase;
import mymou.database.User;
import mymou.database.UserDao;
import mymou.preferences.PrefsActSystem;

import java.util.List;

/**
 * Data viewer
 *
 * Loads previous performance on tasks and displays in graph format
 * TODO: In development, not currently functioning
 *
 */
public class DataViewer extends Activity {

    private String TAG = "DataViewer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_viewer);

        findViewById(R.id.butt_dv_select_task).setOnClickListener(buttonClickListener);
        findViewById(R.id.butt_dv_select_sess).setOnClickListener(buttonClickListener);

        // Cues disabled
        UtilsTask.toggleCue((Button) findViewById(R.id.butt_dv_select_task), false);
        UtilsTask.toggleCue((Button) findViewById(R.id.butt_dv_select_sess), false);

        LoadDataAndCreateGraphs();

        MymouDatabase db = Room.databaseBuilder(getApplicationContext(),
                MymouDatabase.class, "MymouDatabase").build();

        // Insert new entry into database
        User user = new User();
        user.firstName = "james";
        user.lastName = "butler";
        user.uid = 2;

//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                // Insert Data
//                db.userDao().insertAll(user);
//
//                // Read entry from database
//                List<User> users = db.userDao().getAll();
//
//                int n = users.size();
//                for (User u : users) {
//                    Log.d(TAG, u.lastName);
//                }
//
//            }
//        });

    }

    private void LoadDataAndCreateGraphs() {
        int[] y_valuesPart = new int[]{300, 550, 400, 200, 600};
        int[] y_valuesPerf = new int[]{50, 54, 61, 72, 83};
        int[] y_valuesRew = new int[]{100, 150, 180, 150, 400};
        int num_sessions = 5;
        DataPoint[] dataPointsPart = new DataPoint[num_sessions];
        DataPoint[] dataPointsPerf = new DataPoint[num_sessions];
        DataPoint[] dataPointsRew = new DataPoint[num_sessions];
        for (int i = 0; i < num_sessions; i++) {
            dataPointsPart[i] = new DataPoint(i, y_valuesPart[i]);
            dataPointsPerf[i] = new DataPoint(i, y_valuesPerf[i]);
            dataPointsRew[i] = new DataPoint(i, y_valuesRew[i]);
        }


        // Add participation graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPointsPart);
        GraphView graphPart = (GraphView) findViewById(R.id.graph_part);
        UtilsSystem.addGraph(graphPart, series, "Session", "Number trials", num_sessions);
        graphPart.getViewport().setMinY(0);
        graphPart.getViewport().setYAxisBoundsManual(true);

        // Add performance graph
        LineGraphSeries<DataPoint> seriesPerf = new LineGraphSeries<>(dataPointsPerf);
        GraphView graphPerf = (GraphView) findViewById(R.id.graph_perf);
        UtilsSystem.addGraph(graphPerf, seriesPerf, "Session", "Percent correct", num_sessions);
        graphPerf.getViewport().setMinY(0);
        graphPerf.getViewport().setMaxY(100);
        graphPerf.getViewport().setYAxisBoundsManual(true);

        // Add reward graph
        LineGraphSeries<DataPoint> seriesRew = new LineGraphSeries<>(dataPointsRew);
        GraphView graphRew = (GraphView) findViewById(R.id.graph_rew);
        UtilsSystem.addGraph(graphRew, seriesRew, "Session", "Reward given", num_sessions);
        graphRew.getViewport().setMinY(0);
        graphRew.getViewport().setYAxisBoundsManual(true);

    }


    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.butt_dv_select_sess:
                    //
                    break;
                case R.id.butt_dv_select_task:
                    //
                    break;

            }
        }
    };

}
