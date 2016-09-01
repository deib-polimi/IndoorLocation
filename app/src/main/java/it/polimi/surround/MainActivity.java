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

import it.polimi.surround.util.DistanceEstimator;


public class MainActivity extends AppCompatActivity implements UIActivity, SensorEventListener, OnInitListener{

//    public static final Map<String, List<String>> PLACES_BY_BEACONS;
    public static final Map<String, double[]> LOCATION_BY_BEACONS;
//   public static final short TRILATERATION_N = 6;
    private BeaconManager beaconManager;
//    private final List<String> blackList;
    private Map <String, Map<Integer, String>> results;
    private Region region;
//    private File result;
//    private FileWriter fw;
//    private BufferedWriter bw;
    private DistanceEstimator de;
    private int counter;
//    private boolean found;
    private boolean start = false;
    private String pathName;
    private int previousPoint = 0;
//    private int lastGrid = R.id.G00;
    private Location deviceLocation;

    private Location prevPosition;
    private Location destination;
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
        de = new DistanceEstimator(this);
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
        deviceLocation = new Location("location");
        deviceLocation.setLatitude(0);
        deviceLocation.setLongitude(0);

    }

    public void doorClicked(View v){
        setContentView(R.layout.door_menu);
        //set starting position
        prevPosition = new Location("location");
        prevPosition.setLatitude(5);
        prevPosition.setLongitude(0);

        ImageButton fromDoorToWindow = (ImageButton) findViewById(R.id.fromDoorToWindow);
        ImageButton fromDoorToDesk = (ImageButton) findViewById(R.id.fromDoorToDesk);

        fromDoorToWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.from_door_to_window_menu);
                destination = new Location("location");
                destination.setLatitude(2);
                destination.setLongitude(7);

                ImageButton path1 = (ImageButton) findViewById(R.id.path1door);
                ImageButton path2 = (ImageButton) findViewById(R.id.path2door);
                ImageButton path3 = (ImageButton) findViewById(R.id.path3door);

                path1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 1 ");
                        counter = 0;
                        pathName = "door-window-1";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set start
                        Button pos50 = (Button) findViewById(R.id.pos50);
                        //pos50.setVisibility(View.VISIBLE);
                        usedButtons.add(pos50);
                        //pos50.setBackgroundColor(Color.RED);
                        //set finish
                        Button pos27 = (Button) findViewById(R.id.pos27);
                        pos27.setVisibility(View.VISIBLE);
                        pos27.setBackgroundColor(Color.BLUE);

                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        Button pos41 = (Button) findViewById(R.id.pos41);
                        usedButtons.add(pos41);
                        Button pos31 = (Button) findViewById(R.id.pos31);
                        usedButtons.add(pos31);
                        Button pos21 = (Button) findViewById(R.id.pos21);
                        usedButtons.add(pos21);
                        Button pos22 = (Button) findViewById(R.id.pos22);
                        usedButtons.add(pos22);
                        Button pos23 = (Button) findViewById(R.id.pos23);
                        usedButtons.add(pos23);
                        Button pos24 = (Button) findViewById(R.id.pos24);
                        usedButtons.add(pos24);
                        Button pos25 = (Button) findViewById(R.id.pos25);
                        usedButtons.add(pos25);
                        Button pos26 = (Button) findViewById(R.id.pos26);
                        usedButtons.add(pos26);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);

                    }
                });

                path2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 2 ");
                        counter = 0;
                        pathName = "door-window-2";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set start
                        Button pos50 = (Button) findViewById(R.id.pos50);
