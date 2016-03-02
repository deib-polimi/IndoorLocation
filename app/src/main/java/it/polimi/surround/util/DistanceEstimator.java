package it.polimi.surround.util;

import android.location.Location;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

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

    public DistanceEstimator(UIActivity ui){
        this.ui = ui;
        rssiByBeacon = new HashMap<String, Double>();
    }

    public Location estimateLocation(List<Beacon> beacons, Location deviceLoc){

        if(beacons.size() < 3)
            return null;

        double [][] positions = new double [Math.min(4, beacons.size())][2];
        double [] distances = new double[Math.min(4, beacons.size())];

        String rssi = "";
        String dist = "";
        for(int i = 0; i < Math.min(4, beacons.size()); i++){
            Beacon b = beacons.get(i);
            double [] bData = MainActivity.LOCATION_BY_BEACONS.get(b.getMajor() + ":" + b.getMinor());
            b = createBeacon(b, bData, deviceLoc);
            double [] p  = getLatLng(bData);
            Double d = calculate2DProjection(Utils.computeAccuracy(b), bData);
            positions[i] = p;
            distances[i] = d;
             rssi += String.format("%d: %d ", b.getMajor(), b.getRssi());
            dist += String.format("%d: %f ", b.getMajor(), d);
        }

        ui.printText(R.id.rssi_val,
                rssi + "\n" +
                dist
        );

        return getLocationWithNonLinearTrilateration(
                positions,
                distances
        );
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
}
