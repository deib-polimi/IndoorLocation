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
import it.polimi.surround.util.DistanceEstimator;

/**
 * Created by Luca on 01/09/2016.
 */
public class PathUI extends AppCompatActivity implements UIActivity, SensorEventListener, OnInitListener{

    private DistanceEstimator de;
    private MainActivity ma;
    private Buttons bs;
    private CalculatePath cp;


    {
        de = new DistanceEstimator(this);
        ma = new MainActivity();
        bs = new Buttons();
        cp = new CalculatePath();

    }


    public void path1UI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 0:
//                            if (Math.abs(previousPoint) <= 1) {
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            cp.previousPoint = 0;
                            System.out.println("sono nel punto (5,0) \n");
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
//                            }
                            break;
                        case 1:
//                            if (Math.abs(previousPoint-1) <= 1) {
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            cp.previousPoint = 1;
                            System.out.println("sono nel punto (5,1) \n");
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
//                            }
                            break;
                    }break;
                case 4:
                    if ((int) cp.prevPosition.getLongitude() == 1) {
//                        if (Math.abs(previousPoint-2) <= 1) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(2);
                        cp.previousPoint = 2;
                        System.out.println("sono nel punto (4,1) \n");
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;
//                        }
                    }
                    break;
                case 3:
                    if ((int) cp.prevPosition.getLongitude() == 1) {
//                        if (Math.abs(previousPoint-3) <= 1) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(3);
                        cp.previousPoint = 3;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;
//                        }
                    }
                    break;
                case 2:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
//                            if (Math.abs(previousPoint-4) <= 1) {
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            cp.previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
//                            }
                            break;
                        case 2:
//                            if (Math.abs(previousPoint-5) <= 1) {
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            cp.previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
//                            }
                            break;
                        case 3:
//                            if (Math.abs(previousPoint-6) <= 1) {
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            cp.previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
//                            }
                            break;
                        case 4:
//                            if (Math.abs(previousPoint-7) <= 1) {
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            cp.previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            //   }
                            break;
                        case 5:
//                            if (Math.abs(previousPoint-8) <= 1) {
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            cp.previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            //      }
                            break;
                        case 6:
//                            if (Math.abs(previousPoint-9) <= 1) {
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            cp.previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            //   }
                            break;
                    }break;
                default:break;

            } //if (!found) {
            //counter++;
            if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");

            }
