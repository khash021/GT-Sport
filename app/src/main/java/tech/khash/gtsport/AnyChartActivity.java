package tech.khash.gtsport;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;

import java.util.ArrayList;
import java.util.List;

import tech.khash.gtsport.Model.Score;
import tech.khash.gtsport.Model.ScoreEntry;
import tech.khash.gtsport.Utils.SaveLoad;

public class AnyChartActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private AnyChartView chartView;
    private ArrayList<Score> scores;
    private TextView errorText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_any);

        progressBar = findViewById(R.id.progress_bar);
        chartView = findViewById(R.id.chart_view);
        chartView.setProgressBar(progressBar);
        //errorText = findViewById(R.id.text_error);
        scores = SaveLoad.loadScoresDb(this);

        plotData(scores);
    }//onCreate

    private void plotData(ArrayList<Score> scores) {
        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("DR rating overview");

        cartesian.yAxis(0).title("DR Rating");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        List<DataEntry> entries = getEntries(scores);
        if (entries == null || entries.size() < 1) {
            //errorText.setVisibility(View.VISIBLE);
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
            }
            return;
        }


        Set set = Set.instantiate();
        set.data(entries);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("DR Rating");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        chartView.setChart(cartesian);

    }//plotData

    private List<DataEntry> getEntries(ArrayList<Score> scores) {
        List<DataEntry> entries = new ArrayList<>();
        ScoreEntry scoreEntry;
        int i = 1;
        for (Score score : scores) {
            String id = String.valueOf(i);
            int drRating = score.getDr();
            long epoch = score.getEpoch();
            scoreEntry = new ScoreEntry(id, drRating, epoch);
            entries.add(scoreEntry);
            i++;
        }
        return entries;
    }

    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value, Number value2, Number value3) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
        }

    }
}//ChartActivity
