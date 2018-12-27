package com.ck.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;

import com.ck.App_DataPara;
import com.ck.base.TitleBaseActivity;
import com.ck.bean.MeasureDataBean;
import com.ck.ui.OpenCvCameraView;
import com.ck.utils.Catition;
import com.ck.utils.FileUtil;
import com.ck.utils.FindLieFenUtils;
import com.ck.utils.PathUtils;
import com.ck.utils.Stringutil;
import com.google.gson.Gson;
import com.hc.u8x_ck.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author fei
 * @date on 2018/12/26 0026
 * @describe TODO :
 **/
public class EditPicActivity extends TitleBaseActivity {
    private OpenCvCameraView editPic_cameraView;
    private String proName;
    private String fileName;
    private int filePosition;
    private int proPosition;

    @Override
    protected int initLayout() {
        return R.layout.ac_editpic;
    }

    @Override
    protected void initView() {
        editPic_cameraView = findView(R.id.editPic_cameraView);

        super.initView();
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if(null != intent){
            proPosition = intent.getIntExtra("objPosition",0);
            filePosition = intent.getIntExtra("gjPosition",0);
            proName = App_DataPara.getApp().proData.get(proPosition).mFileProjectName;
            fileName =  App_DataPara.getApp().proData.get(proPosition).mstrArrFileGJ.get(filePosition).mFileGJName;
            editPic_cameraView.setBitmap(getPicBitmat());
            String json = FileUtil.readData(PathUtils.PROJECT_PATH+"/"+proName+ "/"+fileName+".CK");
            if(!Stringutil.isEmpty(json)) {
                MeasureDataBean bean = new Gson().fromJson(json, MeasureDataBean.class);
                FindLieFenUtils.m_nCLXLineSite = bean.getLeftX();
                FindLieFenUtils.m_nCLYLineSite = bean.getLeftY();
                FindLieFenUtils.m_nCRXLineSite = bean.getRightX();
                FindLieFenUtils.m_nCRYLineSite = bean.getRightY();
                FindLieFenUtils.bytGrayAve = bean.getAvage();
                editPic_cameraView.makeInitSetting();
                editPic_cameraView.setZY(1);
            }
        }
        super.initData();
    }

    private Bitmap getPicBitmat(){
        String path = PathUtils.PROJECT_PATH + "/" + proName  + "/" + fileName+".bmp";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(fis);
        FindLieFenUtils.setBitmapWidth(bitmap.getWidth());
        return bitmap;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finshWhitBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 返回数据给文件浏览接界面
     */
    private void finshWhitBack(){
        Intent intent = new Intent();
        intent.putExtra("objPosition",proPosition);
        setResult(Catition.EDITBACKTOFILE);
        finish();
    }

}
