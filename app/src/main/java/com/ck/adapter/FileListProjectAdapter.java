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
import com.ck.info.ClasFileProjectInfo;
import com.hc.u8x_ck.R;

import java.util.List;

public class FileListProjectAdapter extends RecyclerView.Adapter<FileListProjectAdapter.ViewHolder> {
    public static final int File = 0;
    private Context mContext;
    private List<ClasFileProjectInfo> mProjects;
    private int nSelect;
    private boolean isCanSelect = false;
    private OnFileProItemClick mOnFileProItemClick;
    private LayoutInflater mInflater;

    public FileListProjectAdapter(Context context, List<ClasFileProjectInfo> projects) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mProjects = projects;
    }

    public void setData(List<ClasFileProjectInfo> projects, int nSelect) {
        this.mProjects = projects;
        this.nSelect = nSelect;
        notifyDataSetChanged();
    }
    public void toSelectView(boolean isCanSelect){
        this.isCanSelect = isCanSelect;
        this.notifyDataSetChanged();
    }

    public int getSelect() {
        return this.nSelect;
    }

    public void setSelect(int nSelect) {
        this.nSelect = nSelect;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.ui_file_select_list_project,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.mCBNidx.setVisibility(isCanSelect ? View.VISIBLE :View.GONE );
        holder.mCBNidx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnFileProItemClick) {
                    mOnFileProItemClick.onClickIsChoice(true, position);
                }
            }
        });
        holder.cilik_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mOnFileProItemClick) {
                    mOnFileProItemClick.onClickIsChoice(false, position);
                }
            }
        });
        holder.cilik_ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (null != mOnFileProItemClick) {
                    mOnFileProItemClick.onLongClick(position);
                }
                return false;
            }
        });
        if (mProjects.get(position).nIsSelect == 2) {
            holder.mCBNidx.setButtonDrawable(R.drawable.checkbox_all_true);
        }
        if (mProjects.get(position).nIsSelect == 1) {
            holder.mCBNidx.setButtonDrawable(R.drawable.checkbox_half_true);
        }
        if (mProjects.get(position).nIsSelect == 0) {
            holder.mCBNidx.setButtonDrawable(R.drawable.checkbox_false);
        }
        if (nSelect == position) {
            holder.m_LL.setBackgroundColor(Color.rgb(246, 184, 00));
        } else {
            if (App_DataPara.getApp().nTheme == R.style.AppTheme_Black)
                holder.m_LL.setBackgroundColor(Color.BLACK);
            else
                holder.m_LL.setBackgroundColor(Color.WHITE);
        }
        holder.m_TVProject.setText(position+"    "+mProjects.get(position).mFileProjectName);
    }

    @Override
    public int getItemCount() {
        return null == mProjects ? 0 : mProjects.size();
    }

    public void setOnFileProItemClick(OnFileProItemClick onFileProItemClick) {
        this.mOnFileProItemClick = onFileProItemClick;
    }

    public interface OnFileProItemClick {
        void onClickIsChoice(boolean isChoice, int position);
        void onLongClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox mCBNidx;
        TextView m_TVProject;
        LinearLayout m_LL;
        LinearLayout cilik_ll;

        public ViewHolder(View view) {
            super(view);
            if (null == mCBNidx) {
                mCBNidx = (CheckBox) view.findViewById(R.id.project_checkboxID);
            }
            if (null == m_TVProject) {
                m_TVProject = (TextView) view.findViewById(R.id.tv_projectName);
            }
            if (null == m_LL) {
                m_LL = (LinearLayout) view.findViewById(R.id.ui_list_project);
                m_LL.setFocusable(true);
            }
            if (null == cilik_ll) {
                cilik_ll = view.findViewById(R.id.LinearLayout1);
            }
        }
    }

}
