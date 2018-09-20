package com.ck.utils;

import android.os.Environment;

import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * TODO<提供文件路径，已经网络数据的路径>
 *
 * @author
 * @data: 2015-9-10 上午9:44:57
 * @version: V1.0
 */
public class PathUtils {
    public static final String ExtSD_PATH = Environment.getExternalStorageDirectory().getPath();

    /**
     * 软件文件夹
     */
    public static final String SYS_PATH = ExtSD_PATH + "/HC-U8X";
    /**
     * 工程目录
     */
    public static final String PROJECT_PATH = SYS_PATH + "/测宽/工程";
    /**
     * 数据库目录
     */
    public static final String DB_PATH = SYS_PATH + "/DB";

    public static final String DB_PATH_NAME = DB_PATH + "/user.db";

    public static final String FILE_PATH = SYS_PATH + "/测宽/FILE";
    public static final String NET_SOFT_UPDATE_PATH = "UpdateFile\\PhoneUpdate\\HCDT5X\\";

    static {
        while (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        File sysFolder = new File(SYS_PATH);
        if (!sysFolder.exists()) {
            sysFolder.mkdirs();
        }

        File dbFolder = new File(DB_PATH);
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
        }
        File proFolder = new File(PROJECT_PATH);
        if (!proFolder.exists()) {
            proFolder.mkdirs();
        }
        File filFolder = new File(FILE_PATH);
        if (!filFolder.exists()) {
            filFolder.mkdirs();
        }
    }

    public static ArrayList<ClasFileProjectInfo> getProFileList() {
        // File projectFile = new File(PathUtils.PROJECT_PATH);
        File[] proList = OrderByDate(PathUtils.PROJECT_PATH);

        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd");
        ArrayList<ClasFileProjectInfo> m_ListProject = new ArrayList<ClasFileProjectInfo>();
        for (int i = 0; i < proList.length; i++) {
            ClasFileProjectInfo project = new ClasFileProjectInfo();
            project.mFileProjectName = proList[i].getName();
            project.mLastModifiedDate = format.format(proList[i].lastModified());
            // File gjFile = new File(PathUtils.PROJECT_PATH,
            // project.mFileProjectName);
            File[] gjList = OrderByDate(PathUtils.PROJECT_PATH + File.separator + project.mFileProjectName);
            // String[] arr = gjFile.list();

            for (int j = 0; j < gjList.length; j++) {
                ClasFileGJInfo gj = new ClasFileGJInfo();
                gj.mFileGJName = gjList[j].getName();
                File file = new File(PathUtils.PROJECT_PATH + File.separator + project.mFileProjectName, gjList[j].getName());
                gj.mLastModifiedDate = format.format(file.lastModified());
                project.mstrArrFileGJ.add(gj);
            }
            m_ListProject.add(project);
        }
        return m_ListProject;
    }

    public static File[] OrderByDate(String fliePath) {
        File file = new File(fliePath);
        File[] fs = file.listFiles();
        if(null == fs){
            return new File[]{};
        }
        Arrays.sort(fs, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff < 0) {
                    return 1;
                } else if (diff == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }

            public boolean equals(Object obj) {
                return true;
            }
        });
        return fs;
    }
}


