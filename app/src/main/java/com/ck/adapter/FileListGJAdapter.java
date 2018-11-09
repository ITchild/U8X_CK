package com.ck.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ck.App_DataPara;
import com.ck.bean.MeasureDataBean;
import com.ck.db.DBService;
import com.ck.info.ClasFileProjectInfo;
import com.hc.u8x_ck.R;

import java.util.ArrayList;
import java.util.List;

public class FileListGJAdapter extends RecyclerView.Adapter<FileListGJAdapter.ViewHolder> {
    public static final int File = 0;
    public static final int Folder = 0;
    ClasFileProjectInfo mProject;
    ViewHolder holder;
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
        View view = mInflater.inflate(R.layout.ui_file_select_list_gj,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.mCBNidx.setText(" " + (position + 1));
        holder.mCBNidx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mOnFileGJItemClick) {
                    mOnFileGJItemClick.onGJSelect(true, position);
                }
            }
        });
        holder.gjBackGround_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnFileGJItemClick) {
                    mOnFileGJItemClick.onGJSelect(false, position);
                }
            }
        });

        if (nSelect == position) {
            holder.m_LL.setBackgroundColor(Color.rgb(246, 184, 00));
        } else {
            if (App_DataPara.getApp().nTheme == R.style.AppTheme_Black)
                holder.m_LL.setBackgroundColor(Color.BLACK);
            else
                holder.m_LL.setBackgroundColor(Color.WHITE);
        }
        holder.mCBNidx.setChecked(mProject.mstrArrFileGJ.get(position).bIsSelect);
        holder.tv_gjName.setText(mProject.mstrArrFileGJ.get(position).mFileGJName);
        List<MeasureDataBean> dataBeans =  new ArrayList<>();
        dataBeans = DBService.getInstence(mContext).getMeasureData(mProject.mFileProjectName,
                mProject.mstrArrFileGJ.get(position).mFileGJName,null,MeasureDataBean.FILESTATE_USERING);
        float max = 0;
        for (MeasureDataBean bean : dataBeans){
            if(bean.getWidth() > max){
                max = bean.getWidth();
            }
        }
        holder.tv_maxLf.setText(max + "mm");
        holder.tv_num.setText(dataBeans.size()+"");
        holder.m_TVTime.setText("" + mProject.mstrArrFileGJ.get(position).mLastModifiedDate + "");
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
        CheckBox mCBNidx;
        TextView tv_gjName;
        TextView tv_maxLf;
        TextView tv_num;
        TextView m_TVTime;
        LinearLayout m_LL;
        LinearLayout gjBackGround_ll;

        public ViewHolder(View view) {
            super(view);
            if(null == mCBNidx) {
                mCBNidx = (CheckBox) view.findViewById(R.id.project_checkboxID);
            }
            if(null == tv_gjName) {
                tv_gjName = (TextView) view.findViewById(R.id.tv_gjName);
            }
            if(null == tv_maxLf){
                tv_maxLf = (TextView) view.findViewById(R.id.tv_maxLf);
            }
            if(null == tv_num){
                tv_num = (TextView) view.findViewById(R.id.tv_num);
            }
            if(null == m_TVTime) {
                m_TVTime = (TextView) view.findViewById(R.id.tv_time);
            }
            mCBNidx.setVisibility(View.VISIBLE);
            m_TVTime.setVisibility(View.VISIBLE);
            if(null ==m_LL) {
                m_LL = (LinearLayout) view.findViewById(R.id.ui_list_gj);
            }
            if(null == gjBackGround_ll){
                gjBackGround_ll = view.findViewById(R.id.LinearLayout1);
            }
        }
    }
}
