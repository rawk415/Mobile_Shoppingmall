package jo.beacon.project.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jo.beacon.project.R;

public class TopBackMintFragment extends BaseFragment {
    private Context mContext;
    private Activity mActivity;
    private View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_top_back_mint, container, false);
        ButterKnife.bind(this, root);
        mContext = getContext();
        mActivity = getActivity();
        return root;
    }

    //----------------------------------------------------------------------------------------------
    // OnClick
    //----------------------------------------------------------------------------------------------
    @OnClick(R.id.iv_back_mint)
    void onClickMenu(View view) {
        switch (view.getId()) {
            case R.id.iv_back_mint: {
                mActivity.finish();
            }
        }
    }
}
