package com.ck.collect;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ck.main.App_DataPara;

public class Clas_spinner_adapter extends ArrayAdapter<String> {
		private App_DataPara mApp;
        Context context;
        String[] items = new String[]{};
        public int selectItem = -1; //控制被选中的 item颜色变化
        
        public void setSelection(int sel){
        	this.selectItem = sel;
        	notifyDataSetChanged();
        }
      
       public Clas_spinner_adapter(final Context context, final int textViewResourceId, final String[] objects) {
            super(context, textViewResourceId, objects);  
            this.items = objects;  
            this.context = context; 
            mApp = (App_DataPara) context.getApplicationContext();
        }        
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {  
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);  
            }  
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setGravity(Gravity.LEFT);
            if(this.selectItem == position){
            	tv.setTextColor(Color.rgb(88, 198, 203));
            	tv.setText(" " + items[position] + " ✓"); 
            }
            else{
            	tv.setTextColor(Color.BLACK);
            	tv.setText(" " + items[position] + "  "); 
            }            
            tv.setTextSize(17);  
            return convertView;  
        }        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {  
                LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);  
            }
			TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(items[position]);
			tv.setGravity(Gravity.CENTER);
			tv.setTextColor(Color.BLACK);
            tv.setTextSize(17);
            return convertView;
        }
    }
