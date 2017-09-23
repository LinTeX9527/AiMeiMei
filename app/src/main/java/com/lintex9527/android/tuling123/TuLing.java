package com.lintex9527.android.tuling123;


/**
 * 向图灵机器人服务器发送请求的结构体
 *
 * Created by LinTeX9527 on 2017/9/20.
 */

public class TuLing {

    //==================================  接口地址  ======================================
    public static final String API_URL = "http://www.tuling123.com/openapi/api";

    //===================================  自定义属性  =====================================
    // 用户对机器人的称呼，可以是“小白”，“小明”，“小红”，“美眉”等等
    private String name = null;

    //====================================  请求的参数说明  ==================================
    // key 必须，长度32字节，
    private String key = null;
    // info 必须，长度1-30字节，请求的内容，编码方式必须为UTF-8
    private String info = null;
    // userid 必须，长度1-32字节，只支持0-9,a-z,A-Z，不能包含特殊字符。用户唯一标识，用户对话上下文控制
    private String userid = null;
    // loc 非必须，长度1-30字节，位置信息，请求跟地理位置相关的内容时使用，编码方式必须为UTF-8
    private String loc = null;

    // 经纬度信息，例如 lng = 116.234632, lat = 40.243632 小数点后保留6位。在请求“附近的餐厅”时必须要有 loc, lng, lat 3个参数
    private String lng = null;  // 经度
    private String lat = null;  // 纬度





    private static TuLing uniqueInstance = null;
    //====================================  单实例  ========================================
    private TuLing(){
    }

    /**
     * 获取单实例
     * @return
     */
    public static TuLing getUniqueInstance(){
        Object object = new Object();
        synchronized (object){
            if (uniqueInstance == null){
                synchronized (object){
                    uniqueInstance = new TuLing();
                }
            }
        }
        return uniqueInstance;
    }


    /**
     * 初始化的时候，必须要给 key 赋值，必须使用一个有效的值
     * @param key 访问API接口唯一的认证标志
     */
    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getLoc() {
        return loc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLng() {
        return lng;
    }

    /**
     * 设置经度
     * 百度经纬度查询：
     * http://www.gwm.com.cn/baidumap/index.html
     * @param lng
     */
    public void setLng(String lng) {
        this.lng = lng;
    }

    /**
     * 设置纬度
     * @return
     */
    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }
}
