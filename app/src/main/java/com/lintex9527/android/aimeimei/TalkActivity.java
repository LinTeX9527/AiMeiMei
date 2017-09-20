package com.lintex9527.android.aimeimei;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TalkActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String USER_I = "我";
    private static final String USER_ROBOT = "美眉";

    // 关于图灵机器人API的设置
    private static final String API_URL = "http://www.tuling123.com/openapi/api";
    private static final String API_KEY = "10df2c2c1b5f4a63ad7383b977592e15";
    // 自定义当前用户ID
    private static final String API_USERID = "123456abc";

    private EditText editText = null; // 用户在这里输入消息
    private Button btnSendMsg = null;  // 点击发送用户消息
    private TextView tvSayings = null;  // 显示聊天记录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        initView();
        initEvent();

    }

    /**
     * 控件初始化
     */
    private void initView(){
        editText = (EditText) findViewById(R.id.editMsg);
        btnSendMsg = (Button) findViewById(R.id.btnSendMsg);
        tvSayings = (TextView) findViewById(R.id.tvSayings);

        addWhosSaying(USER_I, "你好呀");
    }

    /**
     * 设定控件响应事件
     */
    private void initEvent(){
        btnSendMsg.setOnClickListener(this);
    }

    /**
     * 按钮单击响应事件处理
     * @param view 响应单击事件的控件，主要是Button
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSendMsg:
                String msg = editText.getText().toString();
                //Toast.makeText(getBaseContext(), "消息：" + msg, Toast.LENGTH_SHORT).show();
                addWhosSaying(USER_ROBOT, msg);
                clearMyMessage();

                //-------------------------------------------------------------
                // 这里添加测试 volley 的代码
                RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());
                // 封装请求参数
                Map<String, String> map = new HashMap<>();
                map.put("key", API_KEY);
                map.put("info", "今天天气怎么样？");
                map.put("loc", "上海市黄浦区");
                map.put("userid", API_USERID);
                JSONObject jsonObject = new JSONObject(map);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        API_URL, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                addWhosSaying(USER_ROBOT, jsonObject.toString());
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getBaseContext(), "出错了", Toast.LENGTH_SHORT).show();
                    }
                });

                jsonObjectRequest.setTag("jsonObjectPOST");
                requestQueue.add(jsonObjectRequest);
                // ------------------------------------------------------------
                break;

            default:

                break;

        }
    }

    /**
     * 清空输入框中用户输入的内容
     */
    private void clearMyMessage(){
        editText.setText("");
    }

    /**
     * 在对话框中增加一条消息，显示格式如下：
     * 我：你好呀
     * 机器人：你在干嘛呀？
     * @param who 说话的人的名字
     * @param msg 说的内容
     */
    private void addWhosSaying(String who, String msg){
        String line = who + " : " + msg;
        tvSayings.setText(tvSayings.getText().toString() + "\n" + line);
    }
}
