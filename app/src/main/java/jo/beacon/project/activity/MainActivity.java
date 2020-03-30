package jo.beacon.project.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jo.beacon.project.R;
import jo.beacon.project.util.AddressInfo;
import jo.beacon.project.util.PreferenceUtil;
import jo.beacon.project.adapter.MainProductAdapter;
import jo.beacon.project.adapter.RecommendDTO;
import jo.beacon.project.network.MyService;
import jo.beacon.project.network.Task;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.drawer)
    View drawerView;
    @BindView(R.id.navigationView)
    NavigationView navigationView;
    // Intent
    public Intent intentService;

    // nav
    Button btLoginState;
    Button btRegister;

    // recycleview
    private MainProductAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    @BindView(R.id.item_main)
    RecyclerView mView;

    // http 통신 setting
    public AddressInfo info;
    public String ip;
    public int port;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize();
    }

    //----------------------------------------------------------------------------------------------
    // initialize
    //----------------------------------------------------------------------------------------------
    public void initialize() {
        // NavigationView
        View nav = navigationView.getHeaderView(0);
        TextView tvUserName = nav.findViewById(R.id.tv_user_name);
        btLoginState = nav.findViewById(R.id.bt_user_login_state);
        btRegister = nav.findViewById(R.id.bt_nav_register);

        btLoginState.setOnClickListener(this);
        btRegister.setOnClickListener(this);
        String userId = PreferenceUtil.getInstance(this).get(PreferenceUtil.PreferenceKey.userId, ""); // get userID
        if (userId.equals("") || userId.equals(null)) { // 로그인이 안되어있으면
            userId = "guest";
        } else {
            btLoginState.setText("LOGOUT");
            //btRegister.setVisibility(View.GONE); // 해당 뷰를 안 보여줌(공간마저 감춤)
            btRegister.setText("MYPAGE");
        }
        tvUserName.setText("Hello, " + userId);
        // 백그라운드 서비스 시작(비콘)
        intentService = new Intent(this, MyService.class);
        startService(intentService);

        // RecycleView
        //item 명단 list 받아오기
        String msg = "";
        try {
            msg = new Task("HOME", this).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // json
        final ArrayList<String> productName = new ArrayList<>();

        JSONArray jarray = null;
        try {
            jarray = new JSONObject(msg).getJSONArray("ITEMS");
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(i);
                productName.add(i, jObject.optString("itemName"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // init Data
        final ArrayList<RecommendDTO> data = new ArrayList<>();

        // http 통신 setting
        info = new AddressInfo();
        ip = info.getIp();
        port = info.getPort();

        // 추천상품 이미지
        for (int i = 0; i < productName.size(); i++) {
            String url = "http://" + ip + ":" + port + "/" + productName.get(i) + ".png";
            data.add(new RecommendDTO(productName.get(i), url));
        }

        //------------------------------------------------------------------------------------------
        // 2020.01.07 add Item decoration for dual line recycler view
        //------------------------------------------------------------------------------------------
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mView.setLayoutManager(mLayoutManager);

        // init Adapter
        mAdapter = new MainProductAdapter(this);

        // set Data
        mAdapter.setData(data);

        // set Adapter
        mView.setAdapter(mAdapter);

        //for Smooth scroll
        mView.setHasFixedSize(true);
        mView.setItemViewCacheSize(20);
        mView.setNestedScrollingEnabled(true);

    }

    /**
     * nav.Onclick
     * @param view: 로그인(로그아웃), 회원가입 버튼
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_user_login_state: { // LOGIN, LOGOUT
                if (btLoginState.getText().equals("LOGIN")) // LOGIN
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                else { // LOGOUT
                    PreferenceUtil.getInstance(getApplicationContext()).remove(PreferenceUtil.PreferenceKey.userId); // remove userID
                    Intent intent = getIntent();
                    this.finish();
                    startActivity(intent);
                }
                break;
            }
            case R.id.bt_nav_register: { // Register Activity
                if (btRegister.getText().equals("REGISTER"))
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                else
                    startActivity(new Intent(MainActivity.this, MypageActivity.class));
                break;
            }
        }
    }

    /**
     * onClick
     * case가 1개이므로 if로 변경
     * @param view: 우측 상단 메뉴
     */
    @OnClick({R.id.iv_left})
    void onClickMenu(View view) {
        if(view.getId() == R.id.iv_left) {
            drawerLayout.openDrawer(drawerView);
        }
//        switch (view.getId()) {
//            case R.id.iv_left: {
//                drawerLayout.openDrawer(drawerView);
//                break;
//            }
//        }
    }

    //----------------------------------------------------------------------------------------------
    // exitDialog
    //----------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        exitDialog();
    }

    public void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("확인");
        builder.setMessage("종료하시겠습니까?");
        // 다이얼로그
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceUtil.getInstance(getApplicationContext()).remove(PreferenceUtil.PreferenceKey.userId); // remove userID
                        stopService(intentService);
                        moveTaskToBack(true); // 현재 실행중인 어플리케이션을 백그라운드로 전환하기
                        finish(); // 종료
                        android.os.Process.killProcess(android.os.Process.myPid()); // 프로세스를 모두 종료
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        builder.show();
    }

    //----------------------------------------------------------------------------------------------
    // onDestroy
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
