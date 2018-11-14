package com.ck.listener;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.ck.utils.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author  fei
 * 程序异常退出后的日志处理（保存为文件）
 */
public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    // Log文件路径
    private static final String LOG_FILE_DIR = "log";
    // log文件的后缀名
    private static final String FILE_NAME = ".log";
    //文件的保存路径
    private static String path;
    private static MyExceptionHandler instance = null;
    // 是否打开上传
    public boolean openUpload = true;
    // 上下文
    private Context mContext;
    // 系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    private MyExceptionHandler(Context cxt) {
        // 获取系统默认的异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        // 获取Context，方便内部使用
        this.mContext = cxt.getApplicationContext();
    }

    public synchronized static MyExceptionHandler create(Context cxt, String errPath) {
        if (null == instance) {
            instance = new MyExceptionHandler(cxt);
        }
        path = null == errPath ? "" : errPath;
        Log.i("fei", path);
        return instance;
    }

    /**
     * 当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            // 保存导出异常日志信息到SD卡中
            saveToSDCard(ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
            Toast.makeText(mContext,
                    "很抱歉，程序出错，即将退出:\r\n" + ex.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
            if (mDefaultCrashHandler != null) {
                mDefaultCrashHandler.uncaughtException(thread, ex);
            } else {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 保存文件到SD卡
     *
     * @description：
     * @author fei
     */
    private void saveToSDCard(Throwable ex) throws Exception {
        File file = FileUtil.getFile(path,
                getDataTime("yyyy-MM-dd-HH-mm-ss") + FILE_NAME);
        PrintWriter pw = new PrintWriter(new BufferedWriter(
                new FileWriter(file)));
        // 导出发生异常的时间
        pw.println(getDataTime("yyyy-MM-dd-HH-mm-ss"));
        // 导出手机信息
        savePhoneInfo(pw);
        pw.println();
        // 导出异常的调用栈信息
        ex.printStackTrace(pw);
        pw.close();
    }

    /**
     * 根据时间格式返回时间
     *
     * @description：
     * @author ldm
     * @date 2016-4-18 上午11:39:30
     */
    private String getDataTime(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }

    /**
     * 保存手机硬件信息
     *
     * @description：
     * @author ldm
     * @date 2016-4-18 上午11:38:01
     */
    private void savePhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        // 应用的版本名称和版本号
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),
                PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);
        pw.println();

        // android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);
        pw.println();

        // 手机制造商
        pw.print("Manufacturer: ");
        pw.println(Build.MANUFACTURER);
        pw.println();

        // 手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);
        pw.println();
    }
}
