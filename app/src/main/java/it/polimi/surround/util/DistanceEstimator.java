package it.polimi.surround.util;

import android.location.Location;
import android.text.format.DateFormat;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
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

    //variables to use wieghted sum (if needed)
    private final double weight1 = 0.7;
    private final double weight2 = 0.2;
    private final double weight3 = 0.1;


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
        int bestIndex = 0;
        double secondSignal = -100;
        int secondIndex = 1;
        double lastSignal = -100;
        int lastIndex = 2;
        //cancelling noise on y acceleration
        accData[1] = accData[1] - (float) 0.44;
        //cancelling gravity
        accData[0] = accData[0] - (float) (9.81*Math.sin(deviceData[2]));
        accData[1] = accData[1] - (float) (9.81*Math.sin(deviceData[1]));
        accData[2] = accData[2] - (float) (9.81*Math.cos(deviceData[2])*Math.cos(deviceData[1]));
        //bw.write("orientation: yawn= "+deviceData[0]+", pitch= "+deviceData[1]+", roll= "+ deviceData[2]+"\n");
        //bw.write("accX= "+accData[0]+", accY= "+accData[1]+", accZ= "+accData[2]+"\n");
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
        double [] selectedSignals = new double[3];
        int [] selectedIndexes = new int[3];
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
                System.out.println("found "+ beaconDimension + " beacons \n");
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
            }else if (beaconDimension == 6) {
                System.out.println("found "+ beaconDimension + " beacons \n");
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
        //scan beacons to get best 3 beacons for trilateration
        for (int k = 0; k < beaconDimension; k++){
            //if is over a beacon, the signal is strong
            //return beacon's position
        /*    if (signals[k] > (double) -63 ){
                bw.write("Starting positioning with signals @ "+
                        DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n");
                bw.write("from position: "+previousPosition[0]+", "+previousPosition[1]+"\n");
                switch (k) {
                    case 0:
                        prevPosition.setLatitude((double) 2);
                        prevPosition.setLongitude((double) 7);
                        bw.write("Strong signal: final position over beacon ("+
                                (int)prevPosition.getLatitude() + ", "
                                + (int)prevPosition.getLongitude()+") \n");
                        bw.flush();
                        return prevPosition;

                    case 1:
                        prevPosition.setLatitude((double) 5);
                        prevPosition.setLongitude((double) 7);
                        bw.write("Strong signal: final position over beacon ("+
                                (int)prevPosition.getLatitude() + ", "
                                + (int)prevPosition.getLongitude()+") \n");
                        bw.flush();
                        return prevPosition;

                    case 2:
                        prevPosition.setLatitude((double) 2);
                        prevPosition.setLongitude((double) 4);
                        bw.write("Strong signal: final position over beacon ("+
                                (int)prevPosition.getLatitude() + ", "
                                + (int)prevPosition.getLongitude()+") \n");
                        bw.flush();
                        return prevPosition;

                    case 3:
                        prevPosition.setLatitude((double) 5);
                        prevPosition.setLongitude((double) 4);
                        bw.write("Strong signal: final position over beacon ("+
                                (int)prevPosition.getLatitude() + ", "
                                + (int)prevPosition.getLongitude()+") \n");
                        bw.flush();
                        return prevPosition;

                    case 4:
                        prevPosition.setLatitude((double) 2);
                        prevPosition.setLongitude((double) 1);
                        bw.write("Strong signal: final position over beacon ("+
                                (int)prevPosition.getLatitude() + ", "
                                + (int)prevPosition.getLongitude()+") \n");
                        bw.flush();
                        return prevPosition;

                    case 5:
                        prevPosition.setLatitude((double) 5);
                        prevPosition.setLongitude((double) 1);
                        bw.write("Strong signal: final position over beacon ("+
                                (int)prevPosition.getLatitude() + ", "
                                + (int)prevPosition.getLongitude()+") \n");
                        bw.flush();
                        return prevPosition;

                }
            }
       */   if (signals[k] > bestSignal){
                lastSignal = secondSignal;
                lastIndex = secondIndex;
                secondSignal = bestSignal;
                secondIndex = bestIndex;
                bestSignal = signals[k];
                bestIndex = k;

            }else if (signals[k] > secondSignal){
                lastSignal = secondSignal;
                lastIndex = secondIndex;
                secondSignal = signals[k];
                secondIndex = k;

            }else if (signals[k] > lastSignal){
                lastSignal = signals[k];
                lastIndex = k;
            }
        }
        selectedSignals[0] = signals[bestIndex];
        bw.write("bestIndex = "+ bestIndex+", bestSignal = "+ bestSignal + "\n");
        selectedSignals[1] = signals[secondIndex];
        bw.write("secondIndex = "+ secondIndex+", secondSignal = "+ secondSignal + "\n");
        selectedSignals[2] = signals[lastIndex];
        bw.write("lastIndex = "+ lastIndex+ ", lastSignal = "+ lastSignal + "\n");
        selectedIndexes[0] = bestIndex;
        selectedIndexes[1] = secondIndex;
        selectedIndexes[2] = lastIndex;

        ui.printText(R.id.rssi_val,
                rssi + "\n" +
                dist
        );
/*
        return getLocationWithTrilateration(
                positions[bestIndex], positions[secondIndex], positions[lastIndex],
                distances[bestIndex], distances[secondIndex], distances[lastIndex]);


        return getLocationWithNonLinearTrilateration(
                positions,
                distances
        );

*/
        try {
            //bw.write("Starting positioning with signals @ "+
            //        DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n" +
            //        "oriented accX = "+ accX.getX()+", " + "oriented accY = "+ accY.getX()+"\n");
            bw.write("Starting positioning with signals @ "+
                    DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n");
            bw.write("from position: "+previousPosition[0]+", "+previousPosition[1]+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        return getGridLocation6BVector(signals, previousPosition, accX, accY, selectedIndexes);

        return getInductiveLocation(signals,previousPosition, beaconDimension, selectedIndexes);
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
        double [][] map = createMapMatrix4B();
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

    private Location getGridLocation6B(double [] signals, int [] prevPosition, int[] selectedIndexes) throws IOException {
        Location foundLocation = new Location("location");
        double [][] map = createMapMatrix6B();
        int [] pos;
        try {
            bw.write("previous position: ("+ prevPosition[0] + ", "+ prevPosition[1] +")\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (prevPosition[0]){
            case 0: switch (prevPosition[1]){
                case 0: pos = scanCornerLocation(prevPosition[0], prevPosition[1], map, signals, selectedIndexes);
                    break;
                case 8: pos = scanCornerLocation(prevPosition[0], prevPosition[1]-1, map, signals, selectedIndexes);
                    break;
                default: //if (accY.getX() >= 0){ /* on left border */
                    //going down
                    pos = scanBorderLRLocation(prevPosition[0], prevPosition[1]-1, map, signals, selectedIndexes);
                //}else{
                    //going up
                //    pos = scanVectorLocation(prevPosition[0], prevPosition[1], map, signals);
               // }
                    break;
            }break;

            case 7: switch (prevPosition[1]){
                case 0: pos = scanCornerLocation(prevPosition[0]-1, prevPosition[1], map, signals, selectedIndexes);
                    break;
                case 8: pos = scanCornerLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals, selectedIndexes);
                    break;
                default://if (accY.getX() >= 0){ /* on right border */
                    //going down
                    pos = scanBorderLRLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals, selectedIndexes);
                //}else{
                    //going up
                //    pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1], map, signals);
                //}
                    break;
            }break;

            default: switch (prevPosition[1]){
                case 0: //if (accX.getX() >= 0){ /* on bottom border */
                    //going left
                    pos = scanBorderUDLocation(prevPosition[0]-1, prevPosition[1], map, signals, selectedIndexes);
//                }else{
//                    //going right
//                    pos = scanVectorLocation(prevPosition[0], prevPosition[1], map, signals);
//                }
                    break;
                case 8: //if (accX.getX() >= 0){ /* on top border */
                    //going left
                    pos = scanBorderUDLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals, selectedIndexes);
//                }else{
//                    //going right
//                    pos = scanVectorLocation(prevPosition[0], prevPosition[1]-1, map, signals);
//                }
                    break;
                default: //if (accX.getX() >= 0){ /* on bottom border */
                    //going left
                    //if (accY.getX() >= 0){
                        //going down
                        pos = scanInternalLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals, selectedIndexes);
//                    }else{
//                        //going up
//                        pos = scanVectorLocation(prevPosition[0]-1, prevPosition[1], map, signals);
//                    }
//                }else{
//                    //going right
//                    if (accY.getX() >= 0){
//                        //going down
//                        pos = scanVectorLocation(prevPosition[0], prevPosition[1]-1, map, signals);
//                    }else{
//                        //going up
//                        pos = scanVectorLocation(prevPosition[0], prevPosition[1], map, signals);
//                    }
//                }
                    break;
            }break;
        }
        foundLocation.setLatitude(pos[0]);
        foundLocation.setLongitude(pos[1]);
        bw.write("final position: ("+ (int)foundLocation.getLatitude() + ", "+ (int)foundLocation.getLongitude()+") \n");
        bw.flush();
        return foundLocation;
    }

    private Location getGridLocation6BVector(double [] signals, int [] prevPosition, Vector1D accX, Vector1D accY, int[] selectedIndexes) throws IOException {
        Location foundLocation = new Location("location");
        double [][] map = createMapMatrix6B();
        int [] pos;
        try {
            bw.write("previous position: ("+ prevPosition[0] + ", "+ prevPosition[1] +")\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (prevPosition[0]){
            case 0: switch (prevPosition[1]){
                case 0: pos = scanVector6BLocation(prevPosition[0], prevPosition[1], map, signals, selectedIndexes);
                    break;
                case 8: pos = scanVector6BLocation(prevPosition[0], prevPosition[1]-1, map, signals, selectedIndexes);
                    break;
                default: if (accY.getX() >= 0){ /* on left border */
                    //going down
                    pos = scanVector6BLocation(prevPosition[0], prevPosition[1]-1, map, signals, selectedIndexes);
                }else{
                    //going up
                    pos = scanVector6BLocation(prevPosition[0], prevPosition[1], map, signals, selectedIndexes);
                }
                    break;
            }break;

            case 7: switch (prevPosition[1]){
                case 0: pos = scanVector6BLocation(prevPosition[0]-1, prevPosition[1], map, signals, selectedIndexes);
                    break;
                case 8: pos = scanVector6BLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals, selectedIndexes);
                    break;
                default:if (accY.getX() >= 0){ /* on right border */
                    //going down
                    pos = scanVector6BLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals, selectedIndexes);
                }else{
                    //going up
                    pos = scanVector6BLocation(prevPosition[0]-1, prevPosition[1], map, signals, selectedIndexes);
                }
                    break;
            }break;

            default: switch (prevPosition[1]){
                case 0: if (accX.getX() >= 0){ /* on bottom border */
                    //going left
                    pos = scanVector6BLocation(prevPosition[0]-1, prevPosition[1], map, signals, selectedIndexes);
                }else{
                    //going right
                    pos = scanVector6BLocation(prevPosition[0], prevPosition[1], map, signals, selectedIndexes);
                }
                    break;
                case 8: if (accX.getX() >= 0){ /* on top border */
                    //going left
                    pos = scanVector6BLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals, selectedIndexes);
                }else{
                    //going right
                    pos = scanVector6BLocation(prevPosition[0], prevPosition[1]-1, map, signals, selectedIndexes);
                }
                    break;
                default:if (accX.getX() >= 0){ /* on bottom border */
                    //going left
                    if (accY.getX() >= 0){
                        //going down
                        pos = scanVector6BLocation(prevPosition[0]-1, prevPosition[1]-1, map, signals, selectedIndexes);
                    }else{
                        //going up
                        pos = scanVector6BLocation(prevPosition[0]-1, prevPosition[1], map, signals, selectedIndexes);
                    }
                }else{
                    //going right
                    if (accY.getX() >= 0){
                        //going down
                        pos = scanVector6BLocation(prevPosition[0], prevPosition[1]-1, map, signals, selectedIndexes);
                    }else{
                        //going up
                        pos = scanVector6BLocation(prevPosition[0], prevPosition[1], map, signals, selectedIndexes);
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

    private Location getInductiveLocation(double [] signals, int [] prevPosition, int beaconDimension, int[] selectedIndexes) throws IOException {
        Location foundLocation = new Location("location");
        int [][] beaconPositions = new int[6][2];
        //initialize beacon positions
        int counter = 0;
        for (int r = 7; r > 0; r-=3){
            for (int c = 2; c < 6; c+=3){
                beaconPositions[counter][0] = c;
                beaconPositions[counter][1] = r;
                counter++;
            }
        }
        try {
            bw.write("previous position: ("+ prevPosition[0] + ", "+ prevPosition[1] +")\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int k = 0; k < beaconDimension; k++){
            //looking at strongest signal
            if (signals[k] > (double) -70 ){
                for (int j = 0; j < beaconDimension; j++){
                    //between two beacons
                    if ((j != k) && (signals[j] > (double) -70)){
                        bw.write("maybe between two beacons \n");
                        switch (k){
                            case 0: switch (j){
                                        case 1: foundLocation.setLatitude(3.5);
                                                foundLocation.setLongitude(7);
                                                bw.flush();
                                                return foundLocation;

                                        case 2: foundLocation.setLatitude(2);
                                                foundLocation.setLongitude(5.5);
                                                bw.flush();
                                                return foundLocation;
                                        default:break;
                                    }break;

                            case 1: switch (j){
                                        case 2: foundLocation.setLatitude(5);
                                                foundLocation.setLongitude(5.5);
                                                bw.flush();
                                                return foundLocation;
                                        default:break;
                                    }break;

                            case 2: switch (j){
                                        case 3: foundLocation.setLatitude(3.5);
                                                foundLocation.setLongitude(4);
                                            bw.flush();
                                                return foundLocation;

                                        case 4: foundLocation.setLatitude(2);
                                                foundLocation.setLongitude(2.5);
                                            bw.flush();
                                                return foundLocation;
                                        default:break;
                                    }break;

                            case 3: switch (j){
                                        case 5: foundLocation.setLatitude(5);
                                                foundLocation.setLongitude(2.5);
                                            bw.flush();
                                                return foundLocation;

                                        default:break;
                                    }break;

                            case 4: switch (j){
                                        case 5: foundLocation.setLatitude(3.5);
                                                 foundLocation.setLongitude(1);
                                            bw.flush();
                                                 return foundLocation;
                                        default:break;
                                    }break;

                            default:break;
                        }
                    }
                }//end second for cycle
                if (signals[k] > (double) -62){
                    // over a beacon
                    switch (k) {
                        case 0:
                            foundLocation.setLatitude((double) 2);
                            foundLocation.setLongitude((double) 7);
                            bw.write("Strong signal: final position over beacon ("+
                                    (int)foundLocation.getLatitude() + ", "
                                    + (int)foundLocation.getLongitude()+") \n");
                            bw.flush();
                            return foundLocation;

                        case 1:
                            foundLocation.setLatitude((double) 5);
                            foundLocation.setLongitude((double) 7);
                            bw.write("Strong signal: final position over beacon ("+
                                    (int)foundLocation.getLatitude() + ", "
                                    + (int)foundLocation.getLongitude()+") \n");
                            bw.flush();
                            return foundLocation;

                        case 2:
                            foundLocation.setLatitude((double) 2);
                            foundLocation.setLongitude((double) 4);
                            bw.write("Strong signal: final position over beacon ("+
                                    (int)foundLocation.getLatitude() + ", "
                                    + (int)foundLocation.getLongitude()+") \n");
                            bw.flush();
                            return foundLocation;

                        case 3:
                            foundLocation.setLatitude((double) 5);
                            foundLocation.setLongitude((double) 4);
                            bw.write("Strong signal: final position over beacon ("+
                                    (int)foundLocation.getLatitude() + ", "
                                    + (int)foundLocation.getLongitude()+") \n");
                            bw.flush();
                            return foundLocation;

                        case 4:
                            foundLocation.setLatitude((double) 2);
                            foundLocation.setLongitude((double) 1);
                            bw.write("Strong signal: final position over beacon ("+
                                    (int)foundLocation.getLatitude() + ", "
                                    + (int)foundLocation.getLongitude()+") \n");
                            bw.flush();
                            return foundLocation;

                        case 5:
                            foundLocation.setLatitude((double) 5);
                            foundLocation.setLongitude((double) 1);
                            bw.write("Strong signal: final position over beacon ("+
                                    (int)foundLocation.getLatitude() + ", "
                                    + (int)foundLocation.getLongitude()+") \n");
                            bw.flush();
                            return foundLocation;

                    }
                }else if (signals[k] > (double) -66){
                    // check near points at 1meter
                    bw.write("one meter from beacon:  " + k +  " \n");
                    bw.flush();
                    return scanPlusLocation(signals, beaconPositions[k], prevPosition, k );
                }else {
                    // check near points at 1.5meter
                    bw.write("one meter and half from beacon:  " + k +  " \n");
                    bw.flush();
                    return scanCrossLocation(signals, beaconPositions[k], prevPosition, k);
                }
            }//end strongest signal
        }//end of for cycle on beacons
        bw.write("away from all beacons \n");
        bw.flush();
        return getGridLocation6B(signals, prevPosition, selectedIndexes);
    }

    private Location scanCrossLocation(double[] signals, int[] beaconPosition, int[] prevPosition, int k) throws IOException {
        Location foundLocation = new Location("location");
        int [][] nearPoints = new int[4][2];
        boolean [] possiblePoints = new boolean[4];
        double best = -100;
        for (boolean counter : possiblePoints ) {
            counter = false;
        }
        int i = 0;
        //scan positions
        if (Math.abs(prevPosition[0] -  (beaconPosition[0]-1)) <= 1 ){
            if (Math.abs(prevPosition[1] - (beaconPosition[1]-1)) <= 1){
                //corner left bottom point
                nearPoints[0][0] = beaconPosition[0] - 1;
                nearPoints[0][1] = beaconPosition[1] - 1;
                possiblePoints[0] = true;
                i++;
            }
        }
        if (Math.abs(prevPosition[0] -  (beaconPosition[0]+1)) <= 1 ){
            if (Math.abs(prevPosition[1] - (beaconPosition[1]-1)) <= 1){
                //corner right bottom point
                nearPoints[1][0] = beaconPosition[0] + 1;
                nearPoints[1][1] = beaconPosition[1] - 1;
                possiblePoints[1] = true;
                i++;
            }
        }
        if (Math.abs(prevPosition[0] -  (beaconPosition[0] - 1)) <= 1 ){
            if (Math.abs(prevPosition[1] - (beaconPosition[1] + 1)) <= 1){
                //corner top left point
                nearPoints[2][0] = beaconPosition[0] -1;
                nearPoints[2][1] = beaconPosition[1] +1;
                possiblePoints[2] = true;
                i++;
            }
        }
        if (Math.abs(prevPosition[0] -  (beaconPosition[0] +1)) <= 1 ){
            if (Math.abs(prevPosition[1] - (beaconPosition[1] + 1)) <= 1){
                //corner top right point
                nearPoints[3][0] = beaconPosition[0] + 1;
                nearPoints[3][1] = beaconPosition[1] + 1;
                possiblePoints[3] = true;
                i++;
            }
        }
        for (int n=0; n<4; n++){
            if (possiblePoints[n]){
                bw.write("nearPoints of " + n + ": "+ nearPoints[n][0] + ", "+ nearPoints[n][1] + "\n");
            }
        }

        if(i==0){
            nearPoints[0][0] = beaconPosition[0] - 1;
            nearPoints[0][1] = beaconPosition[1] - 1;
            nearPoints[1][0] = beaconPosition[0] + 1;
            nearPoints[1][1] = beaconPosition[1] - 1;
            nearPoints[2][0] = beaconPosition[0] - 1;
            nearPoints[2][1] = beaconPosition[1] + 1;
            nearPoints[3][0] = beaconPosition[0] + 1;
            nearPoints[3][1] = beaconPosition[1] + 1;
        }

        switch (i){
            case 1: for (int j = 0; j < 4; j++) {
                if (possiblePoints[j] == true) {
                    foundLocation.setLatitude(nearPoints[j][0]);
                    foundLocation.setLongitude(nearPoints[j][1]);
                    return foundLocation;
                }
            }break;

            case 2: switch (k){
                //control on possible points of beacon 1
                case 0: if (possiblePoints[0] && possiblePoints[1]){
                    //bottom corners possible
                    if (signals[k+1] >= signals[k+2]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[1] && possiblePoints[3]){
                    //right corners possible
                    if (signals[k+2] >= signals[k+1]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left corners possible
                    if (signals[k+2] > signals[k+1]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else{
                    //top corners possible
                    if (signals[k+1] > signals[k+2]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 2
                case 1: if (possiblePoints[0] && possiblePoints[1]){
                    //bottom corners possibles
                    if (signals[k-1] >= signals[k+2]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left corners
                    if (signals[k+2] >= signals[k-1]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[3] && possiblePoints[1]){
                    //right corners
                    if (signals[k+2] > signals[k-1]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }else{
                    //top corners
                    if (signals[k-1] > signals[k+2]){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 3
                case 2: if (possiblePoints[0] && possiblePoints[1]){
                    //bottom corners
                    if (signals[k+1] > signals[k+2]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left corners
                    if (signals[k+2] >= signals[k-2]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[3] && possiblePoints[1]){
                    //right corners
                    if (signals[k+2] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }else{
                    //top corners
                    if (signals[k+1] >= signals[k-2]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 4
                case 3: if (possiblePoints[0] && possiblePoints[1]){
                    //bottom corners
                    if (signals[k-1] >= signals[k+2]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left corners
                    if (signals[k+2] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[3] && possiblePoints[1]){
                    //right corners
                    if (signals[k-2] > signals[k+2]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else{
                    //top corners
                    if (signals[k-1] >= signals[k-2]){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 5
                case 4: if (possiblePoints[1] && possiblePoints[3]){
                    //right corners
                    if (signals[k-2] >= signals[k+1]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left corners
                    if (signals[k-2] > signals[k+1]){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[1]){
                    //bottom corners
                    if (signals[k+1] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else{
                    //top corners
                    if (signals[k+1] >= signals[k-2]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 6
                case 5: if (possiblePoints[0] && possiblePoints[1]){
                    //bottom corners
                    if (signals[k-1] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left corners
                    if (signals[k-2] >= signals[k-1]){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[3] && possiblePoints[1]){
                    //right corners
                    if (signals[k-2] > signals[k-1]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else{
                    //top corners
                    if (signals[k-1] >= signals[k-2]){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }
            }break;

            default: switch (k){
                //control on possible points of beacon 1
                case 0:
                    for (int b=0; b<6; b++){
                        if((b != k) && (signals[b] > best)){
                            best = signals[b];
                        }
                    }
                    if ((best == signals[k+1]) && ((signals[k+1] - signals[k+2]) > (double) 4)){
                        //top right corner
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }else if ((best == signals[k+2]) && ((signals[k+2] - signals[k+1]) > (double) 4)){
                        //left bottom corner
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }else if (((best == signals[k+1]) || (best == signals[k+2])) && (Math.abs(signals[k+1] - signals[k+2]) < (double) 4)){
                        //bottom right corner
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }else{
                        //top left corner
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }
                    //control of possible points of beacon 2
                case 1: for (int b=0; b<6; b++){
                    if((b != k) && (signals[b] > best)){
                        best = signals[b];
                    }
                }
                    if ((best == signals[k-1]) && ((signals[k-1] - signals[k+2]) > (double) 4)){
                        //top left corner
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }else if ((best == signals[k+2]) && ((signals[k+2] - signals[k-1]) > (double) 4)){
                        //right bottom corner
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }else if (((best == signals[k-1]) || (best == signals[k+2])) && (Math.abs(signals[k-1] - signals[k+2]) < (double) 4)){
                        //bottom left corner
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }else{
                        //top right corner
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }
                    //control of possible points of beacon 3
                case 2: for (int b=0; b<6; b++){
                    if((b != k) && (signals[b] > best)){
                        best = signals[b];
                    }
                }
                    if ((best == signals[k+2]) && ((signals[k+2] - signals[k+1]) > (double) 4)){
                        //bottom left corner
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }else if ((best == signals[k-2]) && ((signals[k-2] - signals[k+1]) > (double) 4)){
                        //top left corner
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }else if (((best == signals[k+2]) || (best == signals[k+1])) && (Math.abs(signals[k+1] - signals[k+2]) < (double) 4)){
                        //bottom right corner
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }else{
                        //top right corner
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }

                    //control of possible points of beacon 4
                case 3: for (int b=0; b<6; b++){
                    if((b != k) && (signals[b] > best)){
                        best = signals[b];
                    }
                }
                    if ((best == signals[k+2]) && ((signals[k+2] - signals[k-1]) > (double) 4)){
                        //bottom right corner
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }else if ((best == signals[k-2]) && ((signals[k-2] - signals[k-1]) > (double) 4)){
                        //top right corner
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }else if (((best == signals[k-1]) || (best == signals[k-2])) && (Math.abs(signals[k-1] - signals[k-2]) < (double) 4)){
                        //top left corner
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }else{
                        //bottom left corner
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }

                    //control of possible points of beacon 5
                case 4: for (int b=0; b<6; b++){
                    if((b != k) && (signals[b] > best)){
                        best = signals[b];
                    }
                }
                    if ((best == signals[k+1]) && ((signals[k+1] - signals[k-2]) > (double) 4)){
                        //bottom right corner
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }else if ((best == signals[k-2]) && ((signals[k-2] - signals[k+1]) > (double) 4)){
                        //top left corner
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }else if (((best == signals[k+1]) || (best == signals[k-2])) && (Math.abs(signals[k+1] - signals[k-2]) < (double) 4)){
                        //top right corner
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }else{
                        //bottom left corner
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }

                    //control of possible points of beacon 6
                case 5: for (int b=0; b<6; b++){
                    if((b != k) && (signals[b] > best)){
                        best = signals[b];
                    }
                }
                    if ((best == signals[k-1]) && ((signals[k-1] - signals[k-2]) > (double) 4)){
                        //bottom left corner
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }else if ((best == signals[k-2]) && ((signals[k-2] - signals[k-1]) > (double) 4)){
                        //top right corner
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }else if (((best == signals[k-1]) || (best == signals[k-2])) && (Math.abs(signals[k-1] - signals[k-2]) < (double) 4)){
                        //top left corner
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }else{
                        //bottom right corner
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }
            }break;

        }

        return foundLocation;
    }

    private Location scanPlusLocation(double[] signals, int[] beaconPosition,  int[] prevPosition, int k) {
        Location foundLocation = new Location("location");
        int [][] nearPoints = new int[4][2];
        boolean [] possiblePoints = new boolean[4];
        double best = -100;
        for (boolean counter : possiblePoints ) {
            counter = false;
        }
        int i = 0;
        //scan positions
        if (Math.abs(prevPosition[0] -  (beaconPosition[0]-1)) <= 1 ){
            if (Math.abs(prevPosition[1] - (beaconPosition[1])) <= 1){
                //left point
                nearPoints[0][0] = beaconPosition[0] - 1;
                nearPoints[0][1] = beaconPosition[1];
                possiblePoints[0] = true;
                i++;
            }
        }
        if (Math.abs(prevPosition[0] -  (beaconPosition[0]+1)) <= 1 ){
            if (Math.abs(prevPosition[1] - (beaconPosition[1])) <= 1){
                //right point
                nearPoints[1][0] = beaconPosition[0] + 1;
                nearPoints[1][1] = beaconPosition[1];
                possiblePoints[1] = true;
                i++;
            }
        }
        if (Math.abs(prevPosition[0] -  beaconPosition[0]) <= 1 ){
            if (Math.abs(prevPosition[1] - (beaconPosition[1] - 1)) <= 1){
                //bottom point
                nearPoints[2][0] = beaconPosition[0];
                nearPoints[2][1] = beaconPosition[1] -1;
                possiblePoints[2] = true;
                i++;
            }
        }
        if (Math.abs(prevPosition[0] -  beaconPosition[0]) <= 1 ){
            if (Math.abs(prevPosition[1] - (beaconPosition[1] + 1)) <= 1){
                //top point
                nearPoints[3][0] = beaconPosition[0];
                nearPoints[3][1] = beaconPosition[1] + 1;
                possiblePoints[3] = true;
                i++;
            }
        }

        if(i==0){
            //assign values to do default action
            nearPoints[0][0] = beaconPosition[0] - 1;
            nearPoints[0][1] = beaconPosition[1];
            nearPoints[1][0] = beaconPosition[0] + 1;
            nearPoints[1][1] = beaconPosition[1];
            nearPoints[2][0] = beaconPosition[0];
            nearPoints[2][1] = beaconPosition[1] - 1;
            nearPoints[3][0] = beaconPosition[0];
            nearPoints[3][1] = beaconPosition[1] + 1;
        }

        switch (i){
            case 1: for (int j = 0; j < 4; j++) {
                        if (possiblePoints[j] == true) {
                            foundLocation.setLatitude(nearPoints[j][0]);
                            foundLocation.setLongitude(nearPoints[j][1]);
                            return foundLocation;
                        }
                    }break;

            case 2: switch (k){
                //control on possible points of beacon 1
                case 0: if (possiblePoints[0] && possiblePoints[3]){
                            //left and top possibles
                            if (signals[k+1] > signals[k+2]){
                                foundLocation.setLatitude(nearPoints[3][0]);
                                foundLocation.setLongitude(nearPoints[3][1]);
                            }else{
                                foundLocation.setLatitude(nearPoints[0][0]);
                                foundLocation.setLongitude(nearPoints[0][1]);
                            }
                            return foundLocation;
                        }else if (possiblePoints[0] && possiblePoints[2]){
                            //left and bottom
                            if (signals[k+2] > signals[k+1]){
                                foundLocation.setLatitude(nearPoints[2][0]);
                                foundLocation.setLongitude(nearPoints[2][1]);
                            }else{
                                foundLocation.setLatitude(nearPoints[0][0]);
                                foundLocation.setLongitude(nearPoints[0][1]);
                            }
                            return foundLocation;
                        }else if (possiblePoints[3] && possiblePoints[1]){
                            //top and right
                            if (signals[k+1] > signals[k+2]){
                                foundLocation.setLatitude(nearPoints[1][0]);
                                foundLocation.setLongitude(nearPoints[1][1]);
                            }else{
                                foundLocation.setLatitude(nearPoints[3][0]);
                                foundLocation.setLongitude(nearPoints[3][1]);
                            }
                            return foundLocation;
                        }else{
                            //right and bottom
                            if (signals[k+1] > signals[k+2]){
                                foundLocation.setLatitude(nearPoints[1][0]);
                                foundLocation.setLongitude(nearPoints[1][1]);
                            }else{
                                foundLocation.setLatitude(nearPoints[2][0]);
                                foundLocation.setLongitude(nearPoints[2][1]);
                            }
                            return foundLocation;
                        }
                    //control on possible points of beacon 2
                case 1: if (possiblePoints[0] && possiblePoints[3]){
                            //left and top possibles
                            if (signals[k-1] > signals[k+2]){
                                foundLocation.setLatitude(nearPoints[0][0]);
                                foundLocation.setLongitude(nearPoints[0][1]);
                            }else{
                                foundLocation.setLatitude(nearPoints[3][0]);
                                foundLocation.setLongitude(nearPoints[3][1]);
                            }
                            return foundLocation;
                        }else if (possiblePoints[0] && possiblePoints[2]){
                            //left and bottom
                            if (signals[k+2] > signals[k-1]){
                                foundLocation.setLatitude(nearPoints[2][0]);
                                foundLocation.setLongitude(nearPoints[2][1]);
                            }else{
                                foundLocation.setLatitude(nearPoints[0][0]);
                                foundLocation.setLongitude(nearPoints[0][1]);
                            }
                            return foundLocation;
                        }else if (possiblePoints[3] && possiblePoints[1]){
                            //top and right
                            if (signals[k+2] > signals[k-1]){
                                foundLocation.setLatitude(nearPoints[1][0]);
                                foundLocation.setLongitude(nearPoints[1][1]);
                            }else{
                                foundLocation.setLatitude(nearPoints[3][0]);
                                foundLocation.setLongitude(nearPoints[3][1]);
                            }
                            return foundLocation;
                        }else{
                            //right and bottom
                            if (signals[k+2] > signals[k-1]){
                                foundLocation.setLatitude(nearPoints[2][0]);
                                foundLocation.setLongitude(nearPoints[2][1]);
                            }else{
                                foundLocation.setLatitude(nearPoints[1][0]);
                                foundLocation.setLongitude(nearPoints[1][1]);
                            }
                            return foundLocation;
                        }
                    //control on possible points of beacon 3
                case 2: if (possiblePoints[0] && possiblePoints[3]){
                    //left and top possibles
                    if (signals[k-2] > signals[k+2]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left and bottom
                    if (signals[k+2] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[3] && possiblePoints[1]){
                    //top and right
                    if (signals[k-2] > signals[k+1]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else{
                    //right and bottom
                    if (signals[k+1] > signals[k+2]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 4
                case 3: if (possiblePoints[0] && possiblePoints[3]){
                    //left and top possibles
                    if (signals[k-2] > signals[k-1]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left and bottom
                    if (signals[k-1] > signals[k+2]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[3] && possiblePoints[1]){
                    //top and right
                    if (signals[k-2] > signals[k+2]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else{
                    //right and bottom
                    if (signals[k+2] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 5
                case 4: if (possiblePoints[0] && possiblePoints[3]){
                    //left and top possibles
                    if (signals[k-2] > signals[k+1]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left and bottom
                    if (signals[k-2] > signals[k+1]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[3] && possiblePoints[1]){
                    //top and right
                    if (signals[k+1] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }else{
                    //right and bottom
                    if (signals[k+1] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 6
                case 5: if (possiblePoints[0] && possiblePoints[3]){
                    //left and top possibles
                    if (signals[k-2] > signals[k-1]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[0] && possiblePoints[2]){
                    //left and bottom
                    if (signals[k-1] > signals[k-2]){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (possiblePoints[3] && possiblePoints[1]){
                    //top and right
                    if (signals[k-2] > signals[k-1]){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else{
                    //right and bottom
                    if (signals[k-2] > signals[k-1]){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }
            }break;

            case 3: switch (k){
                //control on possible points of beacon 1
                case 0: if (!possiblePoints[0]){
                    //right, top and bottom possibles
                    if ((signals[k+1] > signals[k+2]) && (signals[k+1] > signals[k+3])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else if ((signals[k+2] > signals[k+1]) && (signals[k+2] > signals[k+3])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[1]){
                    //left, top and bottom possibles
                    if ((signals[k+2] > signals[k+1]) && (signals[k+2] > signals[k+3])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else if ((signals[k+1] > signals[k+2]) && (signals[k+1] > signals[k+3])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[2]){
                    //left, right and top  possibles
                    if ((signals[k+1] > signals[k+2]) && (signals[k+1] > signals[k+3])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else if ((signals[k+2] > signals[k+1]) && (signals[k+2] > signals[k+3])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }else{
                    //left, right and bottom possibles
                    if ((signals[k+1] > signals[k+2]) && (signals[k+1] > signals[k+3])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else if ((signals[k+2] > signals[k+1]) && (signals[k+2] > signals[k+3])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 2
                case 1: if (!possiblePoints[0]){
                    //right, top and bottom possibles
                    if ((signals[k+2] > signals[k-1]) && (signals[k+2] > signals[k+1])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else if ((signals[k-1] > signals[k+1]) && (signals[k-1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[1]){
                    //left, top and bottom possibles
                    if ((signals[k-1] > signals[k+1]) && (signals[k-1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else if ((signals[k+2] > signals[k-1]) && (signals[k+2] > signals[k+1])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[2]){
                    //left, right and top  possibles
                    if ((signals[k-1] > signals[k+1]) && (signals[k-1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else if ((signals[k+2] > signals[k-1]) && (signals[k+2] > signals[k+1])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }
                    return foundLocation;
                }else{
                    //left, right and bottom possibles
                    if ((signals[k-1] > signals[k+1]) && (signals[k-1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else if ((signals[k+2] > signals[k-1]) && (signals[k+2] > signals[k+1])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 3
                case 2: if (!possiblePoints[0]){
                    //right, top and bottom possibles
                    if ((signals[k-2] > signals[k+1]) && (signals[k-2] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else if ((signals[k+1] > signals[k-2]) && (signals[k+1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[1]){
                    //left, top and bottom possibles
                    if ((signals[k-2] > signals[k+1]) && (signals[k-2] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else if ((signals[k+2] > signals[k-2]) && (signals[k+2] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[2]){
                    //left, right and top  possibles
                    if ((signals[k+1] > signals[k-2]) && (signals[k+1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else if ((signals[k+-2] > signals[k+1]) && (signals[k-2] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else{
                    //left, right and bottom possibles
                    if ((signals[k+1] > signals[k+2]) && (signals[k+1] > signals[k-2])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else if ((signals[k+2] > signals[k+1]) && (signals[k+2] > signals[k-2])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 4
                case 3: if (!possiblePoints[0]){
                    //right, top and bottom possibles
                    if ((signals[k-2] > signals[k-1]) && (signals[k-2] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else if ((signals[k+2] > signals[k-1]) && (signals[k+2] > signals[k-2])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[1]){
                    //left, top and bottom possibles
                    if ((signals[k-2] > signals[k-1]) && (signals[k-2] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else if ((signals[k-1] > signals[k-2]) && (signals[k-1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[2]){
                    //left, right and top  possibles
                    if ((signals[k-2] > signals[k-1]) && (signals[k-2] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else if ((signals[k-1] > signals[k-2]) && (signals[k-1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else{
                    //left, right and bottom possibles
                    if ((signals[k-1] > signals[k-2]) && (signals[k-1] > signals[k+2])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else if ((signals[k+2] > signals[k-1]) && (signals[k+2] > signals[k-2])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 5
                case 4: if (!possiblePoints[0]){
                    //right, top and bottom possibles
                    if ((signals[k-2] > signals[k+1]) && (signals[k-2] > signals[k-1])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else if ((signals[k+1] > signals[k-2]) && (signals[k+1] > signals[k-1])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[1]){
                    //left, top and bottom possibles
                    if ((signals[k-2] > signals[k+1]) && (signals[k-2] > signals[k-1])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else if ((signals[k+1] > signals[k-2]) && (signals[k+1] > signals[k-1])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[2]){
                    //left, right and top  possibles
                    if ((signals[k+1] > signals[k-2]) && (signals[k+1] > signals[k-1])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else if ((signals[k-2] > signals[k+1]) && (signals[k-2] > signals[k-1])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }
                    return foundLocation;
                }else{
                    //left, right and bottom possibles
                    if ((signals[k+1] > signals[k-2]) && (signals[k+1] > signals[k-1])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else if ((signals[k-2] > signals[k-1]) && (signals[k-2] > signals[k+1])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }
                    //control on possible points of beacon 6
                case 5: if (!possiblePoints[0]){
                    //right, top and bottom possibles
                    if ((signals[k-2] > signals[k-3]) && (signals[k-2] > signals[k-1])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else if ((signals[k-1] > signals[k-2]) && (signals[k-1] > signals[k-3])){
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[1]){
                    //left, top and bottom possibles
                    if ((signals[k-1] > signals[k-2]) && (signals[k-1] > signals[k-3])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else if ((signals[k-2] > signals[k-1]) && (signals[k-1] > signals[k-3])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }else if (!possiblePoints[2]){
                    //left, right and top  possibles
                    if ((signals[k-1] > signals[k-2]) && (signals[k-1] > signals[k-3])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else if ((signals[k-2] > signals[k-1]) && (signals[k-2] > signals[k-3])){
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }
                    return foundLocation;
                }else{
                    //left, right and bottom possibles
                    if ((signals[k-1] > signals[k-2]) && (signals[k-1] > signals[k-3])){
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                    }else if ((signals[k-2] > signals[k-1]) && (signals[k-2] > signals[k-3])){
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                    }else{
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                    }
                    return foundLocation;
                }
            }break;

            default: switch (k){
                //control on possible points of beacon 1
                case 0:
                        for (int b=0; b<6; b++){
                            if((b != k) && (signals[b] > best)){
                                best = signals[b];
                            }
                        }
                        if (best == signals[k+1]){
                            //right
                            foundLocation.setLatitude(nearPoints[1][0]);
                            foundLocation.setLongitude(nearPoints[1][1]);
                            return foundLocation;
                        }else if (best == signals[k+2]){
                            //bottom
                            foundLocation.setLatitude(nearPoints[2][0]);
                            foundLocation.setLongitude(nearPoints[2][1]);
                            return foundLocation;
                        }else if (signals[k+1] > signals[k+2]){
                            //top
                            foundLocation.setLatitude(nearPoints[3][0]);
                            foundLocation.setLongitude(nearPoints[3][1]);
                            return foundLocation;
                        }else{
                            //left
                            foundLocation.setLatitude(nearPoints[0][0]);
                            foundLocation.setLongitude(nearPoints[0][1]);
                            return foundLocation;
                        }
                //control of possible points of beacon 2
                case 1: for (int b=0; b<6; b++){
                            if((b != k) && (signals[b] > best)){
                                best = signals[b];
                            }
                        }
                        if (best == signals[k-1]){
                            //left
                            foundLocation.setLatitude(nearPoints[0][0]);
                            foundLocation.setLongitude(nearPoints[0][1]);
                            return foundLocation;
                        }else if (best == signals[k+2]){
                            //bottom
                            foundLocation.setLatitude(nearPoints[2][0]);
                            foundLocation.setLongitude(nearPoints[2][1]);
                            return foundLocation;
                        }else if (signals[k-1] > signals[k+2]){
                            //top
                            foundLocation.setLatitude(nearPoints[3][0]);
                            foundLocation.setLongitude(nearPoints[3][1]);
                            return foundLocation;
                        }else{
                            //right
                            foundLocation.setLatitude(nearPoints[1][0]);
                            foundLocation.setLongitude(nearPoints[1][1]);
                            return foundLocation;
                        }
                    //control of possible points of beacon 3
                case 2: for (int b=0; b<6; b++){
                            if((b != k) && (signals[b] > best)){
                                best = signals[b];
                            }
                        }
                    if (best == signals[k-2]){
                        //top
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }else if (best == signals[k+1]){
                        //right
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }else if (best == signals[k+2]){
                        //bottom
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }else{
                        //left
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }

                    //control of possible points of beacon 4
                case 3: for (int b=0; b<6; b++){
                    if((b != k) && (signals[b] > best)){
                        best = signals[b];
                    }
                }
                    if (best == signals[k-2]){
                        //top
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }else if (best == signals[k-1]){
                        //left
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }else if (best == signals[k+2]){
                        //bottom
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }else{
                        //right
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }

                    //control of possible points of beacon 5
                case 4: for (int b=0; b<6; b++){
                    if((b != k) && (signals[b] > best)){
                        best = signals[b];
                    }
                }
                    if (best == signals[k-2]){
                        //top
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }else if (best == signals[k+1]){
                        //right
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }else if (signals[k-2] > signals[k+1]){
                        //left
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }else{
                        //bottom
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }

                    //control of possible points of beacon 6
                case 5: for (int b=0; b<6; b++){
                    if((b != k) && (signals[b] > best)){
                        best = signals[b];
                    }
                }
                    if (best == signals[k-1]){
                        //left
                        foundLocation.setLatitude(nearPoints[0][0]);
                        foundLocation.setLongitude(nearPoints[0][1]);
                        return foundLocation;
                    }else if (best == signals[k-2]){
                        //top
                        foundLocation.setLatitude(nearPoints[3][0]);
                        foundLocation.setLongitude(nearPoints[3][1]);
                        return foundLocation;
                    }else if (signals[k-1] > signals[k-2]){
                        //bottom
                        foundLocation.setLatitude(nearPoints[2][0]);
                        foundLocation.setLongitude(nearPoints[2][1]);
                        return foundLocation;
                    }else{
                        //right
                        foundLocation.setLatitude(nearPoints[1][0]);
                        foundLocation.setLongitude(nearPoints[1][1]);
                        return foundLocation;
                    }
            }break;

        }

        return foundLocation;
    }



    private double [][] createMapMatrix4B(){
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

    private double [][] createMapMatrix6B(){
        double [][] pointsMap = new double [] []{
                {-82.29,	-81.43,	-76.97,	-80.54,	-84.42,	-83.94},
                {-74.65,	-80.81,	-78.47,	-80.01,	-71.56,	-78.05},
                {-75.46,	-78.48,	-73.54,	-85.25,	-72.03,	-72.47},
                {-77.38,	-79.53,	-79.95,	-78.83,	-68.95,	-72.39},
                {-82.73,	-72.75,	-80.89,	-84.57,	-79.50,	-70.57},
                {-84.83,	-80.08,	-81.68,	-77.42,	-82.51,	-68.56},
                {-82.08,	-77.69,	-76.73,	-78.91,	-74.21,	-67.23},
                {-81.08,	-84.05,	-87.21,	-73.08,	-81.79,	-72.47},
                {-85.26,	-81.12,	-72.37,	-77.70,	-68.98,	-73.74},
                {-77.54,	-80.90,	-74.52,	-84.51,	-65.88,	-74.07},
                {-79.16,	-78.78,	-72.13,	-78.73,	-63.39,	-72.94},
                {-77.76,	-82.04,	-75.35,	-78.60,	-64.29,	-70.01},
                {-75.17,	-79.11,	-79.51,	-72.44,	-73.73,	-66.71},
                {-84.35,	-78.22,	-78.40,	-77.39,	-74.41,	-61.78},
                {-83.20,	-80.95,	-76.67,	-78.81,	-81.98,	-68.44},
                {-80.65,	-82.74,	-85.80,	-72.81,	-72.06,	-66.99},
                {-82.23,	-82.45,	-77.27,	-85.18,	-70.93,	-79.11},
                {-75.97,	-75.39,	-79.54,	-78.46,	-79.52,	-76.11},
                {-80.99,	-76.24,	-80.96,	-77.93,	-64.33,	-77.76},
                {-81.65,	-76.51,	-77.22,	-75.76,	-74.46,	-70.74},
                {-80.55,	-78.66,	-73.30,	-71.99,	-74.75,	-69.19},
                {-72.66,	-76.19,	-74.08,	-68.43,	-77.41,	-66.45},
                {-79.90,	-77.75,	-84.03,	-69.97,	-82.03,	-73.41},
                {-82.23,	-87.22,	-75.06,	-77.63,	-82.61,	-72.07},
                {-79.58,	-75.89,	-78.99,	-77.43,	-76.39,	-73.31},
                {-76.03,	-82.95,	-72.90,	-77.15,	-70.84,	-78.04},
                {-79.46,	-84.10,	-72.07,	-74.77,	-76.72,	-77.39},
                {-74.03,	-76.47,	-70.60,	-79.57,	-74.03,	-74.12},
                {-83.47,	-74.03,	-75.41,	-68.38,	-78.52,	-72.10},
                {-81.49,	-79.11,	-77.75,	-69.85,	-79.06,	-71.98},
                {-79.21,	-72.70,	-80.68,	-71.28,	-81.75,	-71.51},
                {-78.98,	-84.32,	-76.77,	-74.34,	-78.16,	-76.10},
                {-77.02,	-79.57,	-72.31,	-80.96,	-84.68,	-79.53},
                {-83.48,	-80.52,	-62.39,	-80.21,	-82.66,	-76.09},
                {-79.79,    -78.18,	-58.74,	-72.61,	-72.02,	-74.08},
                {-71.68,    -76.23,	-63.76,	-73.73,	-78.63,	-83.20},
                {-77.38,    -79.08,	-79.80,	-69.67,	-80.98,	-71.63},
                {-79.92,    -77.38,	-77.57,	-58.16,	-75.65,	-76.24},
                {-80.51,	-77.97,	-83.92,	-63.20,	-82.43,	-77.62},
                {-82.78,	-74.46,	-77.73,	-66.98,	-83.01,	-76.79},
                {-74.23,	-69.86,	-73.32,	-72.21,	-78.67,	-80.66},
                {-70.10,	-75.52,	-69.63,	-79.65,	-75.88,	-84.17},
                {-74.55,	-81.13,	-64.39,	-77.48,	-75.33,	-78.32},
                {-75.23,	-70.53,	-72.04,	-78.46,	-79.96,	-78.48},
                {-82.58,	-75.61,	-85.15,	-72.47,	-75.85,	-78.71},
                {-73.65,	-82.21,	-78.64,	-67.53,	-79.06,	-73.41},
                {-82.09,	-75.87,	-76.76,	-67.05,	-78.67,	-79.06},
                {-81.50,	-75.10,	-78.59,	-75.32,	-81.10,	-77.76},
                {-67.45,	-70.99,	-73.74,	-72.54,	-86.29,	-82.36},
                {-67.90,	-75.92,	-79.99,	-78.78,	-83.40,	-84.33},
                {-69.12,	-73.79,	-76.65,	-78.94,	-86.71,	-84.08},
                {-72.00,	-75.37,	-75.14,	-78.88,	-81.54,	-81.83},
                {-80.72,	-72.68,	-83.28,	-78.84,	-78.24,	-78.64},
                {-80.35,	-69.35,	-78.88,	-73.65,	-81.19,	-82.34},
                {-77.37,	-66.10,	-82.66,	-75.73,	-79.81,	-80.22},
                {-78.59,	-71.86,	-88.23,	-80.33,	-82.40,	-76.70},
                {-62.94,	-77.55,	-73.11,	-79.35,	-78.43,	-79.38},
                {-64.36,	-79.34,	-75.96,	-76.45,	-86.16,	-77.55},
                {-55.43,	-71.42,	-75.00,	-79.11,	-85.46,	-83.32},
                {-70.19,	-69.11,	-75.89,	-72.16,	-82.65,	-75.48},
                {-67.63,	-67.32,	-78.85,	-72.97,	-84.04,	-88.92},
                {-74.19,	-60.46,	-79.46,	-83.96,	-85.67,	-76.58},
                {-78.93,	-63.66,	-81.08,	-79.80,	-78.20,	-79.08},
                {-76.71,	-70.27,	-75.09,	-78.52,	-90.88,	-84.52},
                {-79.01,	-83.51,	-79.67,	-81.32,	-81.85,	-85.78},
                {-66.65,	-75.05,	-79.53,	-71.72,	-80.82,	-78.26},
                {-65.98,	-71.62,	-77.43,	-84.35,	-77.48,	-85.80},
                {-75.18,	-77.24,	-85.11,	-80.39,	-79.70,	-78.37},
                {-73.01,	-76.08,	-79.85,	-77.79,	-81.14,	-74.01},
                {-76.30,	-64.86,	-83.46,	-79.90,	-82.46,	-70.82},
                {-76.16,	-68.13,	-77.54,	-82.82,	-87.80,	-78.40},
                {-87.98,	-71.98,	-78.78,	-75.26,	-88.00,	-82.14}
        };

        return pointsMap;
    }

    private int [] scanInternalLocation(int x, int y, double [][] map, double[] signals, int[] selectedIndexes) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
        //weight on beacons
//        for (int k = 0; k<4; k++) {
//            if (bestbeacon < signals[k]){
//                bestbeacon = signals[k];
//                bestIndex = k;
//            }
//        }
        //row coordinate
        for (int i = 0; i<3; i++ ){
            //column coordinate
            for (int j = 0; j<3; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<6; b++){
                    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                    bw.write("mapIndex = " + mapIndex + "\n");
                    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
                    if (b == selectedIndexes[0]){
                        tempSignal += weight1 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[1]){
                        tempSignal += weight2 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[0]){
                        tempSignal += weight3 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }
                    bw.write("signal = "+ tempSignal + "\n");
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
                bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }

    private int [] scanBorderLRLocation(int x, int y, double [][] map, double[] signals, int[] selectedIndexes) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
        //weight on beacons
//        for (int k = 0; k<4; k++) {
//            if (bestbeacon < signals[k]){
//                bestbeacon = signals[k];
//                bestIndex = k;
//            }
//        }
        //row coordinate
        for (int i = 0; i<2; i++ ){
            //column coordinate
            for (int j = 0; j<3; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<6; b++){
                    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                    bw.write("mapIndex = " + mapIndex + "\n");
                    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
                    if (b == selectedIndexes[0]){
                        tempSignal += weight1 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[1]){
                        tempSignal += weight2 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[0]){
                        tempSignal += weight3 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }
                    bw.write("signal = "+ tempSignal + "\n");
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
                bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }

    private int [] scanBorderUDLocation(int x, int y, double [][] map, double[] signals, int[] selectedIndexes) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
        //weight on beacons
//        for (int k = 0; k<4; k++) {
//            if (bestbeacon < signals[k]){
//                bestbeacon = signals[k];
//                bestIndex = k;
//            }
//        }
        //row coordinate
        for (int i = 0; i<3; i++ ){
            //column coordinate
            for (int j = 0; j<2; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<6; b++){
                    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                    bw.write("mapIndex = " + mapIndex + "\n");
                    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
                    if (b == selectedIndexes[0]){
                        tempSignal += weight1 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[1]){
                        tempSignal += weight2 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[0]){
                        tempSignal += weight3 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }
                    bw.write("signal = "+ tempSignal + "\n");
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
                bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }

    private int [] scanCornerLocation(int x, int y, double [][] map, double[] signals, int[] selectedIndexes) throws IOException {
        int [] pos = new int[2];
        int mapIndex;
        double tempSignal = 0;
        double bestSignal = 50;
        double bestbeacon = -100;
        double bestIndex = 0;
        //weight on beacons
//        for (int k = 0; k<4; k++) {
//            if (bestbeacon < signals[k]){
//                bestbeacon = signals[k];
//                bestIndex = k;
//            }
//        }
        //row coordinate
        for (int i = 0; i<2; i++ ){
            //column coordinate
            for (int j = 0; j<2; j++){
                //check values starting from bottom left position
                //for each beacon saved
                for (int b = 0; b<6; b++){
                    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                    bw.write("mapIndex = " + mapIndex + "\n");
                    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
                    if (b == selectedIndexes[0]){
                        tempSignal += weight1 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[1]){
                        tempSignal += weight2 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[0]){
                        tempSignal += weight3 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }
                    bw.write("signal = "+ tempSignal + "\n");
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
                bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
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
                        bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                        bw.write("mapIndex = " + mapIndex + "\n");
                        bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
/*                    if (b == bestIndex){
                        tempSignal += 0.7 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else{
                        tempSignal += 0.1 * Math.abs(signals[b] - map[mapIndex][b]);
                    }
 */                    bw.write("signal = "+ tempSignal + "\n");
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
                bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }

    private int [] scanVector6BLocation(int x, int y, double [][] map, double[] signals, int[] selectedIndexes) throws IOException {
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
                for (int b = 0; b<6; b++){
                    bw.write("beacon signal = "+ signals[b]+"\n");
                    //get difference between rssis of position and map
                    mapIndex = (y+j)*8 + (x+i);
                    bw.write("mapIndex = " + mapIndex + "\n");
                    bw.write("map signal = "+ map[mapIndex][b]+"\n");
                    //weighted sum
/*                    if (b == bestIndex){
                        tempSignal += 0.7 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else{
                        tempSignal += 0.1 * Math.abs(signals[b] - map[mapIndex][b]);
                    }
 */                 if (b == selectedIndexes[0]){
                        tempSignal += weight1 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[1]){
                        tempSignal += weight2 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }else if (b == selectedIndexes[0]){
                        tempSignal += weight3 * (Math.abs(signals[b] - map[mapIndex][b]));
                    }
                    bw.write("signal = "+ tempSignal + "\n");
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
                bw.write("best signal: "+ bestSignal + ", position: (" + pos[0] + ", " + pos[1]+") \n");
            }
        }
        return pos;
    }
}
