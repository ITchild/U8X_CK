package com.ck.bean;

public class RxBusMsgBean {
    private int what;
    private Object obj;
    private String msg;
    private int arg1;
    private int arg2;

    public int getArg1() {
        //其它类型返回字段值本身
        return arg1;
    }

    public void setArg1(int arg1) {
        this.arg1 = arg1;
    }

    public int getArg2() {
        //其它类型返回字段值本身
        return arg2;
    }

    public void setArg2(int arg2) {
        this.arg2 = arg2;
    }

    public int getWhat() {
        //其它类型返回字段值本身
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public Object getObj() {
        //其它类型返回字段值本身
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getMsg() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return msg == null ? "" : msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
