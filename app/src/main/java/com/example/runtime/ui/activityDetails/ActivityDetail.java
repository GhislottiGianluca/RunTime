package com.example.runtime.ui.activityDetails;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.runtime.R;
import com.example.runtime.firestore.FirestoreHelper;
import com.example.runtime.firestore.models.RunSegment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import firebase.com.protolitewrapper.BuildConfig;

public class ActivityDetail extends AppCompatActivity {

    private MapView mapView;

    private AnyChartView anyChartView;

    private ConstraintLayout runInfo;

    private LinearLayout buttonContainer;
    private TextView stepsText;
    private TextView caloriesText;
    private TextView kmText;
    private TextView paceText;

    private TextView durationText;

    private Button prevChartButton;
    private Button nextChartButton;

    private final ArrayList<String> chartType = new ArrayList<>(Arrays.asList("Steps", "Kms", "Calories"));
    private int indexList = 1;

    private Cartesian cartesian;

    private Column column;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Retrieve the runId from the Intent
        String runId = getIntent().getStringExtra("runId");

        //TextView textView = findViewById(R.id.detailTitle);

        List<RunSegment> runSegments = new ArrayList<>();

        // Set the user agent for osmdroid
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        stepsText = findViewById(R.id.textSteps);
        kmText = findViewById(R.id.textKm);
        paceText = findViewById(R.id.paceText);
        caloriesText = findViewById(R.id.textCalories);
        durationText = findViewById(R.id.durationText);

        prevChartButton = findViewById(R.id.prevChart);
        nextChartButton = findViewById(R.id.nextChart);

        buttonContainer = findViewById(R.id.buttonContainer);

        //testBackend
        getItemsFromBackend(runId, runSegments);

        //map settings
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        //chart settings
        initChartView();

        nextChartButton.setOnClickListener(e -> {
            if (indexList == 2) {
                indexList = 0;
            } else {
                indexList++;
            }

            if (runSegments.size() > 1) {
                updateChart(runSegments);
            }
        });

