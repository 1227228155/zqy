package net.mlh.zqy.bean;

/**
 * Created by 1227228155@qq.com on 2018/1/16.
 */

public class share {
    public String url ;
    public String version;
    public String content;

    public String userid;
    public String orderNo;
    public String typeIds;
    public String mark;
    public boolean state;

    public share(boolean aState){
        state=aState;
    }

    public share(String aUrl) {
        url = aUrl ;
    }
    public share(String aVersion,String aContent){
        version=aVersion;
        content=aContent;
    }
    public share(String s1,String s2,String s3,String s4){
        orderNo=s1;
        typeIds=s2;
        userid= s3;
        mark = s4;
    }
}
