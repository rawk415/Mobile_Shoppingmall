package jo.beacon.project.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jo.beacon.project.R;
import jo.beacon.project.network.MyService;

public class TopMainFragment extends BaseFragment {
    // Button flag
    int flag = 0;
    // Intent
    public Intent intentService;

    @BindView(R.id.iv_left)
    ImageView iv_left;
    @BindView(R.id.iv_right)
    ImageView iv_right;

    private Context mContext;

    private View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_top_main, container, false);
        ButterKnife.bind(this, root);
        mContext = getContext();
        intentService = new Intent(mContext, MyService.class);
        return root;
    }

    //----------------------------------------------------------------------------------------------
    // OnClick
    //----------------------------------------------------------------------------------------------
    @OnClick({R.id.iv_right, R.id.iv_left})
    void onClickMenu(View view) {
        switch (view.getId()) {
            case R.id.iv_right: {
                if (flag == 0) {
                    flag = 1;
                    iv_right.setImageResource(R.drawable.ic_beacon_on);
                    Toast.makeText(mContext, "상품 수신을 시작합니다.", Toast.LENGTH_SHORT).show();
                    mContext.startService(intentService);
                    break;
                } else if (flag == 1) {
                    flag = 0;
                    iv_right.setImageResource(R.drawable.ic_beacon_off);
                    Toast.makeText(mContext, "상품 수신을 종료합니다.", Toast.LENGTH_SHORT).show();
                    mContext.stopService(intentService);
                    break;
                }

            }
        }
    }
}
