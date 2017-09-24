package com.lintex9527.android.aimeimei;

import android.app.Application;
import android.util.Log;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;

/**
 * AiMeiMei 应用程序启动入口，在这里初始化语音识别工具类
 * Created by LinTeX9527 on 2017/9/24.
 */

public class MainApp extends Application {
    @Override
    public void onCreate() {

        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符

        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
        SpeechUtility.createUtility(MainApp.this, "appid=" + getString(R.string.app_id));
        //Setting.setShowLog(true);   // 科大讯飞语音库设置开启打印调试信息
        Log.d("=", "能不能启动程序");
        super.onCreate();
    }
}
