package com.ck.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ck.bean.MeasureDataBean;
import com.ck.info.ClasFileProjectInfo;
import com.ck.utils.FileUtil;
import com.ck.utils.PathUtils;
import com.ck.utils.Stringutil;
import com.google.gson.Gson;
import com.hc.u8x_ck.R;

public class FileListGJAdapter extends RecyclerView.Adapter<FileListGJAdapter.ViewHolder> {
    ClasFileProjectInfo mProject;
    private Context mContext;
    private int nSelect;
    private LayoutInflater mInflater;
    private OnFileGJItemClick mOnFileGJItemClick;

    public FileListGJAdapter(Context context, ClasFileProjectInfo project) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mProject = project;
    }

    public void clearProInfo() {
        mProject.mstrArrFileGJ.clear();
        this.notifyDataSetChanged();
    }

    public void setData(ClasFileProjectInfo project, int nSelect) {
        this.mProject = project;
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

    public void initSelect(boolean flag) {
        for (int i = 0; i < mProject.mstrArrFileGJ.size(); i++) {
            mProject.mstrArrFileGJ.get(i).bIsSelect = flag;
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
        String objName = mProject.mFileProjectName;
        String fileName = mProject.mstrArrFileGJ.get(position).mFileGJName;
        if(null != mProject.mstrArrFileGJ.get(position).getSrc()){
            holder.morePic_show_mpsv.setImageBitmap(mProject.mstrArrFileGJ.get(position).getSrc());
        }
        String width = mProject.mstrArrFileGJ.get(position).getWidth();
        if(Stringutil.isEmpty(width)) { //TODO ：如果列表的宽度为空，则进行重新的查询
            String json = FileUtil.readData(PathUtils.PROJECT_PATH + "/" + objName + "/" + fileName + ".CK");
            if (!Stringutil.isEmpty(json)) {
                MeasureDataBean bean = new Gson().fromJson(json, MeasureDataBean.class);
                holder.morePic_show_name.setText(fileName + "-" + bean.getWidth());
            }
        }else{//TODO :列表的宽度不为空，则直接使用
            holder.morePic_show_name.setText(fileName + "-" + width+"mm");
        }
        holder.morePic_show_mpsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnFileGJItemClick){
                    mOnFileGJItemClick.onGJSelect(false,position);
                }
            }
        });

        holder.morePic_choice_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnFileGJItemClick){
                    mOnFileGJItemClick.onGJSelect(true,position);
                    holder.morePic_choice_cb.setChecked(
                            mProject.mstrArrFileGJ.get(position).bIsSelect? true:false);
                }
            }
        });
        holder.morePic_choice_cb.setChecked(mProject.mstrArrFileGJ.get(position).bIsSelect);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mProject.mstrArrFileGJ.size();
    }

    public void setOnFileGJItemClick(OnFileGJItemClick onFileGJItemClick) {
        mOnFileGJItemClick = onFileGJItemClick;
    }

    public interface OnFileGJItemClick {
        void onGJSelect(boolean isSelect, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView morePic_show_mpsv;
        TextView morePic_show_name;
        LinearLayout morePic_choice_ll;
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
        }
    }
}
