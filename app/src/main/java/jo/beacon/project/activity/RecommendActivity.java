package jo.beacon.project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jo.beacon.project.util.AddressInfo;
import jo.beacon.project.adapter.RecommendDTO;
import jo.beacon.project.network.MyService;
import jo.beacon.project.R;
import jo.beacon.project.adapter.RecommendActivityAdapter;

public class RecommendActivity extends AppCompatActivity {
    @BindView(R.id.tv_productName)
    TextView tv_product;

    @BindView(R.id.iv_product)
    ImageView iv_product;

    @BindView(R.id.item_list)
    RecyclerView mView;

    // http 통신 setting
    public AddressInfo info;
    public String ip;
    public int port;
    // RecycleView
    private RecommendActivityAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    //use at initialize & onClickView3dButton
    private final ArrayList<String> productName = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
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
        // Intent 데이터 수신
        Intent intent = getIntent();
        String strJsonData = intent.getStringExtra("product");

        JSONArray jarray = null;
        try {
            jarray = new JSONObject(strJsonData).getJSONArray("ITEMS");
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(i);
                productName.add(i, jObject.optString("itemName"));
                Log.e("BeaconTest", jObject.optString("itemName"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // init Data
        final ArrayList<RecommendDTO> data = new ArrayList<>();
        // 메인상품 이미지, 상품명
        tv_product.setText(productName.get(0));
        Glide.with(getApplicationContext()).load("http://" + ip + ":" + port + "/" + productName.get(0) + ".png")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(iv_product);

        // 추천상품 이미지
        for (int i = 1; i < productName.size(); i++) {
            String url = "http://" + ip + ":" + port + "/" + productName.get(i) + ".png";
            data.add(new RecommendDTO(productName.get(i), url));
        }

        // init LayoutManager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL); // 기본값이 VERTICAL
        // setLayoutManager
        mView.setLayoutManager(mLayoutManager);
        // init Adapter
        mAdapter = new RecommendActivityAdapter(this);

        // set Data
        mAdapter.setData(data);

        // set Adapter
        mView.setAdapter(mAdapter);


        // 백그라운드 서비스 종료.
        stopService(new Intent(this, MyService.class));
    }

    /*
     * 이전버튼
     * */
    @Override
    public void onBackPressed() {
        this.finish();
    }

    //----------------------------------------------------------------------------------------------
    // onDestroy
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickViewDetail(View view) {
        String url = "http://" + ip + ":" + port + "/" + productName.get(0) + ".png";
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("pName", productName.get(0));
        intent.putExtra("pImg", url);
        this.startActivity(intent);
    }
}
