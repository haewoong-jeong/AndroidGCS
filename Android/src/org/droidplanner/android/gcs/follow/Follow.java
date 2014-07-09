package org.droidplanner.android.gcs.follow;

import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class Follow implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener, OnDroneListener {
	private static final long MIN_TIME_MS = 500;
	private static final float MIN_DISTANCE_M = 0.0f;
	
	private Context context;
	private boolean followMeEnabled = false;
	private Drone drone;
	private LocationClient mLocationClient;
	
	private FollowType followAlgorithm;
	
	public Follow(Context context, Drone drone) {
		this.context = context;
		this.drone = drone;
		followAlgorithm = new FollowLeash(drone,new Length(5.0),MIN_TIME_MS);
		mLocationClient = new LocationClient(context, this, this);
		mLocationClient.connect();
		drone.events.addDroneListener(this);
	}

	public void toggleFollowMeState() {
		if (isEnabled()) {
			disableFollowMe();
			drone.state.changeFlightMode(ApmModes.ROTOR_LOITER);
		} else {
			drone.state.changeFlightMode(ApmModes.ROTOR_GUIDED);
			enableFollowMe();
		}
	}

	private void enableFollowMe() {
		drone.events.notifyDroneEvent(DroneEventsType.FOLLOW_START);
		Log.d("follow", "enable");
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();

		// Register the listener with the Location Manager to receive location
		// updates

		LocationRequest mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(MIN_TIME_MS);
		mLocationRequest.setFastestInterval(MIN_TIME_MS);
		mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_M);
		mLocationClient.requestLocationUpdates(mLocationRequest, this);

		followMeEnabled = true;
	}

	private void disableFollowMe() {
		if(followMeEnabled){
			Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
			followMeEnabled = false;
			Log.d("follow", "disable");
		}
		mLocationClient.removeLocationUpdates(this);
	}

	public boolean isEnabled() {
		return followMeEnabled;
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			if ((drone.state.getMode() != ApmModes.ROTOR_GUIDED)) {
				disableFollowMe();
			}
			break;
		default:
			return;

		}

	}

	public Length getRadius() {
		return followAlgorithm.radius;
	}

	@Override
	public void onLocationChanged(Location location) {
		MavLinkROI.setROI(drone, new Coord3D(location.getLatitude(),location.getLongitude(), new Altitude(0.0)));
		followAlgorithm.processNewLocation(location);
	}


	public void setType(FollowModes item) {
		followAlgorithm = item.getAlgorithmType(drone,new Length(5.0),MIN_TIME_MS);
	}


	public enum FollowModes {
		LEASH("Leash"), HEADING("Heading"), WAKEBOARD(
				"Wakeboard"),CIRCLE("Circle");
	
		private String name;
	
		FollowModes(String str) {
			name = str;
		}
		@Override
		public String toString() {
			return name;
		}
		
		public FollowType getAlgorithmType(Drone drone, Length radius, double mIN_TIME_MS) {
			switch (this) {
			default:
			case LEASH:
				return new FollowLeash(drone, radius, mIN_TIME_MS);
			case CIRCLE:
				return new FollowCircle(drone, radius, mIN_TIME_MS);
			case HEADING:
				return new FollowHeading(drone, radius, mIN_TIME_MS);
			case WAKEBOARD:
				return new FollowWakeboard(drone, radius, mIN_TIME_MS);
			}
		}
	}


	public void changeRadius(double increment) {
		followAlgorithm.changeRadius(increment);
		
	}
}