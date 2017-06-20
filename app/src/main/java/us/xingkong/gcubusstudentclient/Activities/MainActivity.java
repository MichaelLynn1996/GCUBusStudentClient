package us.xingkong.gcubusstudentclient.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;

import us.xingkong.gcubusstudentclient.Others.Caculator;
import us.xingkong.gcubusstudentclient.Others.Global;
import us.xingkong.gcubusstudentclient.R;
import us.xingkong.gcubusstudentclient.gbn.Net;
import us.xingkong.gcubusstudentclient.gbn.NetException;
import us.xingkong.gcubusstudentclient.gbn.NetListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import us.xingkong.gcubusstudentclient.Others.ActivityCollector;
import us.xingkong.gcubusstudentclient.gbn.Point;

/**
 * Created by SeaLynn0 on 2017/4/24.
 */

public class MainActivity extends ToolbarBaseActivity implements AMapLocationListener {

    private DrawerLayout mDrawerLayout;
    MapView mMapView = null;
    AMap aMap;
    Net net;
    int[] ids;
    com.getbase.floatingactionbutton.FloatingActionButton fab_me;
    com.getbase.floatingactionbutton.FloatingActionButton fab_campus;
    FloatingActionsMenu fam;

    public AMapLocationClient mLocationClient = null;
    AMapLocationClientOption mLocationOption;
    MyLocationStyle myLocationStyle;

    LatLng myLatLng;
    //LatLngBounds campusBounds = new LatLngBounds(new LatLng(23.427081,113.173019),new LatLng(23.438657,113.174349));

    boolean isFinish;
    boolean isRunning = false;
    boolean need2refresh = false;
    boolean isAdd = false;
    boolean isDel = false;
    boolean isNotActive;

    SparseArray<Marker> array = new SparseArray<>();
    SparseArray<Caculator> array_cal = new SparseArray<>();

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Marker marker = aMap.addMarker(new MarkerOptions());

                array_cal.put((int) msg.obj, new Caculator(myLatLng));

