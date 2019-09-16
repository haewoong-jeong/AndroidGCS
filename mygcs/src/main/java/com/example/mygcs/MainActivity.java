package com.example.mygcs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.maps.android.SphericalUtil;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.GroundOverlay;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.action.ControlActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;
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
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCommands;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DroneListener, TowerListener, LinkListener, OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    MapFragment mNaverMapFragment = null;
    private Drone drone;
    private MavLinkDrone drone2;
    private int droneType = Type.TYPE_UNKNOWN;
    private ControlTower controlTower;
    private final Handler handler = new Handler();
    private NaverMap nMap;
    private Marker drone_marker = new Marker();
    private Marker my_pos = new Marker();
    private Marker map_marker = new Marker();
    private Marker map_marker2 = new Marker();
    private Marker A_marker = new Marker();
    private Marker B_marker = new Marker();
    private Marker C_marker = new Marker();
    private Marker D_marker = new Marker();
    private  PolylineOverlay polyline = new PolylineOverlay();
    private final InfoWindow location = new InfoWindow();
    private LatLng la1, la2;
    private List<LatLng> Map_point =new ArrayList<>();
    private List<Waypoint> wayp = new ArrayList<>();
    private   LatLongAlt coordinate;
    private Mission mis = new Mission();

    private PolylineOverlay Mapclic2_polyline = new PolylineOverlay();

    ArrayList Line = new ArrayList();
    private int count = 3;
    private int count1 = 0;
    private int count2 = 0;
    private boolean marker_count=true;
    private int dis=50;
    private int rksrur=5;

    boolean check = false;
    boolean check1 = true;
    boolean check2 = true;
    boolean check3 = true;
    boolean check4 = false;
    boolean your_name = true;

    //test boolean
    boolean ch = true;
    boolean ch1 = true;
    private int count_test=0;
    private float angle = 30;
    private float cw =1.0f;


    

    private Spinner modeSelector;

    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Start mainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        final Context context = getApplicationContext();
        this.controlTower = new ControlTower(context);
        this.drone = new Drone(context);

        this.modeSelector = (Spinner) findViewById(R.id.modeSelect);
        this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFlightModeSelected(view);
                ((TextView)parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView)parent.getChildAt(0)).setTextSize(10);
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

        Button btn = findViewById(R.id.rc_test_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });

        Button btn2 = findViewById(R.id.rc_stop_btn);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test2();
            }
        });

    }

    public void testMethod() {
        nMap.setMapType(NaverMap.MapType.Satellite);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    public void onMapReady(@NonNull NaverMap naverMap) {

        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        naverMap.setMapType(NaverMap.MapType.Basic);
        Gps test = this.drone.getAttribute(AttributeType.GPS);
        nMap = naverMap;
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
                //mypos();
                line();

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
            case AttributeEvent.GUIDED_POINT_UPDATED:
                Map_Click();

                break;
            case AttributeEvent.MISSION_SENT:
                set_mis_text();


            case AttributeEvent.HOME_UPDATED:
                updateDistanceFromHome();
                break;


            default:
                // Log.i("DRONE_EVENT", event); //Uncomment to see events from the drone
                break;
        }
    }
    //지적도
    public void cadastral(View view){
        Button map_Cad = (Button) findViewById(R.id.btn_cadastral);

        if(check2==true) {
            nMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, true);
            map_Cad.setText("지적도 off");
            check2=false;
        }
        else if(check2==false){
            nMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false);
            map_Cad.setText("지적도 on");
            check2=true;
        }
    }

    //궤적 클리어
    public void lineClear(View view){
        Button Clear = (Button) findViewById(R.id.line_clear);

        Mapclic2_polyline.setMap(null);
        A_marker.setMap(null);
        B_marker.setMap(null);
        C_marker.setMap(null);
        D_marker.setMap(null);
        map_marker.setMap(null);
        Map_point.clear();
        //map_marker.setMap(nMap);

        Line.clear();
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
    //드론마커표시및방향
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
        drone_marker.setAnchor(new PointF(0.5f,0.5f));

        if(int_Yaw >-180 || int_Yaw<0)  {  int_Yaw= 360+int_Yaw; }
        drone_marker.setAngle(int_Yaw);
    }
    //사용자위치마커
    protected  void mypos()  {
        OverlayImage image =OverlayImage.fromResource(R.drawable.floating_guru_48px);
        Home mypos =this.drone.getAttribute(AttributeType.HOME);
        LatLong myPosition = mypos.getCoordinate();
        LatLng my = new LatLng(myPosition.getLatitude(), myPosition.getLongitude());

        my_pos.setPosition(my);
        my_pos.setIcon(image);
        my_pos.setWidth(150);
        my_pos.setHeight(150);
        my_pos.setAnchor(new PointF(0.5f,0.5f));
        my_pos.setMap(nMap);
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
            ControlApi.getApi(this.drone).takeoff((double)count, new AbstractCommandListener() {

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
        altitudeTextView.setText(String.format("%3.1f", droneAltitude.getRelativeAltitude()) + "m");
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
       // Log.d("test","좌표확인 : " + satellite.getPosition().getLatitude());
        SatelliteTextView.setText(String.format("%d", satellite.getSatellitesCount()));
    }
    //yaw값
    protected void updateYaw(){
        TextView YawTextView = (TextView) findViewById(R.id.YawValueTextView);
        Attitude Yaw = this.drone.getAttribute(AttributeType.ATTITUDE);
        if(Yaw.getYaw() >-180 && Yaw.getYaw()<0){
        YawTextView.setText(String.format("%.1f", 360+Yaw.getYaw()) + "Deg");}
        else if(Yaw.getYaw() <180 && Yaw.getYaw()>0) {YawTextView.setText(String.format("%.1f", Yaw.getYaw()) + "Deg");}
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

    //고도변경
    public void Altitude_up(View view){
        Button up = (Button) findViewById(R.id.Altitude_up);
        Button Altval = (Button) findViewById(R.id.Altitude_Val);
        count++;
        Altval.setText(String.format("고도  %dM", count));

    }
    public void Altitude_down(View view){
        Button down = (Button) findViewById(R.id.Altitude_down);
        Button Altval = (Button) findViewById(R.id.Altitude_Val);

        count--;
        Altval.setText(String.format("고도  %dM", count));
    }
    public void Altitude_val(View view){
        Button Altval = (Button) findViewById(R.id.Altitude_Val);
        Button down = (Button) findViewById(R.id.Altitude_down);
        Button up = (Button) findViewById(R.id.Altitude_up);

        if(check3 == true) {
            up.setVisibility(View.INVISIBLE);
            down.setVisibility(View.INVISIBLE);
            check3 = false;
        }
        else if(check3 == false) {
            up.setVisibility(View.VISIBLE);
            down.setVisibility(View.VISIBLE);
            check3 = true;
        }
    }


    protected void Map_Click(){


            nMap.setOnMapLongClickListener((point, coord) -> {

                if(count2==1) {
                    LatLng la = coord;
                    OverlayImage image = OverlayImage.fromResource(R.drawable.filled_flag_2_40px);
                    map_marker.setPosition(la);
                    map_marker.setMap(nMap);
                    map_marker.setIcon(image);
                    map_marker.setWidth(70);
                    map_marker.setHeight(70);
                    VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_GUIDED, new SimpleCommandListener() {
                        public void onSuccess() {
                            alertUser("간다...");
                        }
                    });

                    LatLong here = new LatLong(coord.latitude, coord.longitude);
                    ControlApi.getApi(this.drone).goTo(here, true, new SimpleCommandListener() {
                        public void onSuccess() {
                            alertUser("다왔다...");
                        }

                        @Override
                        public void onError(int i) {
                            alertUser("Unable to go to.");
                        }

                        @Override
                        public void onTimeout() {
                            alertUser("Unable to go to.");
                        }
                    });
                }
                else if(count2==3) {
                    if (marker_count == true) {
                        OverlayImage image1 = OverlayImage.fromResource(R.drawable.map_pin_40px);

                        la1 = coord;
                        your_name = true;

                        A_marker.setPosition(la1);
                        A_marker.setMap(nMap);
                        A_marker.setIcon(image1);
                        A_marker.setWidth(70);
                        A_marker.setHeight(70);
                        marker_count = false;
                        Mapclic2_polyline.setMap(null);
                        B_marker.setMap(null);
                        C_marker.setMap(null);
                        D_marker.setMap(null);
                        Map_point.clear();
                        map_marker.setMap(null);
                        mis.clear();
                       // map_marker.setMap(nMap);

                    } else if (marker_count == false) {
                        OverlayImage image = OverlayImage.fromResource(R.drawable.map_pin_filled_50px);
                        OverlayImage image1 = OverlayImage.fromResource(R.drawable.map_pin_40px);
                        la2 = coord;
                        B_marker.setPosition(la2);
                        B_marker.setMap(nMap);
                        B_marker.setIcon(image);
                        B_marker.setWidth(70);
                        B_marker.setHeight(70);
                        marker_count = true;
                        double distance = discomputeAngleBetween(la1, la2) * 6371009.0D;
                        Toast.makeText(this, (int) distance + "m", Toast.LENGTH_LONG).show();

                        LatLong point1 = change_LatLong(la2);
                        LatLong point2 = change_LatLong(la1);

                        for (int i = 0; i <= dis; i ++) {
                            if(i%rksrur==0) {
                                LatLng a =change_LatLng(MathUtils.newCoordFromBearingAndDistance(point2, MathUtils.getHeadingFromCoordinates(point2, point1) + 90, i));
                                LatLng b =change_LatLng(MathUtils.newCoordFromBearingAndDistance(point1, MathUtils.getHeadingFromCoordinates(point2, point1) + 90, i));
                                if (your_name == true) {
                                    Map_point.add(a);
                                    Map_point.add(b);
                                    your_name = false;
                                } else if (your_name == false) {
                                    Map_point.add(b);
                                    Map_point.add(a);
                                    your_name = true;
                                }
                            }
                            if(i==dis)
                            {
                                C_marker.setPosition(Map_point.get(Map_point.size() - 1));
                                C_marker.setMap(nMap);
                                C_marker.setIcon(image);
                                C_marker.setWidth(70);
                                C_marker.setHeight(70);

                                D_marker.setPosition(Map_point.get(Map_point.size() - 2));
                                D_marker.setMap(nMap);
                                D_marker.setIcon(image1);
                                D_marker.setWidth(70);
                                D_marker.setHeight(70);

                            }
                        }
                       Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);


                        for (int i = 0; i < Map_point.size(); i++) {
                            Waypoint way = new Waypoint();
                            way.setDelay(1);
                            way.setCoordinate(new LatLongAlt(Map_point.get(i).latitude, Map_point.get(i).longitude, droneAltitude.getRelativeAltitude()));
                            mis.addMissionItem(way);
                        }


                        Toast.makeText(this, MathUtils.getHeadingFromCoordinates(point2, point1) + "도", Toast.LENGTH_LONG).show();

                        Mapclic2_polyline.setCoords(Map_point);
                        Mapclic2_polyline.setColor(Color.WHITE);
                        Mapclic2_polyline.setMap(nMap);
                    }
                }

                if(count2==2){


                    enableVirtualStick();
                    sendVirtualStickCommands(2000,2000,2000);



                }
                if(count2==4)
                {
                    VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_GUIDED, new SimpleCommandListener() {
                        public void onSuccess() {
                            alertUser("회전중...");
                        }
                    });

                        ControlApi.getApi(this.drone).turnTo(180, 1.0f, true, new SimpleCommandListener() {
                            public void onSuccess() {
                                alertUser("북쪽회전완료...");
                            }
                            @Override
                            public void onError(int i) {
                                alertUser("Unable .");
                            }

                            @Override
                            public void onTimeout() {
                                alertUser("Unable.");
                            }
                        });
                    }


            });

    }

    //모드변경
    public void modeButton(View view) {
        Button basicmode = (Button) findViewById(R.id.basic_mode_btn);
        Button interval = (Button) findViewById(R.id.interval_monitoring_btn);
        Button area = (Button) findViewById(R.id.Area_Monitoring_btn);
        Button route = (Button) findViewById(R.id.route_flight_mode_btn);
        Button statemodebutton = (Button) findViewById(R.id.mode_btn);

        if(check4 == true) {
            basicmode.setVisibility(View.INVISIBLE);
            interval.setVisibility(View.INVISIBLE);
            area.setVisibility(View.INVISIBLE);
            route.setVisibility(View.INVISIBLE);
            check4 = false;
        }
        else if(check4 == false) {
            basicmode.setVisibility(View.VISIBLE);
            interval.setVisibility(View.VISIBLE);
            area.setVisibility(View.VISIBLE);
            route.setVisibility(View.VISIBLE);
            check4 = true;
        }
    }
    public void basicmodeButton(View view) {
        Button statemodebutton = (Button) findViewById(R.id.mode_btn);
        Button basicmode = (Button) findViewById(R.id.basic_mode_btn);
        Button mission =  (Button) findViewById(R.id.mission_sent_btn);
        //추가란
        mission.setVisibility(View.INVISIBLE);
        count2=1;
        statemodebutton.setText("일반모드");
    }
    public void routeodeButton(View view){
        Button statemodebutton = (Button) findViewById(R.id.mode_btn);
        Button route = (Button) findViewById(R.id.route_flight_mode_btn);
        Button mission =  (Button) findViewById(R.id.mission_sent_btn);
        mission.setVisibility(View.INVISIBLE);
        count2=2;
        statemodebutton.setText("경로비행");
    }
    public void intervalmodeButton(View view){
        Button statemodebutton = (Button) findViewById(R.id.mode_btn);
        Button test = (Button) findViewById(R.id.test_btn);
        Button test1 = (Button) findViewById(R.id.rksrur_btn);
        Button mission =  (Button) findViewById(R.id.mission_sent_btn);
        //추가란
        mission.setVisibility(View.VISIBLE);
        test.setVisibility(View.VISIBLE);
        test1.setVisibility(View.VISIBLE);
        count2=3;
        statemodebutton.setText("간격감시");

    }
    public void AreamodeButton(View view){
        Button statemodebutton = (Button) findViewById(R.id.mode_btn);
        Button area = (Button) findViewById(R.id.Area_Monitoring_btn);
        Button mission =  (Button) findViewById(R.id.mission_sent_btn);
        mission.setVisibility(View.INVISIBLE);
        count2=4;
        statemodebutton.setText("면적감시");
    }
    public void set_mis_text(){
        alertUser("미션 업로드 완료");
        Button BtnSendMission = (Button) findViewById(R.id.mode_btn);
        BtnSendMission.setText("임무 시작");
    }

    public void select1(View view){

        final EditText edittext = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("거리 설정");
        builder.setMessage("거리:");
        builder.setView(edittext);
        builder.setPositiveButton("저장",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String d = edittext.getText().toString();
                            dis = Integer.parseInt(d);
                            Toast.makeText(getApplicationContext(),edittext.getText().toString() ,Toast.LENGTH_LONG).show();
                            Log.d("test","거리값 : " + dis);
                        }
                    });
            builder.setNegativeButton("취소",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.show();
        }
    public void select2(View view){

        final EditText edittext = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);


        builder.setTitle("간격 설정");
        builder.setMessage("간격:");
        builder.setView(edittext);
        builder.setPositiveButton("저장",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String d = edittext.getText().toString();
                        rksrur = Integer.parseInt(d);
                        Toast.makeText(getApplicationContext(),edittext.getText().toString() ,Toast.LENGTH_LONG).show();
                        Log.d("test","거리값 : " + rksrur);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    /*public void mis_set(){
        MissionApi.getApi(this.drone).setMission(mis, true);
    }
    public void mis_start(){

        VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_AUTO , new SimpleCommandListener(){
            public void onSuccess() {
                alertUser("미션시작");
            }
        });
}
    public void mis_stop(){
        VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_LOITER , new SimpleCommandListener(){
            public void onSuccess() {
                alertUser("미션중지");
            }
        });
    }*/

    public void missionbtn(View view){
        Button mission = (Button) findViewById(R.id.mission_sent_btn);

        if(count1==0) {
            MissionApi.getApi(this.drone).setMission(mis, true);
            mission.setText("임무시작");
            //mis_set();
            count1=1;
        }
        else if(count1==1){
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_AUTO , new SimpleCommandListener(){
                public void onSuccess() {
                    alertUser("미션시작");
                }
            });
            mission.setText("임무중지");
            //mis_start();
            count1=2;
        }
        else if(count1==2){
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_LOITER , new SimpleCommandListener(){
                public void onSuccess() {
                    alertUser("미션중지");
                }
            });
           // mis_stop();
            mission.setText("임무전송");
            count1=0;
        }
    }
    //LatLong, LatLng 변환함수
    public LatLong change_LatLong(LatLng a){
        LatLong Long = new LatLong(a.latitude,a.longitude);
        return  Long;
    }
    public LatLng change_LatLng(LatLong a){
        double Lat= a.getLatitude();
        double Long = a.getLongitude();
        LatLng new_Lng = new LatLng(Lat,Long);
        return new_Lng;
    }

    //두좌표사이거리계산
    public double discomputeAngleBetween(LatLng from, LatLng to){
        return distanceRadians(Math.toRadians(from.latitude), Math.toRadians(from.longitude), Math.toRadians(to.latitude), Math.toRadians(to.longitude));
    }
    private static double distanceRadians(double lat1, double lng1, double lat2, double lng2) {
        return arcHav(havDistance(lat1, lat2, lng1 - lng2));
    }
    static double arcHav(double x) {
        return 2.0D * Math.asin(Math.sqrt(x));
    }
    static double havDistance(double lat1, double lat2, double dLng) {
        return hav(lat1 - lat2) + hav(dLng) * Math.cos(lat1) * Math.cos(lat2);
    }
    static double hav(double x) {
        double sinHalf = Math.sin(x * 0.5D);
        return sinHalf * sinHalf;
    }


    //드론 궤적
   protected void line(){
        Gps drone_Gps = this.drone.getAttribute(AttributeType.GPS);
        LatLng drone = new LatLng(drone_Gps.getPosition().getLatitude(), drone_Gps.getPosition().getLongitude());

        Line.add(drone);
        polyline.setCoords(Line);
        polyline.setWidth(10);
        polyline.setColor(Color.RED);
        polyline.setJoinType(PolylineOverlay.LineJoin.Round);
        polyline.setMap(nMap);
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


    //yaw_rc_test
    protected void test() {

        enableVirtualStick();
        sendVirtualStickCommands(0.5f,0.5f,0.5f);
        /*
        ControlApi.getApi(this.drone).turnTo(30, 1.0f, true, new SimpleCommandListener() {
            public void onSuccess() {
                alertUser("회전완료...");
            }
            @Override
            public void onError(int i) {
                alertUser("Unable .");
            }

            @Override
            public void onTimeout() {
                alertUser("timeout.");
            }
        });*/
    }
    protected void test2() {

        ControlApi.getApi(this.drone).turnTo(angle, cw, true, new SimpleCommandListener() {
            public void onSuccess() {
                alertUser("회전완료...");
            }
            @Override
            public void onError(int i) {
                alertUser("Unable .");
            }

            @Override
            public void onTimeout() {
                alertUser("timeout.");
            }
        });

    }
    //값설정

    public void angle_select(View view){

        final EditText edittext = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("각 설정");
        builder.setMessage("각:");
        builder.setView(edittext);
        builder.setPositiveButton("저장",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String d = edittext.getText().toString();
                        angle = Float.parseFloat(d);
                        Toast.makeText(getApplicationContext(),edittext.getText().toString() ,Toast.LENGTH_LONG).show();
                        Log.d("test","각 : " + dis);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }
    public void cw_select(View view){

        final EditText edittext = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);


        builder.setTitle("방향 설정");
        builder.setMessage("방향:");
        builder.setView(edittext);
        builder.setPositiveButton("저장",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String d = edittext.getText().toString();
                        cw = Float.parseFloat(d);
                        Toast.makeText(getApplicationContext(),edittext.getText().toString() ,Toast.LENGTH_LONG).show();
                        Log.d("test","방향 : " + rksrur);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    public void enableVirtualStick(){
        if(this.drone.isConnected()){
            ControlApi.getApi(this.drone).enableManualControl(true, new ControlApi.ManualControlStateListener() {
                @Override
                public void onManualControlToggled(boolean isEnabled) {
                    if(isEnabled)
                    {
                        alertUser("Joystick Mode Enabled");
                    }
                    else{alertUser("Could not enable Joystick Mode");}
                }
            });
        } else {
            alertUser("No Vehicle is connected");

        }
    }

    public void sendVirtualStickCommands(float pitch_val, float roll_val, float throttle_val ){


        ControlApi.getApi(this.drone).manualControl(pitch_val,roll_val,throttle_val, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                Log.d("mycheck","성공입니다.");
                //alertUser("Joystick command accepted by vehicle");
            }
            @Override
            public void onError(int i) {
                Log.d("mycheck","에러입니다");
                //alertUser("Error in Joystick");
            }
            @Override
            public void onTimeout() {
                Log.d("mycheck","타임아웃입니다");
                //alertUser("Joystick command timed out");
            }

        });



    }

}
