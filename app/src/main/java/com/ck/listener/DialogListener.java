package com.ck.listener;

/**
 * @author  fei
 * Dialog的公用接口
 */
public interface DialogListener {
    /**
     * @author fei
     * @param type  类型
     * @param obj   传递的参数 Object类型
     * @param strs  传递的参数 多参数类型
     */
    void setLister(int type, Object obj, String... strs);

}
