
/*
 * @Title:  ClasFileGJManage.java
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * @Description:  TODO<请描述此文件是做什么的>
 * @author:
 * @data:  2016-3-18 下午3:57:30
 * @version:  V1.0
 */
package com.ck.info;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * TODO<请描述这个类是干什么的>
 *
 * @author
 * @data: 2016-3-18 下午3:57:30
 * @version: V1.0
 */
public class ClasFileGJInfo implements Serializable {
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
    public Bitmap src;
    private String width;

    public String getWidth() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return width == null ? "" : width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public Bitmap getSrc() {
        //其它类型返回字段值本身
        return src;
    }

    public void setSrc(Bitmap src) {
        this.src = src;
    }

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


}
