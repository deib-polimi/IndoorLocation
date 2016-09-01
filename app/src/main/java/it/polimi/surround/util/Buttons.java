package it.polimi.surround.util;

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

import it.polimi.surround.R;
import it.polimi.surround.UIActivity;
import it.polimi.surround.MainActivity;

/**
 * Created by Luca on 01/09/2016.
 */
public class Buttons extends AppCompatActivity implements UIActivity, SensorEventListener, OnInitListener{

    private MainActivity ma;
    private CalculatePath cp;
    private PathUI pu;

    private String pathName;

    {

        ma = new MainActivity();
        cp = new CalculatePath();
        pu = new PathUI();
    }

    public void doorClicked(View v){
        setContentView(R.layout.door_menu);
        //set starting position
        cp.prevPosition = new Location("location");
        cp.prevPosition.setLatitude(5);
        cp.prevPosition.setLongitude(0);

        ImageButton fromDoorToWindow = (ImageButton) findViewById(R.id.fromDoorToWindow);
        ImageButton fromDoorToDesk = (ImageButton) findViewById(R.id.fromDoorToDesk);

        fromDoorToWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.from_door_to_window_menu);
                cp.destination = new Location("location");
                cp.destination.setLatitude(2);
                cp.destination.setLongitude(7);

                ImageButton path1 = (ImageButton) findViewById(R.id.path1door);
                ImageButton path2 = (ImageButton) findViewById(R.id.path2door);
                ImageButton path3 = (ImageButton) findViewById(R.id.path3door);

                path1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 1 ");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);

                    }
                });

                path2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 2 ");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);

                    }
                });

                path3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 3 ");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);

                    }
                });
            }
        });

        fromDoorToDesk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.from_door_to_desk_menu);
                cp.destination = new Location("destination");
                cp.destination.setLatitude(5);
                cp.destination.setLongitude(7);

                ImageButton path_to_desk = (ImageButton) findViewById(R.id.pathToDesk);
                ImageButton path_2_to_desk = (ImageButton) findViewById(R.id.path2ToDesk);

                path_to_desk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso scrivania 1");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);

                    }
                });

                path_2_to_desk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso scrivania 2");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);

                    }
                });
            }
        });
    }

    public void windowClicked(View v){
        setContentView(R.layout.window_menu);
        //set starting position
        cp.prevPosition = new Location("location");
        cp.prevPosition.setLatitude(2);
        cp.prevPosition.setLongitude(7);

        ImageButton fromWindowtoDoor = (ImageButton) findViewById(R.id.fromWindowToDoor);

        fromWindowtoDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.from_window_to_door_menu);
                cp.destination = new Location("destination");
                cp.destination.setLatitude(5);
                cp.destination.setLongitude(0);

                ImageButton path1window = (ImageButton) findViewById(R.id.path1window);
                ImageButton path2window = (ImageButton) findViewById(R.id.path2window);
                ImageButton path3window = (ImageButton) findViewById(R.id.path3window);

                path1window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 1 ritorno");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);

                    }
                });

                path2window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 2 ritorno");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);

                    }
                });

                path3window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso 3 ");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);

                    }
                });
            }
        });
    }

    public void deskClicked(View v) {
        setContentView(R.layout.desk_menu);
        //set starting position
        cp.prevPosition = new Location("location");
        cp.prevPosition.setLatitude(5);
        cp.prevPosition.setLongitude(7);

        ImageButton fromDesktoDoor = (ImageButton) findViewById(R.id.fromDeskToDoor);

        fromDesktoDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.from_desk_to_door_menu);
                cp.destination = new Location("destination");
                cp.destination.setLatitude(5);
                cp.destination.setLongitude(0);

                ImageButton path_from_desk = (ImageButton) findViewById(R.id.pathFromDesk);

                path_from_desk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("prova percorso scrivania ");
                        cp.counter = 0;
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
                        ma.calculatePosition(usedButtons);


                    }
                });
            }
        });
    }

    public void findPosition(List<Beacon> beacons, ArrayList<Button> usedButtons){
        try{
//            System.out.println("starting from position ("+ prevPosition.getLatitude()+","+prevPosition.getLongitude()+") \n");
//            dbgbw.write("starting from position " + prevPosition.getLatitude()+ ", "+prevPosition.getLongitude()+"\n");
            switch (pathName){
                case "door-window-1": cp.calculatePath1(beacons);
                    pu.path1UI(usedButtons);
                    break;
                case "door-window-2": cp.calculatePath2(beacons);
                    pu.path2UI(usedButtons);
                    break;
                case "door-window-3": cp.calculatePath3(beacons);
                    pu.path3UI(usedButtons);
                    break;
                case "door-desk": cp.calculatePathToDesk(beacons);
                    pu.pathToDeskUI(usedButtons);
                    break;
                case "door-desk-2": cp.calculatePathToDesk2(beacons);
                    pu.path2ToDeskUI(usedButtons);
                    break;
                case "window-door-1": cp.calculatePath1Window(beacons);
                    pu.path1WindowUI(usedButtons);
                    break;
                case "window-door-2": cp.calculatePath2Window(beacons);
                    pu.path2WindowUI(usedButtons);
                    break;
                case "window-door-3": cp.calculatePath3Window(beacons);
                    pu.path3WindowUI(usedButtons);
                    break;
                case "desk-door": cp.calculatePathFromDesk(beacons);
                    pu.pathFromDeskUI(usedButtons);
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


    public void endJourney(){
        setContentView(R.layout.end_menu);
        ma.speakWords("Complimenti, sei arrivato a destinazione!");
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


    @Override
    public void onInit(int status) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void printText(int id, String text) {

    }
}