//                        pos50.setVisibility(View.VISIBLE);
//                        pos50.setBackgroundColor(Color.RED);
                        usedButtons.add(pos50);
                        //set finish
                        Button pos27 = (Button) findViewById(R.id.pos27);
                        pos27.setVisibility(View.VISIBLE);
                        pos27.setBackgroundColor(Color.BLUE);

                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        Button pos52 = (Button) findViewById(R.id.pos52);
                        usedButtons.add(pos52);
                        Button pos53 = (Button) findViewById(R.id.pos53);
                        usedButtons.add(pos53);
                        Button pos54 = (Button) findViewById(R.id.pos54);
                        usedButtons.add(pos54);
                        Button pos44 = (Button) findViewById(R.id.pos44);
                        usedButtons.add(pos44);
                        Button pos34 = (Button) findViewById(R.id.pos34);
                        usedButtons.add(pos34);
                        Button pos24 = (Button) findViewById(R.id.pos24);
                        usedButtons.add(pos24);
                        Button pos25 = (Button) findViewById(R.id.pos25);
                        usedButtons.add(pos25);
                        Button pos26 = (Button) findViewById(R.id.pos26);
                        usedButtons.add(pos26);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);

                    }
                });

                path3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 3 ");
                        counter = 0;
                        pathName = "door-window-3";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set start
                        Button pos50 = (Button) findViewById(R.id.pos50);
//                        pos50.setVisibility(View.VISIBLE);
//                        pos50.setBackgroundColor(Color.RED);
                        usedButtons.add(pos50);
                        //set finish
                        Button pos27 = (Button) findViewById(R.id.pos27);
                        pos27.setVisibility(View.VISIBLE);
                        pos27.setBackgroundColor(Color.BLUE);

                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        Button pos52 = (Button) findViewById(R.id.pos52);
                        usedButtons.add(pos52);
                        Button pos53 = (Button) findViewById(R.id.pos53);
                        usedButtons.add(pos53);
                        Button pos54 = (Button) findViewById(R.id.pos54);
                        usedButtons.add(pos54);
                        Button pos55 = (Button) findViewById(R.id.pos55);
                        usedButtons.add(pos55);
                        Button pos56 = (Button) findViewById(R.id.pos56);
                        usedButtons.add(pos56);
                        Button pos57 = (Button) findViewById(R.id.pos57);
                        usedButtons.add(pos57);
                        Button pos47 = (Button) findViewById(R.id.pos47);
                        usedButtons.add(pos47);
                        Button pos37 = (Button) findViewById(R.id.pos37);
                        usedButtons.add(pos37);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);

                    }
                });
            }
        });

        fromDoorToDesk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.from_door_to_desk_menu);
                destination = new Location("destination");
                destination.setLatitude(5);
                destination.setLongitude(7);

                ImageButton path_to_desk = (ImageButton) findViewById(R.id.pathToDesk);
                ImageButton path_2_to_desk = (ImageButton) findViewById(R.id.path2ToDesk);

                path_to_desk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso scrivania 1");
                        counter = 0;
                        pathName = "door-desk";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set start
                        Button pos50 = (Button) findViewById(R.id.pos50);
//                        pos50.setVisibility(View.VISIBLE);
//                        pos50.setBackgroundColor(Color.RED);
                        usedButtons.add(pos50);
                        //set finish
                        Button pos57 = (Button) findViewById(R.id.pos57);
                        pos57.setVisibility(View.VISIBLE);
                        pos57.setBackgroundColor(Color.BLUE);

                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        Button pos41 = (Button) findViewById(R.id.pos41);
                        usedButtons.add(pos41);
                        Button pos31 = (Button) findViewById(R.id.pos31);
                        usedButtons.add(pos31);
                        Button pos21 = (Button) findViewById(R.id.pos21);
                        usedButtons.add(pos21);
                        Button pos22 = (Button) findViewById(R.id.pos22);
                        usedButtons.add(pos22);
                        Button pos23 = (Button) findViewById(R.id.pos23);
                        usedButtons.add(pos23);
                        Button pos24 = (Button) findViewById(R.id.pos24);
                        usedButtons.add(pos24);
                        Button pos25 = (Button) findViewById(R.id.pos25);
                        usedButtons.add(pos25);
                        Button pos26 = (Button) findViewById(R.id.pos26);
                        usedButtons.add(pos26);
                        Button pos27 = (Button) findViewById(R.id.pos27);
                        usedButtons.add(pos27);
                        Button pos37 = (Button) findViewById(R.id.pos37);
                        usedButtons.add(pos37);
                        Button pos47 = (Button) findViewById(R.id.pos47);
                        usedButtons.add(pos47);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);

                    }
                });

                path_2_to_desk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso scrivania 2");
                        counter = 0;
                        pathName = "door-desk-2";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set start
                        Button pos50 = (Button) findViewById(R.id.pos50);
