package jo.beacon.project.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
import jo.beacon.project.network.Task;


public class MypageActivity extends AppCompatActivity {
    // http 통신 setting
    public AddressInfo info;
    public String ip;
    public int port;
    // recycleview
    private MainProductAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    @BindView(R.id.item_mypage)
    RecyclerView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        ButterKnife.bind(this);
        initialize();
    }


    //----------------------------------------------------------------------------------------------
    // initialize
    //----------------------------------------------------------------------------------------------
    public void initialize() {
        // http 통신 setting
        info = new AddressInfo();
        ip = info.getIp();
        port = info.getPort();
        String userId = PreferenceUtil.getInstance(this).get(PreferenceUtil.PreferenceKey.userId, ""); // get userID

        ///// RecycleView
        String msg = "";
        try {
            msg = new Task("MYPAGE", this).execute(userId).get();
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

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // setLayoutManager
        mView.setLayoutManager(mLayoutManager);
        // init Adapter
        mAdapter = new MainProductAdapter(this);

        // set Data
        mAdapter.setData(data);

        // set Adapter
        mView.setAdapter(mAdapter);
    }

    //----------------------------------------------------------------------------------------------
    // OnClick
    //----------------------------------------------------------------------------------------------
    @OnClick({})
    void buttonEvents(View view) {
        switch (view.getId()) {

        }
    }

    //----------------------------------------------------------------------------------------------
    // onDestroy
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
