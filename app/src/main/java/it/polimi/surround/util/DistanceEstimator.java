package it.polimi.surround.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.text.format.DateFormat;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3DFormat;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polimi.surround.MainActivity;
import it.polimi.surround.R;
import it.polimi.surround.UIActivity;

/**
 * Created by seadev on 2/4/16.
 */
public class DistanceEstimator {

    UIActivity ui;
    private Map<String, Double> rssiByBeacon;

    private BufferedWriter bw;
    private Vector1D accX;
    private Vector1D accY;


    public DistanceEstimator(UIActivity ui){
        this.ui = ui;
        rssiByBeacon = new HashMap<String, Double>();
    }

    public Location estimateLocation(List<Beacon> beacons, Location deviceLoc, Location prevPosition,
                                                BufferedWriter dbgbw, float[] deviceData, float[] accData) throws IOException {

        bw = dbgbw;
        //set the number of visible beacons
        int beaconDimension = 1;
        double bestSignal = -100;
        double secondSignal = -100;
        double lastSignal = -100;
        //cancelling noise on y acceleration
        accData[1] = accData[1] - (float) 0.44;
        //cancelling gravity
        accData[0] = accData[0] - (float) (9.81*Math.sin(deviceData[2]));
        accData[1] = accData[1] - (float) (9.81*Math.sin(deviceData[1]));
        accData[2] = accData[2] - (float) (9.81*Math.cos(deviceData[2])*Math.cos(deviceData[1]));
        bw.write("orientation: yawn= "+deviceData[0]+", pitch= "+deviceData[1]+", roll= "+ deviceData[2]+"\n");
        bw.write("accX= "+accData[0]+", accY= "+accData[1]+", accZ= "+accData[2]+"\n");
        accX = new Vector1D((accData[0]*Math.cos(deviceData[2]))*Math.cos(deviceData[0]) +
                (accData[1]*Math.cos(deviceData[1])*Math.sin(deviceData[0])) +
                        (accData[2]*Math.cos(deviceData[1]))*Math.sin(deviceData[2]));
        accY = new Vector1D((accData[0]*Math.cos(deviceData[2]))*Math.sin(deviceData[0]) +
                (accData[1]*Math.cos(deviceData[1])*Math.cos(deviceData[0])) +
                (accData[2]*Math.cos(deviceData[2]))*Math.sin(deviceData[1]));


        if(beacons.size() < 3) {
            return null;
        }
        else if (beacons.size() == 4){
           beaconDimension = 4;
        }
        else if (beacons.size() == 6){
            beaconDimension = 6;
        }
        //beacon positioning data
        double [][] positions = new double [beaconDimension][2];
        double [] distances = new double[beaconDimension];
        double [] signals = new double[beaconDimension];
        double [] bestSignalBeacons = new double[3];
        int [] previousPosition = new int[2];
        String rssi = "";
        String dist = "";
        //device orientation data (radiant, positive counter-clockwise)

        //get previous position from main activity
        previousPosition[0] = (int) prevPosition.getLatitude();
        previousPosition [1] = (int) prevPosition.getLongitude();
        for(int i = 0; i < beaconDimension; i++){
            Beacon b = beacons.get(i);
            double [] bData = MainActivity.LOCATION_BY_BEACONS.get(b.getMajor() + ":" + b.getMinor());
            b = createBeacon(b, bData, deviceLoc);
            double [] p  = getLatLng(bData);
            Double d = calculate2DProjection(Utils.computeAccuracy(b), bData);
            positions[i] = p;
            distances[i] = d;
            if (beaconDimension == 4){
                //4 beacon topology
                switch (b.getMajor()){
                    case 26943: signals[0] = b.getRssi();
                        break;
                    case 34061: signals[1] = b.getRssi();
                        break;
                    case 42730: signals[2] = b.getRssi();
                        break;
                    case 48147: signals[3] = b.getRssi();
                        break;
                }
                //ordering beacon signals
                if (b.getRssi() > bestSignal){
                    //update signals

                }
            }else if (beaconDimension == 6) {
                //6 beacon topology
                switch (b.getMajor()) {
                    case 26943:
                        signals[0] = b.getRssi();
                        break;
                    case 29491:
                        signals[1] = b.getRssi();
                        break;
                    case 32505:
                        signals[2] = b.getRssi();
                        break;
                    case 34061:
                        signals[3] = b.getRssi();
                        break;
                    case 42730:
                        signals[4] = b.getRssi();
                        break;
                    case 48147:
                        signals[5] = b.getRssi();
                        break;
                }
            }
            //signals[i] = b.getRssi();
             rssi += String.format("%d: %d ", b.getMajor(), b.getRssi());
            dist += String.format("%d: %f ", b.getMajor(), d);
        }

        ui.printText(R.id.rssi_val,
                rssi + "\n" +
                dist
        );


/*
        return getLocationWithNonLinearTrilateration(
                positions,
                distances
        );

        return getLocationWithTrilateration(double [] dA, double [] dB, double [] dC, double distanceA, double distanceB, double distanceC)
*/
        try {
            bw.write("Starting positioning with signals @ "+
                    DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n" +
                    "oriented accX = "+ accX.getX()+", " + "oriented accY = "+ accY.getX()+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getGridLocation(signals, previousPosition, accX, accY);
    }

    private double [] getLatLng(double [] p){
        return new double [] {p[0], p[1]};
    }

    private double calculate2DProjection(double d, double[] p) {
        double lat = p[0];
        double lng = p[1];
        double alt = p[2];
        return Math.sqrt(Math.pow(d, 2) - Math.pow(alt, 2));
        //double ang = Math.acos(alt / d);
        //double projectedD = Math.sin(ang) * d;
        //return projectedD;
    }


    //UUID proximityUUID, MacAddress macAddress, int major, int minor, int measuredPower, int rssi
    private Beacon createBeacon(Beacon beacon, double [] bData, Location deviceLoc) {
        double lat = bData[0];
        double lng = bData[1];
        double alt = bData[2];
        double mPower = bData[3];
        double aLoss = bData[4];
        double cPower = mPower + aLoss * calculateAngleFactor(bData, deviceLoc);
        return new Beacon(beacon.getProximityUUID(), beacon.getMacAddress(), beacon.getMajor(), beacon.getMinor(), (int)cPower, beacon.getRssi());
    }

    private double calculateAngleFactor(double [] position, Location devLocation){

        double lat = position[0];
        double lng = position[1];
        double devLat = devLocation.getLatitude();
        double devLng = devLocation.getLongitude();
        double angleFactor = Math.abs(lat - devLat) / Math.abs(lng - devLng);
        if (angleFactor == Double.POSITIVE_INFINITY)//90º
            return 0;
        else if (angleFactor == Double.NaN)//ON TOP
            return 0;
        else if(angleFactor == 0)//0º
            return 1;
        else // 0 < a < 90º
            return (1 - (Math.atan(angleFactor) / (Math.PI/2)));
    }

    private int updatedRssi(Beacon beacon){
        int major = beacon.getMajor();
        int minor = beacon.getMinor();
        int rssi = beacon.getRssi();
        if(rssiByBeacon.containsKey(major + ":" + minor))
            rssiByBeacon.put(major + ":" + minor, filteredRssi(rssi, rssiByBeacon.get(major + ":" + minor)));
        else
            rssiByBeacon.put(major + ":" + minor, rssi + 0.0);
        return (int) Math.round(rssiByBeacon.get(major + ":" + minor));
    }

    private double filteredRssi(double rssi, double rollingRssi){
        double kFilteringFactor = 0.1;
        return (rssi * kFilteringFactor) + (rollingRssi * (1.0 - kFilteringFactor));
    }

    private double calculateAccuracy(int txPower, double rssi) {
        //txPower += 8 ;
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    private Location getLocationWithNonLinearTrilateration(double[][] positions, double [] distances){

        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        // the answer
        double[] calculatedPosition = optimum.getPoint().toArray();

        // error and geometry information
        RealVector standardDeviation = optimum.getSigma(0);
        RealMatrix covarianceMatrix = optimum.getCovariances(0);

        Location foundLocation = new Location("Location");
        foundLocation.setLatitude(calculatedPosition[0]);
        foundLocation.setLongitude(calculatedPosition[1]);
        return foundLocation;
    }

    private Location getLocationWithTrilateration(double [] dA, double [] dB, double [] dC, double distanceA, double distanceB, double distanceC){

        double bAlat = dA[0];
        double bAlong = dA[1];
        double bBlat = dB[0];
        double bBlong = dB[1];
        double bClat = dC[0];
        double bClong = dC[1];

        double W, Z, foundBeaconLat, foundBeaconLong, foundBeaconLongFilter;
        W = distanceA * distanceA - distanceB * distanceB - bAlat * bAlat - bAlong * bAlong + bBlat * bBlat + bBlong * bBlong;
        Z = distanceB * distanceB - distanceC * distanceC - bBlat * bBlat - bBlong * bBlong + bClat * bClat + bClong * bClong;

        foundBeaconLat = (W * (bClong - bBlong) - Z * (bBlong - bAlong)) / (2 * ((bBlat - bAlat) * (bClong - bBlong) - (bClat - bBlat) * (bBlong - bAlong)));
        foundBeaconLong = (W - 2 * foundBeaconLat * (bBlat - bAlat)) / (2 * (bBlong - bAlong));
        //`foundBeaconLongFilter` is a second measure of `foundBeaconLong` to mitigate errors
        foundBeaconLongFilter = (Z - 2 * foundBeaconLat * (bClat - bBlat)) / (2 * (bClong - bBlong));

        foundBeaconLong = (foundBeaconLong + foundBeaconLongFilter) / 2;

        Location foundLocation = new Location("Location");
        foundLocation.setLatitude(foundBeaconLat);
        foundLocation.setLongitude(foundBeaconLong);

        return foundLocation;
    }

    private Location getGridLocation(double [] signals,  int [] prevPosition, Vector1D accX, Vector1D accY) throws IOException {
        Location foundLocation = new Location("location");
        double [][] map = createMapMatrix();
        int [] pos;
        try {
            bw.write("previous position: ("+ prevPosition[0] + ", "+ prevPosition[1] +")\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (prevPosition[0]){
            case 0: switch (prevPosition[1]){
                            case 0: pos = scanVectorLocation(prevPosition[0], prevPosition[1], map, signals);
                                    break;
                            case 8: pos = scanVectorLocation(prevPosition[0], prevPosition[1]-1, map, signals);
                                    break;
                            default: if (accY.getX() >= 0){ /* on left border */
                                        //going down
                                        pos = scanVectorLocation(prevPosition[0], prevPosition[1]-1, map, signals);
                                     }else{
                                        //going up
                                        pos = scanVectorLocation(prevPosition[0], prevPosition[1], map, signals);
                                     }
                                     break;
                    }break;

            case 7: switch (prevPosition[1]){
                        case 0: pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1], map, signals);
                                break;
                        case 8: pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals);
                                break;
                        default:if (accY.getX() >= 0){ /* on right border */
                                    //going down
                                    pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals);
                                }else{
                                    //going up
                                    pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1], map, signals);
                                }
                                break;
                    }break;

