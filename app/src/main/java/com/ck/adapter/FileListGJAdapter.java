package com.ck.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ck.bean.MeasureDataBean;
import com.ck.db.DBService;
import com.ck.info.ClasFileProjectInfo;
import com.ck.ui.MorePicShowView;
import com.ck.utils.PathUtils;
import com.hc.u8x_ck.R;

import java.util.List;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //按采样方式进行压缩  inSampleSize 为4，表示长宽缩为原来的1/4
        String objName = mProject.mFileProjectName;
        String fileName = mProject.mstrArrFileGJ.get(position).mFileGJName;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(PathUtils.PROJECT_PATH+"/"+objName+ "/"+fileName,options);
        holder.morePic_show_mpsv.setBitmap(bitmap,0,0,"");

        List<MeasureDataBean> datas = DBService.getInstence(mContext).getMeasureData(objName,fileName,MeasureDataBean.FILESTATE_USERING);
        String length = "";
        if(null != datas && datas.size() > 0){
            length = datas.get(0).getWidth()+"mm";
        }
        holder.morePic_show_name.setText(fileName.replace(".bmp","-")+length);

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
        MorePicShowView morePic_show_mpsv;
        TextView morePic_show_name;
        public ViewHolder(View view) {
            super(view);
            if(null == morePic_show_mpsv) {
                morePic_show_mpsv = (MorePicShowView) view.findViewById(R.id.morePic_show_mpsv);
            }
            if(null == morePic_show_name){
                morePic_show_name = (TextView) view.findViewById(R.id.morePic_show_name);
            }
        }
    }
}
