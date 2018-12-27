package com.ck.bean;

/**
 * @author fei
 * @date on 2018/12/20 0020
 * @describe TODO :
 **/
public class MeasureObjBean {

    private int id;
    private String objName;
    private String objCreateDate;

    public int getId() {
        //其它类型返回字段值本身
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObjName() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return objName == null ? "" : objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public String getObjCreateDate() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return objCreateDate == null ? "" : objCreateDate;
    }

    public void setObjCreateDate(String objCreateDate) {
        this.objCreateDate = objCreateDate;
    }
}
