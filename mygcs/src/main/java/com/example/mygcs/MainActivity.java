package com.example.mygcs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.MAVLink.common.msg_rc_channels_override;
import com.google.maps.android.SphericalUtil;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.GroundOverlay;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.ExperimentalApi;
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
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCommands;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DroneListener, TowerListener, LinkListener, OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    public enum My_Type {
        MISSION_START, MISSION_STOP, MISSION_WAIT, BASIC_MODE, MODE_WAIT, CAM_MODE, INTERVAL_MODE
    }
    private My_Type MyMissionState = My_Type.MISSION_WAIT;
    private My_Type MyModeState = My_Type.MODE_WAIT;

    MapFragment mNaverMapFragment = null;
    private Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private ControlTower controlTower;
    private final Handler handler = new Handler();
    private NaverMap nMap;
    private Marker drone_marker = new Marker();
    private Marker map_marker = new Marker();
    private Marker A_marker = new Marker();
    private Marker B_marker = new Marker();
    private Marker C_marker = new Marker();
    private Marker D_marker = new Marker();
    private PolylineOverlay polyline = new PolylineOverlay();
    private LatLng la1, la2;
    private List<LatLng> Map_point = new ArrayList<>();
    private Mission IntervalMission = new Mission();
    private WebView RaspberryStream;

    private PolylineOverlay Mapclic2_polyline = new PolylineOverlay();

    ArrayList Line = new ArrayList();
    private int TakeoffAltitude = 3;
    private boolean marker_count = true;
    private int dis = 50;
    private int Interval = 5;
    private int count1 = 0;

    boolean Map_move_rock_bool = true;
    boolean Canstral_bool = true;
    boolean Interval_bool = true;

    //조이스틱 조작시 드론의 속도
    double speedYaw =1.25;
    double speedUpDown = 1.5;
    double speedMove = 1.25;

    int cameraType = 0;

    //rc컨트롤러
    msg_rc_channels_override rc_override;

    //조이스틱
    RelativeLayout layout_Leftjoystick,layout_Rightjoystick;
    JoyStickClass jstickLeft,jstickRight;

    //test boolean
    boolean ch2 = true;
    private float angle = 30;
    private float cw = 1.0f;

    private Spinner modeSelector;

    ///소켓통신변수

    private Socket socket;  //소켓생성
    BufferedReader in;      //서버로부터 온 데이터를 읽는다.
    TextView output;        //화면구성
    String data;
    String data2;
    char data3[] = new char[6];
    int rc_mode_change= 0;
    boolean socket_bool = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Start mainActivity");
        super.onCreate(savedInstanceState);

        //상태창 제거 및 풀스크린
        hideUI();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //내위치
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        final Context context = getApplicationContext();
        this.controlTower = new ControlTower(context);
        this.drone = new Drone(context);

        //모드변경 스피너
        this.modeSelector = (Spinner) findViewById(R.id.modeSelect);
        this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFlightModeSelected(view);
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(10);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        //지도표시
        FragmentManager fm = getSupportFragmentManager();
        mNaverMapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mNaverMapFragment == null) {
            mNaverMapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mNaverMapFragment).commit();
        }
        mNaverMapFragment.getMapAsync(this);

        RaspberryStream = (WebView) findViewById(R.id.webView);

        WebSettings streamingSet = RaspberryStream.getSettings();//Mobile Web Setting
        streamingSet.setJavaScriptEnabled(true);//자바스크립트 허용
        streamingSet.setLoadWithOverviewMode(true);//컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정

        streamingSet.setBuiltInZoomControls(false);
        streamingSet.setUseWideViewPort(true);

        RaspberryStream.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        RaspberryStream.loadUrl("http://192.168.43.84:8080/?action=stream");


        rc_override = new msg_rc_channels_override();
        rc_override.chan4_raw = 1500;
        rc_override.chan3_raw = 1500;
        rc_override.chan2_raw = 1500;
        rc_override.chan1_raw = 1500;
        rc_override.target_system = 0;
        rc_override.target_component = 0;

        joystick();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    public void onMapReady(@NonNull NaverMap naverMap) {
        nMap = naverMap;

        //핸드폰위치표시
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        //기본지도표시
        naverMap.setMapType(NaverMap.MapType.Basic);

        //줌버튼 제거
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setZoomControlEnabled(false);
        //버튼이벤트
        my_btn_control();
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
        try {
            socket.close(); //소켓을 닫는다.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Drone Listener
    // ==========================================================
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


    /////////////////////버튼 이벤트//////////////////////////////
    public void my_btn_control() {
        final Button BtnMapCadstral = (Button) findViewById(R.id.btn_cadastral);
        final Button BtnLineClear = (Button) findViewById(R.id.line_clear);

        final Button armButton = (Button) findViewById(R.id.arm_button);
        final Button BtnMapMoveRock = (Button) findViewById(R.id.btn_map_move);

        final Button BtnMapTypeState = findViewById(R.id.mapbutton);
        final Button BtnMapTypeBasic = (Button) findViewById(R.id.basicMapbutton);
        final Button BtnMapTypeTerrain = (Button) findViewById(R.id.mapTerrainbutton);
        final Button BtnMapTypeSatellite = (Button) findViewById(R.id.mapSatellitebutton);

        final Button BtnModeBasic = (Button) findViewById(R.id.basic_mode_btn);
        final Button BtnModeInterval = (Button) findViewById(R.id.interval_monitoring_btn);
        final Button BtnModeCAM = (Button) findViewById(R.id.route_flight_mode_btn);
        final Button BtnModeState = (Button) findViewById(R.id.mode_btn);

        Button BtnMission = (Button) findViewById(R.id.mission_sent_btn);
        final Button BtnSetDistance = (Button) findViewById(R.id.test_btn);
        final Button BtnSetInterval = (Button) findViewById(R.id.rksrur_btn);

        final Button BtnAltitudeValue = (Button) findViewById(R.id.Altitude_Val);
        final Button BtnAltitudeDown = (Button) findViewById(R.id.Altitude_down);
        final Button BtnAltitudeUp = (Button) findViewById(R.id.Altitude_up);
        final WebView web = (WebView) findViewById(R.id.webView);

        final Button BtnX = (Button) findViewById(R.id.btn_back_X);
        final Button con = (Button) findViewById(R.id.connect);
        final TextView output = (TextView) findViewById(R.id.textView2);


        final RelativeLayout videocontrolview= findViewById(R.id.VideoControlView);
        con.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(socket_bool ==false){socket_bool =true;}
                else if(socket_bool ==true){socket_bool =false;}
                    Thread worker = new Thread() {    //worker 를 Thread 로 생성
                    public void run() { //스레드 실행구문
                        try {
                            //소켓을 생성하고 입출력 스트립을 소켓에 연결한다.
                            socket = new Socket("192.168.43.84", 8888); //소켓생성

                            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //데이터 수신시 stream을 받아들인다.
                            while (socket_bool) {
                                data = in.readLine(); // in으로 받은 데이타를 String 형태로 읽어 data 에 저장
                                //in.read(data3,0,6);
                                //data = String.valueOf(data3);
                                output.setText(data);
                                //Bdn.setText(data);
                                Log.d("datav","데이터 : " + data);
                                stack();

                                //Log.d("datav2","데이터 : " + rc_mode_change);
                                //rc_socket();

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                worker.start();
            }
        });




        //////////////////////////미션버튼///////////////////////////////////
        /*BtnMission.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyMissionState == My_Type.MISSION_WAIT) {
                    MyMissionState = My_Type.MISSION_START;
                    BtnMission.setText("임무시작");
                } else if (MyMissionState == My_Type.MISSION_START) {
                    MyMissionState = My_Type.MISSION_STOP;
                    BtnMission.setText("임무중지");
                } else if (MyMissionState == My_Type.MISSION_STOP) {
                    MyMissionState = My_Type.MISSION_WAIT;
                    BtnMission.setText("임무전송");
                }
            }
        });*/

        //////////////////TakeOff 고도 변경//////////////////

        BtnAltitudeValue.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BtnAltitudeUp.getVisibility() == view.VISIBLE) {
                    BtnAltitudeUp.setVisibility(View.INVISIBLE);
                    BtnAltitudeDown.setVisibility(View.INVISIBLE);
                } else if (BtnAltitudeUp.getVisibility() == view.INVISIBLE) {
                    BtnAltitudeUp.setVisibility(View.VISIBLE);
                    BtnAltitudeDown.setVisibility(View.VISIBLE);
                }
            }
        });
        //고도up
        BtnAltitudeUp.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                TakeoffAltitude++;
                ShowAltitude();
            }
        });
        //고도down
        BtnAltitudeDown.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                TakeoffAltitude--;
                ShowAltitude();
            }
        });

        ///////////////////////////////////모드변경//////////////////////////////////////////////
        BtnModeState.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BtnModeBasic.getVisibility() == view.VISIBLE) {
                    BtnModeBasic.setVisibility(View.INVISIBLE);
                    BtnModeInterval.setVisibility(View.INVISIBLE);
                    BtnModeCAM.setVisibility(View.INVISIBLE);
                } else if (BtnModeBasic.getVisibility() == view.INVISIBLE) {
                    BtnModeBasic.setVisibility(View.VISIBLE);
                    BtnModeInterval.setVisibility(View.VISIBLE);
                    BtnModeCAM.setVisibility(View.VISIBLE);
                }
            }
        });

        //일반모드
        BtnModeBasic.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                BtnMission.setVisibility(View.INVISIBLE);
                BtnSetDistance.setVisibility(View.INVISIBLE);
                BtnSetInterval.setVisibility(View.INVISIBLE);
                BtnModeBasic.setVisibility(View.INVISIBLE);
                BtnModeInterval.setVisibility(View.INVISIBLE);
                BtnModeCAM.setVisibility(View.INVISIBLE);

                MyModeState = My_Type.BASIC_MODE;
                BtnModeState.setText("일반모드");
            }
        });
        //CAM모드
        BtnModeCAM.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                BtnMission.setVisibility(View.INVISIBLE);
                BtnSetDistance.setVisibility(View.INVISIBLE);
                BtnSetInterval.setVisibility(View.INVISIBLE);
                BtnModeBasic.setVisibility(View.INVISIBLE);
                BtnModeInterval.setVisibility(View.INVISIBLE);
                BtnModeCAM.setVisibility(View.INVISIBLE);

                BtnModeState.setVisibility(View.INVISIBLE);
                armButton.setVisibility(View.INVISIBLE);


                //버튼가리기

                BtnAltitudeValue.setVisibility(View.INVISIBLE);
                BtnAltitudeDown.setVisibility(View.INVISIBLE);
                BtnAltitudeUp.setVisibility(View.INVISIBLE);

                web.setVisibility(View.VISIBLE);
                BtnX.setVisibility(View.VISIBLE);

                BtnMapMoveRock.setVisibility(View.INVISIBLE);
                BtnMapTypeState.setVisibility(View.INVISIBLE);
                BtnMapCadstral.setVisibility(View.INVISIBLE);
                BtnLineClear.setVisibility(View.INVISIBLE);

                videocontrolview.setVisibility(View.VISIBLE);


                /*Intent intent = new Intent(getApplicationContext(), SubActivity.class);
                startActivity(intent);*/
                MyModeState = My_Type.CAM_MODE;
                BtnModeState.setText("CAM");
            }
        });
        BtnX.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                web.setVisibility(View.INVISIBLE);
                BtnX.setVisibility(View.INVISIBLE);
                BtnAltitudeValue.setVisibility(View.VISIBLE);
                BtnAltitudeDown.setVisibility(View.VISIBLE);
                BtnAltitudeUp.setVisibility(View.VISIBLE);
                BtnMapMoveRock.setVisibility(View.VISIBLE);
                BtnMapTypeState.setVisibility(View.VISIBLE);
                BtnMapCadstral.setVisibility(View.VISIBLE);
                BtnLineClear.setVisibility(View.VISIBLE);
                BtnModeState.setVisibility(View.VISIBLE);
                armButton.setVisibility(View.VISIBLE);

                videocontrolview.setVisibility(View.INVISIBLE);
            }
        });
        //간격감시모드
        BtnModeInterval.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                BtnMission.setVisibility(View.VISIBLE);
                BtnSetDistance.setVisibility(View.VISIBLE);
                BtnSetInterval.setVisibility(View.VISIBLE);
                BtnModeBasic.setVisibility(View.INVISIBLE);
                BtnModeInterval.setVisibility(View.INVISIBLE);
                BtnModeCAM.setVisibility(View.INVISIBLE);
                MyModeState = My_Type.INTERVAL_MODE;
                BtnModeState.setText("간격감시");
            }
        });


        /////////////////////////지도타입변경///////////////////////////////////
        BtnMapTypeState.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BtnMapTypeBasic.getVisibility() == view.VISIBLE) {
                    BtnMapTypeBasic.setVisibility(View.INVISIBLE);
                    BtnMapTypeTerrain.setVisibility(View.INVISIBLE);
                    BtnMapTypeSatellite.setVisibility(View.INVISIBLE);
                } else if (BtnMapTypeBasic.getVisibility() == view.INVISIBLE) {
                    BtnMapTypeBasic.setVisibility(View.VISIBLE);
                    BtnMapTypeTerrain.setVisibility(View.VISIBLE);
                    BtnMapTypeSatellite.setVisibility(View.VISIBLE);
                }
            }
        });
        //일반지도
        BtnMapTypeBasic.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                nMap.setMapType(NaverMap.MapType.Basic);
                BtnMapTypeState.setText("일반지도");

                BtnMapTypeBasic.setVisibility(View.INVISIBLE);
                BtnMapTypeTerrain.setVisibility(View.INVISIBLE);
                BtnMapTypeSatellite.setVisibility(View.INVISIBLE);
            }
        });
        //지형도
        BtnMapTypeTerrain.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                nMap.setMapType(NaverMap.MapType.Terrain);
                BtnMapTypeState.setText("지형도");

                BtnMapTypeBasic.setVisibility(View.INVISIBLE);
                BtnMapTypeTerrain.setVisibility(View.INVISIBLE);
                BtnMapTypeSatellite.setVisibility(View.INVISIBLE);
            }
        });
        //위성지도
        BtnMapTypeSatellite.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                nMap.setMapType(NaverMap.MapType.Satellite);
                BtnMapTypeState.setText("위성지도");

                BtnMapTypeBasic.setVisibility(View.INVISIBLE);
                BtnMapTypeTerrain.setVisibility(View.INVISIBLE);
                BtnMapTypeSatellite.setVisibility(View.INVISIBLE);
            }
        });


        //맵 이동, 잠금 버튼
        BtnMapMoveRock.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Map_move_rock_bool == true) {
                    Map_move_rock_bool = false;
                    BtnMapMoveRock.setText("맵 잠금");
                } else if (Map_move_rock_bool == false) {
                    Map_move_rock_bool = true;
                    BtnMapMoveRock.setText("맵 이동");
                }
            }
        });

        //지적도 ON OFF
        BtnMapCadstral.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Canstral_bool == true) {
                    nMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, true);
                    BtnMapCadstral.setText("지적도 off");
                    Canstral_bool = false;
                } else if (Canstral_bool == false) {
                    nMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false);
                    BtnMapCadstral.setText("지적도 on");
                    Canstral_bool = true;
                }
            }
        });
        // Clear
        BtnLineClear.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

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
        });


    }

    //상태창제거
    private void hideUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }
    //takeoff고도값출력
    private void ShowAltitude() {
        Button BtnAltitudeValue = (Button) findViewById(R.id.Altitude_Val);
        BtnAltitudeValue.setText(String.format("고도  %dM", TakeoffAltitude));
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
            ControlApi.getApi(this.drone).takeoff((double) TakeoffAltitude, new AbstractCommandListener() {

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

    //Arm button update
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



    ////////////////////앱 상단 UI 업데이트/////////////////////////////////
    //드론마커표시및방향
    protected void marker() {
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        LatLng droneCoordinates =
           new LatLng(droneGps.getPosition().getLatitude(), droneGps.getPosition().getLongitude());
        OverlayImage image = OverlayImage.fromResource(R.drawable.circled_chevron_up_100px);
        drone_marker.setPosition(droneCoordinates);
        drone_marker.setMap(nMap);
        drone_marker.setIcon(image);
        drone_marker.setWidth(150);
        drone_marker.setHeight(150);
        drone_marker.setAnchor(new PointF(0.5f, 0.5f));

        //드론회전시 마커회전
        Attitude Marker_Yaw = this.drone.getAttribute(AttributeType.ATTITUDE);
        int int_Yaw = (int) Marker_Yaw.getYaw();
        if (int_Yaw > -180 || int_Yaw < 0) {
            int_Yaw = 360 + int_Yaw;
        }
        drone_marker.setAngle(int_Yaw);

        //드론마커위치 맵잠금
        if (Map_move_rock_bool == true) {
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(droneCoordinates);
            nMap.moveCamera(cameraUpdate);
        } else if (Map_move_rock_bool == false) {
            nMap.moveCamera(null);
        }

        //모드를바꾸면 간헐적으로 궤적이생성되지않음??원인이뭐지
        //드론 궤적
        Line.add(droneCoordinates);
        polyline.setCoords(Line);
        polyline.setWidth(10);
        polyline.setColor(Color.RED);
        polyline.setJoinType(PolylineOverlay.LineJoin.Round);
        polyline.setMap(nMap);
    }

    // 전압
    protected void updateVoltage() {
        TextView VoltageTextView = (TextView) findViewById(R.id.VoltageValueTextView);
        Battery BatteyVoltage = this.drone.getAttribute(AttributeType.BATTERY);
        //Log.d("test","배터리확인 : " + BatteyVoltage.getBatteryVoltage());
        VoltageTextView.setText(String.format("%.1f", BatteyVoltage.getBatteryVoltage()) + "V");
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
        ArrayAdapter<VehicleMode> vehicleModeArrayAdapter =
        new ArrayAdapter<VehicleMode>(this, android.R.layout.simple_spinner_item, vehicleModes);
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

    //yaw값
    protected void updateYaw() {
        TextView YawTextView = (TextView) findViewById(R.id.YawValueTextView);
        Attitude Yaw = this.drone.getAttribute(AttributeType.ATTITUDE);
        if (Yaw.getYaw() > -180 && Yaw.getYaw() < 0) {
            YawTextView.setText(String.format("%.1f", 360 + Yaw.getYaw()) + "Deg");
        } else if (Yaw.getYaw() < 180 && Yaw.getYaw() > 0) {
            YawTextView.setText(String.format("%.1f", Yaw.getYaw()) + "Deg");
        }
    }

    //위성개수
    protected void updateSatellite() {
        TextView SatelliteTextView = (TextView) findViewById(R.id.satelliteValueTextView);
        Gps satellite = this.drone.getAttribute(AttributeType.GPS);
        // Log.d("test","좌표확인 : " + satellite.getPosition().getLatitude());
        SatelliteTextView.setText(String.format("%d", satellite.getSatellitesCount()));
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

    ////////////////////////////////////////////////////////////////////////////


    //////////////////////////// RC TEST LINE//////////////////////////////////

    public void stack()
    {
        //장애물
        if (data.endsWith("e")) {
            rc_mode_change = 1;
            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
            rc_override.chan1_raw = 1500; // 중앙
            rc_override.chan2_raw = 1440; //전진
        } else if (data.endsWith("t")) {
            //Toast.makeText(getApplicationContext(), "오른쪽", Toast.LENGTH_SHORT).show();
            rc_mode_change= 2;
            //rc_override.chan1_raw = 1440;// 왼쪽비행
            rc_override.chan2_raw = 1500;
            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
        } else if (data.endsWith("l")) {
            //Toast.makeText(getApplicationContext(), "왼쪽", Toast.LENGTH_SHORT).show();
            rc_mode_change= 3;
            //rc_override.chan1_raw = 1560;//오른쪽비행
            rc_override.chan2_raw = 1500;

            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
        }

        //장애물
        /*else if (data.endsWith("l")) {
            //왼쪽
            rc_mode_change= 3;
            rc_override.chan1_raw = 1420;
            rc_override.chan2_raw = 1500;
            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
        }
        else if (data.endsWith("t")) {
            //오른쪽비행
            rc_mode_change= 3;
            rc_override.chan1_raw = 1580;
            rc_override.chan2_raw = 1500;
            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
        } if (data.endsWith("e")) {
            //전진
            rc_mode_change = 1;
            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
            rc_override.chan1_raw = 1500;
            rc_override.chan2_raw = 1560;
    }*/

        else {
            //Toast.makeText(getApplicationContext(), "안되잖아?", Toast.LENGTH_SHORT).show();
            rc_mode_change=1;
            rc_override.chan4_raw = 1500;
            //rc_override.chan1_raw = 1500;
            //rc_override.chan2_raw = 1500;

            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
        }
    }

    //yaw_rc_test
    public void test(View view) {
        /*Button btn = (Button) findViewById(R.id.rc_test_btn);
        if (ch2 == true) {
            ch2 = false;
            btn.setText("move");

            msg_rc_channels_override rc_override;
            rc_override = new msg_rc_channels_override();
            rc_override.chan4_raw = 1500;
            rc_override.chan3_raw = 1500;
            rc_override.chan2_raw = 1500;
            rc_override.chan1_raw = 1500;
            rc_override.target_system = 0;
            rc_override.target_component = 0;

            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
            Toast.makeText(getApplicationContext(), "호버링중", Toast.LENGTH_SHORT).show();

        } else if (ch2 == false) {
            ch2 = true;
            btn.setText("호버링");


            msg_rc_channels_override rc_override;
            rc_override = new msg_rc_channels_override();
            rc_override.chan4_raw = 1500;
            rc_override.chan3_raw = 1500;
            rc_override.chan2_raw = 900;
            rc_override.chan1_raw = 1500;
            rc_override.target_system = 0;
            rc_override.target_component = 0;


            ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
            Toast.makeText(getApplicationContext(), "이동중~~~~.", Toast.LENGTH_SHORT).show();
        }*/

    }
    public void test2(View view) {

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

    //드론 회전각도 및 방향 설정
    public void angle_select(View view) {

        /*final EditText edittext = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("각 설정");
        builder.setMessage("각:");
        builder.setView(edittext);
        builder.setPositiveButton("저장",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String d = edittext.getText().toString();
                        angle = Float.parseFloat(d);
                        Toast.makeText(getApplicationContext(), edittext.getText().toString(), Toast.LENGTH_LONG).show();
                        Log.d("test", "각 : " + dis);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();*/
    }

    public void cw_select(View view) {

        /*final EditText edittext = new EditText(this);
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
                        Toast.makeText(getApplicationContext(), edittext.getText().toString(), Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();*/
    }
    /////////////가이드모드/////////////////////////////////////////////////////

    protected void Map_Click() {
        nMap.setOnMapLongClickListener((point, coord) -> {

            if (MyModeState == My_Type.BASIC_MODE) {
                LatLng guide_co = coord;
                OverlayImage image = OverlayImage.fromResource(R.drawable.filled_flag_2_40px);
                map_marker.setPosition(guide_co);
                map_marker.setMap(nMap);
                map_marker.setIcon(image);
                map_marker.setWidth(70);
                map_marker.setHeight(70);
                VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_GUIDED, new SimpleCommandListener() {
                    public void onSuccess() {
                        alertUser("간다...");
                    }
                });

                LatLong destination = new LatLong(coord.latitude, coord.longitude);
                ControlApi.getApi(this.drone).goTo(destination, true, new SimpleCommandListener() {
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
            } else if (MyModeState == My_Type.INTERVAL_MODE) {
                if (marker_count == true) {
                    OverlayImage image1 = OverlayImage.fromResource(R.drawable.map_pin_40px);

                    la1 = coord;
                    Interval_bool = true;

                    A_marker.setPosition(la1);
                    A_marker.setMap(nMap);
                    A_marker.setIcon(image1);
                    A_marker.setWidth(70);
                    A_marker.setHeight(70);
                    marker_count = false;

                    //기존에찍혀잇던 마커및 라인 제거
                    Mapclic2_polyline.setMap(null);
                    B_marker.setMap(null);
                    C_marker.setMap(null);
                    D_marker.setMap(null);
                    Map_point.clear();
                    map_marker.setMap(null);
                    IntervalMission.clear();
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

                    //두마커사이 거리 계산 GoogleMap Api 오픈소스사용
                    double distance = discomputeAngleBetween(la1, la2) * 6371009.0D;
                    Toast.makeText(this, (int) distance + "m", Toast.LENGTH_LONG).show();

                    LatLong point1 = change_LatLong(la2);
                    LatLong point2 = change_LatLong(la1);

                    for (int i = 0; i <= dis; i++) {
                        if (i % Interval == 0) {
                            LatLng a = change_LatLng(MathUtils.newCoordFromBearingAndDistance(point2, MathUtils.getHeadingFromCoordinates(point2, point1) + 90, i));
                            LatLng b = change_LatLng(MathUtils.newCoordFromBearingAndDistance(point1, MathUtils.getHeadingFromCoordinates(point2, point1) + 90, i));
                            if (Interval_bool == true) {
                                Map_point.add(a);
                                Map_point.add(b);
                                Interval_bool = false;
                            } else if (Interval_bool == false) {
                                Map_point.add(b);
                                Map_point.add(a);
                                Interval_bool = true;
                            }
                        }
                        if (i == dis) {
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
                        IntervalMission.addMissionItem(way);
                    }
                    Toast.makeText(this, MathUtils.getHeadingFromCoordinates(point2, point1) + "도", Toast.LENGTH_LONG).show();

                    Mapclic2_polyline.setCoords(Map_point);
                    Mapclic2_polyline.setColor(Color.WHITE);
                    Mapclic2_polyline.setMap(nMap);
                }
                //mission();
            }

        });
    }

    private void set_mis_text() {
        Button BtnSendMission = (Button) findViewById(R.id.mode_btn);
        alertUser("미션 업로드 완료");
        BtnSendMission.setText("임무 시작");
    }

    /*private void mission() {
        if (MyMissionState == My_Type.MISSION_WAIT) {
            MissionApi.getApi(this.drone).setMission(IntervalMission, true);
        } else if (MyMissionState == My_Type.MISSION_START) {
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_AUTO, new SimpleCommandListener() {
                public void onSuccess() {
                    alertUser("미션시작");
                }
            });
        } else if (MyMissionState == My_Type.MISSION_STOP) {
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_LOITER, new SimpleCommandListener() {
                public void onSuccess() {
                    alertUser("미션중지");
                }
            });
        }
    }*/
    public void missionbtn(View view) {
        Button mission = (Button) findViewById(R.id.mission_sent_btn);

        if (count1 == 0) {
            MissionApi.getApi(this.drone).setMission(IntervalMission, true);
            mission.setText("임무시작");
            //mis_set();
            count1 = 1;
        } else if (count1 == 1) {
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_AUTO, new SimpleCommandListener() {
                public void onSuccess() {
                    alertUser("미션시작");
                }
            });
            mission.setText("임무중지");
            //mis_start();
            count1 = 2;
        } else if (count1 == 2) {
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_LOITER, new SimpleCommandListener() {
                public void onSuccess() {
                    alertUser("미션중지");
                }
            });
            // mis_stop();
            mission.setText("임무전송");
            count1 = 0;
        }
    }


    //간격감시 거리, 간격 설정
    public void select1(View view) {
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
                        Toast.makeText(getApplicationContext(), edittext.getText().toString(), Toast.LENGTH_LONG).show();
                        Log.d("test", "거리값 : " + dis);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    public void select2(View view) {
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
                        Interval = Integer.parseInt(d);
                        Toast.makeText(getApplicationContext(), edittext.getText().toString(), Toast.LENGTH_LONG).show();
                        Log.d("test", "거리값 : " + Interval);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }




    //LatLong, LatLng 변환함수
    private LatLong change_LatLong(LatLng a) {
        LatLong Long = new LatLong(a.latitude, a.longitude);
        return Long;
    }

    private LatLng change_LatLng(LatLong a) {
        double Lat = a.getLatitude();
        double Long = a.getLongitude();
        LatLng new_Lng = new LatLng(Lat, Long);
        return new_Lng;
    }

    ///조이스틱/////
    public void joystick(){

        layout_Leftjoystick = (RelativeLayout)findViewById(R.id.layout_joystick);
        layout_Rightjoystick = (RelativeLayout)findViewById(R.id.layout_joystick2);

        jstickLeft = new JoyStickClass(getApplicationContext()
                , layout_Leftjoystick, R.drawable.image_button);
        jstickLeft.setStickSize(150, 150);
        jstickLeft.setLayoutSize(500, 500);
        jstickLeft.setLayoutAlpha(150);
        jstickLeft.setStickAlpha(100);
        jstickLeft.setOffset(90);
        jstickLeft.setMinimumDistance(20);

        jstickRight = new JoyStickClass(getApplicationContext()
                , layout_Rightjoystick, R.drawable.image_button);
        jstickRight.setStickSize(150, 150);
        jstickRight.setLayoutSize(500, 500);
        jstickRight.setLayoutAlpha(150);
        jstickRight.setStickAlpha(100);
        jstickRight.setOffset(90);
        jstickRight.setMinimumDistance(20);


        //조이스틱 왼쪽 컨트롤러
        layout_Leftjoystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                jstickLeft.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int Xpoint = jstickLeft.getX();
                    int Ypoint = jstickLeft.getY();
                    if(Xpoint<0){Xpoint= -Xpoint;}
                    else if(Xpoint>200){Xpoint=200;}
                    if(Ypoint<0){Ypoint= -Ypoint;}
                    else if(Ypoint>200){Ypoint=200;}

                    double XmotorValue = Xpoint*speedYaw;
                    double YmotorValue = Ypoint*speedUpDown;

                    if(jstickLeft.getDistance()>200) { float distance = 200; }

                    int direction = jstickLeft.get8Direction();
                    if(direction == JoyStickClass.STICK_UP) {
                        rc_override.chan3_raw = 1500 + (int)YmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("상승");
                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                        rc_override.chan3_raw = 1500 + (int)YmotorValue;
                        rc_override.chan4_raw = 1500 + (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));

                    } else if(direction == JoyStickClass.STICK_RIGHT) {
                        rc_override.chan4_raw = 1500 + (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("시계회전");
                    } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
                        rc_override.chan3_raw = 1500 - (int)YmotorValue;
                        rc_override.chan4_raw = 1500 + (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));

                    } else if(direction == JoyStickClass.STICK_DOWN) {
                        rc_override.chan3_raw = 1500 - (int)YmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("하강");
                    } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
                        rc_override.chan3_raw = 1500 - (int)YmotorValue;
                        rc_override.chan4_raw = 1500 - (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));

                    } else if(direction == JoyStickClass.STICK_LEFT) {
                        rc_override.chan4_raw = 1500 - (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("반시계회전");
                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
                        rc_override.chan3_raw = 1500 + (int)YmotorValue;
                        rc_override.chan4_raw = 1500 - (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));

                    } else if(direction == JoyStickClass.STICK_NONE) {
                        rc_override.chan1_raw = 1500;
                        rc_override.chan2_raw = 1500;
                        rc_override.chan3_raw = 1500;
                        rc_override.chan4_raw = 1500;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("정지");
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    rc_override.chan1_raw = 1500;
                    rc_override.chan2_raw = 1500;
                    rc_override.chan3_raw = 1500;
                    rc_override.chan4_raw = 1500;
                    ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                    alertUser("정지");
                }
                return true;
            }
        });

        //조이스틱 오른쪽 컨트롤러
        layout_Rightjoystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                jstickRight.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int Xpoint = jstickRight.getX();
                    int Ypoint = jstickRight.getY();
                    if(Xpoint < 0){Xpoint = - Xpoint;}
                    else if(Xpoint > 200){Xpoint = 200;}
                    if(Ypoint < 0){Ypoint = - Ypoint;}
                    else if(Ypoint > 200){Ypoint = 200;}

                    double XmotorValue = Xpoint * speedMove;
                    double YmotorValue = Ypoint * speedMove;

                    int direction = jstickRight.get8Direction();
                    if(direction == JoyStickClass.STICK_UP) {
                        rc_override.chan2_raw = 1500 - (int)YmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("전진");
                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                        rc_override.chan2_raw = 1500 - (int)YmotorValue;
                        rc_override.chan1_raw = 1500 + (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));

                    } else if(direction == JoyStickClass.STICK_RIGHT) {
                        rc_override.chan1_raw = 1500 + (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("우회전");

                    } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
                        rc_override.chan2_raw = 1500 + (int)YmotorValue;
                        rc_override.chan1_raw = 1500 + (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));

                    } else if(direction == JoyStickClass.STICK_DOWN) {
                        rc_override.chan2_raw = 1500 + (int)YmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("후진");
                    } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
                        rc_override.chan2_raw = 1500 + (int)YmotorValue;
                        rc_override.chan1_raw = 1500 - (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));

                    } else if(direction == JoyStickClass.STICK_LEFT) {
                        rc_override.chan1_raw = 1500 - (int)XmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("좌회전");

                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
                        rc_override.chan1_raw = 1500 - (int)XmotorValue;
                        rc_override.chan2_raw = 1500 - (int)YmotorValue;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));


                    } else if(direction == JoyStickClass.STICK_NONE) {
                        rc_override.chan1_raw = 1500;
                        rc_override.chan2_raw = 1500;
                        rc_override.chan3_raw = 1500;
                        rc_override.chan4_raw = 1500;
                        ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                        alertUser("정지");

                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    rc_override.chan1_raw = 1500;
                    rc_override.chan2_raw = 1500;
                    rc_override.chan3_raw = 1500;
                    rc_override.chan4_raw = 1500;
                    ExperimentalApi.getApi(drone).sendMavlinkMessage(new MavlinkMessageWrapper(rc_override));
                    alertUser("정지");

                }
                return true;
            }
        });

    }

    public void onCameraModeChange(View view){
        final Button btnconnect= (Button)findViewById(R.id.btnConnect);


        final RelativeLayout videocontrolview= findViewById(R.id.VideoControlView);
        if(cameraType==0){

            videocontrolview.setVisibility(View.VISIBLE);
            cameraType=1;
        }
        else if(cameraType==1)
        {
            videocontrolview.setVisibility(View.INVISIBLE);
            cameraType=0;
        }

    }



    ////소켓통신////



    /////////////////건든적없는코드/////////////////////////////////


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
    // DroneKit-Android Listener
    // ==========================================================
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


    //두좌표사이거리계산
    public double discomputeAngleBetween(LatLng from, LatLng to) {
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

}