                array.put((int) msg.obj, marker);
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_buslocation));
                isAdd = true;
            } else if (msg.what == 2) {
                double[] data = (double[]) msg.obj;

                int id = (int) data[0];
                Caculator cal = array_cal.get(id);
                Marker mark = array.get(id);
                mark.setPosition(new LatLng(data[1], data[2]));
                cal.addPoint(data[1], data[2]);
                mark.setTitle(id + "号车");
                mark.setSnippet("距离：" + Global.roundTo1(cal.getDistance()) + "米\n速度：" + Global.roundTo1(cal.getSpeed()) + "m/s\n剩余时间：" + Global.roundTo1(cal.getTime()) + "秒");
            } else if (msg.what == 3) {
                int id = (int) msg.obj;
                Marker mark = array.get(id);
                mark.remove();
                isDel = true;
            }
        }
    };

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        net = new Net(Net.SERVER_TEST);

        pref = this.getSharedPreferences("userData", MODE_PRIVATE);
        isNotActive = pref.getBoolean("isNotActive", false);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        if (navView.getHeaderCount() > 0) {
            TextView name = (TextView) navView.getHeaderView(0).findViewById(R.id.textView);
            name.setText(LoginActivity.name + "    " + "▼");
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Global.makeDialog(MainActivity.this, R.string.tips, R.string.isLogout, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            editor = pref.edit();
                            editor.putBoolean("autoLogin", false);
                            editor.apply();
                            Toast.makeText(MainActivity.this, R.string.success_register, Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                }
            });
        }

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.map) {
                    mDrawerLayout.closeDrawers();
                    goToCampusCenter();
                } else if (id == R.id.about) {
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                } else if (id == R.id.exit) {
                    Global.makeDialog(MainActivity.this, R.string.tips, R.string.exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCollector.finishAll();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                } else if (id == R.id.setting) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });

        initMap();

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        if (!isNotActive) {
            activate();
        }

        goToCampusCenter();
        updateDriverIDs();

        fab_me = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.locate_me);
        fab_campus = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.locate_campus);
        fam = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        if (isNotActive) {
            fam.removeButton(fab_me);
        }
        fab_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.collapse();
                goToMyPosition();
            }
        });
        fab_campus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.collapse();
                goToCampusCenter();
            }
        });
    }

    private void goToCampusCenter() {
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(23.431827, 113.173008), 17, 30, 0)));//23.433141,113.174011 //23.4340300000, 113.1728190000
    }

    private void goToMyPosition() {
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(myLatLng, 18, 30, 0)));
    }

    private void updateDriverIDs() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    return;
                }

                isRunning = true;
                net.GetVaildbusID(new NetListener() {

                    @Override
                    public void done(Object data, NetException e) {
                        if (e != null) {
                            ids = null;
                            System.out.println(e.toString());
                        } else {
                            try {
                                ids = Net.getIDs(data.toString());
                                try {
                                    getDriverLocation();
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }

                            } catch (JSONException e1) {
                                ids = null;
                                System.out.println(e1.toString());
                            }

                        }
                        isRunning = false;
                    }
                });


            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
        service.scheduleAtFixedRate(runnable, 0, 2, TimeUnit.SECONDS);
    }

    private void getDriverLocation() throws InterruptedException {

        if (ids == null) {
            return;
        }

        isFinish = true;
        need2refresh = false;

        for (final int id : ids) {

            Marker marker = array.get(id);
            if (marker == null) {


                isAdd = false;
                Message msg = new Message();
                msg.what = 1;
                msg.obj = id;
                handler.sendMessage(msg);
                while (!isAdd) {
                    Thread.sleep(10);
                }

            }
            isFinish = false;
            net.GetPoint(id, new NetListener() {
                @Override
                public void done(Object data, NetException e) {
                    if (e != null) {
                        System.out.println(e.toString());
                    } else {

                        try {
                            Point point = new Point(data.toString());
                            double[] dt = new double[]{(double) id, point.getLatitude(), point.getLongitude()};
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = dt;
                            handler.sendMessage(msg);
                        } catch (JSONException e1) {
                            Message msg = new Message();
                            msg.what = 3;
                            msg.obj = id;
                            isDel = false;
                            handler.sendMessage(msg);
                            while (!isDel) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e2) {
                                    System.out.println(e2.toString());
                                }
                            }
                            array.remove(id);
                            array_cal.remove(id);
                        }
                    }
                    isFinish = true;
                }
            });

            while (!isFinish) {
                Thread.sleep(30);
            }
        }


        for (int i = 0; i < array.size(); i++) {
            boolean has = false;
            for (int id1 : ids) {
                if (array.keyAt(i) == id1) {
                    has = true;
                    break;
                }
            }

            if (!has) {
                need2refresh = true;
                Message msg = new Message();
                msg.what = 3;
                msg.obj = array.keyAt(i);
                isDel = false;
                handler.sendMessage(msg);
                while (!isDel) {
                    Thread.sleep(10);
                }
                int id = array.keyAt(i);
                array.remove(id);
                array_cal.remove(id);
                i--;
            }

        }

        if (need2refresh) {
            handler.sendEmptyMessage(0);
        }

        isRunning = false;


    }

    private void initMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            //aMap.setMapStatusLimits(campusBounds);
        }
    }

    void initMyLocationStyle() {
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        myLocationStyle.radiusFillColor(android.R.color.transparent);
        myLocationStyle.strokeColor(android.R.color.transparent);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    /**
     * 激活定位
     */

    public void activate() {
        if (mLocationClient == null) {
            //初始化定位
            mLocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
            //设置定位间隔,单位毫秒,默认为2000ms
            mLocationOption.setInterval(2000);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption.setHttpTimeOut(30000);
            mLocationOption.setMockEnable(true);//设置是否允许模拟位置,默认为false，不允许模拟位置
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();//启动定位

            initMyLocationStyle();
        }
    }

    /**
     * 停止定位
     */

    public void deactivate() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {


        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                myLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
                Toast.makeText(MainActivity.this, errText, Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        if (!isNotActive) {
            mLocationClient.startLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        if (!isNotActive) {
            mLocationClient.stopLocation();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

}