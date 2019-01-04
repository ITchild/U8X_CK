package com.ck.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ck.info.ClasFileGJInfo;
import com.hc.u8x_ck.R;

import java.util.List;

public class FileListGJAdapter extends RecyclerView.Adapter<FileListGJAdapter.ViewHolder> {
    private List<ClasFileGJInfo> fileGJData;
    private Context mContext;
    private int nSelect;
    private boolean isCanSelect = false;
    private LayoutInflater mInflater;
    private OnFileGJItemClick mOnFileGJItemClick;

    public FileListGJAdapter(Context context, List<ClasFileGJInfo> fileGJData) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        this.fileGJData = fileGJData;
    }

    public void clearProInfo() {
        fileGJData.clear();
        this.notifyDataSetChanged();
    }

    public void setData(List<ClasFileGJInfo> fileGJData, int nSelect) {
        this.fileGJData = fileGJData;
        this.nSelect = nSelect;
        notifyDataSetChanged();
    }

    public int getSelect() {
        return this.nSelect;
    }

    public void setSelect(int nSelect) {
        this.nSelect = nSelect;
        this.notifyDataSetChanged();
    }

    public void toSelectView(boolean isCanSelect){
        this.isCanSelect = isCanSelect;
        this.notifyDataSetChanged();
    }

    public void initSelect(boolean flag) {
        for (int i = 0; i < fileGJData.size(); i++) {
            fileGJData.get(i).bIsSelect = flag;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_morepic,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String fileName = fileGJData.get(position).mFileGJName.replace(".CK","");
        if(null != fileGJData.get(position).getSrc()){
            holder.morePic_show_mpsv.setImageBitmap(fileGJData.get(position).getSrc());
        }
        String width = fileGJData.get(position).getWidth();
//        if(Stringutil.isEmpty(width)) { //TODO ：如果列表的宽度为空，则进行重新的查询
//            String json = FileUtil.readData(PathUtils.PROJECT_PATH + "/" + objName + "/" + fileName + ".CK");
//            if (!Stringutil.isEmpty(json)) {
//                MeasureDataBean bean = new Gson().fromJson(json, MeasureDataBean.class);
//                holder.morePic_show_name.setText(fileName + "-" + bean.getWidth());
//            }
//        }else{//TODO :列表的宽度不为空，则直接使用
            holder.morePic_show_name.setText(fileName + "-" + width+"mm");
//        }
        holder.morePic_show_mpsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnFileGJItemClick){
                    mOnFileGJItemClick.onGJSelect(false,position);
                }
            }
        });
        holder.morePic_show_mpsv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(null != mOnFileGJItemClick){
                    mOnFileGJItemClick.onGJLongClick(position);
                }
                return false;
            }
        });

        if(nSelect == position){
            holder.morePic_show_ll.setBackgroundColor(Color.rgb(0xF6,0xB8,0x00));
        }else{
            holder.morePic_show_ll.setBackgroundColor(Color.rgb(0x00,0x00,0x00));
        }

        holder.morePic_choice_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnFileGJItemClick){
                    mOnFileGJItemClick.onGJSelect(true,position);
                    holder.morePic_choice_cb.setChecked(
                            fileGJData.get(position).bIsSelect? true:false);
                }
            }
        });
        holder.morePic_choice_cb.setVisibility(isCanSelect ? View.VISIBLE : View.GONE);
        holder.morePic_choice_cb.setChecked(fileGJData.get(position).bIsSelect);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return fileGJData.size();
    }

    public void setOnFileGJItemClick(OnFileGJItemClick onFileGJItemClick) {
        mOnFileGJItemClick = onFileGJItemClick;
    }

    public interface OnFileGJItemClick {
        void onGJSelect(boolean isSelect, int position);
        void onGJLongClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView morePic_show_mpsv;
        TextView morePic_show_name;
        LinearLayout morePic_choice_ll;
        LinearLayout morePic_show_ll;
        CheckBox morePic_choice_cb;
        public ViewHolder(View view) {
            super(view);
            if(null == morePic_show_mpsv) {
                morePic_show_mpsv = (ImageView) view.findViewById(R.id.morePic_show_mpsv);
            }
            if(null == morePic_show_name){
                morePic_show_name = (TextView) view.findViewById(R.id.morePic_show_name);
            }
            if(null == morePic_choice_ll){
                morePic_choice_ll = (LinearLayout) view.findViewById(R.id.morePic_choice_ll);
            }
            if(null == morePic_choice_cb){
                morePic_choice_cb = (CheckBox) view.findViewById(R.id.morePic_choice_cb);
            }
            if(null == morePic_show_ll){
                morePic_show_ll = (LinearLayout) view.findViewById(R.id.morePic_show_ll);
            }
        }
    }
}
