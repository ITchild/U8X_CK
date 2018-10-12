package com.ck.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageUtil {

    /**
     * {功能}<获取手机软件版本号>
     *
     * @return String 版本号
     * @throw
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return "V " + version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "V 1.0.0";
    }


    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }


}
