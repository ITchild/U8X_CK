package com.ck.activity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ck.App_DataPara;
import com.ck.base.TitleBaseActivity;
import com.ck.bean.MeasureDataBean;
import com.ck.info.ClasFileGJInfo;
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
import java.util.List;

/**
 * @author fei
 * @date on 2018/12/26 0026
 * @describe TODO :
 **/
public class EditPicActivity extends TitleBaseActivity implements View.OnClickListener,View.OnLongClickListener{
    private OpenCvCameraView editPic_cameraView;
    private LinearLayout editPic_drag_ll;
    private LinearLayout editPic_boot_ll;
    private Button editPic_blackWrite_bt;
    private Button editPic_before_bt;
    private Button editPic_next_bt;

    private String proName;
    private String fileName;
    private MeasureDataBean dataBean ;
    private int filePosition;
    private int proPosition;

    @Override
    protected int initLayout() {
        return R.layout.ac_editpic;
    }

    @Override
    protected void initView() {
        editPic_boot_ll = findView(R.id.editPic_boot_ll);
        editPic_cameraView = findView(R.id.editPic_cameraView);
        editPic_drag_ll = findView(R.id.editPic_drag_ll);
        editPic_drag_ll.setVisibility(View.VISIBLE);
        editPic_blackWrite_bt = findView(R.id.editPic_blackWrite_bt);
        editPic_before_bt = findView(R.id.editPic_before_bt);
        editPic_next_bt = findView(R.id.editPic_next_bt);
        super.initView();
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if(null != intent){
            proPosition = intent.getIntExtra("objPosition",0);
            filePosition = intent.getIntExtra("gjPosition",0);
            proName = App_DataPara.getApp().proData.get(proPosition).mFileProjectName;
            refresfFileData();
        }
        super.initData();
    }

    @Override
    protected void initListener() {
        editPic_boot_ll.setOnDragListener(new myDragEventListener());
        editPic_drag_ll.setOnLongClickListener(this);
        editPic_blackWrite_bt.setOnClickListener(this);
        editPic_blackWrite_bt.setOnLongClickListener(this);
        editPic_before_bt.setOnClickListener(this);
        editPic_before_bt.setOnLongClickListener(this);
        editPic_next_bt.setOnClickListener(this);
        editPic_next_bt.setOnLongClickListener(this);
        super.initListener();
    }

    /**
     * 刷新文件的
     */
    private void refresfFileData(){
        fileName =  App_DataPara.getApp().proData.get(proPosition).mstrArrFileGJ.get(filePosition).mFileGJName;
        editPic_cameraView.setBitmap(getPicBitmat());
        String json = FileUtil.readData(PathUtils.PROJECT_PATH+"/"+proName+ "/"+fileName+".CK");
        if(!Stringutil.isEmpty(json)) {
            dataBean = new Gson().fromJson(json, MeasureDataBean.class);
            FindLieFenUtils.m_nCLXLineSite = dataBean.getLeftX();
            FindLieFenUtils.m_nCLYLineSite = dataBean.getLeftY();
            FindLieFenUtils.m_nCRXLineSite = dataBean.getRightX();
            FindLieFenUtils.m_nCRYLineSite = dataBean.getRightY();
            FindLieFenUtils.bytGrayAve = dataBean.getAvage();
            editPic_cameraView.makeInitSetting();
            editPic_cameraView.setZY(1);
        }
    }

    /**
     * 获取到图片的
     * @return
     */
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
    /**
     * 返回数据给文件浏览接界面
     */
    private void finshWhitBack(){
        Intent intent = new Intent();
        intent.putExtra("objPosition",proPosition);
        setResult(Catition.EDITBACKTOFILE);
        finish();
    }

    /**
     * 上一张或下一张的选择
     * @param isToNext  TODO true :下一张   false：上一张
     */
    private void toBeforeOrNext(boolean isToNext){
        List<ClasFileGJInfo> fileList = App_DataPara.getApp().proData.get(proPosition).mstrArrFileGJ;
        if(isToNext){ //进行下一张
            filePosition ++;
            if(filePosition >= fileList.size()){
                filePosition = 0;
            }
        }else{//进行上一张
            filePosition --;
            if(filePosition < 0){
                filePosition = fileList.size()-1;
            }
        }
        //刷新数据
        refresfFileData();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.editPic_blackWrite_bt :
                if (editPic_cameraView.isBlackWrite) {
                    editPic_cameraView.setBlackWrite(false, true);
                } else {
                    editPic_cameraView.setBlackWrite(true, true);
                }
                break;
            case R.id.editPic_before_bt: //上一张
                toBeforeOrNext(false);
                break;
            case R.id.editPic_next_bt: //下一张
                toBeforeOrNext(true);
                break;
        }
    }

    /**
     * 编辑后的保存
     */
    private void onUpDate() {
        int typeFlag = editPic_cameraView.m_nDrawFlag;
        editPic_cameraView.setZY(0);
        editPic_cameraView.setDrawingCacheEnabled(true);
        FileUtil.saveDrawBmpFile(editPic_cameraView.getDrawingCache(),
                "/" + proName, fileName, "%s.bmp");
        editPic_cameraView.setDrawingCacheEnabled(false);
        //更新数据文件
        dataBean.setWidth(editPic_cameraView.width);
        dataBean.setLeftY(FindLieFenUtils.m_nCLYLineSite);
        dataBean.setLeftX(FindLieFenUtils.m_nCLXLineSite);
        dataBean.setRightY(FindLieFenUtils.m_nCRYLineSite);
        dataBean.setRightX(FindLieFenUtils.m_nCRXLineSite);
        Gson gson = new Gson();
        FileUtil.saveBmpFile(gson.toJson(dataBean), "/" + proName, fileName, "%s.CK");
        App_DataPara.getApp().proData.get(proPosition).mstrArrFileGJ.get(filePosition).setWidth(editPic_cameraView.width+"");
        showToast(getStr(R.string.str_saveSuccess));
        editPic_cameraView.setZY(typeFlag);
    }


    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){
            case R.id.editPic_blackWrite_bt :
            case R.id.editPic_before_bt :
            case R.id.editPic_next_bt:
            case R.id.editPic_drag_ll :
                ClipData.Item item = new ClipData.Item("11");
                ClipData dragData = new ClipData("11", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                // Instantiates the drag shadow builder.
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(editPic_drag_ll);
                editPic_drag_ll.startDrag(dragData, myShadow, null, 0);
                editPic_drag_ll.setVisibility(View.GONE);
                break;
        }
        return false;
    }

    /**
     * 拖动的监听的回调
     */
    protected class myDragEventListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {
            if (0 != event.getX() && 0 != event.getY()) {
                editPic_drag_ll.setX(event.getX()-40);
                editPic_drag_ll.setY(event.getY()-20);
            } else {
                editPic_drag_ll.setVisibility(View.VISIBLE);
            }
            final int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        v.invalidate();
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
    }

    /**
     * 按键的监听
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_F1 ://切换按键，功能为删除
                onUpDate();
                return true;
            case KeyEvent.KEYCODE_F2 : //存储按键,功能为转U盘
                return true;
            case KeyEvent.KEYCODE_BACK ://返回键
                finshWhitBack();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP :
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_ENTER:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
