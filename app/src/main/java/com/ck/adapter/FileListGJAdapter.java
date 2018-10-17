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
import com.ck.App_DataPara;
import com.hc.u8x_ck.R;

public class FileListGJAdapter extends BaseAdapter {
    public static final int File = 0;
    public static final int Folder = 0;
    ClasFileProjectInfo mProject;
    ViewHolder holder;
    private Context mContext;
    private int nSelect;
    private OnFileGJItemClick mOnFileGJItemClick;

    public FileListGJAdapter(Context context, ClasFileProjectInfo project) {
        mContext = context;
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

    @Override
    public int getCount() {
        return mProject.mstrArrFileGJ.size();
    }

    @Override
    public Object getItem(int position) {
        return mProject.mstrArrFileGJ.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup arg2) {
        if (view == null) {
            view = View.inflate(mContext, R.layout.ui_file_select_list_gj, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
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
        holder.m_TVProject.setText(mProject.mstrArrFileGJ.get(position).mFileGJName.substring(0, mProject.mstrArrFileGJ.get(position).mFileGJName.length() - 4));
        holder.m_TVTime.setText("" + mProject.mstrArrFileGJ.get(position).mLastModifiedDate + "");

        return view;
    }

    public void setOnFileGJItemClick(OnFileGJItemClick onFileGJItemClick) {
        mOnFileGJItemClick = onFileGJItemClick;
    }

    public interface OnFileGJItemClick {
        void onGJSelect(boolean isSelect, int position);
    }

    class ViewHolder {
        CheckBox mCBNidx;
        TextView m_TVProject;
        TextView m_TVTime;
        LinearLayout m_LL;
        LinearLayout gjBackGround_ll;

        public ViewHolder(View view) {
            if(null == mCBNidx) {
                mCBNidx = (CheckBox) view.findViewById(R.id.project_checkboxID);
            }
            if(null == m_TVProject) {
                m_TVProject = (TextView) view.findViewById(R.id.tv_projectName);
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
