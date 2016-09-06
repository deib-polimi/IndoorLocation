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
public class CalculatePath extends AppCompatActivity implements UIActivity, SensorEventListener, OnInitListener{

    private boolean start = false;
    public int previousPoint = 0;
    private DistanceEstimator de;
    private MainActivity ma;
    private Buttons bs;
    private Location deviceLocation;
    public int counter;
    public Location prevPosition;
    public Location destination;

    {

        de = new DistanceEstimator(this);
        //ma = new MainActivity();
        //bs = new Buttons();
        deviceLocation = new Location("location");
        deviceLocation.setLatitude(0);
        deviceLocation.setLongitude(0);
    }

    public void calculatePath1(List<Beacon> beacons) {

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1) {
                    counter = 0;
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 3: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 9: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
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

    public void calculatePath2(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1) {
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 3: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 9: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
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

    public void calculatePath3(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1) {
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 3: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 5: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 6: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 9: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
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

    public void calculatePathToDesk(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1) {
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 3: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 9: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }
            case 10: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 11: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 12: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
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

    public void calculatePathToDesk2(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1) {
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 2: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 1){
                    counter = 0;
                }else if (distance == 2){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 3 || distance == 0){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 2){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else{
                    distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                    if (distance == 2) {
                        counter = 0;
                    }else {
                        counter++;
                    }
                }break;
            case 4: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 2){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else {
                    distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                    if (distance == 2) {
                        counter = 0;
                    }else {
                        counter++;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                } else {
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 2){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else {
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 2){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else {
                    distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                    if (distance == 2) {
                        counter = 0;
                    }else {
                        counter++;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else{
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else {
                    distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                    if (distance == 0 || distance == 3) {
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 10: distance = de.distanceFromBeacon(1, beacons, deviceLocation);
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

    public void calculatePath1Window(List<Beacon> beacons){
        int distance;

        switch (previousPoint){

            case 0: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 2: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
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

    public void calculatePath2Window(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 2: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
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

    public void calculatePath3Window(List<Beacon> beacons){

        int distance;

        switch (previousPoint){
            case 0: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 2: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(3, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
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

    public void calculatePathFromDesk(List<Beacon> beacons){

        int distance;

        switch (previousPoint){

            case 0: distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    if (!start){
                        ma.speakWords("Sei al punto di partenza, avanza");
                        start = true;
                    }
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 1: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(1, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 2: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(7);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 3: distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(6);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 4: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(5);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(0, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 5: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(4);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 6: distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(3);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 7: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(2);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(2, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 8: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(2);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a sinistra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 9: distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                if (distance == 1){
                    prevPosition.setLatitude(3);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 0){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 10: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    counter = 0;
                }else if (distance == 1){
                    prevPosition.setLatitude(4);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Continua dritto");
                    counter = 0;
                }else if (distance == 2){
                    counter = 0;
                }else if (distance == 3){
                    distance = de.distanceFromBeacon(4, beacons, deviceLocation );
                    if (distance == 0 || distance == 3){
                        counter++;
                    }else {
                        counter = 0;
                    }
                }break;
            case 11: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
                if (distance == 0){
                    prevPosition.setLatitude(5);
                    prevPosition.setLongitude(1);
                    ma.speakWords("Gira a destra");
                    counter = 0;
                }else if (distance == 1){
                    counter = 0;
                }else if (distance > 1){
                    counter++;
                }break;
            case 12: distance = de.distanceFromBeacon(5, beacons, deviceLocation );
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

    @Override
    public void onInit(int initStatus) {

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

    public void passCallerActivity(MainActivity maExt, Buttons bsExt){
        this.ma = maExt;
        this.bs = bsExt;
    }


}