        prevChartButton.setOnClickListener(e -> {
            if (indexList == 0) {
                indexList = 2;
            } else {
                indexList--;
            }

            if (runSegments.size() > 1) {
                updateChart(runSegments);
            }
        });

    }

    private void getItemsFromBackend(String runId, List<RunSegment> runSegments) {
        CollectionReference runsCollection = FirestoreHelper.getDb().collection("runSegments");
        Query query = runsCollection.whereEqualTo("runId", runId);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.w("RUN SEGMENT", "try to deserialize segment");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        RunSegment segment = document.toObject(RunSegment.class);
                        runSegments.add(segment);
                    }
                    //runSegments.forEach(segment -> Log.d(segment.getRunId(), "at time " + FirestoreHelper.getLocalDateTimeFromFirebaseTimestampLocalDateTime(segment.getStartDateTime()) ));

                    updateUI(runSegments);

                })
                .addOnFailureListener(e -> {
                    Log.w("failed to get run", "Error adding document", e);
                });
    }

    private void updateUI(List<RunSegment> runSegments) {
        if (runSegments.isEmpty()) {
            return;
        }

        //init mapView if data are available
        initMapView(runSegments);

        if (runSegments.size() > 1) {
            //init chart
            createColumnChart(runSegments);
            anyChartView.setChart(cartesian);
            //updateChart(runSegments);
        } else {
            //anyChartView.setActivated(false);
        }


        //property prep

        LocalDateTime startRun = FirestoreHelper.getLocalDateTimeFromFirebaseTimestamp(runSegments.get(0).getStartDateTime());
        LocalDateTime endRun = FirestoreHelper.getLocalDateTimeFromFirebaseTimestamp(runSegments.get(runSegments.size() - 1).getEndDateTime());

        long totalMinutes = ChronoUnit.MINUTES.between(endRun, startRun);

        Log.e("chr", String.valueOf(totalMinutes));

        DecimalFormat df = new DecimalFormat("#.##");

        int totalSteps = runSegments.stream().mapToInt(RunSegment::getSteps).sum();
        double totalDistanceKM = runSegments.stream().mapToDouble(item -> item.getKm()).sum();
        double calories = runSegments.stream().mapToDouble(item -> item.getCalories()).sum();
        double averagePace = runSegments.stream().mapToDouble(item -> item.getAveragePace()).sum() / runSegments.size();

        stepsText.setText(String.valueOf(totalSteps) + " steps");
        caloriesText.setText(df.format(calories) + " cal");
        paceText.setText(df.format(averagePace) + " min/Km");
        kmText.setText(df.format(totalDistanceKM) + " km");
        durationText.setText(String.valueOf(totalMinutes) + " min");


        //createInfoItem("totalDurationInMinutes", String.valueOf(totalDuration.toMinutes()), infoContainer);

        //double averagePace = itemList.get(0).getAveragePace();
        /*createInfoItem("startRun", FirestoreHelper.formatDateTimeWithSecondos(startRun), infoContainer);
        createInfoItem("endRun", FirestoreHelper.formatDateTimeWithSecondos(endRun), infoContainer);

        createInfoItem("Nr.steps", String.valueOf(totalSteps), infoContainer);
        createInfoItem("average_pace", String.valueOf(averagePace), infoContainer);
        createInfoItem("totalDistanceKM", String.valueOf(totalDistanceKM), infoContainer);*/
        //createInfoItem("calories", String.valueOf(calories), infoContainer);

        //createInfoItem("totalDurationInSeconds", String.valueOf(totalDuration.getSeconds()), infoContainer);


    }

    private void initMapView(List<RunSegment> runSegments) {
        ArrayList<GeoPoint> totalpaths = new ArrayList<>();
        for (RunSegment segment : runSegments) {
            totalpaths.addAll(segment.getGeoPoints());
        }
        if (totalpaths.size() > 0) {
            // Set the map center to the first GeoPoint in the list
            IMapController mapController = mapView.getController();
            mapController.setZoom(14.0); // adjust the zoom level as needed
            mapController.setCenter(totalpaths.get(0));

            // Add a Polyline to connect the GeoPoints
            Polyline line = new Polyline();
            line.setPoints(totalpaths);
            line.setColor(Color.RED);
            mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
            mapView.getOverlayManager().add(line);
        }
    }

    private /*Cartesian */ void createColumnChart(List<RunSegment> runSegments) {
        Map<Integer, Number> graph_map = new TreeMap<>();

        for (RunSegment segment : runSegments) {
            if(indexList == 0){
                Log.e("called with", "zero" + String.valueOf(segment.getSteps()));
                graph_map.put(runSegments.indexOf(segment) + 1, segment.getSteps());
            } else if (indexList == 1){
                Log.e("called with", "uno" + String.valueOf(segment.getKm()));
                graph_map.put(runSegments.indexOf(segment) + 1, segment.getKm());
            } else {
                Log.e("called with", "due" + String.valueOf(segment.getCalories()));
                graph_map.put(runSegments.indexOf(segment) + 1, segment.getCalories());
            }
        }

        //init of cartesian
        cartesian = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<Integer, Number> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        //init of column series
        column = cartesian.column(data);

        column.fill("#1EB980");
        column.stroke("#1EB980");

        column.tooltip()
                .titleFormat("segment nr.: {%X}")
                .format("{%Value}")
                .anchor(Anchor.RIGHT_BOTTOM);

        column.tooltip()
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);

        cartesian.yAxis(0).title("#" + chartType.get(indexList));
        cartesian.xAxis(0).title("Per segment");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);

       // return cartesian;
    }

    private void initChartView() {
        anyChartView = findViewById(R.id.barChart);
        anyChartView.setBackgroundColor("#00000000");
    }

    private void updateChart(List<RunSegment> runSegments) {
        if (runSegments.size() > 1) {
            //anyChartView.clear();

            Map<Integer, Number> graph_map = new TreeMap<>();

            for (RunSegment segment : runSegments) {
                if (indexList == 0) {
                    Log.e("called with", "zero" + String.valueOf(segment.getSteps()));
                    graph_map.put(runSegments.indexOf(segment) + 1, segment.getSteps());
                } else if (indexList == 1) {
                    Log.e("called with", "uno" + String.valueOf(segment.getKm()));
                    graph_map.put(runSegments.indexOf(segment) + 1, segment.getKm());
                } else {
                    Log.e("called with", "due" + String.valueOf(segment.getCalories()));
                    graph_map.put(runSegments.indexOf(segment) + 1, segment.getCalories());
                }
            }

            List<DataEntry> newData = new ArrayList<>();

            for (Map.Entry<Integer, Number> entry : graph_map.entrySet())
                newData.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

            column.data(newData);
            cartesian.yAxis(0).title("#" + chartType.get(indexList));
            cartesian.xAxis(0).title("Per segment");
        } else {
            anyChartView.setVisibility(View.GONE);
            buttonContainer.setVisibility(View.GONE);
        }
    }

    private void createInfoItem(String titleText, String propertyText, LinearLayout layout) {
        LinearLayout infoLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.info_run_detail, layout, false);
        // Customize the card content based on item data
        TextView title = infoLayout.findViewById(R.id.itemTitle);
        TextView property = infoLayout.findViewById(R.id.itemProperty);
        //cardText.setText(item.getRunId());
        title.setText(titleText);
        property.setText(propertyText);

        layout.addView(infoLayout);
    }
}
