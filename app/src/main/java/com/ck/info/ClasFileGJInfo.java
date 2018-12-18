
/*
 * @Title:  ClasFileGJManage.java
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * @Description:  TODO<请描述此文件是做什么的>
 * @author:
 * @data:  2016-3-18 下午3:57:30
 * @version:  V1.0
 */
package com.ck.info;

import android.support.annotation.NonNull;

import com.ck.utils.Stringutil;

/**
 * TODO<请描述这个类是干什么的>
 *
 * @author
 * @data: 2016-3-18 下午3:57:30
 * @version: V1.0
 */
public class ClasFileGJInfo implements Comparable<ClasFileGJInfo> {
    /**
     * 文件名
     */
    public String mFileGJName;
    /**
     * 修改日期
     */
    public String mLastModifiedDate;
    /**
     * 是否选中
     */
    public boolean bIsSelect = false;

    public String getFileGJName() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return mFileGJName == null ? "" : mFileGJName;
    }

    public void setFileGJName(String fileGJName) {
        mFileGJName = fileGJName;
    }

    public String getLastModifiedDate() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return mLastModifiedDate == null ? "" : mLastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        mLastModifiedDate = lastModifiedDate;
    }

    public boolean isbIsSelect() {
        //其它类型返回字段值本身
        return bIsSelect;
    }

    public void setbIsSelect(boolean bIsSelect) {
        this.bIsSelect = bIsSelect;
    }

    @Override
    public int compareTo(@NonNull ClasFileGJInfo clasFileGJInfo) {
        return Stringutil.compareTo(this.getFileGJName(),clasFileGJInfo.getFileGJName());
    }


}