//                        pos50.setVisibility(View.VISIBLE);
//                        pos50.setBackgroundColor(Color.RED);
                        usedButtons.add(pos50);
                        //set finish
                        Button pos57 = (Button) findViewById(R.id.pos57);
                        pos57.setVisibility(View.VISIBLE);
                        pos57.setBackgroundColor(Color.BLUE);

                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        Button pos52 = (Button) findViewById(R.id.pos52);
                        usedButtons.add(pos52);
                        Button pos42 = (Button) findViewById(R.id.pos42);
                        usedButtons.add(pos42);
                        Button pos32 = (Button) findViewById(R.id.pos32);
                        usedButtons.add(pos32);
                        Button pos33 = (Button) findViewById(R.id.pos33);
                        usedButtons.add(pos33);
                        Button pos34 = (Button) findViewById(R.id.pos34);
                        usedButtons.add(pos34);
                        Button pos35 = (Button) findViewById(R.id.pos35);
                        usedButtons.add(pos35);
                        Button pos36 = (Button) findViewById(R.id.pos36);
                        usedButtons.add(pos36);
                        Button pos37 = (Button) findViewById(R.id.pos37);
                        usedButtons.add(pos37);
                        Button pos47 = (Button) findViewById(R.id.pos47);
                        usedButtons.add(pos47);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);

                    }
                });
            }
        });
    }

    public void windowClicked(View v){
        setContentView(R.layout.window_menu);
        //set starting position
        prevPosition = new Location("location");
        prevPosition.setLatitude(2);
        prevPosition.setLongitude(7);

        ImageButton fromWindowtoDoor = (ImageButton) findViewById(R.id.fromWindowToDoor);

        fromWindowtoDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.from_window_to_door_menu);
                destination = new Location("destination");
                destination.setLatitude(5);
                destination.setLongitude(0);

                ImageButton path1window = (ImageButton) findViewById(R.id.path1window);
                ImageButton path2window = (ImageButton) findViewById(R.id.path2window);
                ImageButton path3window = (ImageButton) findViewById(R.id.path3window);

                path1window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 1 ritorno");
                        counter = 0;
                        pathName = "window-door-1";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set finish
                        Button pos50 = (Button) findViewById(R.id.pos50);
                        pos50.setVisibility(View.VISIBLE);
                        pos50.setBackgroundColor(Color.BLUE);
                        //set start
                        Button pos27 = (Button) findViewById(R.id.pos27);
//                        pos27.setVisibility(View.VISIBLE);
//                        pos27.setBackgroundColor(Color.RED);
                        usedButtons.add(pos27);

                        Button pos26 = (Button) findViewById(R.id.pos26);
                        usedButtons.add(pos26);
                        Button pos25 = (Button) findViewById(R.id.pos25);
                        usedButtons.add(pos25);
                        Button pos24 = (Button) findViewById(R.id.pos24);
                        usedButtons.add(pos24);
                        Button pos23 = (Button) findViewById(R.id.pos23);
                        usedButtons.add(pos23);
                        Button pos22 = (Button) findViewById(R.id.pos22);
                        usedButtons.add(pos22);
                        Button pos21 = (Button) findViewById(R.id.pos21);
                        usedButtons.add(pos21);
                        Button pos31 = (Button) findViewById(R.id.pos31);
                        usedButtons.add(pos31);
                        Button pos41 = (Button) findViewById(R.id.pos41);
                        usedButtons.add(pos41);
                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);

                    }
                });

                path2window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 2 ritorno");
                        counter = 0;
                        pathName = "window-door-2";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set finish
                        Button pos50 = (Button) findViewById(R.id.pos50);
                        pos50.setVisibility(View.VISIBLE);
                        pos50.setBackgroundColor(Color.BLUE);
                        //set start
                        Button pos27 = (Button) findViewById(R.id.pos27);
