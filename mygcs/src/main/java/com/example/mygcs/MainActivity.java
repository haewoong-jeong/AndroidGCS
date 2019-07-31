package com.example.mygcs;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DroneListener, TowerListener, LinkListener, OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    MapFragment mNaverMapFragment = null;
    private Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private ControlTower controlTower;
    private final Handler handler = new Handler();
    private NaverMap nMap;
    private Marker drone_marker = new Marker();
    boolean check = true;
    boolean check1 = true;

    private Spinner modeSelector;

    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Start mainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        final Context context = getApplicationContext();
        this.controlTower = new ControlTower(context);
        this.drone = new Drone(context);

        this.modeSelector = (Spinner) findViewById(R.id.modeSelect);
        this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFlightModeSelected(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        mNaverMapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mNaverMapFragment == null) {
            mNaverMapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mNaverMapFragment).commit();
        }
        mNaverMapFragment.getMapAsync(this);

        Button mapButton = findViewById(R.id.mapbutton);
        Button Basicbutton = (Button) findViewById(R.id.basicMapbutton);
        Button Terrainbutton = (Button) findViewById(R.id.mapTerrainbutton);
        Button Satellitebutton = (Button) findViewById(R.id.mapSatellitebutton);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapStateButton();
            }
        });
        Basicbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapbasicButton();
            }
        });
        Terrainbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapTerrainButton();
            }
        });
        Satellitebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapSatelliteButton();
            }
        });

        Button MoveMap = (Button) findViewById(R.id.btn_map_move);
        MoveMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateMapMoveButton();
            }
        });


        LocationOverlay locationOverlay = nMap.getLocationOverlay();
        locationOverlay.setVisible(true);
    }

    public void testMethod() {
        nMap.setMapType(NaverMap.MapType.Satellite);
    }

    public void onMapReady(@NonNull NaverMap naverMap) {

        naverMap.setMapType(NaverMap.MapType.Basic);
        Gps test = this.drone.getAttribute(AttributeType.GPS);
        nMap = naverMap;


        // LatLng knu = new LatLng(test.getPosition().getLatitude(), test.getPosition().getLongitude());
        //Log.d("test","좌표확인 : " + test.getPosition());

        // CameraPosition cameraPosition = new CameraPosition(knu, 9);
        // naverMap.setCameraPosition(cameraPosition);
        // Marker marker = new Marker();
        // marker.setPosition(knu);
        // marker.setMap(naverMap);
    }
    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (this.drone.isConnected()) {
            this.drone.disconnect();
            updateConnectedButton(false);
        }

        this.controlTower.unregisterDrone(this.drone);
        this.controlTower.disconnect();
    }
    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                alertUser("Drone Connected");
                updateConnectedButton(this.drone.isConnected());
                updateArmButton();
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                alertUser("Drone Disconnected");
                updateConnectedButton(this.drone.isConnected());
                updateArmButton();
                break;
            case AttributeEvent.STATE_UPDATED:
            case AttributeEvent.STATE_ARMING:
                updateArmButton();
                break;

            case AttributeEvent.TYPE_UPDATED:
                Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
                if (newDroneType.getDroneType() != this.droneType) {
                    this.droneType = newDroneType.getDroneType();
                    updateVehicleModesForType(this.droneType);
                }
                break;
            case AttributeEvent.GPS_POSITION:
                marker();
                camera();
                break;
            case AttributeEvent.BATTERY_UPDATED:
                updateVoltage();
                break;
            case AttributeEvent.GPS_COUNT:
                updateSatellite();

                break;
            case AttributeEvent.ATTITUDE_UPDATED:
                updateYaw();
                break;
            case AttributeEvent.STATE_VEHICLE_MODE:
                updateVehicleMode();
                break;
            case AttributeEvent.ALTITUDE_UPDATED:
                updateAltitude();
                break;
            case AttributeEvent.SPEED_UPDATED:
                updateSpeed();
                break;

            case AttributeEvent.HOME_UPDATED:
                updateDistanceFromHome();
                break;


            default:
                // Log.i("DRONE_EVENT", event); //Uncomment to see events from the drone
                break;
        }
    }
    //맵잠금
    protected  void camera()  {
        Button MapMove = (Button) findViewById(R.id.btn_map_move);
        Gps test = this.drone.getAttribute(AttributeType.GPS);
        LatLng knu = new LatLng(test.getPosition().getLatitude(), test.getPosition().getLongitude());
        if(check1==true)
        {CameraUpdate cameraUpdate = CameraUpdate.scrollTo(knu);  nMap.moveCamera(cameraUpdate);}
        else if(check1 == false) {
         nMap.moveCamera(null);}

    }
    protected void updateMapMoveButton(){
        Button MapMove = (Button) findViewById(R.id.btn_map_move);
        if (check1 == true) {
            MapMove.setText("맵 잠금");
            check1 = false;
        } else if(check1 == false) {
            MapMove.setText("맵 이동");
            check1 = true;

        }
    }
    //드론마커표시
    protected  void marker()  {
        OverlayImage image =OverlayImage.fromResource(R.drawable.circled_chevron_up_100px);
        Gps test = this.drone.getAttribute(AttributeType.GPS);
        LatLng knu = new LatLng(test.getPosition().getLatitude(), test.getPosition().getLongitude());
        Attitude Marker_Yaw = this.drone.getAttribute(AttributeType.ATTITUDE);
        int int_Yaw = (int)Marker_Yaw.getYaw();
        drone_marker.setPosition(knu);
        drone_marker.setMap(nMap);
        drone_marker.setIcon(image);
        drone_marker.setWidth(150);
        drone_marker.setHeight(150);

        if(int_Yaw >-180 || int_Yaw<0)  {  int_Yaw= 360+int_Yaw; }
        drone_marker.setAngle(int_Yaw);
    }

    //ARM
    public void onArmButtonTap(View view) {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);

        if (vehicleState.isFlying()) {
            // Land
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_LAND, new SimpleCommandListener() {
                @Override
                public void onError(int executionError) {
                    alertUser("Unable to land the vehicle.");
                }

                @Override
                public void onTimeout() {
                    alertUser("Unable to land the vehicle.");
                }
            });
        } else if (vehicleState.isArmed()) {
            // Take off
            ControlApi.getApi(this.drone).takeoff(3, new AbstractCommandListener() {

                @Override
                public void onSuccess() {
                    alertUser("Taking off...");
                }

                @Override
                public void onError(int i) {
                    alertUser("Unable to take off.");
                }

                @Override
                public void onTimeout() {
                    alertUser("Unable to take off.");
                }
            });
        } else if (!vehicleState.isConnected()) {
            // Connect
            alertUser("Connect to a drone first");
        } else {
            // Connected but not Armed
            VehicleApi.getApi(this.drone).arm(true, false, new SimpleCommandListener() {
                @Override
                public void onError(int executionError) {
                    alertUser("Unable to arm vehicle.");
                }

                @Override
                public void onTimeout() {
                    alertUser("Arming operation timed out.");
                }
            });
        }
    }
    protected void updateArmButton() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        Button armButton = (Button) findViewById(R.id.arm_button);

        if (!this.drone.isConnected()) {
            armButton.setVisibility(View.INVISIBLE);
        } else {
            armButton.setVisibility(View.VISIBLE);
        }

        if (vehicleState.isFlying()) {
            // Land
            armButton.setText("LAND");
        } else if (vehicleState.isArmed()) {
            // Take off
            armButton.setText("TAKE OFF");
        } else if (vehicleState.isConnected()) {
            // Connected but not Armed
            armButton.setText("ARM");
        }
    }
    //비행모드
    public void onFlightModeSelected(View view) {
        VehicleMode vehicleMode = (VehicleMode) this.modeSelector.getSelectedItem();

        VehicleApi.getApi(this.drone).setVehicleMode(vehicleMode, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser("Vehicle mode change successful.");
            }

            @Override
            public void onError(int executionError) {
                alertUser("Vehicle mode change failed: " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Vehicle mode change timed out.");
            }
        });
    }
    protected void updateVehicleModesForType(int droneType) {

        List<VehicleMode> vehicleModes = VehicleMode.getVehicleModePerDroneType(droneType);
        ArrayAdapter<VehicleMode> vehicleModeArrayAdapter = new ArrayAdapter<VehicleMode>(this, android.R.layout.simple_spinner_item, vehicleModes);
        vehicleModeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.modeSelector.setAdapter(vehicleModeArrayAdapter);
    }
    protected void updateVehicleMode() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        VehicleMode vehicleMode = vehicleState.getVehicleMode();
        ArrayAdapter arrayAdapter = (ArrayAdapter) this.modeSelector.getAdapter();
        this.modeSelector.setSelection(arrayAdapter.getPosition(vehicleMode));
    }
    //고도값
    protected void updateAltitude() {
        TextView altitudeTextView = (TextView) findViewById(R.id.altitudeValueTextView);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        altitudeTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");
    }
    //속도
    protected void updateSpeed() {
        TextView speedTextView = (TextView) findViewById(R.id.speedValueTextView);
        Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
        speedTextView.setText(String.format("%3.1f", droneSpeed.getGroundSpeed()) + "m/s");
    }
    //거리
    protected double distanceBetweenPoints(LatLongAlt pointA, LatLongAlt pointB) {
        if (pointA == null || pointB == null) {
            return 0;
        }
        double dx = pointA.getLatitude() - pointB.getLatitude();
        double dy = pointA.getLongitude() - pointB.getLongitude();
        double dz = pointA.getAltitude() - pointB.getAltitude();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    protected void updateDistanceFromHome() {
        TextView distanceTextView = (TextView) findViewById(R.id.distanceValueTextView);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        double vehicleAltitude = droneAltitude.getAltitude();
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        LatLong vehiclePosition = droneGps.getPosition();

        double distanceFromHome = 0;

        if (droneGps.isValid()) {
            LatLongAlt vehicle3DPosition = new LatLongAlt(vehiclePosition.getLatitude(), vehiclePosition.getLongitude(), vehicleAltitude);
            Home droneHome = this.drone.getAttribute(AttributeType.HOME);
            distanceFromHome = distanceBetweenPoints(droneHome.getCoordinate(), vehicle3DPosition);
        } else {
            distanceFromHome = 0;
        }

        distanceTextView.setText(String.format("%3.1f", distanceFromHome) + "m");
    }
    //위성개수
    protected void updateSatellite(){
        TextView SatelliteTextView = (TextView) findViewById(R.id.satelliteValueTextView);
        Gps satellite = this.drone.getAttribute(AttributeType.GPS);
        Log.d("test","좌표확인 : " + satellite.getPosition().getLatitude());
        SatelliteTextView.setText(String.format("%d", satellite.getSatellitesCount()));
    }
    //yaw값
    protected void updateYaw(){
        TextView YawTextView = (TextView) findViewById(R.id.YawValueTextView);
        Attitude Yaw = this.drone.getAttribute(AttributeType.ATTITUDE);
        YawTextView.setText(String.format("%.1f", Yaw.getYaw()) + "Deg");
    }
    // 전압
    protected void updateVoltage() {
        TextView VoltageTextView = (TextView) findViewById(R.id.VoltageValueTextView);
        Battery BatteyVoltage = this.drone.getAttribute(AttributeType.BATTERY);
        //Log.d("test","배터리확인 : " + BatteyVoltage.getBatteryVoltage());
        VoltageTextView.setText(String.format("%.1f", BatteyVoltage.getBatteryVoltage())+"V");
    }

    //드론연결
    protected void updateConnectedButton(Boolean isConnected) {
        Button connectButton = (Button) findViewById(R.id.btnConnect);
        if (isConnected) {
            connectButton.setText("Disconnect");
           // drone_marker.setMap(null);
        } else {
            connectButton.setText("Connect");

        }
    }
    public void onBtnConnectTap(View view) {
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        } else {
            ConnectionParameter params = ConnectionParameter.newUdpConnection(null);
            this.drone.connect(params);
        }
    }

    //지도상태변경
    protected void MapStateButton() {
        Button Basicbutton = (Button) findViewById(R.id.basicMapbutton);
        Button Terrainbutton = (Button) findViewById(R.id.mapTerrainbutton);
        Button Satellitebutton = (Button) findViewById(R.id.mapSatellitebutton);

        if(check == true) {
            Basicbutton.setVisibility(View.INVISIBLE);
            Terrainbutton.setVisibility(View.INVISIBLE);
            Satellitebutton.setVisibility(View.INVISIBLE);
            check = false;
        }
        else if(check == false) {
            Basicbutton.setVisibility(View.VISIBLE);
            Terrainbutton.setVisibility(View.VISIBLE);
            Satellitebutton.setVisibility(View.VISIBLE);
            check = true;
        }
    }
    protected void MapbasicButton() {
        Button statebutton = (Button) findViewById(R.id.mapbutton);
        Button Basicbutton = (Button) findViewById(R.id.basicMapbutton);
        nMap.setMapType(NaverMap.MapType.Basic);
        statebutton.setText("일반지도");
    }
    protected void MapTerrainButton(){
        Button statebutton = (Button) findViewById(R.id.mapbutton);
        Button Terrainbutton = (Button) findViewById(R.id.mapTerrainbutton);
        nMap.setMapType(NaverMap.MapType.Terrain);
        statebutton.setText("지형도");
    }
    protected void MapSatelliteButton(){
        Button statebutton = (Button) findViewById(R.id.mapbutton);
        Button Satellitebutton = (Button) findViewById(R.id.mapSatellitebutton);
        nMap.setMapType(NaverMap.MapType.Satellite);

        statebutton.setText("위성지도");

        OverlayImage image =OverlayImage.fromResource(R.drawable.middle_finger);
        drone_marker.setIcon(image);
        drone_marker.setWidth(150);
        drone_marker.setHeight(150);
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }
    @Override
    public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
        switch (connectionStatus.getStatusCode()) {
            case LinkConnectionStatus.FAILED:
                Bundle extras = connectionStatus.getExtras();
                String msg = null;
                if (extras != null) {
                    msg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                }
                alertUser("Connection Failed:" + msg);
                break;
        }
    }
    @Override
    public void onTowerConnected() {
        alertUser("DroneKit-Android Connected");
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
    }
    @Override
    public void onTowerDisconnected() {
        alertUser("DroneKit-Android Interrupted");
    }

    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }
}
