package com.lintex9527.android.aimeimei;

/**
 * 消息内容和消息发送者的绑定
 * Created by LinTeX9527 on 2017/9/23.
 */
public class MsgEntity {

    public MsgEntity(FLAGS flag, String msg) {
        this.msg = msg;
        this.flag = flag;
    }

    private String msg; // 消息内容

    public enum FLAGS {SENDER, RECEIVER};
    private FLAGS flag; // 表示消息是发送方的还是接收方的


    public void setFlag(FLAGS flag) {
        this.flag = flag;
    }

    public FLAGS getFlag() {
        return flag;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
