package it.polimi.surround;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.accessibilityservice.*;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import java.util.Locale;

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

import it.polimi.surround.util.Buttons;
import it.polimi.surround.util.DistanceEstimator;


public class MainActivity extends AppCompatActivity implements UIActivity, SensorEventListener, OnInitListener{

//    public static final Map<String, List<String>> PLACES_BY_BEACONS;
    public static final Map<String, double[]> LOCATION_BY_BEACONS;
//   public static final short TRILATERATION_N = 6;
    private BeaconManager beaconManager;
    private Buttons bs;
//    private final List<String> blackList;
    private Map <String, Map<Integer, String>> results;
    private Region region;
//    private File result;
//    private FileWriter fw;
//    private BufferedWriter bw;
    private int counter;
//    private boolean found;

//    private int lastGrid = R.id.G00;


    private SensorManager manager;
    private Sensor compass;
    private Sensor accelerometer;
    private File debug;
//    private File pointsFile;
    private FileWriter dbgfw;
//    private FileWriter ptsfw;
    private BufferedWriter dbgbw;
//    private BufferedWriter ptsbw;
    float [] compassData;
    float [] accData;
    float[] deviceData;

    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;



    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {
//        Map<String, List<String>> placesByBeacons = new HashMap<>();
//        placesByBeacons.put("42730:37336", new ArrayList<String>() {{
//            add("Heavenly Sandwiches");
//            // read as: "Heavenly Sandwiches" is closest
//            // to the beacon with major 22504 and minor 48827
//            add("Green & Green Salads");
//            // "Green & Green Salads" is the next closest
//            add("Mini Panini");
//            // "Mini Panini" is the furthest away
//        }});
//        placesByBeacons.put("48147:52400", new ArrayList<String>() {{
//            add("Mini Panini");
//            add("Green & Green Salads");
//            add("Heavenly Sandwiches");
//        }});
//        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
        Map<String, double[]> locationByBeacons = new HashMap<>();
        //updated position of beacons in 6 beacon topology
        locationByBeacons.put("26943:13368", new double[] {2,  7,   0,   -89,   0});//E
        locationByBeacons.put("29491:46151", new double[] {5,  7,   0,   -89,   0});//F
        locationByBeacons.put("32505:29466", new double[] {2,  4,   0,   -79,   0});//D mint
        locationByBeacons.put("34061:44153", new double[] {5,  4,   0,   -79,   0});//A
        locationByBeacons.put("42730:37336", new double[] {2,  1,   0,   -89,   0});//B
        locationByBeacons.put("48147:52400", new double[] {5,  1,   0,   -79,   0});//C
        LOCATION_BY_BEACONS = Collections.unmodifiableMap(locationByBeacons);
    }

    {
        results = new TreeMap<String, Map<Integer, String>>();
//        blackList = new ArrayList<String>();
        //blackList.add("26943:13368");//E - Beacon 1
        //blackList.add("32505:29466");//D mint
        //blackList.add("29491:46151");//F
        //blackList.add("34061:44153");//A - Beacon 2
        //blackList.add("48147:52400");//C - Beacon 4
        //blackList.add("42730:37336");//Beacon 3
 //       de = new DistanceEstimator(this);
        bs = new Buttons ();
        bs.passMainActivity(this);
        File sd = Environment.getExternalStorageDirectory();
//        result = new File(sd,  "Surround.txt");
//        try {
//            fw = new FileWriter(result, true);
//            bw = new BufferedWriter(fw);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //add debug
        debug = new File(sd, "Debug.txt");
 //       pointsFile = new File(sd, "PointResult.txt");
        try {
            dbgfw = new FileWriter(debug, true);
            dbgbw = new BufferedWriter(dbgfw);
   //         ptsfw = new FileWriter(pointsFile, true);
   //         ptsbw = new BufferedWriter(ptsfw);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void stopCalculation(){
        beaconManager.stopRanging(region);
//        for (String bId : results.keySet())
//            writeToResult(bId + "\t\t", false);
//        writeToResult("\n", false);
//        for(int i = 1; i <= counter; i++) {
//            writeToResult(i + "", false);
//            for (String bId : results.keySet()) {
//                if (bId.equals("LOC"))
//                    continue;
//                if(results.get(bId).containsKey(i))
//                    writeToResult(results.get(bId).get(i), false);
//                else
//                    writeToResult("\t0\t0.000000000000", false);
//            }
//            if(results.get("LOC").containsKey(i))
//                writeToResult(results.get("LOC").get(i), false);
//            else
//                writeToResult("\t0.000000000000\t0.000000000000", false);
//            writeToResult("\n", false);
//        }
//        writeToResult("\n\n\n", true);
    }

    public void calculatePosition(final ArrayList<Button> usedButtons){
        System.out.println("inizio calcolo mia posizione \n");
        if(beaconManager == null) {
            System.out.println("inizio a cercare beacons \n");
            beaconManager = new BeaconManager(this);
            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                    if (!list.isEmpty())
                        System.out.println("lista beacons \n");
                    bs.findPosition(new ArrayList<Beacon>(list), usedButtons);
                }
            });
            region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        }
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });

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
//        writeToResult(
//                "\n\n\n\nRef. Dis.:\t" +
// //                       ((EditText) findViewById(R.id.ref_distance)).getText().toString() +
//                        "\tStarted at\t" +
//                        DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n", false);
    }

    public void stopExperiment(View v){
        beaconManager.stopRanging(region);
//        for (String bId : results.keySet())
//            writeToResult(bId + "\t\t", false);
//        writeToResult("\n", false);
//        for(int i = 1; i <= counter; i++) {
//            writeToResult(i + "", false);
//            for (String bId : results.keySet()) {
//                if (bId.equals("LOC"))
//                    continue;
//                if(results.get(bId).containsKey(i))
//                    writeToResult(results.get(bId).get(i), false);
//                else
//                    writeToResult("\t0\t0.000000000000", false);
//            }
//            if(results.get("LOC").containsKey(i))
//                writeToResult(results.get("LOC").get(i), false);
//            else
//                writeToResult("\t0.000000000000\t0.000000000000", false);
//            writeToResult("\n", false);
//        }
//        writeToResult("\n\n\n", true);
    }

