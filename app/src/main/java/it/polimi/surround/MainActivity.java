package it.polimi.surround;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;

import org.apache.commons.math3.linear.SingularMatrixException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import it.polimi.surround.util.DistanceEstimator;


public class MainActivity extends AppCompatActivity implements UIActivity{

    public static final Map<String, List<String>> PLACES_BY_BEACONS;
    public static final Map<String, double[]> LOCATION_BY_BEACONS;
    public static final short TRILATERATION_N = 4;
    private BeaconManager beaconManager;
    private final List<String> blackList;
    private Map <String, Map<Integer, String>> results;
    private Region region;
    private File result;
    private FileWriter fw;
    private BufferedWriter bw;
    private DistanceEstimator de;
    private int counter;
    private int lastGrid = R.id.G00;
    private Location deviceLocation;

    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("42730:37336", new ArrayList<String>() {{
            add("Heavenly Sandwiches");
            // read as: "Heavenly Sandwiches" is closest
            // to the beacon with major 22504 and minor 48827
            add("Green & Green Salads");
            // "Green & Green Salads" is the next closest
            add("Mini Panini");
            // "Mini Panini" is the furthest away
        }});
        placesByBeacons.put("48147:52400", new ArrayList<String>() {{
            add("Mini Panini");
            add("Green & Green Salads");
            add("Heavenly Sandwiches");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
        Map<String, double[]> locationByBeacons = new HashMap<>();
        locationByBeacons.put("42730:37336", new double[] {-2.3,    0,      0,      -89,    0});//B
        locationByBeacons.put("29491:46151", new double[] {0,       2.3,    0,      -89,    0});//F
        locationByBeacons.put("26943:13368", new double[] {2.3,     0,      0,      -89,    0});//E
        locationByBeacons.put("32505:29466", new double[] {0,       -2.3,   0,      -79,    0});//D mint
        locationByBeacons.put("34061:44153", new double[] {0,       0,      1.05,   -79,    0});//A
        locationByBeacons.put("48147:52400", new double[] {0,        0,      0,      -79,    0});//C
        LOCATION_BY_BEACONS = Collections.unmodifiableMap(locationByBeacons);
    }

    {
        results = new TreeMap<String, Map<Integer, String>>();
        blackList = new ArrayList<String>();
        //blackList.add("26943:13368");//E
        //blackList.add("32505:29466");//D mint
        //blackList.add("29491:46151");//F
        //blackList.add("34061:44153");//A
        blackList.add("48147:52400");//C
        de = new DistanceEstimator(this);
        File sd = Environment.getExternalStorageDirectory();
        result = new File(sd,  "Surround.txt");
        try {
            fw = new FileWriter(result, true);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        deviceLocation = new Location("location");
        deviceLocation.setLatitude(0);
        deviceLocation.setLongitude(0);
    }

    public void startExperiment(View v) {
        counter = 0;
        if(beaconManager == null) {
            beaconManager = new BeaconManager(this);
            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                    if (!list.isEmpty())
                        nearBeacons(new ArrayList<Beacon>(list));
                }
            });
            region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
            //region = new Region("ranged region", UUID.fromString("8492e75f-4fd6-469d-b132-043fe94921d8"), null, null);
        }
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
        writeToResult(
                "\n\n\n\nRef. Dis.:\t" +
                        ((EditText) findViewById(R.id.ref_distance)).getText().toString() +
                        "\tStarted at\t" +
                        DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n", false);
    }

    public void stopExperiment(View v){
        beaconManager.stopRanging(region);
        for (String bId : results.keySet())
            writeToResult(bId + "\t\t", false);
        writeToResult("\n", false);
        for(int i = 1; i <= counter; i++) {
            writeToResult(i + "", false);
            for (String bId : results.keySet()) {
                if (bId.equals("LOC"))
                    continue;
                if(results.get(bId).containsKey(i))
                    writeToResult(results.get(bId).get(i), false);
                else
                    writeToResult("\t0\t0.000000000000", false);
            }
            if(results.get("LOC").containsKey(i))
                writeToResult(results.get("LOC").get(i), false);
            else
                writeToResult("\t0.000000000000\t0.000000000000", false);
            writeToResult("\n", false);
        }
        writeToResult("\n\n\n", true);
    }

    public void setPosition(View v){
        switch (v.getId()){
            case R.id.G01:
                this.deviceLocation.setLongitude(-1.3);
                this.deviceLocation.setLatitude(1.3);
            case R.id.G02:
                this.deviceLocation.setLongitude(1.3);
                this.deviceLocation.setLatitude(1.3);
                break;
            case R.id.G11:
                this.deviceLocation.setLongitude(-1.3);
                this.deviceLocation.setLatitude(-1.3);
                break;
            case R.id.G12:
                this.deviceLocation.setLongitude(1.3);
                this.deviceLocation.setLatitude(-1.3);
                break;
            default:
                break;
        }
    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }

    private void nearBeacons(List<Beacon> beacons){

        Beacon nearestBeacon = beacons.get(0);
        List<String> places = placesNearBeacon(nearestBeacon);
        Log.d("Airport", "Nearest places: " + places);

        counter++;
        for(Beacon b : beacons) {
            storeResult(b, counter);
            Log.d("Airport", "Beacon: " + String.format("%d:%d:%d", b.getMajor(), b.getMinor(), b.getRssi()));
        }
        filterBeacons(beacons, TRILATERATION_N);
        try{
            Location eLocation = de.estimateLocation(beacons, deviceLocation);
            if(eLocation != null){
                drawGrid(eLocation);
                storeResult(eLocation, counter);
                Log.d("Airport", "Beacon: " + String.format("%f %f", eLocation.getLatitude(), eLocation.getLongitude()));
                ((TextView) findViewById(R.id.lat_lng_val)).setText("Lat: " + eLocation.getLatitude() + "\nLng: " + eLocation.getLongitude());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void storeResult(Beacon b, int counter) {
        if(!results.containsKey(b.getMajor() + ":" + b.getMinor()))
            results.put(b.getMajor() + ":" + b.getMinor(), new HashMap<Integer, String>());
        results.get(b.getMajor() + ":" + b.getMinor()).put(
                counter,
                "\t" + b.getRssi() + "\t" + Utils.computeAccuracy(b)
        );
    }

    private void storeResult(Location l, int counter) {
        if(!results.containsKey("LOC"))
            results.put("LOC", new HashMap<Integer, String>());
        results.get("LOC").put(
                counter,
                "\t" + l.getLatitude() + "\t" + l.getLongitude()
        );
    }

    private void filterBeacons(List<Beacon> beacons, short max){
        List<Beacon> toRemove = new ArrayList<Beacon>();
        for(Beacon b : beacons)
            if(blackList.contains(b.getMajor() + ":" + b.getMinor()))
                toRemove.add(b);

        beacons.removeAll(toRemove);
        while(beacons.size() > max)
            beacons.remove(max);
    }

    private void writeToResult(String txt, boolean flush){
        try {
            bw.write(txt);
            if(flush)
                bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void drawGrid(Location l){
        ((GradientDrawable)((View)findViewById(lastGrid)).getBackground()).setColor(Color.WHITE);
        lastGrid = getGridId(l);
        ((GradientDrawable)((View)findViewById(lastGrid)).getBackground()).setColor(Color.RED);
    }

    public int getGridId(Location l){
        if(l.getLongitude() <= 0){
            if(l.getLongitude() <=-2)
                if(l.getLatitude() <= 0)
                    return R.id.G10;
                else
                    return R.id.G00;
            else
                if(l.getLatitude() <= 0)
                    return R.id.G11;
                else
                    return R.id.G01;
        }else{
            if(l.getLongitude() <=2)
                if(l.getLatitude() <= 0)
                    return R.id.G12;
                else
                    return R.id.G02;
            else
                if(l.getLatitude() <= 0)
                    return R.id.G13;
                else
                    return R.id.G03;
        }
    }

    @Override
    public void printText(int id, String text) {
        ((TextView) findViewById(id)).setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(it.polimi.surround.R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(it.polimi.surround.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == it.polimi.surround.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
