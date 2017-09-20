package com.lintex9527.android.tuling123;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * 请求消息从服务器返回的结果。
 * Created by shzhan on 2017/9/20.
 */

public class TuLingResult {


    //====================================  返回的数据格式  ===================================
    // 文本类，返回结果包含 code, text
    public static final int CODE_TEXT = 100000;
    // 链接类，返回结果包含 code, text, url
    public static final int CODE_LINK = 200000;
    // 新闻类，返回结果包含 code, text, list，其中list又包含 article, source(图片地址), icon, detailurl
    public static final int CODE_NEWS = 302000;
    // 菜谱类，返回结果包含 code, text, list,其中 list 又包含 name, icon(图片地址), info, detailurl
    public static final int CODE_COOKBOOK = 308000;

    //儿歌类，诗词类仅限儿童版使用，暂不用理会
    // 儿歌类
    public static final int CODE_CHILDEN_SONG = 313000;
    // 诗词类
    public static final int CODE_POETRY = 314000;


    //===================================  异常码  =========================================
    // 参数key错误
    public static final int CODE_ERR_KEY = 40001;
    // 请求内容 info 为空
    public static final int CODE_ERR_INFO = 40002;
    // 当天请求次数已使用完
    public static final int CODE_ERR_REQNUM = 40004;
    // 数据格式异常
    public static final int CODE_ERR_FORMAT = 40007;

    private int code;
    private String text;
    private String url;

    HashMap<String, String> listitem = null;
    List<HashMap<String, String>> lists = null;

    //=============================  单实例  ================
    private static TuLingResult uniqueInstance = null;
    private TuLingResult(){

    }
    /**
     * 获取单实例
     * @return
     */
    public static TuLingResult getUniqueInstance(){
        Object object = new Object();
        synchronized (object){
            if (uniqueInstance == null){
                synchronized (object){
                    uniqueInstance = new TuLingResult();
                }
            }
        }
        return uniqueInstance;
    }

    /**
     * 解析 JSONObject 来填充返回的结果
     * @param jsonObject
     */
    public void parseJSONObject(JSONObject jsonObject){
        clearLastResult();
        try {
            setCode(jsonObject.getInt("code"));
            switch (getCode()){
                // 文本类，返回结果包含 code, text
                case CODE_TEXT:
                    setText(jsonObject.getString("text"));
                    break;

                // 链接类，返回结果包含 code, text, url
                case CODE_LINK:
                    setText(jsonObject.getString("text"));
                    setUrl(jsonObject.getString("url"));
                    break;

                // 新闻类，返回结果包含 code, text, list，其中list又包含 article, source(图片地址), icon, detailurl
                case CODE_NEWS:
                    setText(jsonObject.getString("text"));
                    JSONArray thelist = jsonObject.getJSONArray("list");

                    break;

                case CODE_COOKBOOK:
                    break;

                case CODE_CHILDEN_SONG:
                    break;

                case CODE_POETRY:
                    break;

                case CODE_ERR_KEY:
                    break;

                case CODE_ERR_INFO:
                    break;

                case CODE_ERR_REQNUM:
                    break;

                case CODE_ERR_FORMAT:
                    break;

                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空上一次的查询结果，或者初始化所有状态
     */
    public void clearLastResult(){
        code = 0;
        text = "";
        url = "";
    }


    /**
     * 清空列表 lists
     */
    public void clearLists(){
        if (lists == null){
            return;
        }
        lists.clear();
    }

    /**
     * 检验状态码是否出错
     * @return true 表示返回结果有错误；false 表示返回结果正常。
     */
    public boolean isCodeError(){
        return ((this.code == CODE_ERR_FORMAT) ||
                (this.code == CODE_ERR_INFO) ||
                (this.code == CODE_ERR_KEY) ||
                (this.code == CODE_ERR_REQNUM));
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
