package com.ck.netcloud;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hc.u8x_ck.R;

import java.util.ArrayList;
import java.util.List;


public class Adapter_wifi_sel extends BaseAdapter {
	
	List<clasWifiInfo> list = new ArrayList<clasWifiInfo>();
	
	Context mContext;
	LayoutInflater mInflater;
	public int selectItem = -1; //控制被选中的 item颜色变化	
	
	public Adapter_wifi_sel(Context context , List<clasWifiInfo> sList){
        mContext = context;
        list = sList;
        mInflater = LayoutInflater.from(mContext);
    }	
	
	public int getSelectItem() {
		return selectItem;
	}

	public void setSelectItem(int selectItem) {
		this.selectItem = selectItem;
		notifyDataSetChanged();	//刷新lsitview 的语句
	}
		
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;				
		
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.ui_net_wifi_list_item, null);
			holder = new ViewHolder();
			
			//holder.cb = (CheckBox) convertView.findViewById(R.id.CheckBox01);
			holder.tv1 = (TextView) convertView.findViewById(R.id.check_tv1);
			holder.tv2 = (TextView) convertView.findViewById(R.id.check_tv2);
			holder.tv3 = (TextView) convertView.findViewById(R.id.check_tv3);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		if(selectItem != -1 && selectItem == position){	
			convertView.setBackgroundColor(Color.rgb(0xF6,0xB8,0x00));   //海创黄色 Color.rgb(0xF6,0xB8,0x00)  Color.YELLOW
			//holder.cb.setTextColor(Color.BLACK);
			
			holder.tv1.setTextColor(Color.rgb(0x90,0x90,0x90));
			holder.tv2.setTextColor(Color.BLACK);
			holder.tv3.setTextColor(Color.BLACK);
			
		}else if(selectItem != position){
			convertView.setBackgroundColor(Color.BLACK);
			//holder.cb.setTextColor(Color.rgb(0x90,0x90,0x90));
			holder.tv1.setTextColor(Color.rgb(0x70,0x70,0x70));
			holder.tv2.setTextColor(Color.WHITE);
			holder.tv3.setTextColor(Color.WHITE);
		}
				
		clasWifiInfo cProjInfoBuf;
		cProjInfoBuf = list.get(position);
		
		String strttt = "" + (position+1);
		//holder.cb.setText(strttt);
        holder.tv1.setText(strttt);   //No        
        holder.tv2.setText(cProjInfoBuf.strWifiName);   //工程名称         
        holder.tv3.setText(cProjInfoBuf.strWifiContent);	
		return convertView;
	}

	
	class ViewHolder{		
		CheckBox cb;
		TextView tv1;
		TextView tv2;
		TextView tv3;
	}
}
