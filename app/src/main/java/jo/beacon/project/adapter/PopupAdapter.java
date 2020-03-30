package jo.beacon.project.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Vector;

import jo.beacon.project.util.AddressInfo;
import jo.beacon.project.activity.RecommendActivity;
import jo.beacon.project.databinding.ItemPopupRecyclerviewBinding;

public class PopupAdapter extends RecyclerView.Adapter {
    public AddressInfo info;
    public String sIP;
    public int sPORT;

    // Vector란. 유동적으로 크기 조절이 가능한 배열 자료 구조를 구현한 것.
    private Vector<PopupDTO> items;
    private Context context; // 액티비티의 정보를 담을 공간

    public PopupAdapter(Vector<PopupDTO> items, Context context) {
        this.items = items;
        this.context = context;
        // ip
        this.info = new AddressInfo();
        this.sIP = info.getIp();
        this.sPORT = info.getPort();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // ItemPopupRecyclerviewBinding - > res/layout/item_popup_recyclerview.xml
        ItemPopupRecyclerviewBinding binding = ItemPopupRecyclerviewBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ItemHolders(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        try {
            ItemHolders itemViewHolder = (ItemHolders) holder;
            final ItemPopupRecyclerviewBinding binding = itemViewHolder.binding;
            binding.tvProductName.setText("" + items.get(position).getProductName());
            // 이미지 설정
            Glide.with(this.context).load("http://" + sIP + ":" + sPORT + "/" + items.get(position).getProductName() + ".png")
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.ivProductImg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RecommendActivity.class);
                String list = items.get(position).getJobj();
                intent.putExtra("product", list);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private class ItemHolders extends RecyclerView.ViewHolder {

        ItemPopupRecyclerviewBinding binding;

        ItemHolders(ItemPopupRecyclerviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
