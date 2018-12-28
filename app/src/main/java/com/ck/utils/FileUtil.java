package com.ck.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.ck.App_DataPara;
import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    private static FileUtil mInstance = null;
    public int fileSize = 0;
    public long m_lTotalfileSize = 0;
    private Intent intent;

    static public FileUtil getInstance() {
        if (null == mInstance) {
            mInstance = new FileUtil();
        }
        return mInstance;
    }

    /**
     * 判断文件是否有写权限
     *
     * @param file
     * @return
     */
    public static Boolean canWrite(File file) {
        if (file.isDirectory()) {
            try {
                file = new File(file, "canWriteTestDeleteOnExit.temp");
                if (file.exists()) {
                    boolean checkWrite = checkWrite(file);
                    if (!deleteFile(file)) {
                        file.deleteOnExit();
                    }
                    return checkWrite;
                } else if (file.createNewFile()) {
                    if (!deleteFile(file)) {
                        file.deleteOnExit();
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.i("fei", e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return checkWrite(file);
    }

    /**
     * 检测文件是否有写权限
     *
     * @param file
     * @return
     */
    private static boolean checkWrite(File file) {
        FileWriter fw = null;
        boolean delete = !file.exists();
        boolean result = false;
        try {
            fw = new FileWriter(file, true);
            fw.write("");
            fw.flush();
            result = true;
            return result;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (null != fw) {
                    fw.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (delete && result) {
                deleteFile(file);
            }
        }
    }

    /**
     * 删除文件，如果要删除的对象是文件夹，先删除所有子文件(夹)，再删除该文件
     *
     * @param file 要删除的文件对象
     * @return 删除是否成功
     */
    public static boolean deleteFile(File file) {
        return deleteFile(file, true);
    }

    /**
     * 删除文件，如果要删除的对象是文件夹，则根据delDir判断是否同时删除文件夹
     *
     * @param file   要删除的文件对象
     * @param delDir 是否删除目录
     * @return 删除是否成功
     */
    public static boolean deleteFile(File file, boolean delDir) {
        if (!file.exists()) { // 文件不存在
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        } else {
            boolean result = true;
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) { // 删除所有子文件和子文件夹
                result = deleteFile(children[i], delDir);// 递归删除文件
                if (!result) {
                    return false;
                }
            }
            if (delDir) {
                result = file.delete(); // 删除当前文件夹
            }
            return result;
        }
    }

    // 读文件
    public static byte[] readFile(FileInputStream in, int nLength) {
        byte[] buf = null;
        try {
            buf = new byte[nLength];
            in.read(buf, 0, nLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buf;
    }

    /**
     * 保存bitmap图片为bmp格式
     * @param bmp
     */
    public static void saveBmpImageFile(Bitmap bmp, String m_strSaveProName, String m_strSaveGJName, String style) {
        String mediaState = Environment.getExternalStorageState();
        if ((!mediaState.equals(Environment.MEDIA_MOUNTED)) || (mediaState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            Log.d("fei", "Media storage not ready:" + mediaState);
            return;
        }
        File path = null;
        File imageFile = null;
        path = new File(PathUtils.PROJECT_PATH + m_strSaveProName);
        if (!path.exists()) {
            path.mkdirs();
        }
        String fileName = String.format(style, m_strSaveGJName);
        imageFile = new File(path, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存bitmap图片自定义格式
     * @param json
     */
    public static void saveBmpFile(String json, String m_strSaveProName, String m_strSaveGJName, String style) {
        String mediaState = Environment.getExternalStorageState();
        if ((!mediaState.equals(Environment.MEDIA_MOUNTED)) || (mediaState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            Log.d("fei", "Media storage not ready:" + mediaState);
            return;
        }
        File path = null;
        File imageFile = null;
        path = new File(PathUtils.PROJECT_PATH + m_strSaveProName);
        if (!path.exists()) {
            path.mkdirs();
        }
        String fileName = String.format(style, m_strSaveGJName);
        try {
            imageFile = new File(path, fileName);
            if (!imageFile.exists()) {
                imageFile.getParentFile().mkdirs();
                imageFile.createNewFile();
            }else{
                imageFile.delete();
                imageFile.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(imageFile, "rwd");
            raf.seek(imageFile.length());
            raf.write(json.getBytes());
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存包含标志的bitmap图片为bmp格式
     * 路径自己设置
     *
     * @param bmp
     */
    public static void saveDrawBmpFile(Bitmap bmp, String m_strSaveProName, String m_strSaveGJName, String style) {
        String mediaState = Environment.getExternalStorageState();
        if ((!mediaState.equals(Environment.MEDIA_MOUNTED)) || (mediaState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            Log.d("fei", "Media storage not ready:" + mediaState);
            return;
        }
        File path = null;
        File imageFile = null;
        path = new File(PathUtils.DRAWPROJECT_PATH + m_strSaveProName);
        if (!path.exists() ) {
            path.mkdirs();
        }
        if( !path.canWrite()){
            return;
        }

        String fileName = String.format(style, m_strSaveGJName);

        imageFile = new File(path, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件为字符串
     * @param filePath
     * @return
     */
    public static String readData(String filePath){
        if(Stringutil.isEmpty(filePath)){
            return null;
        }
        String str = "";
        File file = new File(filePath);
        if(file.exists()) {
            try {
                FileInputStream fin = new FileInputStream(file);
                int length = fin.available();
                byte[] buffer = new byte[length];
                fin.read(buffer);
                str = new String(buffer);
                fin.close();
            } catch (Exception e) {

            }
        }
        return str;
    }

    /**
     * 获取现有的文件文件名称，并在存储时进行加1
     *
     * @param strData
     * @return
     */
    public static String GetDigitalPile(String strData) {
        String strName = ""; // 汉字部分
        String strDigital = ""; // 数字部分
        int nDigital = 1; // 数字部分
        for (int i = strData.length() - 1; i >= 0; i--) {
            if (Character.isDigit(strData.charAt(i))) {
                strDigital = String.valueOf(strData.charAt(i)) + strDigital;
            } else {
                strName = strData.substring(0, i + 1);
                break;
            }
        }

        if (!strDigital.equals("")) {
            nDigital = Integer.parseInt(strDigital) + 1;
        }
        return strName + nDigital;
    }

    /**
     * 删除选中的文件
     *
     * @param proData
     */
    public static List<ClasFileProjectInfo> delSeleceFile(List<ClasFileProjectInfo> proData) {
        for (int i = 0; i < proData.size(); i++) {
            int proSelect = proData.get(i).nIsSelect;
            if (proSelect == 1 || proSelect == 2) {
                List<ClasFileGJInfo> ArrFileGJ = proData.get(i).mstrArrFileGJ;
                for (int j = 0; j < ArrFileGJ.size(); j++) {
                    ClasFileGJInfo clasFileGJInfo = ArrFileGJ.get(j);
                    if (clasFileGJInfo.bIsSelect) {
                        //删除原图的单张图片
                        deleteFile(PathUtils.PROJECT_PATH
                                + "/" + proData.get(i).mFileProjectName
                                + "/" + clasFileGJInfo.mFileGJName+".bmp");
                        deleteFile(PathUtils.PROJECT_PATH
                                + "/" + proData.get(i).mFileProjectName
                                + "/" + clasFileGJInfo.mFileGJName+".CK");
                        //删除含有光标的图片的单张图片
                        deleteFile(PathUtils.DRAWPROJECT_PATH
                                + "/" + proData.get(i).mFileProjectName
                                + "/" + clasFileGJInfo.mFileGJName+".bmp");
                        ArrFileGJ.remove(j);
                    }
                }
                if (proSelect == 2) { //全部选中的时候删除文件夹
                    //删除原图的工程
                    deleteDirectory(PathUtils.PROJECT_PATH + "/" + proData.get(i).mFileProjectName);
                    //删除含有光标的图片的工程
                    deleteDirectory(PathUtils.DRAWPROJECT_PATH + "/" + proData.get(i).mFileProjectName);
                    proData.remove(i);
                }
            }
        }
        return proData;
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Log.i("fei", "删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Log.i("fei", "删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Log.i("fei", "删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }


    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.i("fei", "删除单个文件" + fileName + "成功！");
                return true;
            } else {
                Log.i("fei", "删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            Log.i("fei", "删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }


    /**
     * 获取所有所有被选中的文件的大小
     *
     * @param m_ListProject
     * @return
     */
    public static long GetFileSize(List<ClasFileProjectInfo> m_ListProject,int type) {
        long lProFileSize = 0;
        long lGjFileSize = 0;
        for (int i = 0; i < m_ListProject.size(); i++) {
            if (m_ListProject.get(i).nIsSelect == 2) {
                // isExistSelect = true;
                if(type == 1){ //导出之后可直接使用的图片
                    String soDir = PathUtils.DRAWPROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName;
                    lProFileSize += FileUtil.getInstance().getFileSize(new File(soDir));
                }else if(type == 2){ //导出到电脑的图片
                    String soDir = PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName;
                    lProFileSize += FileUtil.getInstance().getFileSize(new File(soDir));
                }else if(type == 3){//以上两种都有的图片
                    String soDir = PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName;
                    lProFileSize += FileUtil.getInstance().getFileSize(new File(soDir));
                    soDir = PathUtils.DRAWPROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName;
                    lProFileSize += FileUtil.getInstance().getFileSize(new File(soDir));
                }
            } else if (m_ListProject.get(i).nIsSelect == 1) {
                // isExistSelect = true;
                for (int j = 0; j < m_ListProject.get(i).mstrArrFileGJ.size(); j++) {
                    if (m_ListProject.get(i).mstrArrFileGJ.get(j).bIsSelect == true) {
                        if(type == 1){ //导出之后可直接使用的图片
                            File sourceF = new File(PathUtils.DRAWPROJECT_PATH +
                                    "/" + m_ListProject.get(i).mFileProjectName + "/"
                                    + m_ListProject.get(i).mstrArrFileGJ.get(j).mFileGJName+".bmp");
                            lGjFileSize += FileUtil.getInstance().getFileSizes(sourceF);
                        }else if(type == 2){ //导出到电脑的图片
                            File sourceF = new File(PathUtils.PROJECT_PATH +
                                    "/" + m_ListProject.get(i).mFileProjectName + "/"
                                    + m_ListProject.get(i).mstrArrFileGJ.get(j).mFileGJName+".bmp");
                            lGjFileSize += FileUtil.getInstance().getFileSizes(sourceF);
                        }else if(type == 3){//以上两种都有的图片
                            File sourceF = new File(PathUtils.PROJECT_PATH +
                                    "/" + m_ListProject.get(i).mFileProjectName + "/" +
                                    m_ListProject.get(i).mstrArrFileGJ.get(j).mFileGJName+".bmp");
                            lGjFileSize += FileUtil.getInstance().getFileSizes(sourceF);
                            sourceF = new File(PathUtils.DRAWPROJECT_PATH +
                                    "/" + m_ListProject.get(i).mFileProjectName + "/"
                                    + m_ListProject.get(i).mstrArrFileGJ.get(j).mFileGJName+".bmp");
                            lGjFileSize += FileUtil.getInstance().getFileSizes(sourceF);
                        }
                    }
                }
            }
        }
        FileUtil.getInstance().m_lTotalfileSize = lProFileSize + lGjFileSize;
        return lProFileSize + lGjFileSize;
    }

    /**
     * 获取设备占用内存的百分比
     *
     * @return
     */
    public static String getFreeSpaceperSend() {
        File datapath = Environment.getDataDirectory();
        StatFs dataFs = new StatFs(datapath.getPath());

        long freeSizes = (long) dataFs.getFreeBlocks() * (long) dataFs.getBlockSize();
        long allSizes = (long) dataFs.getBlockCount() * (long) dataFs.getBlockSize();

        long free = freeSizes / (1024 * 1024);
        long all = allSizes / (1024 * 1024);
        return (free * 100 / all) + "%";
    }

    /**
     * 将文件存储在外部存储中
     * 路径不存在先生成路径，文件不存在生成文件
     *
     * @param path
     * @param name
     * @return
     * @throws IOException
     */
    public static File getFile(String path, String name) throws IOException {
        File parent = new File(path + "/");
        if (!parent.exists()) {
            parent.mkdirs();
        }
        File file = new File(path + "/" + name);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    // 截取byte数组中某段�?
    public byte[] SubByte(byte[] bBefData, int nStartPos, int nEndPos) {
        int nPos = nEndPos - nStartPos;
        // System.out.println("nPos = " + nPos);
        byte[] bData = new byte[nPos];
        for (int i = nStartPos; i < nEndPos; i++) {
            bData[i - nStartPos] = bBefData[i];
        }
        return bData;
    }

    // copy file
    public void copyFile(File source, File target, Context m_Context) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            if(target.exists()){
                target.delete();
            }
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            long len = 0;
            len = in.size();
            in.transferTo(0, in.size(), out);
            fileSize += len;
            // 显示拷贝进度
            // DLG_FileProgress.getInstance().setProgressValue(fileSize,
            // m_lTotalfileSize);
            if (null == intent) {
                intent = new Intent(BroadcastAction.UpdataProgress);
            }
            intent.putExtra("ProgressValue", fileSize);
            m_Context.sendBroadcast(intent);
        } catch (IOException e) {
            intent.putExtra("ProgressValue", -1);
            m_Context.sendBroadcast(intent);
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                in.close();
                outStream.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 复制文件�?
    public void copyDirectiory(String sourceDir, String targetDir, Context m_Context) throws IOException {
        // 新建目标目录
        (new File(targetDir)).mkdirs();
        // 获取源文件夹当前下的文件或目�?
        File soFile = new File(sourceDir);
        if (soFile.exists() && null != soFile) {
            File[] file = (soFile).listFiles();
            for (int i = 0; i < file.length; i++) {
                if (file[i].isFile()) {
                    // 源文�?
                    File sourceFile = file[i];
                    // 目标文件
                    File tarFile = new File(targetDir);
                    if (tarFile.exists() && null != tarFile) {
                        File targetFile = new File(tarFile.getAbsolutePath() + File.separator + file[i].getName());
                        copyFile(sourceFile, targetFile, m_Context);
                    }
                }
                if (file[i].isDirectory()) {
                    // 准备复制的源文件�?
                    String dir1 = sourceDir + "/" + file[i].getName();
                    // 准备复制的目标文件夹
                    String dir2 = targetDir + "/" + file[i].getName();
                    copyDirectiory(dir1, dir2, m_Context);
                }
            }
        }
    }

    // 获得文件大小
    public long getFileSizes(File f) {
        long s = 0;
        try {
            if (f.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(f);
                s = fis.available();
                fis.close();
            } else {
                f.createNewFile();
                // System.out.println("文件夹不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    // 获得文件夹大�?
    public long getFileSize(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    // 获得文件个数
    public long getlist(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        size = flist.length;
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getlist(flist[i]);
                size--;
            }
        }
        return size;
    }

    public String getExternalStorageDirectory() {
        String dir = new String();
        dir = "";
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;

                if (line.contains("fat")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        dir = dir.concat(columns[1] + "\n");
                    }
                } else if (line.contains("fuse")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        dir = dir.concat(columns[1] + "\n");
                    }
                }
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (dir.equals("")) {
            return "";
        }
        if (!dir.contains("USB")) {
            if (!dir.contains("usb")) {
                if (!dir.contains("Usb")) {
                    return "";
                }
            }
        }
        // String m_testStr = dir;
        // m_testStr = initDataSavePath.FILE_PATH;
        String[] strArr = dir.split("\n");
        return strArr[strArr.length - 1];
    }

    /*
     * //新建构件文件，保存构件信�? public void SaveFile(byte[] bfile, String
     * filePath,String fileName) { BufferedOutputStream bos = null;
     * FileOutputStream fos = null; File file = null; try { File dir = new
     * File(filePath); if(!dir.exists()&&!dir.isDirectory()){//判断文件目录是否存在
     * dir.mkdirs(); }
     *
     * String path = filePath + File.separator + fileName + ".US"; file = new
     * File(path); fos = new FileOutputStream(file,true); bos = new
     * BufferedOutputStream(fos); bos.write(bfile); } catch (Exception e) {
     * e.printStackTrace(); } finally { if (bos != null) { try { bos.close(); }
     * catch (IOException e1) { e1.printStackTrace(); } } if (fos != null) { try
     * { fos.close(); } catch (IOException e1) { e1.printStackTrace(); } } } }
     */
    // 新建构件文件，保存构件信�?
    public void SaveFile(ArrayList<byte[]> ProjInfoList, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && !dir.isDirectory()) {// 判断文件目录是否存在
                dir.mkdirs();
            }

            String path = filePath + File.separator + fileName + ".US";
            file = new File(path);
            fos = new FileOutputStream(file, true);
            bos = new BufferedOutputStream(fos);

            for (int i = 0; i < ProjInfoList.size(); i++) {
                bos.write(ProjInfoList.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    /**
     * 文件管理U盘导出文件的最后阶段 ，根据type确定导出的类型
     * @param targetDir
     * @param type 1；导出可直接使用的图片 2：导出到上位机的文件  3： 两种都进行导出
     * @param AppDatPara
     */
    public static void copyFileInThread(String targetDir,int type,App_DataPara AppDatPara){
        File sourceF = null;
        File ckSourceF = null;
        File drawSourceF = null;
        File targetPathF = null;
        File drawTargetPathF = null;
        File targetF = null;
        File ckTargetF = null;
        File drawTargetF = null;

        String soDir = null;
        String drawSoDir = null;
        String tarDir = null;
        String drawTarDir = null;
        for (int i = 0; i < App_DataPara.getApp().proData.size(); i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (App_DataPara.getApp().proData.get(i).nIsSelect == 2) {
                soDir = PathUtils.PROJECT_PATH + "/" + App_DataPara.getApp().proData.get(i).mFileProjectName;
                drawSoDir = PathUtils.DRAWPROJECT_PATH+"/"+App_DataPara.getApp().proData.get(i).mFileProjectName;
                tarDir = targetDir + File.separator +"工程"+File.separator + App_DataPara.getApp().proData.get(i).mFileProjectName;
                drawTarDir = targetDir + File.separator +"Draw工程"+ File.separator+ App_DataPara.getApp().proData.get(i).mFileProjectName;
                try {
                    if(type == 1){ //导出之后可直接使用的图片
                        if (null != drawSoDir && drawSoDir.length() != 0 && null != drawSoDir && drawSoDir.length() != 0) {
                            // 拷贝含有标志的文件
                            FileUtil.getInstance().copyDirectiory(drawSoDir, drawTarDir, AppDatPara);
                        }
                    }else if(type == 2){ //导出到电脑的图片
                        if (null != soDir && soDir.length() != 0 && null != tarDir && tarDir.length() != 0) {
                            // 拷贝原图文件
                            FileUtil.getInstance().copyDirectiory(soDir, tarDir, AppDatPara);
                        }
                    }else if(type == 3){//以上两种都有的图片
                        if (null != soDir && soDir.length() != 0 && null != tarDir && tarDir.length() != 0) {
                            // 拷贝原图文件
                            FileUtil.getInstance().copyDirectiory(soDir, tarDir, AppDatPara);
                        }
                        if (null != drawSoDir && drawSoDir.length() != 0 && null != drawSoDir && drawSoDir.length() != 0) {
                            // 拷贝含有标志的文件
                            FileUtil.getInstance().copyDirectiory(drawSoDir, drawTarDir, AppDatPara);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (App_DataPara.getApp().proData.get(i).nIsSelect == 1) {
                for (int j = 0; j < App_DataPara.getApp().proData.get(i).mstrArrFileGJ.size(); j++) {
                    if (App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).bIsSelect == true) {
                        targetPathF = new File(targetDir + File.separator +
                                "工程"+ File.separator+App_DataPara.getApp().proData.get(i).mFileProjectName);
                        drawTargetPathF = new File(targetDir + File.separator +
                                "Draw工程"+ File.separator+App_DataPara.getApp().proData.get(i).mFileProjectName);
                        sourceF = new File(PathUtils.PROJECT_PATH
                                + "/" + App_DataPara.getApp().proData.get(i).mFileProjectName
                                + "/" + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName+".bmp");
                        ckSourceF = new File(PathUtils.PROJECT_PATH
                                + "/" + App_DataPara.getApp().proData.get(i).mFileProjectName
                                + "/" + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName+".CK");
                        drawSourceF = new File(PathUtils.DRAWPROJECT_PATH
                                + "/" + App_DataPara.getApp().proData.get(i).mFileProjectName
                                + "/" + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName+".bmp");
                        targetF = new File(targetPathF.toString()
                                + File.separator + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName+".bmp");
                        ckTargetF = new File(targetPathF.toString()
                                + File.separator + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName+".CK");
                        drawTargetF = new File(drawTargetPathF.toString()
                                + File.separator + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName+".bmp");

                        if(type == 1){ //导出之后可直接使用的图片
                            // 拷贝含有标志的文件
                            if (null != drawSourceF && null != drawTargetF) {
                                if (drawTargetPathF.exists()) {
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(drawSourceF, drawTargetF, AppDatPara);
                                } else {
                                    // 新建目标目录
                                    (drawTargetPathF).mkdirs();
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(drawSourceF, drawTargetF, AppDatPara);
                                }
                            }
                        }else if(type == 2){ //导出到电脑的图片
                            // 拷贝原图文件
                            if (null != sourceF && null != targetF) {
                                if (targetPathF.exists()) {
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(sourceF, targetF, AppDatPara);
                                } else {
                                    // 新建目标目录
                                    (targetPathF).mkdirs();
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(sourceF, targetF, AppDatPara);
                                }
                            }
                            if (null != ckSourceF && null != ckTargetF) {
                                if (targetPathF.exists()) {
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(ckSourceF, ckTargetF, AppDatPara);
                                } else {
                                    // 新建目标目录
                                    (targetPathF).mkdirs();
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(ckSourceF, ckTargetF, AppDatPara);
                                }
                            }
                        }else if(type == 3){//以上两种都有的图片
                            // 拷贝原图文件
                            if (null != sourceF && null != targetF) {
                                if (targetPathF.exists()) {
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(sourceF, targetF, AppDatPara);
                                } else {
                                    // 新建目标目录
                                    (targetPathF).mkdirs();
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(sourceF, targetF, AppDatPara);
                                }
                            }
                            if (null != ckSourceF && null != ckTargetF) {
                                if (targetPathF.exists()) {
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(ckSourceF, ckTargetF, AppDatPara);
                                } else {
                                    // 新建目标目录
                                    (targetPathF).mkdirs();
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(ckSourceF, ckTargetF, AppDatPara);
                                }
                            }
                            // 拷贝含有标志的文件
                            if (null != drawSourceF && null != drawTargetF) {
                                if (drawTargetPathF.exists()) {
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(drawSourceF, drawTargetF, AppDatPara);
                                } else {
                                    // 新建目标目录
                                    (drawTargetPathF).mkdirs();
                                    // 拷贝文件
                                    FileUtil.getInstance().copyFile(drawSourceF, drawTargetF, AppDatPara);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // 删除路径下所有文�?
    public void deleteAllFilesOfDir(File path) {
        if (!path.exists())
            return;
        if (path.isFile()) {
            path.delete();
            return;
        }
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteAllFilesOfDir(files[i]);
        }
        path.delete();
    }

    // 文件路径是否存在
    public boolean FileExist(String strdir) {
        File file = new File(strdir);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
