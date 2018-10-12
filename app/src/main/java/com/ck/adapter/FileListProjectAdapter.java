package com.ck.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ck.info.ClasFileProjectInfo;
import com.ck.main.App_DataPara;
import com.hc.u8x_ck.R;

import java.util.List;

public class FileListProjectAdapter extends BaseAdapter {
    public static final int File = 0;
    public static final int Folder = 0;
    ViewHolder holder;
    private Context mContext;
    private List<ClasFileProjectInfo> mProjects;
    private int nSelect;
    private OnFileProItemClick mOnFileProItemClick;

    public FileListProjectAdapter(Context context, List<ClasFileProjectInfo> projects) {
        mContext = context;
        mProjects = projects;
    }

    public void setData(List<ClasFileProjectInfo> projects, int nSelect) {
        this.mProjects = projects;
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

    @Override
    public int getCount() {
        return null == mProjects ? 0 : mProjects.size();
    }

    @Override
    public Object getItem(int position) {
        return mProjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup arg2) {
        if (view == null) {
            view = View.inflate(mContext, R.layout.ui_file_select_list_project, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.mCBNidx.setText(" " + (position + 1));
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
        holder.m_TVProject.setText(mProjects.get(position).mFileProjectName);
        holder.m_TVGJNum.setText(mProjects.get(position).mstrArrFileGJ.size() + "");
        holder.m_TVTime.setText(mProjects.get(position).mLastModifiedDate);
        return view;
    }

    public void setOnFileProItemClick(OnFileProItemClick onFileProItemClick) {
        this.mOnFileProItemClick = onFileProItemClick;
    }

    public interface OnFileProItemClick {
        void onClickIsChoice(boolean isChoice, int position);
    }

    class ViewHolder {
        CheckBox mCBNidx;
        TextView m_TVProject;
        TextView m_TVGJNum;
        TextView m_TVTime;
        LinearLayout m_LL;
        LinearLayout cilik_ll;

        public ViewHolder(View view) {
            if (null == mCBNidx) {
                mCBNidx = (CheckBox) view.findViewById(R.id.project_checkboxID);
            }
            if (null == m_TVProject) {
                m_TVProject = (TextView) view.findViewById(R.id.tv_projectName);
            }
            if (null == m_TVGJNum) {
                m_TVGJNum = (TextView) view.findViewById(R.id.tv_gjNum);
            }
            if (null == m_TVTime) {
                m_TVTime = (TextView) view.findViewById(R.id.tv_time);
            }
            mCBNidx.setVisibility(View.VISIBLE);
            m_TVGJNum.setVisibility(View.VISIBLE);
            m_TVTime.setVisibility(View.VISIBLE);
            if (null == m_LL) {
                m_LL = (LinearLayout) view.findViewById(R.id.ui_list_project);
            }
            if (null == cilik_ll) {
                cilik_ll = view.findViewById(R.id.LinearLayout1);
            }
        }
    }

}
