package jo.beacon.project.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import jo.beacon.project.R;

/**
 * 인트로 화면 설정
 */
public class SplashActivity extends BaseActivity {
    /**
     * GPS 권한 요청
     */
    private static final int PERMISSIONS = 100;
    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * GPS 권한 체크 하는 메소드
     * @return 성공여부
     */
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSIONS);
            return false;
        }
        return true;
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted.
                goMain(new Handler());
            }
        }
        /*
        switch (requestCode) {
            case PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted.
                    goMain(new Handler());
                }
            }
        }*/
    }

    /**
     * activity_splash 를 불러오면서 어플을 실행한다.
     * @param savedInstanceState 다른 액티비티 & 가로,세로 전환 시 전역변수 유지
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (checkPermissions()) {
            goMain(new Handler());
        }
    }

    /**
     * MainActivity.class 호출
     * @param handler 1500ms 동안 정지
     */
    private void goMain(Handler handler) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1500);
    }

}
