package jo.beacon.project.network;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import jo.beacon.project.activity.PopupActivity;
import jo.beacon.project.adapter.PopupListDTO;

public class MyService extends Service implements BeaconConsumer {
    // iBecaon 구분 코드 "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25" 다른 제조사인 비콘을 사용하려면 바꿔야함.
    // m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24
    private static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    // DecimalFormat 특정 패턴으로 값을 포맷한다. 0.00
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    // TAG Log 확인. 제목
    private static final String TAG = "MyService";
    BeaconManager mBeaconManager;

    private IBinder mIBinder = new MyBinder();
    private String msg = "";

    private String productName = "";

    // map
    Map<Integer, Boolean> aMap;

    class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("LOG", "onBind()");
        return mIBinder;
    }

    @Override
    public void onCreate() {
        Log.e("LOG", "onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LOG", "onStartCommand()");
        // 비콘 스캔 시작! (백그라운드)
        aMap = new HashMap<Integer, Boolean>();
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER)); // iBeacon로 설정
        mBeaconManager.bind(MyService.this);
        return super.onStartCommand(intent, flags, startId);
        //  return START_STICKY
    }

    @Override
    public void onDestroy() {
        Log.e("LOG", "onDestroy()");
        mBeaconManager.unbind(this);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("LOG", "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                ArrayList<PopupListDTO> array = new ArrayList<>();
                if (beacons.size() > 0) {
                    Iterator<Beacon> iterator = beacons.iterator();
                    while (iterator.hasNext()) {
                        Beacon beacon = iterator.next();
                        // 비콘 정보
                        String uuid = beacon.getId1().toString();
                        int major = beacon.getId2().toInt();
                        int minor = beacon.getId3().toInt();
                        Log.e("beaconID", uuid + ", " + major + ", " + minor);
                        if (aMap.get(major) == null) {
                            aMap.put(major, true);
                        } else {
                            if (aMap.get(major))
                                continue;
                        }
                        // 비콘 감지 거리
                        double distance = Double.parseDouble(decimalFormat.format(beacon.getDistance()));

                        try {
                            // HTTP 통신
                            msg = new Task("COMMUNICATION", getApplicationContext()).execute(uuid, GetDevicesUUID(getApplicationContext()), "" + major, "" + minor, "notification", "split").get(); // 서버와 통신!
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // JSON
                        JSONArray jarray = null;
                        JSONObject json = null;
                        JSONObject jObject = null;
                        try {
                            json = new JSONObject(msg);
                            jarray = json.getJSONArray("ITEMS");
                            jObject = jarray.getJSONObject(0);
                            productName = jObject.optString("itemName");
                            array.add(new PopupListDTO(productName, json));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (array.size() > 0) {
                        Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
                        intent.putExtra("product", array); /*송신*/
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
            }
        });
        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {

            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }


            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // 디바이스의 uuid 얻어오기.
    private String GetDevicesUUID(Context mContext) {
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }
}