//    private List<String> placesNearBeacon(Beacon beacon) {
//        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
//        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
//            return PLACES_BY_BEACONS.get(beaconKey);
//        }
//        return Collections.emptyList();
//    }

    private void nearBeacons(List<Beacon> beacons){

//        Beacon nearestBeacon = beacons.get(0);
//        List<String> places = placesNearBeacon(nearestBeacon);
//        Log.d("Airport", "Nearest places: " + places);

        counter++;
        for(Beacon b : beacons) {
            storeResult(b, counter);
//            Log.d("Airport", "Beacon: " + String.format("%d:%d:%d", b.getMajor(), b.getMinor(), b.getRssi()));
        }
//        filterBeacons(beacons, TRILATERATION_N);
//        try{
//            dbgbw.write("starting from position " + prevPosition.getLatitude()+ ", "+prevPosition.getLongitude()+"\n");
//            Location eLocation = de.estimateLocation(beacons, deviceLocation, prevPosition,
//                                                                    dbgbw, deviceData, accData);
//            if(eLocation != null){
//                prevPosition = eLocation;
//                ptsbw.write(DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\t" +
//                        eLocation.getLatitude() + "\t "+  eLocation.getLongitude()+ "\n");
//                ptsbw.flush();
//                //drawGrid(eLocation);
//                storeResult(eLocation, counter);
////                Log.d("Airport", "Beacon: " + String.format("%f %f", eLocation.getLatitude(), eLocation.getLongitude()));
//                ((TextView) findViewById(R.id.lat_lng_val)).setText("Lat: " + eLocation.getLatitude() + "\nLng: " + eLocation.getLongitude());
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }

    private void storeResult(Beacon b, int counter) {
        if(!results.containsKey(b.getMajor() + ":" + b.getMinor()))
            results.put(b.getMajor() + ":" + b.getMinor(), new HashMap<Integer, String>());
        results.get(b.getMajor() + ":" + b.getMinor()).put(
                counter,
                "\t" + b.getRssi() + "\t" + Utils.computeAccuracy(b)
        );
    }

//    private void storeResult(Location l, int counter) {
//        if(!results.containsKey("LOC"))
//            results.put("LOC", new HashMap<Integer, String>());
//        results.get("LOC").put(
//                counter,
//                "\t" + l.getLatitude() + "\t" + l.getLongitude()
//        );
//    }

//    private void filterBeacons(List<Beacon> beacons, short max){
//        List<Beacon> toRemove = new ArrayList<Beacon>();
//        for(Beacon b : beacons)
//            if(blackList.contains(b.getMajor() + ":" + b.getMinor()))
//                toRemove.add(b);
//
//        beacons.removeAll(toRemove);
//        while(beacons.size() > max)
//            beacons.remove(max);
//    }

//    private void writeToResult(String txt, boolean flush){
//        try {
//            bw.write(txt);
//            if(flush)
//                bw.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

/*    public void drawGrid(Location l){
        ((GradientDrawable)((View)findViewById(lastGrid)).getBackground()).setColor(Color.WHITE);
        lastGrid = getGridId(l);
        ((GradientDrawable)((View)findViewById(lastGrid)).getBackground()).setColor(Color.GREEN);
    }
*/
/*
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
*/
    @Override
    public void printText(int id, String text) {
        ((TextView) findViewById(id)).setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_menu);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        compass = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        deviceData = new float[3];
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(this, compass, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accData = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            compassData = event.values;
        }
        if (accData != null && compassData != null){
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = manager.getRotationMatrix(R, I, accData, compassData);
            if (success){
                float[] orientation = new float[3];
                manager.getOrientation(R, orientation);
                deviceData = orientation;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.ITALY);
        }else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    public void speakWords(String speech){
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void doorClicked(View v){
        bs.doorClicked(v);
    }

    public void windowClicked(View v){
        bs.windowClicked(v);
    }

    public void deskClicked(View v){
        bs.deskClicked(v);
    }
}
