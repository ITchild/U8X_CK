package com.ck.bean;

/**
 * @author fei
 * @date on 2018/12/5 0005
 * @describe TODO :
 **/
public class PointBean {
    private float X;
    private float Y ;
    private int site;

    public int getSite() {
        //其它类型返回字段值本身
        return site;
    }

    public void setSite(int site) {
        this.site = site;
    }

    public PointBean(float x, float y, int site){
        this.X = x;
        this.Y = y;
        this.site = site;

    }
    public  PointBean (){

    }

    public float getX() {
        //其它类型返回字段值本身
        return X;
    }

    public void setX(float x) {
        X = x;
    }

    public float getY() {
        //其它类型返回字段值本身
        return Y;
    }

    public void setY(float y) {
        Y = y;
    }
}