//            }
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }
    }

    public void path2UI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 0:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            cp.previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            cp.previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            cp.previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            cp.previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            cp.previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    if ((int) cp.prevPosition.getLongitude() == 4) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(5);
                        cp.previousPoint = 5;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) cp.prevPosition.getLongitude() == 4) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(6);
                        cp.previousPoint = 6;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 2:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            cp.previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            cp.previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            cp.previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;

            }//if (!found) {
            //counter++;
            if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
            //}
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }

    }

    public void path3UI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 0:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            cp.previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            cp.previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            cp.previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            cp.previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            cp.previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            cp.previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            cp.previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            cp.previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    if ((int) cp.prevPosition.getLongitude() == 7) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(8);
                        cp.previousPoint = 8;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) cp.prevPosition.getLongitude() == 7) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(9);
                        cp.previousPoint = 9;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                default:break;
                //if (!found) {
                //counter++;

                // }
            }if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }

    }

    public void pathToDeskUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 0:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            cp.previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            cp.previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            cp.previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(12);
                            cp.previousPoint = 12;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 3:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            cp.previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(11);
                            cp.previousPoint = 11;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 2:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            cp.previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            cp.previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            cp.previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            cp.previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            cp.previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            cp.previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(10);
                            cp.previousPoint = 10;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;
                //if (!found) {
                //      counter++;

                //    }
            }if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }
    }

    public void path2ToDeskUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
        //       found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 0:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            cp.previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            cp.previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            cp.previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            cp.previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(10);
                            cp.previousPoint = 10;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 3:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            cp.previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            cp.previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            cp.previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            cp.previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            cp.previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            cp.previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;
                //if (!found) {
                //      counter++;

                //    }
            }if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }
    }

    public void path1WindowUI(ArrayList<Button> usedButtons) {


        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    if ((int) cp.prevPosition.getLongitude() == 1) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(9);
                        cp.previousPoint = 9;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 4:
                    if ((int) cp.prevPosition.getLongitude() == 1) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(8);
                        cp.previousPoint = 8;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) cp.prevPosition.getLongitude() == 1) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(7);
                        cp.previousPoint = 7;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 2:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            cp.previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            cp.previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            cp.previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            cp.previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            cp.previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            cp.previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            cp.previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;
                //if (!found) {
                //counter++;

                //}
            }if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }

    }

    public void path2WindowUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
        //       found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            cp.previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            cp.previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            cp.previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            cp.previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    if ((int) cp.prevPosition.getLongitude() == 4) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(5);
                        cp.previousPoint = 5;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) cp.prevPosition.getLongitude() == 4) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(4);
                        cp.previousPoint = 4;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 2:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            cp.previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            cp.previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            cp.previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            cp.previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;

                    }break;
                default:break;
                //if (!found) {
                // counter++;

                // }
            } if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }
    }

    public void path3WindowUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            cp.previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            cp.previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            cp.previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            cp.previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            cp.previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            cp.previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            cp.previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    if ((int) cp.prevPosition.getLongitude() == 7) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(2);
                        cp.previousPoint = 2;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 3:
                    if ((int) cp.prevPosition.getLongitude() == 7) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(1);
                        cp.previousPoint = 1;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                case 2:
                    if ((int) cp.prevPosition.getLongitude() == 7) {
                        bs.clearButtons(usedButtons);
                        button = usedButtons.get(0);
                        cp.previousPoint = 0;
                        button.setBackgroundColor(Color.GREEN);
                        //found = true;
                        //counter = 0;

                    }break;
                default:break;
                //if (!found) {
                //counter++;

                //}
            }if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }

    }

    public void pathFromDeskUI(ArrayList<Button> usedButtons) {

        System.out.println("inizio calcolo...da (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");

        System.out.println("posizione aggiornata a (" + cp.prevPosition.getLatitude() + "," + cp.prevPosition.getLongitude() + ") \n");
//        found = false;
        Button button;
        if (cp.prevPosition.getLatitude() != cp.destination.getLatitude() || cp.prevPosition.getLongitude() != cp.destination.getLongitude()){
            switch ((int) cp.prevPosition.getLatitude()) {
                case 5:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(12);
                            cp.previousPoint = 12;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(0);
                            cp.previousPoint = 0;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 4:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(11);
                            cp.previousPoint = 11;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(1);
                            cp.previousPoint = 1;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 3:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(10);
                            cp.previousPoint = 10;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(2);
                            cp.previousPoint = 2;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                case 2:
                    switch ((int) cp.prevPosition.getLongitude()) {
                        case 1:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(9);
                            cp.previousPoint = 9;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 2:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(8);
                            cp.previousPoint = 8;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 3:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(7);
                            cp.previousPoint = 7;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 4:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(6);
                            cp.previousPoint = 6;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 5:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(5);
                            cp.previousPoint = 5;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 6:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(4);
                            cp.previousPoint = 4;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                        case 7:
                            bs.clearButtons(usedButtons);
                            button = usedButtons.get(3);
                            cp.previousPoint = 3;
                            button.setBackgroundColor(Color.GREEN);
                            //found = true;
                            //counter = 0;
                            break;
                    }break;
                default:break;
                //if (!found) {
                //counter++;

                //}
            }if (cp.counter == 4) {
                Toast.makeText(getBaseContext(), "Ti sei perso, torna indietro!", Toast.LENGTH_SHORT).show();
                System.out.println("Ti sei perso, torna indietro!");
            }
        }else {
            usedButtons.removeAll(usedButtons);
            ma.stopCalculation();
            bs.endJourney();
        }
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
