package com.ck.activity_key;

import android.content.Intent;
import android.util.Log;

import com.ck.activity.HomeActivity;
import com.ck.utils.Catition;
import com.fei.feilibs_1_0_0.bean.RxBusMsgBean;

/**
 * @author fei
 * @date on 2018/10/16 0016
 * @describe TODO :
 **/
public class KeyHomeActivity extends HomeActivity {

    private static String TAG = KeyHomeActivity.class.getSimpleName();
    private int focusPosition = 0;


    @Override
    public void doRxBus(RxBusMsgBean bean) {
        super.doRxBus(bean);
        if(bean.getWhat() == Catition.Key.KEY) {
            Log.i(TAG,TAG+"有值");
            switch (Integer.parseInt(bean.getMsg())) {
                case Catition.Key.TO_UP: // 向上按钮
                    if (focusPosition - num >= 0) {
                        focusPosition = focusPosition - num;
                    }
                    break;
                case Catition.Key.TO_DOWN: // 向下按钮
                    if (focusPosition + num < homeDisData.size()) {
                        focusPosition = focusPosition + num;
                    }
                    break;
                case Catition.Key.TO_RIGHT://向右按钮
                    if (focusPosition + 1 < homeDisData.size()) {
                        focusPosition++;
                    }
                    break;
                case Catition.Key.TO_LEFT://向左按钮
                    if (focusPosition - 1 >= 0) {
                        focusPosition--;
                    }
                    break;
                case Catition.Key.OK:
                    jumpOnAll(focusPosition);
                    break;
            }
            mHomeDisAdapter.setFocusPosition(focusPosition);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (null != intent) {
            String focus = intent.getStringExtra("jump");
            if (null != focus && focus.equals("setting")) {
                focusPosition = 4;
                mHomeDisAdapter.setFocusPosition(focusPosition);
            }
        }
        subScribeRxbus(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unSubscribeRxBus(this);
    }
}
