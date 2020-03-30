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

public class RecommendActivityAdapter extends RecyclerView.Adapter<RecommendActivityAdapter.RecommendActivityHolder> {
    /**
     * view holder
     */
    public class RecommendActivityHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_icon)
        ImageView icon;
        @BindView(R.id.list_text)
        TextView description;
        @BindView(R.id.r1)
        RelativeLayout r1;

        public RecommendActivityHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private ArrayList<RecommendDTO> datas;

    public void setData(ArrayList<RecommendDTO> list) {
        datas = list;
    }

    private Context context; // 액티비티의 정보를 담을 공간

    public RecommendActivityAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecommendActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // 사용할 아이템의 뷰를 생성해준다.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommend_recyclerview, parent, false);
        RecommendActivityHolder holder = new RecommendActivityHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecommendActivityHolder holder, int position) {
        final RecommendDTO data = datas.get(position);

        holder.description.setText(data.getText());
//        Picasso.with(holder.itemView.getContext())
//                .load(data.getUrl())
//                .into(holder.icon);
        // 이미지 설정
        Glide.with(holder.itemView.getContext()).load(data.getUrl())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.icon);

        holder.r1.setOnClickListener(new View.OnClickListener() {
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