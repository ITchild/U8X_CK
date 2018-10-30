package com.ck.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

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
                if(null != fw) {
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
     *
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
        if(!path.exists()) {
            path.mkdirs();
        }

        String fileName = String.format(style, m_strSaveGJName);

        imageFile = new File(path, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
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
    public static void delSeleceFile(List<ClasFileProjectInfo> proData) {
        for (int i = 0; i < proData.size(); i++) {
            int proSelect = proData.get(i).nIsSelect;
            if (proSelect == 1 || proSelect == 2) {
                List<ClasFileGJInfo> ArrFileGJ = proData.get(i).mstrArrFileGJ;
                for (int j = 0; j < ArrFileGJ.size(); j++) {
                    ClasFileGJInfo clasFileGJInfo = ArrFileGJ.get(j);
                    if (clasFileGJInfo.bIsSelect) {
                        String path = PathUtils.PROJECT_PATH
                                + "/" + proData.get(i).mFileProjectName
                                + "/" + clasFileGJInfo.mFileGJName;
                        File file = new File(path);
                        file.delete();
                    }
                }
                if (proSelect == 2) {
                    String path2 = PathUtils.PROJECT_PATH
                            + "/" + proData.get(i).mFileProjectName;
                    File file = new File(path2);
                    file.delete();
                }
            }
        }
    }

    /**
     * 获取所有所有被选中的文件的大小
     *
     * @param m_ListProject
     * @return
     */
    public static long GetFileSize(List<ClasFileProjectInfo> m_ListProject) {
        long lProFileSize = 0;
        long lGjFileSize = 0;
        for (int i = 0; i < m_ListProject.size(); i++) {
            if (m_ListProject.get(i).nIsSelect == 2) {
                // isExistSelect = true;
                String soDir = PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName;
                lProFileSize += FileUtil.getInstance().getFileSize(new File(soDir));
            } else if (m_ListProject.get(i).nIsSelect == 1) {
                // isExistSelect = true;
                for (int j = 0; j < m_ListProject.get(i).mstrArrFileGJ.size(); j++) {
                    if (m_ListProject.get(i).mstrArrFileGJ.get(j).bIsSelect == true) {
                        File sourceF = new File(PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName + "/" + m_ListProject.get(i).mstrArrFileGJ.get(j).mFileGJName);
                        lGjFileSize += FileUtil.getInstance().getFileSizes(sourceF);
                    }
                }
            }
        }
        FileUtil.getInstance().m_lTotalfileSize = lProFileSize + lGjFileSize;
        return lProFileSize + lGjFileSize;
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

    /**
     * 获取设备占用内存的百分比
     * @return
     */
    public static String getFreeSpaceperSend() {
        File datapath = Environment.getDataDirectory();
        StatFs dataFs = new StatFs(datapath.getPath());

        long freeSizes = (long) dataFs.getFreeBlocks() * (long) dataFs.getBlockSize();
        long allSizes = (long) dataFs.getBlockCount() * (long) dataFs.getBlockSize();

        long free = freeSizes / (1024 * 1024);
        long all = allSizes / (1024*1024);
        return (free*100/all )+"%";
    }

}
