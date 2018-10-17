package com.ck.activity_key;

import android.util.Log;

import com.ck.activity.CollectActivity;
import com.fei.feilibs_1_0_0.bean.RxBusMsgBean;

/**
 * @author fei
 * @date on 2018/10/17 0017
 * @describe TODO :
 **/
public class KeyCollectActivity extends CollectActivity {

    private String TAG = KeyCollectActivity.class.getSimpleName();

    @Override
    protected void onResume() {
        super.onResume();
        subScribeRxbus(this);
    }

    @Override
    protected void doRxBus(RxBusMsgBean bean) {
        super.doRxBus(bean);
        Log.i(TAG,TAG + bean.getWhat()+"信息" + bean.getMsg());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribeRxBus(this);
    }
}
