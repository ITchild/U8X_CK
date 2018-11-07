package com.ck.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hc.u8x_ck.R;

import java.util.List;

/**
 * @author fei
 * @date on 2018/11/5 0005
 * @describe TODO :
 **/
public class ShowListOrCreateAdapter extends RecyclerView.Adapter<ShowListOrCreateAdapter.MyViewHolder> {

    private List<String> data ;
    private Context mContext;
    private LayoutInflater mInflater;

    public ShowListOrCreateAdapter(Context mContext,List<String> data){
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
        this.data = data;
    }

    public void setData(List<String> data){
        this.data = data;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_showlistorcreate,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.itemShowlist_name_tv.setText(data.get(position));
        holder.itemShowlist_name_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnitemNameClick){
                    mOnitemNameClick.onNameClick(position,data.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == data ? 0 : data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView itemShowlist_name_tv;
        public MyViewHolder(View itemView) {
            super(itemView);
            if(null == itemShowlist_name_tv){
                itemShowlist_name_tv = itemView.findViewById(R.id.itemShowlist_name_tv);
            }
        }
    }

    public interface OnitemNameClick{
        void onNameClick(int position,String name);
    }

    private OnitemNameClick mOnitemNameClick;

    public void setOnitemNameClick(OnitemNameClick mOniteNameClick){
        this.mOnitemNameClick = mOniteNameClick;
    }

}
