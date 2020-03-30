package jo.beacon.project.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jo.beacon.project.R;
import jo.beacon.project.adapter.PopupAdapter;
import jo.beacon.project.adapter.PopupDTO;
import jo.beacon.project.adapter.PopupListDTO;
import jo.beacon.project.databinding.ActivityPopupBinding;
import jo.beacon.project.network.MyService;
import jo.beacon.project.util.PreferenceUtil;

public class PopupActivity extends Activity {
    // ListView
    public PopupAdapter adapter;
    public ActivityPopupBinding binding;
    public Vector<PopupDTO> items;
    public LinearLayoutManager manager;

    @BindView(R.id.tv_user_id)
    TextView tvUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // setContentView(R.layout.activity_popup);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_popup);

        ButterKnife.bind(this);
        initialize();
    }

    //----------------------------------------------------------------------------------------------
    // initialize
    //----------------------------------------------------------------------------------------------
    public void initialize() {
        // userID
        String userId = PreferenceUtil.getInstance(this).get(PreferenceUtil.PreferenceKey.userId, ""); // get userID
        if (userId.equals(""))
            userId = "guest";
        tvUserId.setText("["+userId + "]");
        // Intent 데이터 수신
        Intent intent = getIntent();
        ArrayList<PopupListDTO> list = (ArrayList<PopupListDTO>) intent.getSerializableExtra("product");

        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recommendList.setLayoutManager(manager);
        // ***** ListView *****
        items = new Vector<>();
        for (int i = 0; i < list.size(); i++) {
            items.add(new PopupDTO(list.get(i).getProductName(), list.get(i).getJobj()));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new PopupAdapter(items, PopupActivity.this);
                binding.recommendList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    // OnClick
    //----------------------------------------------------------------------------------------------
    @OnClick({R.id.btn_close})
    void onClickClose(View view) {
        switch (view.getId()) {
            case R.id.btn_close: {
                stopService(new Intent(this, MyService.class));
                this.finish();
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    //----------------------------------------------------------------------------------------------
    // onDestroy
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}