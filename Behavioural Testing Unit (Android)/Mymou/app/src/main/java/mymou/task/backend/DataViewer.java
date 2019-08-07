package mymou.task.backend;

import android.app.Activity;
import android.os.Bundle;
import mymou.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import mymou.Utils.UtilsSystem;

public class DataViewer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_viewer);

        // Now just need to load the data

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
}
