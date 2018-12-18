package com.ck.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ck.bean.MeasureDataBean;
import com.ck.ui.MorePicShowView;
import com.ck.utils.PathUtils;
import com.hc.u8x_ck.R;

import java.util.List;

/**
 * @author fei
 * @date on 2018/10/31 0031
 * @describe TODO :
 **/
public class MorePicAdapter extends RecyclerView.Adapter<MorePicAdapter.MyViewHodler> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<MeasureDataBean> data ;

    public MorePicAdapter(Context mContext, List<MeasureDataBean> data) {
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
        this.data = data;
    }

    public void setData(List<MeasureDataBean> data){
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_morepic,parent,false);
        return new MyViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHodler holder, final int position) {
        MeasureDataBean bean = data.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(PathUtils.PROJECT_PATH+"/"+bean.getObjName()+"/"+bean.getFileName());
        holder.morePic_show_mpsv.setBitmap(bitmap,bean.getLeftX(),bean.getRightX(),bean.getFileName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnItemPicClickListener){
                    mOnItemPicClickListener.onPicClick(position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(null != mOnItemPicClickListener){
                    mOnItemPicClickListener.onLongClick(position);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == data ? 0 : data.size();
    }

    class MyViewHodler extends RecyclerView.ViewHolder {
        MorePicShowView morePic_show_mpsv;
        public MyViewHodler(View itemView) {
            super(itemView);
            if(null == morePic_show_mpsv){
                morePic_show_mpsv = (MorePicShowView) itemView.findViewById(R.id.morePic_show_mpsv);
            }
        }
    }


    public interface OnItemPicClickListener{
        void onPicClick(int position);
        void onLongClick(int position);
    }
    private OnItemPicClickListener mOnItemPicClickListener;

    public void setOnItemPicClickListener(OnItemPicClickListener onItemPicClickListener){
        this.mOnItemPicClickListener = onItemPicClickListener;
    }
}