//                        pos27.setVisibility(View.VISIBLE);
//                        pos27.setBackgroundColor(Color.RED);
                        usedButtons.add(pos27);

                        Button pos26 = (Button) findViewById(R.id.pos26);
                        usedButtons.add(pos26);
                        Button pos25 = (Button) findViewById(R.id.pos25);
                        usedButtons.add(pos25);
                        Button pos24 = (Button) findViewById(R.id.pos24);
                        usedButtons.add(pos24);
                        Button pos34 = (Button) findViewById(R.id.pos34);
                        usedButtons.add(pos34);
                        Button pos44 = (Button) findViewById(R.id.pos44);
                        usedButtons.add(pos44);
                        Button pos54 = (Button) findViewById(R.id.pos54);
                        usedButtons.add(pos54);
                        Button pos53 = (Button) findViewById(R.id.pos53);
                        usedButtons.add(pos53);
                        Button pos52 = (Button) findViewById(R.id.pos52);
                        usedButtons.add(pos52);
                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);

                    }
                });

                path3window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 3 ");
                        counter = 0;
                        pathName = "window-door-3";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set finish
                        Button pos50 = (Button) findViewById(R.id.pos50);
                        pos50.setVisibility(View.VISIBLE);
                        pos50.setBackgroundColor(Color.BLUE);
                        //set start
                        Button pos27 = (Button) findViewById(R.id.pos27);
//                        pos27.setVisibility(View.VISIBLE);
//                        pos27.setBackgroundColor(Color.RED);
                        usedButtons.add(pos27);

                        Button pos37 = (Button) findViewById(R.id.pos37);
                        usedButtons.add(pos37);
                        Button pos47 = (Button) findViewById(R.id.pos47);
                        usedButtons.add(pos47);
                        Button pos57 = (Button) findViewById(R.id.pos57);
                        usedButtons.add(pos57);
                        Button pos56 = (Button) findViewById(R.id.pos56);
                        usedButtons.add(pos56);
                        Button pos55 = (Button) findViewById(R.id.pos55);
                        usedButtons.add(pos55);
                        Button pos54 = (Button) findViewById(R.id.pos54);
                        usedButtons.add(pos54);
                        Button pos53 = (Button) findViewById(R.id.pos53);
                        usedButtons.add(pos53);
                        Button pos52 = (Button) findViewById(R.id.pos52);
                        usedButtons.add(pos52);
                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);

                    }
                });
            }
        });
    }

    public void deskClicked(View v){
        setContentView(R.layout.desk_menu);
        //set starting position
        prevPosition = new Location("location");
        prevPosition.setLatitude(5);
        prevPosition.setLongitude(7);

        ImageButton fromDesktoDoor = (ImageButton) findViewById(R.id.fromDeskToDoor);

        fromDesktoDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.from_desk_to_door_menu);
                destination = new Location("destination");
                destination.setLatitude(5);
                destination.setLongitude(0);

                ImageButton path_from_desk = (ImageButton) findViewById(R.id.pathFromDesk);

                path_from_desk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso scrivania ");
                        counter = 0;
                        pathName = "desk-door";
                        ArrayList<Button> usedButtons = new ArrayList<Button>();
                        setContentView(R.layout.path_background_grid);
                        //set finish
                        Button pos50 = (Button) findViewById(R.id.pos50);
                        pos50.setVisibility(View.VISIBLE);
                        pos50.setBackgroundColor(Color.BLUE);
                        //set start
                        Button pos57 = (Button) findViewById(R.id.pos57);
