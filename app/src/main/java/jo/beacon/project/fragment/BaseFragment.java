package jo.beacon.project.fragment;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


public class BaseFragment extends Fragment {


    private long mLastClickTime = System.currentTimeMillis();
    private static final long CLICK_TIME_INTERVAL = 500;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Glide.get(getContext()).clearMemory();
        unbindDrawables(getView());
    }

    public static void unbindDrawables(View view) {
        if (view == null) {
            return;
        }
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
            if (view.getParent() == null || !(view.getParent() instanceof SwipeRefreshLayout)) { // onDetatch() 호출 될 때 mCircleView.getBackground().setAlpha() 코드 때문에 제외
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(null);
                }
            }
        }

        if (view instanceof ImageView && ((ImageView) view).getDrawable() != null) {
            ((ImageView) view).getDrawable().setCallback(null);
            ((ImageView) view).setImageDrawable(null);
        }

        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }

        if (view instanceof WebView) {
            ((WebView) view).removeAllViews();
            ((WebView) view).destroy();
        }
    }

}
