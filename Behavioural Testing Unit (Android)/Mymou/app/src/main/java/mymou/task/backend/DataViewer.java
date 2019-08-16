package mymou.task.backend;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class DataViewer extends Activity  {

    private String TAG = "DataViewer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_viewer);

        findViewById(R.id.butt_dv_select_task).setOnClickListener(buttonClickListener);
        findViewById(R.id.butt_dv_select_sess).setOnClickListener(buttonClickListener);

       LoadDataAndCreateGraphs();

        MymouDatabase db = Room.databaseBuilder(getApplicationContext(),
                MymouDatabase.class, "MymouDatabase").build();

        // Insert new entry into database
        User user = new User();
        user.firstName = "james";
        user.lastName = "butler";
        user.uid = 2;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Insert Data
                db.userDao().insertAll(user);

                        // Read entry from database
        List<User> users = db.userDao().getAll();

        int n = users.size();
        for (User u: users) {
            Log.d(TAG, u.lastName);
        }

            }
        });

    }

    private void LoadDataAndCreateGraphs() {
        int[] y_values = new int[] {1,55,3,2,60};
        int num_sessions = 5;
        DataPoint[] dataPoints = new DataPoint[num_sessions];
        for (int i=0; i<num_sessions; i++) {
            dataPoints[i] =  new DataPoint(i, y_values[i]);
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);

        // Add participation graph
        UtilsSystem.addGraph(findViewById(R.id.graph_part), series, "Session", "Number trials", num_sessions);

        // Add performance graph
        GraphView graph = (GraphView) findViewById(R.id.graph_perf);
        UtilsSystem.addGraph(graph, series, "Session", "Percent correct", num_sessions);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setYAxisBoundsManual(true);

        // Add reward graph
        GraphView graph2 = (GraphView) findViewById(R.id.graph_rew);
        UtilsSystem.addGraph(graph2, series, "Session", "Reward given", num_sessions);

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
