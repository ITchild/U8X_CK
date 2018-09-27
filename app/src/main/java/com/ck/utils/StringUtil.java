package com.ck.utils;


/**
 * @author fei
 * 有关于String类型数据的一些操作
 */
public class StringUtil {

    /**
     * 判断String字符串是否为null或者空字符串
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (!isNull(str) && !str.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断字符串是否为null
     *
     * @param str
     * @return
     */
    public static boolean isNull(String str) {
        return (null == str);
    }

}
