package com.ck.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hc.u8x_ck.R;

import java.util.List;

public class HomeDisAdapter extends RecyclerView.Adapter<HomeDisAdapter.myViewHoder> {

    private LayoutInflater mInflater;
    private List<String> data;

    public HomeDisAdapter(Context context, List<String> data) {
        mInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public myViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_homedis,parent,false);
        return new myViewHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHoder holder, final int position) {

        holder.item_homedis_con_tv.setText(data.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnHomeDisItemClick){
                    mOnHomeDisItemClick.onItem(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == data ? 0 : data.size();
    }


    class myViewHoder extends ViewHolder {

        TextView item_homedis_con_tv;

        public myViewHoder(View itemView) {
            super(itemView);
            if(null == item_homedis_con_tv){
                item_homedis_con_tv = itemView.findViewById(R.id.item_homedis_con_tv);
            }
        }
    }
    private OnHomeDisItemClick mOnHomeDisItemClick;
    public void setOnHomeDisItemClick(OnHomeDisItemClick onHomeDisItemClick){
        this.mOnHomeDisItemClick = onHomeDisItemClick;
    }
    public interface OnHomeDisItemClick{
        void onItem(int position);
    }

}