            default: switch (prevPosition[1]){
                        case 0: if (accX.getX() >= 0){ /* on bottom border */
                                    //going left
                                    pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1], map, signals);
                                }else{
                                    //going right
                                    pos = scanVectorLocation(prevPosition[0], prevPosition[1], map, signals);
                                }
                                break;
                        case 8: if (accX.getX() >= 0){ /* on top border */
                                    //going left
                                    pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals);
                                }else{
                                  //going right
                                  pos = scanVectorLocation(prevPosition[0], prevPosition[1]-1, map, signals);
                                 }
                                break;
                        default:if (accX.getX() >= 0){ /* on bottom border */
                                    //going left
                                    if (accY.getX() >= 0){
                                        //going down
                                        pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals);
                                    }else{
                                        //going up
                                        pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1], map, signals);
                                    }
                                }else{
                                    //going right
                                    if (accY.getX() >= 0){
                                        //going down
                                        pos = scanVectorLocation(prevPosition[0], prevPosition[1]-1, map, signals);
                                    }else{
                                        //going up
                                        pos = scanVectorLocation(prevPosition[0], prevPosition[1], map, signals);
                                    }
                                }
                                break;
                    }break;
        }
        foundLocation.setLatitude(pos[0]);
        foundLocation.setLongitude(pos[1]);
        bw.write("final position: ("+ (int)foundLocation.getLatitude() + ", "+ (int)foundLocation.getLongitude()+") \n");
        bw.flush();
        return foundLocation;
    }

    private double [][] createMapMatrix(){
        double [][] pointsMap = new double [] []{
                {-86.02,    -88.55, -85.45, -91.22},
                {-85.14,    -87.02, -83.86, -85.97},
                {-88.25,	-87.92, -87.62,	-83.72},
                {-83.53,	-74.84,	-88.43,	-85.11},
                {-79.31,	-76.94,	-92.43,	-95.65},
                {-91.11,	-82.67,	-94.38,	-86.63},
                {-91.09,	-89.32,	-87.74,	-93.15},
                {-91.34,	-85.09,	-94.13,	-85.86},
                {-91.02,	-92.43,	-88.18,	-90.55},
                {-89.35,	-93.29,	-86.48,	-85.24},
                {-88.00,	-80.52,	-88.88,	-90.57},
                {-86.21,	-89.15,	-88.36,	-94.22},
                {-79.94,	-71.99,	-86.06,	-90.64},
                {-87.26,	-81.44,	-85.76,	-93.25},
                {-85.96,	-87.83,	-90.42,	-89.1},
                {-92.82,	-84.47,	-92.88,	-88.45},
                {-87.35,	-88.99,	-80.17,	-90.76},
                {-92.44,	-85.65,	-86.9,	-86.34},
                {-94.92,	-87.15,	-88.01,	-83.31},
                {-90.4,	    -87.48,	-83.6,	-87},
                {-79.26,	-81.17,	-88.35,	-90.4},
                {-81.15,	-92.68,	-88.17,	-88.36},
                {-84.86,	-88.08,	-92.4,	-87.94},
                {-92.34,    -87.32,	-86.86,	-88.15},
                {-88.37,	-83.55,	-76.1,	-91.17},
                {-93.03,	-82.07,	-86.14,	-86.56},
                {-87.01,	-91.99,	-89.26,	-88.43},
                {-86.55,	-81.88,	-84.09,	-92.06},
                {-79.6, 	-83.96,	-86.09,	-82.19},
                {-90.33,	-82.58, -85.69,	-89.89},
                {-78.65,	-87.78,	-85.96,	-81.45},
                {-84.59,	-91.02,	-89.92,	-93.22},
                {-84.66,	-89.41,	-64.8,	-81.3},
                {-86.58,	-85.92,	-81.7,	-90.92},
                {-87.19,	-91.74,	-87.24,	-92.32},
                {-89.27,	-85.79,	-86.37,	-90.35},
                {-90.83,	-80.11,	-91.42,	-83.13},
                {-80.21,	-85.78,	-92.94,	-85.36},
                {-75.23,	-84.43,	-87.5,	-86.42},
                {-65.89,	-91.98,	-86.12, -88.94},
                {-90.94,	-89.32,	-77.29,	-84.36},
                {-93.53,	-88.01,	-90.72,	-87.58},
                {-83.07,	-88.94,	-92.1,	-91.96},
                {-86.31,	-86.88,	-88.05,	-90.36},
                {-91.27,	-83.8,	-93.16,	-90.19},
                {-83.05,	-85.39,	-84.62,	-86.97},
                {-83.68,	-85.54,	-83.3,	-91.01},
                {-86.52,	-92.49,	-93.41,	-90.64},
                {-93.75,	-86.22,	-78.07,	-82.43},
                {-93.93,	-91.41,	-90.57,	-87.58},
                {-87.82,	-87.7,	-88.14,	-83.09},
                {-89.3, 	-92.24,	-88.36,	-93.77},
                {-84.27,	-91.06,	-88.72,	-85.63},
                {-87.7, 	-87.27,	-93.74,	-85.75},
                {-83.66,	-87.55,	-86.04,	-89.78},
                {-88.33,	-82.28,	-85.76,	-87.6},
                {-86.81,	-87.2,  -83.26,	-93.12},
                {-84.83,	-83.69,	-85.12,	-92.56},
                {-89.49,	-86.87,	-83.31,	-85.87},
                {-94.99,	-93.01,	-86.12,	-76.59},
                {-87.15,	-93.19,	-91.16,	-85.97},
                {-80.14,	-87.28,	-90.42,	-89.31},
                {-83.12,	-87.2,	-86.32,	-87.64},
                {-85.55,	-86.66,	-87.3,	-92.43},
                {-88.1, 	-95.67,	-90.39,	-84.06},
                {-89.12,	-84.97,	-83.93,	-88.25},
                {-80.83,	-89.5,	-82.2,	-87.6},
                {-92.04,	-89.2,	-88.37,	-78.15},
                {-85.67,	-85.61,	-87.6,	-83.66},
                {-89.53,	-85.82,	-90.74,	-87.84},
                {-86.8,	    -93.55,	-82.3,	-90.83},
                {-91.73,	-87.94,	-91.39,	-86.5}
        };

        return pointsMap;
    }

    private int [] scanInternalLocation(int x, int y, double [][] map, double[] signals) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
        //weight on beacons
        for (int k = 0; k<4; k++) {
            if (bestbeacon < signals[k]){
                bestbeacon = signals[k];
                bestIndex = k;
            }
        }
        //row coordinate
        for (int i = 0; i<3; i++ ){
            //column coordinate
            for (int j = 0; j<3; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<4; b++){
                //    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                //    bw.write("mapIndex = " + mapIndex + "\n");
                //    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
                    if (b == bestIndex){
                        tempSignal += 0.7 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else{
                        tempSignal += 0.1 * Math.abs(signals[b] - map[mapIndex][b]);
                    }
                //    bw.write("signal = "+ tempSignal + "\n");
                }
                //update best signal
                if (i== 0 && j==0){
                    bestSignal = tempSignal;
                    pos[0] = x;
                    pos[1] = y;
                }
                else if(tempSignal < bestSignal){
                    bestSignal = tempSignal;
                    pos[0] = x+i;
                    pos[1] = y+j;
                }
                tempSignal = 0;
                //bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }

    private int [] scanBorderLRLocation(int x, int y, double [][] map, double[] signals) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
        //weight on beacons
        for (int k = 0; k<4; k++) {
            if (bestbeacon < signals[k]){
                bestbeacon = signals[k];
                bestIndex = k;
            }
        }
        //row coordinate
        for (int i = 0; i<2; i++ ){
            //column coordinate
            for (int j = 0; j<3; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<4; b++){
                //    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                //    bw.write("mapIndex = " + mapIndex + "\n");
                //    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
                    if (b == bestIndex){
                        tempSignal += 0.7 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else{
                        tempSignal += 0.1 * Math.abs(signals[b] - map[mapIndex][b]);
                    }
                //    bw.write("signal = "+ tempSignal + "\n");
                }
                //update best signal
                if (i== 0 && j==0){
                    bestSignal = tempSignal;
                    pos[0] = x;
                    pos[1] = y;
                }
                else if(tempSignal < bestSignal){
                    bestSignal = tempSignal;
                    pos[0] = x+i;
                    pos[1] = y+j;
                }
                tempSignal = 0;
                //bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }

    private int [] scanBorderUDLocation(int x, int y, double [][] map, double[] signals) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
        //weight on beacons
        for (int k = 0; k<4; k++) {
            if (bestbeacon < signals[k]){
                bestbeacon = signals[k];
                bestIndex = k;
            }
        }
        //row coordinate
        for (int i = 0; i<3; i++ ){
            //column coordinate
            for (int j = 0; j<2; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<4; b++){
                //    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                //    bw.write("mapIndex = " + mapIndex + "\n");
                //    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
                    if (b == bestIndex){
                        tempSignal += 0.7 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else{
                        tempSignal += 0.1 * Math.abs(signals[b] - map[mapIndex][b]);
                    }
                //    bw.write("signal = "+ tempSignal + "\n");
                }
                //update best signal
                if (i== 0 && j==0){
                    bestSignal = tempSignal;
                    pos[0] = x;
                    pos[1] = y;
                }
                else if(tempSignal < bestSignal){
                    bestSignal = tempSignal;
                    pos[0] = x+i;
                    pos[1] = y+j;
                }
                tempSignal = 0;
                //bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }

    private int [] scanCornerLocation(int x, int y, double [][] map, double[] signals) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
        //weight on beacons
        for (int k = 0; k<4; k++) {
            if (bestbeacon < signals[k]){
                bestbeacon = signals[k];
                bestIndex = k;
            }
        }
        //row coordinate
        for (int i = 0; i<2; i++ ){
            //column coordinate
            for (int j = 0; j<2; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<4; b++){
                //    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                //    bw.write("mapIndex = " + mapIndex + "\n");
                //    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
                    if (b == bestIndex){
                        tempSignal += 0.7 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else{
                        tempSignal += 0.1 * Math.abs(signals[b] - map[mapIndex][b]);
                    }
                //    bw.write("signal = "+ tempSignal + "\n");
                }
                //update best signal
                if (i== 0 && j==0){
                    bestSignal = tempSignal;
                    pos[0] = x;
                    pos[1] = y;
                }
                else if(tempSignal < bestSignal){
                    bestSignal = tempSignal;
                    pos[0] = x+i;
                    pos[1] = y+j;
                }
                tempSignal = 0;
                //bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }

    private int [] scanVectorLocation(int x, int y, double [][] map, double[] signals) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
/*        //weight on beacons
        for (int k = 0; k<4; k++) {
            if (bestbeacon < signals[k]){
                bestbeacon = signals[k];
                bestIndex = k;
            }
        }
*/        //row coordinate
        for (int i = 0; i<2; i++ ){
            //column coordinate
            for (int j = 0; j<2; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<4; b++){
                    //    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                    //    bw.write("mapIndex = " + mapIndex + "\n");
                    //    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
/*                    if (b == bestIndex){
                        tempSignal += 0.7 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else{
                        tempSignal += 0.1 * Math.abs(signals[b] - map[mapIndex][b]);
                    }
 */                   //    bw.write("signal = "+ tempSignal + "\n");
                    tempSignal += (Math.abs(signals[b] - map[mapIndex][b]));
                }
                //update best signal
                if (i== 0 && j==0){
                    bestSignal = tempSignal;
                    pos[0] = x;
                    pos[1] = y;
                }
                else if(tempSignal < bestSignal){
                    bestSignal = tempSignal;
                    pos[0] = x+i;
                    pos[1] = y+j;
                }
                tempSignal = 0;
                //bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }
}
