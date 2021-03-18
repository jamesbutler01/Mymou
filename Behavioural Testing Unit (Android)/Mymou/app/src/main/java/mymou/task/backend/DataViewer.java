package mymou.task.backend;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.room.Room;

import mymou.R;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import mymou.Utils.UtilsSystem;
import mymou.database.MymouDatabase;
import mymou.database.Session;
import mymou.database.User;
import mymou.database.UserDao;
import mymou.preferences.PrefsActSystem;

import java.util.List;

/**
 * Data viewer
 * <p>
 * Loads previous performance on tasks and displays in graph format
 * TODO: In development, not currently functioning
 */
public class DataViewer extends Activity {

    private String TAG = "MymouDataViewer";
    int num_sessions;
    DataPoint[] y_valuesPart, y_valuesPerf, y_valuesRew;
    String[] dates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_viewer);

        findViewById(R.id.butt_dv_select_task).setOnClickListener(buttonClickListener);
        findViewById(R.id.butt_dv_select_sess).setOnClickListener(buttonClickListener);

        // Cues disabled
        UtilsTask.toggleCue((Button) findViewById(R.id.butt_dv_select_task), false);
        UtilsTask.toggleCue((Button) findViewById(R.id.butt_dv_select_sess), false);

        MymouDatabase db = Room.databaseBuilder(getApplicationContext(),
                MymouDatabase.class, "MymouDatabase").build();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                List<Session> sessions = db.userDao().getAllSessions();

                num_sessions = sessions.size();
                y_valuesPart = new DataPoint[num_sessions];
                y_valuesPerf = new DataPoint[num_sessions];
                y_valuesRew = new DataPoint[num_sessions];
                dates = new String[num_sessions];
                for (int i = 0; i < num_sessions; i++) {
                    Session session1 = sessions.get(i);
                    y_valuesPart[i] = new DataPoint(i, session1.num_trials);
                    double perf = ((double) session1.num_corr_trials / (double) session1.num_trials) * 100;
                    y_valuesPerf[i] = new DataPoint(i, (int) perf);
                    y_valuesRew[i] = new DataPoint(i, session1.ms_reward_given);
                    dates[i] = " " + session1.date.charAt(4) + session1.date.charAt(5) + "/" + session1.date.charAt(6) + session1.date.charAt(7);
                    Log.d(TAG, "Got entry: "+dates[i]+" "+y_valuesPart[i]+" "+y_valuesPerf[i]+" "+y_valuesRew[i]);
                }

                if (num_sessions==0) {
                    TextView tv = findViewById(R.id.tv_dataviewer_title);
                    tv.setText("No data found - have you run any sessions yet?");
                } else {
                    CreateGraphs();
                }

            }
        });

    }

    private void CreateGraphs() {

        createGraph("Number trials", findViewById(R.id.graph_part), y_valuesPart);
        createGraph("Percent correct", findViewById(R.id.graph_perf), y_valuesPerf);
        createGraph("Reward given (ms)", findViewById(R.id.graph_rew), y_valuesRew);

    }

    private GraphView createGraph(String ylab, View view, DataPoint[] arr) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(arr);
        GraphView graph = (GraphView) view;
        UtilsSystem.addGraph(graph, series, "Session", ylab, num_sessions);
        graph.getViewport().setMinY(0);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        if (num_sessions > 1) {
            graph.getViewport().setMaxX(num_sessions - 1);
            graph.getGridLabelRenderer().setNumHorizontalLabels(num_sessions);
            graph.getViewport().setXAxisBoundsManual(true);
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
            staticLabelsFormatter.setHorizontalLabels(dates);
            graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        }
        return graph;
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
