package com.ck.activity_key;

import android.util.Log;

import com.ck.activity.FileBowerActivity;
import com.ck.utils.Catition;
import com.fei.feilibs_1_0_0.bean.RxBusMsgBean;

/**
 * @author fei
 * @date on 2018/10/16 0016
 * @describe TODO :
 **/
public class KeyFileBowerActivity extends FileBowerActivity {

    private static String TAG = KeyFileBowerActivity.class.getSimpleName();
    /**
     * focusPosition 0:工程列表
     *               1：文件列表
     *               2：全选/取消全选按钮
     *               3：转U盘按钮
     *               4：删除按钮
     *               5：返回按钮
     */
    private int focusPosition = 0 ;
    private int objListPosition = 0;
    private int fileListPosition = 0;

    @Override
    protected void doRxBus(RxBusMsgBean bean) {
        super.doRxBus(bean);
        if(bean.getWhat() == Catition.Key.KEY){
            Log.i(TAG,"有值");
            switch (Integer.parseInt(bean.getMsg())){
                case Catition.Key.TO_UP :
                    activityUp();
                    break;
                case Catition.Key.TO_DOWN :
                    activityDown();
                    break;
                case Catition.Key.TO_LEFT :
                    activityToLeft();
                    break;
                case Catition.Key.TO_RIGHT :
                    activityToRight();
                    break;
                case Catition.Key.OK :
                    activityOk();
                    break;
            }
        }
    }

    private void activityUp(){
        switch (focusPosition){
            case 0 :
                if(objListPosition - 1 >= 0){
                    objListPosition -- ;
                    fileListPosition = 0;
                    clickObjList(objListPosition);
                }
                break;
            case 1 :
                if(fileListPosition - 1 >= 0){
                    fileListPosition --;
                    cilickFileObjList(fileListPosition,false);
                }
                break;
            case 2 :
                focusPosition = 1;
                cilickFileObjList(fileListPosition,false);
                changeBottomBtView(focusPosition);
                break;
            case 3 :
                focusPosition = 1;
                cilickFileObjList(fileListPosition,false);
                changeBottomBtView(focusPosition);
                break;
            case 4 :
                focusPosition = 1;
                cilickFileObjList(fileListPosition,false);
                changeBottomBtView(focusPosition);
                break;
            case 5 :
                focusPosition = 1;
                cilickFileObjList(fileListPosition,false);
                changeBottomBtView(focusPosition);
                break;
        }
    }

    private void activityDown(){
        switch (focusPosition){
            case 0 :
                if(objListPosition + 1 < proData.size()){
                    objListPosition ++ ;
                    fileListPosition = 0;
                    clickObjList(objListPosition);
                }
                break;
            case 1 :
                if(fileListPosition + 1 < fileData.mstrArrFileGJ.size()){
                    fileListPosition ++ ;
                    cilickFileObjList(fileListPosition,false);
                }else{
                    //切换到底部按钮
                    focusPosition = 2;
                    changeBottomBtView(focusPosition);
                    cilickFileObjList(-1,false);
                }
                break;
            case 2 :
                break;
            case 3 :
                break;
            case 4 :
                break;
            case 5 :
                break;
        }
    }

    private void activityToLeft(){
        switch (focusPosition){
            case 0 :
                break;
            case 1 :
                fileListPosition = -1;
                cilickFileObjList(fileListPosition,false);
                focusPosition = 0;
                break;
            case 2 :
                break;
            case 3 :
                focusPosition = 2;
                changeBottomBtView(focusPosition);
                break;
            case 4 :
                focusPosition = 3;
                changeBottomBtView(focusPosition);
                break;
            case 5 :
                focusPosition = 4;
                changeBottomBtView(focusPosition);
                break;
        }
    }

    private void activityToRight(){
        switch (focusPosition){
            case 0 :
                fileListPosition = 0;
                cilickFileObjList(fileListPosition,false);
                focusPosition = 1;
                break;
            case 1 :
                choiceFileList(fileListPosition);
                break;
            case 2 :
                focusPosition = 3;
                changeBottomBtView(focusPosition);
                break;
            case 3 :
                focusPosition = 4;
                changeBottomBtView(focusPosition);
                break;
            case 4 :
                focusPosition = 5;
                changeBottomBtView(focusPosition);
                break;
            case 5 :
                break;
        }
    }

    private void activityOk(){
        switch (focusPosition){
            case 0 :
                choiceObjList(objListPosition);
                break;
            case 1 :
                cilickFileObjList(fileListPosition,true);
                break;
            case 2 :
                selectAllOrCancel();
                break;
            case 3 :
                onSaveSDcard();
                break;
            case 4 :
                onDelete();
                break;
            case 5 :
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        subScribeRxbus(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribeRxBus(this);
    }
}
