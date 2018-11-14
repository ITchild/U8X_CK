package com.ck.dlg;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ck.adapter.ShowListOrCreateAdapter;
import com.ck.base.U8BaseDialog;
import com.ck.utils.FileUtil;
import com.ck.utils.PathUtils;
import com.ck.utils.Stringutil;
import com.hc.u8x_ck.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fei
 * @date on 2018/11/5 0005
 * @describe TODO : 这个dialog用于显示工程 、构件 、 文件的列表的新建与选择
 **/
public class ShowObjGjFileListDialog extends U8BaseDialog {
    private RecyclerView showList_list_rv;//文件列表
    private EditText showList_input_et; //新建的文件名称
    private TextView showlist_title_tv; // dialog标题
    private TextView showlist_create_tv; //新建按钮

    private String title;
    private String proName;
    private String fileName = "";
    private String gjName = "";
    private ShowListOrCreateAdapter adapter;
    private Context mContext;
    private List<String> data;
    private List<String> showData;

    public ShowObjGjFileListDialog(@NonNull Context context,String title,String proName,String gjName,String fileName) {
        super(context);
        this.mContext = context;
        this.title = title;
        this.proName = proName;
        this.fileName = null == fileName ? "" : fileName;
        this.gjName = null == gjName ? "" : gjName;
    }

    public ShowObjGjFileListDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public ShowObjGjFileListDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }


    @Override
    protected int setLayout() {
        return R.layout.dialog_showobjgjfilelist;
    }

    @Override
    protected void initView() {
        showlist_create_tv = findView(R.id.showlist_create_tv);
        showlist_title_tv = findView(R.id.showlist_title_tv);
        showList_input_et = findView(R.id.showList_input_et);
        showList_list_rv = findView(R.id.showList_list_rv);

        if(null == data){
            data = new ArrayList<>();
        }
        if(null == showData){
            showData = new ArrayList<>();
        }
        adapter = new ShowListOrCreateAdapter(mContext,showData);
        showList_list_rv.setLayoutManager(new LinearLayoutManager(mContext));
        showList_list_rv.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        super.initData();
        showlist_title_tv.setText(null == title ? "" : title);
        if(title.equals(getStr(R.string.str_ProNewName))){
            File[] dataFile = new File(PathUtils.PROJECT_PATH).listFiles();
            if(null != dataFile) {
                for (File file : dataFile) {
                    data.add(file.getName());
                }
            }
            showData.addAll(data);
            adapter.setData(showData);
            showList_input_et.setText(proName);
        }else if (title.equals(getStr(R.string.str_GjNewName))){
            if(!Stringutil.isEmpty(proName)){
                File[] dataFile = new File(PathUtils.PROJECT_PATH+"/"+proName).listFiles();
                if(null != dataFile){
                    for (File file : dataFile){
                        data.add(file.getName());
                    }
                }
                showData.addAll(data);
                adapter.setData(showData);
                showList_input_et.setText(gjName);
            }
        }else if (title.equals(getStr(R.string.str_FileNewName))){
            if(!Stringutil.isEmpty(proName) && !Stringutil.isEmpty(gjName)) {
                File[] dataFile = new File(PathUtils.PROJECT_PATH + "/" + proName+"/"+gjName).listFiles();
                if (null != dataFile) {
                    for (File file : dataFile) {
                        data.add(file.getName().replace(".bmp",""));
                    }
                }
                showData.addAll(data);
                adapter.setData(showData);
            }
            if(Stringutil.isEmpty(fileName) && showData.size()>0){
                fileName = showData.get(showData.size()-1);
                showList_input_et.setText(FileUtil.GetDigitalPile(fileName));
            }else{
                boolean isHaveName = false;
                for (String name : showData){
                    if(name.equals(fileName)){
                        isHaveName = true;
                    }
                }
                showList_input_et.setText(isHaveName ? FileUtil.GetDigitalPile(fileName) : fileName);
            }
            showlist_create_tv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        showlist_create_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击新建
                String name = showList_input_et.getText().toString();
                if(null != mOrCreateName){
                    mOrCreateName.retrunName(name);
                }
            }
        });
        adapter.setOnitemNameClick(new ShowListOrCreateAdapter.OnitemNameClick() {
            @Override
            public void onNameClick(int position, String name) {
                if(null != mOrCreateName){
                    showList_input_et.setText(title.equals(getStr(R.string.str_FileNewName)) ?
                            FileUtil.GetDigitalPile(name) : name);
                }
            }
        });
        showList_input_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                showData.clear();
                String conFlag = charSequence.toString();
                if(null == conFlag || conFlag.equals("")){
                    showData.addAll(data);
                }else {
                    for (String name : data) {
                        if (name.contains(charSequence.toString())) {
                            showData.add(name);
                        }
                    }
                }
                if(title.equals(getStr(R.string.str_FileNewName))) {
                    showlist_create_tv.setVisibility(showData.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                }
                adapter.setData(showData);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }



    public interface OnChoiceOrCreateName{
        void retrunName(String name);
    }

    private OnChoiceOrCreateName mOrCreateName;

    public void setOnGetName(OnChoiceOrCreateName onGetName){
        this.mOrCreateName = onGetName;
    }

}