//                        pos57.setVisibility(View.VISIBLE);
//                        pos57.setBackgroundColor(Color.RED);
                        usedButtons.add(pos57);

                        Button pos47 = (Button) findViewById(R.id.pos47);
                        usedButtons.add(pos47);
                        Button pos37 = (Button) findViewById(R.id.pos37);
                        usedButtons.add(pos37);
                        Button pos27 = (Button) findViewById(R.id.pos27);
                        usedButtons.add(pos27);
                        Button pos26 = (Button) findViewById(R.id.pos26);
                        usedButtons.add(pos26);
                        Button pos25 = (Button) findViewById(R.id.pos25);
                        usedButtons.add(pos25);
                        Button pos24 = (Button) findViewById(R.id.pos24);
                        usedButtons.add(pos24);
                        Button pos23 = (Button) findViewById(R.id.pos23);
                        usedButtons.add(pos23);
                        Button pos22 = (Button) findViewById(R.id.pos22);
                        usedButtons.add(pos22);
                        Button pos21 = (Button) findViewById(R.id.pos21);
                        usedButtons.add(pos21);
                        Button pos31 = (Button) findViewById(R.id.pos31);
                        usedButtons.add(pos31);
                        Button pos41 = (Button) findViewById(R.id.pos41);
                        usedButtons.add(pos41);
                        Button pos51 = (Button) findViewById(R.id.pos51);
                        usedButtons.add(pos51);
                        //set to grey
                        setButtons(usedButtons);
                        System.out.println("pronto a cercare posizione");
                        calculatePosition(usedButtons);


                    }
                });
            }
        });
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
                        findPosition(new ArrayList<Beacon>(list), usedButtons);
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

    public void findPosition(List<Beacon> beacons, ArrayList<Button> usedButtons){
        try{
            System.out.println("starting from position ("+ prevPosition.getLatitude()+","+prevPosition.getLongitude()+") \n");
            dbgbw.write("starting from position " + prevPosition.getLatitude()+ ", "+prevPosition.getLongitude()+"\n");
            switch (pathName){
                case "door-window-1": calculatePath1(beacons);
                                        path1UI(usedButtons);
                    break;
                case "door-window-2": calculatePath2(beacons);
                                        path2UI(usedButtons);
                    break;
                case "door-window-3": calculatePath3(beacons);
                                        path3UI(usedButtons);
                    break;
                case "door-desk": calculatePathToDesk(beacons);
                                        pathToDeskUI(usedButtons);
                    break;
                case "door-desk-2": calculatePathToDesk2(beacons);
                                        path2ToDeskUI(usedButtons);
                    break;
                case "window-door-1": calculatePath1Window(beacons);
                                        path1WindowUI(usedButtons);
                    break;
                case "window-door-2": calculatePath2Window(beacons);
                                        path2WindowUI(usedButtons);
                    break;
                case "window-door-3": calculatePath3Window(beacons);
                                        path3WindowUI(usedButtons);
                    break;
                case "desk-door": calculatePathFromDesk(beacons);
                                        pathFromDeskUI(usedButtons);
                    break;
            }
//            Location eLocation = de.estimateLocation(beacons, deviceLocation, prevPosition,
//                    dbgbw, deviceData, accData);
//            if(eLocation != null){
//                prevPosition = eLocation;
//                ptsbw.write(DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\t" +
//                        eLocation.getLatitude() + "\t "+  eLocation.getLongitude()+ "\n");
//                ptsbw.flush();
//                System.out.println("updated position ("+ prevPosition.getLatitude()+","+prevPosition.getLongitude()+") \n");
//
//            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    private void calculatePath1(List<Beacon> beacons) {

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                    if (distance == 0){
                        prevPosition.setLatitude(5);
                        prevPosition.setLongitude(1);
                        speakWords("Gira a sinistra");
                        counter = 0;
                    }else if (distance == 1) {
                        counter = 0;
                        if (!start){
                            speakWords("Sei al punto di partenza, avanza");
                            start = true;
                        }
                    }else if (distance > 1){
                            counter++;
                    }break;
            case 1: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                    if (distance == 1){
                        prevPosition.setLatitude(4);
                        prevPosition.setLongitude(1);
                        speakWords("Continua dritto");
                        counter = 0;
                    }else if (distance == 0){
                        counter = 0;
                    }else if (distance > 1){
                        counter++;
                    }break;
            case 2: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                    if (distance == 0){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(1);
                        speakWords("Gira a destra");
                        counter = 0;
                    }else if (distance == 1){
                        prevPosition.setLatitude(3);
                        prevPosition.setLongitude(1);
                        speakWords("Continua dritto");
                        counter = 0;
                    }else if (distance == 2){
                        counter = 0;
                    }else if (distance == 3){
                        distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                        if (distance == 0 || distance == 3){
                            counter++;
                        }else {
                            counter = 0;
                        }
                    }break;
            case 3: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                    if (distance == 0){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(1);
                        speakWords("Gira a destra");
                        counter = 0;
                    }else if (distance == 1){
                        counter = 0;
                    }else if (distance > 1){
                        counter++;
                    }break;
            case 4: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                    if (distance == 1){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(2);
                        speakWords("Continua dritto");
                        counter = 0;
                    }else if (distance == 0){
                        counter = 0;
                    }else if (distance > 1){
                        counter++;
                    }break;
            case 5: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 0){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(4);
                        speakWords("Continua dritto");
                        counter = 0;
                    }else if (distance == 1){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(3);
                        speakWords("Continua dritto");
                        counter = 0;
                    }else if (distance == 2){
                        counter = 0;
                    }else if (distance == 3){
                        distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                        if (distance == 0 || distance == 3) {
                            counter++;
                        }else {
                            counter = 0;
                        }
                    }break;
            case 6: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 0){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(4);
                        speakWords("Continua dritto");
                        counter = 0;
                    }else if (distance == 1){
                        counter = 0;
                    }else if (distance > 1){
                        counter++;
                    }break;
            case 7: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 1){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(5);
                        speakWords("Continua dritto");
                        counter = 0;
                    }else if (distance == 0){
                        counter = 0;
                    }else if (distance > 1){
                        counter++;
                    }break;
            case 8: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                    if (distance == 0){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(7);
                        counter = 0;
                    }else if (distance == 1){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(6);
                        speakWords("Continua dritto");
                        counter = 0;
                    }else if (distance == 2){
                        counter = 0;
                    }else if (distance == 3){
                        distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                        if (distance == 0 || distance == 3) {
                            counter++;
                        }else {
                            counter = 0;
                        }
                    }break;
            case 9: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                    if (distance == 0){
                        prevPosition.setLatitude(2);
                        prevPosition.setLongitude(7);
                        counter = 0;
                    }else if (distance == 1){
                        counter = 0;
                    }else if (distance > 1){
                        counter++;
                    }
        }

    }

    private void calculatePath2(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1) {
                    if (!start){
                        speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(3);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 3: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 9: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }
        }
    }

    private void calculatePath3(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1) {
                    if (!start){
                        speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(3);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 3: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(5);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 5: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(6);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 6: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 9: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }
        }
    }

    private void calculatePathToDesk(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1) {
                    if (!start){
                        speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 3: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(2);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(3);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 9: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }
            case 10: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 11: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 12: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }
        }
    }

    private void calculatePathToDesk2(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1) {
                    if (!start){
                        speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    counter = 0;
                }else if (distance == 2){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(2);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 3 || distance == 0){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 2){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(2);
                    speakWords("Gira a destra");
                    counter = 0;
                }else{
                    distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                    if (distance == 2) {
                        counter = 0;
                    }else {
                        counter++;
                    }
                }break;
            case 4: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 2){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(3);
                    speakWords("Continua dritto");
                    counter = 0;
                }else {
                    distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                    if (distance == 2) {
                        counter = 0;
                    }else {
                        counter++;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                } else {
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 2){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(5);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else {
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 2){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(6);
                    speakWords("Continua dritto");
                    counter = 0;
                }else {
                    distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 2) {
                        counter = 0;
                    }else {
                        counter++;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else{
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else {
                    distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 10: distance = de.distanceFromBeacon(1, beacons, dbgbw, deviceLocation);
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else{
                    counter++;
                }
        }
    }

    private void calculatePath1Window(List<Beacon> beacons){
        int distance;

        switch (previousPoint){

            case 0: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    if (!start){
                        speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 2: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(3);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(2);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(0);
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
        }
    }

    private void calculatePath2Window(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    if (!start){
                        speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 2: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(3);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(0);
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
        }
    }

    private void calculatePath3Window(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    if (!start){
                        speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 2: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(6);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(5);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(3);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(3, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(0);
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
        }
    }

    private void calculatePathFromDesk(List<Beacon> beacons){

        int distance;

        switch (previousPoint){

            case 0: distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    if (!start){
                        speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(1, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 2: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(3);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(2);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 10: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(1);
                    speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(4, beacons,dbgbw, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 11: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 12: distance = de.distanceFromBeacon(5, beacons,dbgbw, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(0);
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
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

    public void path1UI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    switch ((int) prevPosition.getLongitude()) {
                        case 0:
//                            if (Math.abs(previousPoint) <= 1) {
                                clearButtons(usedButtons);
                                button = usedButtons.get(0);
                                previousPoint = 0;
                                System.out.println("sono nel punto (5,0) \n");
                                button.setBackgroundColor(Color.GREEN);
                                //found = true;
                                //counter = 0;
//                            }
                                break;
                        case 1:
//                            if (Math.abs(previousPoint-1) <= 1) {
                                clearButtons(usedButtons);
                                button = usedButtons.get(1);
                                previousPoint = 1;
                                System.out.println("sono nel punto (5,1) \n");
                                button.setBackgroundColor(Color.GREEN);
                                //found = true;
                                //counter = 0;
//                            }
                                break;
                    }break;
                case 4:
                    if ((int) prevPosition.getLongitude() == 1) {
//                        if (Math.abs(previousPoint-2) <= 1) {
                            clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            previousPoint = 2;
                            System.out.println("sono nel punto (4,1) \n");
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
//                        }
                    }
                            break;
                case 3:
                    if ((int) prevPosition.getLongitude() == 1) {
//                        if (Math.abs(previousPoint-3) <= 1) {
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
//                        }
                    }
                            break;
                case 2:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
//                            if (Math.abs(previousPoint-4) <= 1) {
                                clearButtons(usedButtons);
                                button = usedButtons.get(4);
                                previousPoint = 4;
                                button.setBackgroundColor(Color.GREEN);
                                //found = true;
                                //counter = 0;
//                            }
                                break;
                        case 2:
//                            if (Math.abs(previousPoint-5) <= 1) {
                                clearButtons(usedButtons);
                                button = usedButtons.get(5);
                                previousPoint = 5;
                                button.setBackgroundColor(Color.GREEN);
                                //found = true;
                                //counter = 0;
//                            }
                                break;
                        case 3:
//                            if (Math.abs(previousPoint-6) <= 1) {
                                clearButtons(usedButtons);
                                button = usedButtons.get(6);
                                previousPoint = 6;
                                button.setBackgroundColor(Color.GREEN);
                                //found = true;
                                //counter = 0;
//                            }
                                break;
                        case 4:
//                            if (Math.abs(previousPoint-7) <= 1) {
                                clearButtons(usedButtons);
                                button = usedButtons.get(7);
                                previousPoint = 7;
                                button.setBackgroundColor(Color.GREEN);
                                //found = true;
                                //counter = 0;
                         //   }
                                break;
                        case 5:
//                            if (Math.abs(previousPoint-8) <= 1) {
                                clearButtons(usedButtons);
                                button = usedButtons.get(8);
                                previousPoint = 8;
                                button.setBackgroundColor(Color.GREEN);
                                //found = true;
                                //counter = 0;
                      //      }
                                break;
                        case 6:
//                            if (Math.abs(previousPoint-9) <= 1) {
                                clearButtons(usedButtons);
                                button = usedButtons.get(9);
                                previousPoint = 9;
                                button.setBackgroundColor(Color.GREEN);
                                //found = true;
                                //counter = 0;
                         //   }
                                break;
                    }break;
                default:break;

            } //if (!found) {
                //counter++;
                if (counter == 4) {
                    Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                    System.out.println("Ti sei perso, torna indietro!");

                }
//            }
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }
    }

    private void path2UI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    switch ((int) prevPosition.getLongitude()) {
                        case 0:
                            clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    if ((int) prevPosition.getLongitude() == 4) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(5);
                        previousPoint = 5;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) prevPosition.getLongitude() == 4) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(6);
                        previousPoint = 6;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 2:
                    switch ((int) prevPosition.getLongitude()) {
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;

            }//if (!found) {
                //counter++;
                if (counter == 4) {
                    Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                    System.out.println("Ti sei perso, torna indietro!");
                }
            //}
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }

    }

    private void path3UI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    switch ((int) prevPosition.getLongitude()) {
                        case 0:
                            clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    if ((int) prevPosition.getLongitude() == 7) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(8);
                        previousPoint = 8;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) prevPosition.getLongitude() == 7) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(9);
                        previousPoint = 9;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                default:break;
                    //if (!found) {
                        //counter++;

                   // }
            }if (counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }

    }

    private void pathToDeskUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    switch ((int) prevPosition.getLongitude()) {
                        case 0:
                            clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(12);
                            previousPoint = 12;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 3:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(11);
                            previousPoint = 11;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 2:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(10);
                            previousPoint = 10;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;
                //if (!found) {
                //      counter++;

                //    }
            }if (counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }
    }

    private void path2ToDeskUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
 //       found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    switch ((int) prevPosition.getLongitude()) {
                        case 0:
                            clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    switch ((int) prevPosition.getLongitude()) {
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(10);
                            previousPoint = 10;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 3:
                    switch ((int) prevPosition.getLongitude()) {
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;
                //if (!found) {
                //      counter++;

                //    }
            }if (counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }
    }

    private void path1WindowUI(ArrayList<Button> usedButtons) {


        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    if ((int) prevPosition.getLongitude() == 1) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(9);
                        previousPoint = 9;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 4:
                    if ((int) prevPosition.getLongitude() == 1) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(8);
                        previousPoint = 8;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) prevPosition.getLongitude() == 1) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(7);
                        previousPoint = 7;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 2:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;
                //if (!found) {
                //counter++;

                //}
            }if (counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }

    }

    private void path2WindowUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
 //       found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    if ((int) prevPosition.getLongitude() == 4) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(5);
                        previousPoint = 5;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) prevPosition.getLongitude() == 4) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(4);
                        previousPoint = 4;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 2:
                    switch ((int) prevPosition.getLongitude()) {
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;

                    }break;
                default:break;
                //if (!found) {
                // counter++;

                // }
            } if (counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }
    }

    private void path3WindowUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    if ((int) prevPosition.getLongitude() == 7) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(2);
                        previousPoint = 2;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) prevPosition.getLongitude() == 7) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(1);
                        previousPoint = 1;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 2:
                    if ((int) prevPosition.getLongitude() == 7) {
                        clearButtons(usedButtons);
                        button = usedButtons.get(0);
                        previousPoint = 0;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                default:break;
                //if (!found) {
                //counter++;

                //}
            }if (counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }

    }

    private void pathFromDeskUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + prevPosition.getLatitude() + "," + prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (prevPosition.getLatitude() != destination.getLatitude() || prevPosition.getLongitude() != destination.getLongitude()){
            switch ((int) prevPosition.getLatitude()) {
                case 5:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(12);
                            previousPoint = 12;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(11);
                            previousPoint = 11;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 3:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(10);
                            previousPoint = 10;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 2:
                    switch ((int) prevPosition.getLongitude()) {
                        case 1:
                            clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;
                //if (!found) {
                //counter++;

                //}
            }if (counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            stopCalculation();
            endJourney();
        }
    }

    public void endJourney(){
        setContentView(R.layout.end_menu);
        speakWords("Complimenti, sei arrivato a destinazione!");
        Button endButton = (Button) findViewById(R.id.restartButton);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    public void clearButtons(ArrayList<Button> usedButtons){
        for (Button button : usedButtons  ) {
            button.setBackgroundColor(Color.LTGRAY);
        }
        usedButtons.get(0).setBackgroundColor(Color.RED);
    }

    public void setButtons(ArrayList<Button> usedButtons){
        for (Button button : usedButtons  ) {
            button.setVisibility(View.VISIBLE);
            button.setBackgroundColor(Color.LTGRAY);
        }
        usedButtons.get(0).setBackgroundColor(Color.RED);
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
        try{
            dbgbw.write("starting from position " + prevPosition.getLatitude()+ ", "+prevPosition.getLongitude()+"\n");
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

    private void speakWords(String speech){
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }
}
