package com.ck.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ck.info.ClasFileProjectInfo;
import com.ck.main.App_DataPara;
import com.hc.u8x_ck.R;

import java.util.List;

public class ListProjectAdapter extends BaseAdapter {
    public static final int File = 0;
    public static final int Folder = 0;
    ViewHolder holder;
    private Context mContext;
    private List<ClasFileProjectInfo> mProjects;
    private int nSelect = 0;

    public ListProjectAdapter(Context context, List<ClasFileProjectInfo> projects) {
        mContext = context;
        mProjects = projects;
    }

    public void setData(List<ClasFileProjectInfo> projects,int nSelect){
        this.nSelect = nSelect;
        this.mProjects = projects;
        notifyDataSetChanged();
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
        if (nSelect == position) {
            holder.m_LL.setBackgroundColor(Color.rgb(246, 184, 00));
        } else {
            if (App_DataPara.getApp().nTheme == R.style.AppTheme_Black)
                holder.m_LL.setBackgroundColor(Color.BLACK);
            else
                holder.m_LL.setBackgroundColor(Color.WHITE);
        }
        holder.m_TVProject.setText(mProjects.get(position).mFileProjectName);

        return view;
    }

    class ViewHolder {
        TextView m_TVProject;
        LinearLayout m_LL;

        public ViewHolder(View view) {
            m_TVProject = (TextView) view.findViewById(R.id.tv_projectName);
            m_LL = (LinearLayout) view.findViewById(R.id.ui_list_project);
        }
    }
}
