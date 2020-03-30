package jo.beacon.project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jo.beacon.project.R;
import jo.beacon.project.util.PreferenceUtil;
import jo.beacon.project.network.Task;


public class DetailsActivity extends AppCompatActivity {
    @BindView(R.id.iv_dImg)
    ImageView ivImg;
    @BindView(R.id.tv_dName)
    TextView tvItemName;
    @BindView(R.id.tv_quantity)
    TextView tvQuantity;
    @BindView(R.id.l_quantity)
    LinearLayout lQuantity;

    private int quantity;
    private int quantity_flag;
    private String itemName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        initialize();
    }


    //----------------------------------------------------------------------------------------------
    // initialize
    //----------------------------------------------------------------------------------------------
    public void initialize() {
        quantity = 1;
        tvQuantity.setText("[" + quantity + "]");
        // intent 데이터 수신
        Intent intent = getIntent();
        itemName = intent.getStringExtra("pName");
        String itemImg = intent.getStringExtra("pImg");
        // 상품명
        tvItemName.setText("[ " + itemName + " ]");
        // 이미지
        Glide.with(getApplicationContext()).load(itemImg)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ivImg);
        quantity_flag = 0;
        lQuantity.setVisibility(View.GONE); // 뷰를 안보이게 설정, 공간마저 감춤
    }

    //----------------------------------------------------------------------------------------------
    // OnClick
    //----------------------------------------------------------------------------------------------
    @OnClick({R.id.btn_quantity_minus, R.id.btn_quantity_add, R.id.btn_buy, R.id.btn_view3D, R.id.r1})
    void buttonEvents(View view) {
        switch (view.getId()) {
            case R.id.btn_quantity_minus: {
                if (quantity <= 1) {
                    Toast.makeText(this, "최소 수량은 1개입니다.", Toast.LENGTH_SHORT).show();
                    break;
                }
                quantity -= 1;
                tvQuantity.setText("[" + quantity + "]");
                break;
            }
            case R.id.btn_quantity_add: {
                quantity += 1;
                tvQuantity.setText("[" + quantity + "]");
                break;
            }
            case R.id.btn_view3D: {
                Intent intent = new Intent(this, ARCameraActivity.class);
                intent.putExtra("pName", itemName );
                this.startActivity(intent);
                break;
            }
            case R.id.btn_buy: {
                if (quantity_flag == 0) { // quantity_flag 0이면 수량선택 레이아웃 뷰를 화면에 보여지게 함
                    quantity_flag = 1;
                    lQuantity.setVisibility(View.VISIBLE);
                    break;
                } else { // quantity_flag 0이 아니면 주문을 할 수 있게 하고, 주문이 완료되면 수량선택 레이아웃 뷰를 화면에서 감춤
                    String userId = PreferenceUtil.getInstance(this).get(PreferenceUtil.PreferenceKey.userId, ""); // get userID
                    if (userId.equals("")) { // 로그인 하지 않았을 때
                        Toast.makeText(this, "로그인이 필요합니다. ", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    String msg = "";
                    try {
                        msg = new Task("ORDER", this).execute(userId, itemName, tvQuantity.getText().toString()).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if (msg.equals("true")) { // 주문이 완료되면 서버에서 true 수신됨
                        Toast.makeText(this, "주문 완료되었습니다. ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "주문 실패!", Toast.LENGTH_SHORT).show();
                    }
                    setQuantity();
                    break;
                }
            }
            case R.id.r1: {
                setQuantity();
                break;
            }

        }
    }

    //----------------------------------------------------------------------------------------------
    // setQuantity()
    //----------------------------------------------------------------------------------------------
    public void setQuantity() {
        quantity_flag = 0; // flag=0 화면에 뷰를 안보이게 설정 함
        quantity = 1; // 수량은 1로 변경
        tvQuantity.setText("[" + quantity + "]");
        lQuantity.setVisibility(View.GONE);
    }

    //----------------------------------------------------------------------------------------------
    // onBackPressed()
    //----------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        if (quantity_flag == 1) {
            setQuantity();
        } else {
            this.finish();
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
