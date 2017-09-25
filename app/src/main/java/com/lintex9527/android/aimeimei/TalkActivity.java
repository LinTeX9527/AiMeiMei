package com.lintex9527.android.aimeimei;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;
import com.lintex9527.android.tuling123.TuLing;
import com.lintex9527.android.tuling123.TuLingResult;
import com.lintex9527.android.utils.Constant;
import com.lintex9527.android.utils.DBManager;
import com.lintex9527.android.utils.JsonParser;
import com.lintex9527.android.utils.MySQLiteHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TalkActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TalkActivity.class.getSimpleName();

    // ----------------------------  语音听写相关  -----------------------------
    private SpeechRecognizer mIat;
    // 存储语音识别结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    // 最终的识别结果字符串
    String result_last = null;

    // 语音识别结果成功还是失败
    int ret = 0;

    // ----------------------------- 语音合成相关  --------------------------------
    // 语音合成器
    private SpeechSynthesizer mTts = null;
    // 发音人
    private String voicer = "xiaoyan";

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    // 弹出消息
    private Toast mToast = null;


    // 控件相关
    private EditText editText = null;   // 用户在这里输入消息
    private Button btnSendMsg = null;   // 点击发送用户消息
    private ListView listView = null;   // 加载用户聊天记录
    private Button btnVoice = null;     // 启动语音听写

    // 使用 ListView 来显示数据，所以要绑定适配器MyTextAdapter，还有数据集合 MyMsgLists
    List<MsgEntity> myMsgLists = null;
    MyTextAdapter myTextAdapter = null;

    // 相关的资源
    TuLing robot = null;
    SQLiteDatabase db = null;
    MySQLiteHelper helper = null;

    // 表示数据库中记录的序号
    // 因为数据库中可能已经有了之前的聊天记录，所以需要先获得聊天记录总条数
    int db_index = 0;

    //　Volley　中响应 JsonObjectRequest　的正确返回结果的监听器
    MyResponseListener myResponseListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        initView();
        initData();
        initEvent();

    }

    /**
     * 控件初始化
     */
    private void initView() {
        editText = (EditText) findViewById(R.id.editMsg);
        btnSendMsg = (Button) findViewById(R.id.btnSendMsg);
        listView = (ListView) findViewById(R.id.listview);
        btnVoice = (Button) findViewById(R.id.btnVoice);
    }

    /**
     * 在这里初始化机器人相关的信息
     */
    private void initData() {
        // 弹出消息
        mToast = Toast.makeText(TalkActivity.this, "", Toast.LENGTH_SHORT);

        robot = TuLing.getUniqueInstance();
        robot.setKey(getResources().getString(R.string.key));
        robot.setName("美眉");
        robot.setUserid(UUID.randomUUID().toString());
        //　TODO:
        //　位置信息还需要优化
        robot.setLoc("上海市人民广场");
        robot.setLng("121.478941");
        robot.setLat("31.236009");

        helper = DBManager.getInstance(this);
        db = helper.getWritableDatabase();
        db.close();

        myMsgLists = new ArrayList<>();
        myTextAdapter = new MyTextAdapter(this, myMsgLists);
        listView.setAdapter(myTextAdapter);

        // 当点击ListView中某个项目时，机器人发声
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tvMsg = (TextView) view.findViewById(R.id.tvMsg);
                String text = tvMsg.getText().toString();
                mTts.startSpeaking(text, mTtsListener);
            }
        });

        // Volley 返回正确结果的处理，更新UI并且机器人发生
        myResponseListener = new MyResponseListener();


        // --------------------  语音听写相关 --------------------------
        mIat = SpeechRecognizer.createRecognizer(TalkActivity.this, mIATInitListener);
        // 设置参数
        setParamIAT();

        // --------------------  语音合成相关 -------------------------
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(TalkActivity.this, mTTSInitListener);
        // 语音合成参数初始化
        setParamTTS();
    }

    /**
     * 语音合成初始化监听器。
     */
    private InitListener mTTSInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 语音合成的参数设置
     */
    public void setParamTTS() {
        mTts.setParameter(SpeechConstant.PARAMS, null);

        // 设置引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);

        // 设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);

        // 设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");

        // 设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");

        // 设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");

        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // 设置播放合成音频打断音乐播放器
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存格式只能为 pcm, wav 格式
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");

    }

    /**
     * 设置语音听写参数
     */
    public void setParamIAT() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");


        Log.d(TAG, "语言是 zh_cn");
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");


        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 语音听写初始化监听器。
     */
    private InitListener mIATInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };


    /**
     * 弹出消息
     * @param msg 消息内容
     */
    private void showTip(String msg) {
        mToast.setText(msg);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * 设定控件响应事件
     */
    private void initEvent() {
        btnSendMsg.setOnClickListener(this);
        btnVoice.setOnClickListener(this);
    }

    /**
     * 按钮单击响应事件处理
     *
     * @param view 响应单击事件的控件，主要是Button
     */
    @Override
    public void onClick(View view) {

        // 语音识别初始化失败，直接返回
        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }

        switch (view.getId()) {

            // 启动语音识别
            case R.id.btnVoice:
                // 移动数据分析，收集开始听写事件
                FlowerCollector.onEvent(this, "iat_recognize");
                mIatResults.clear();


                // 不显示听写对话框
                ret = mIat.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("听写失败,错误码：" + ret);
                } else {
                    showTip("开始说话");
                }

                break;

            case R.id.btnSendMsg:
                String msg = editText.getText().toString();
                clearMyMessage(); // 点击按钮需要清空输入框

                //　不能发送空消息
                if (msg.equals("") || msg.trim().equals("")) {
                    showTip("不要发送空消息");
                    return;
                }

                // 添加到数据库
                db = helper.getWritableDatabase();
                // 注意下面字符串的前后需要添加单引号
                db_index = DBManager.getCount(db, Constant.TABLE_NAME);
                // TODO:
                // 比较这里的数据库插入方法
                //String sql = "insert into " + Constant.TABLE_NAME + " values(" + (++db_index) + ", '" + "我" + "', '" + msg + "')";
                //db.execSQL(sql);
                // 插入一个表情符号，例如 ('^') 使用 sql 语句会出错，但是使用 db.insert() 方法却可以成功，这是为什么？
                // 看来最好使用数据库API，而不是 sql 语句
                ContentValues values = new ContentValues();
                values.put(Constant._ID, ++db_index);
                values.put(Constant.USER_NAME, "我");
                values.put(Constant.USER_MESG, msg);
                db.insert(Constant.TABLE_NAME, null, values);
                db.close();

                askRobot(msg);
                break;

            default:

                break;

        }
    }


    /**
     * 用户向机器人发送消息 msg
     *
     * @param msg 用户发送的文本
     */
    private void askRobot(String msg) {

        // 更新UI
        myMsgLists.add(new MsgEntity(MsgEntity.FLAGS.SENDER, msg));
        myTextAdapter.notifyDataSetChanged();

        //-------------------------------------------------------------
        // 这里添加测试 volley 的代码
        RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());
        // 封装请求参数
        Map<String, String> map = new HashMap<>();
        robot.setInfo(msg);
        map.put("key", robot.getKey());
        map.put("info", robot.getInfo());
        map.put("loc", robot.getLoc());
        map.put("userid", robot.getUserid());
        JSONObject jsonObject = new JSONObject(map);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                TuLing.API_URL, jsonObject,
                myResponseListener
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showTip("Volley返回结果出错了");
            }
        });

        jsonObjectRequest.setTag("jsonObjectPOST");
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * JsonObjectRequest 的返回OK 结果的解析器
     * 这里把返回json格式的结果封装成 TuLingResult 对象，并把它的有用信息保存到数据库中
     */
    private class MyResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject jsonObject) {
            TuLingResult tuLingResult = TuLingResult.getUniqueInstance();
            tuLingResult.parseJSONObject(jsonObject);

            // 更新UI
            myMsgLists.add(new MsgEntity(MsgEntity.FLAGS.RECEIVER, tuLingResult.getText()));
            myTextAdapter.notifyDataSetChanged();

            // 朗读机器人返回的结果
            mTts.startSpeaking(tuLingResult.getText().toString(), mTtsListener);

            // 添加到数据库
            db = helper.getWritableDatabase();
            // 更新当前记录的索引号
            db_index = DBManager.getCount(db, Constant.TABLE_NAME);
            // 注意下面字符串的前后需要添加单引号
            String sql = "insert into " + Constant.TABLE_NAME + " values(" + (++db_index) + ", '" + robot.getName() + "', '" + tuLingResult.toString() + "')";
            db.execSQL(sql);
            db.close();
        }
    }

    /**
     * 清空输入框中用户输入的内容
     */
    private void clearMyMessage() {
        editText.setText("");
    }


    /**
     * 解析语音识别过程中讯飞语音返回的json对象，最终形成一个字符串，保存在 result_last 之中
     * @param results 讯飞语音识别语音的结果，多个 results 组成一个完成的识别结果
     */
    private void parseRecognizerResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);


        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        result_last = resultBuffer.toString();

        // 识别结果的最终显示
        //Toast.makeText(getBaseContext(), "识别结果：" + resultBuffer.toString(), Toast.LENGTH_SHORT).show();
    }


    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            if (false && error.getErrorCode() == 14002) {
                showTip(error.getPlainDescription(true) + "\n请确认是否已开通翻译功能");
            } else {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());

            parseRecognizerResult(results);

            if (isLast) {
                // 语音识别的结果保存在字符串 result_last 中，需要发送给图灵机器人
                askRobot(result_last);

            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
}
