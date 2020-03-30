package jo.beacon.project.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jo.beacon.project.R;
import jo.beacon.project.activity.DetailsActivity;

public class MainProductAdapter extends RecyclerView.Adapter<MainProductAdapter.MainActivityHolder> {
    /**
     * view holder
     */
    public class MainActivityHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_product_img)
        ImageView ivProduct;
        @BindView(R.id.tv_product_name)
        TextView tvProduct;
        // 11.14 add price
//        @BindView(R.id.tv_price)
//        TextView tvPrice;
        @BindView(R.id.r_main)
        RelativeLayout rMain;

        public MainActivityHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private ArrayList<RecommendDTO> datas;

    public void setData(ArrayList<RecommendDTO> list) {
        datas = list;
    }

    private Context context; // 액티비티의 정보를 담을 공간

    public MainProductAdapter(Context context) {
        this.context = context;
    }

    @Override
    public MainActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // 사용할 아이템의 뷰를 생성해준다.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main_recycleview, parent, false);
        MainActivityHolder holder = new MainActivityHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MainActivityHolder holder, int position) {
        final RecommendDTO data = datas.get(position);

        holder.tvProduct.setText(data.getText());
        // 11.14 add price and add recommendDTO price
//        holder.tvPrice.setText(data.getPrice());
//        Picasso.with(holder.itemView.getContext())
//                .load(data.getUrl())
//                .into(holder.icon);
        // 이미지 설정
        Glide.with(holder.itemView.getContext()).load(data.getUrl())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.ivProduct);

        holder.rMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("pName", data.getText());
                intent.putExtra("pImg", data.getUrl());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }
}