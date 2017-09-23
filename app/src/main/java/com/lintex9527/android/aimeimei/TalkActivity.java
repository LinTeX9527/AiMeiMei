package com.lintex9527.android.aimeimei;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lintex9527.android.tuling123.TuLing;
import com.lintex9527.android.tuling123.TuLingResult;
import com.lintex9527.android.utils.Constant;
import com.lintex9527.android.utils.DBManager;
import com.lintex9527.android.utils.MySQLiteHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TalkActivity extends AppCompatActivity implements View.OnClickListener {

    // 控件相关
    private EditText editText = null; // 用户在这里输入消息
    private Button btnSendMsg = null;  // 点击发送用户消息
    private ListView listView = null;   // 加载用户聊天记录

    // 使用 ListView 来显示数据，所以要绑定适配器MyTextAdapter，还有数据集合 MyMsgLists
    List<MsgEntity> myMsgLists = null;
    MyTextAdapter myTextAdapter = null;

    // 相关的资源
    TuLing robot = null;
    SQLiteDatabase db = null;
    MySQLiteHelper helper = null;

    // 表示数据库中记录的序号
    // 因为数据库中可能已经有了之前的聊天记录，所以需要先获得聊天记录总条数
    int index = 0;

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
    private void initView(){
        editText = (EditText) findViewById(R.id.editMsg);
        btnSendMsg = (Button) findViewById(R.id.btnSendMsg);
        listView = (ListView) findViewById(R.id.listview);
    }

    /**
     * 在这里初始化机器人相关的信息
     */
    private void initData(){
        robot = TuLing.getUniqueInstance();
        robot.setKey(getResources().getString(R.string.key));
        robot.setName("美眉");
        robot.setUserid("123456789abc");
        //　TODO:
        //　位置信息还需要优化
        robot.setLoc("上海市人民广场");
        robot.setLng("121.478941");
        robot.setLat("31.236009");

        helper = DBManager.getInstance(this);
        db = helper.getWritableDatabase();
        db.close();

        myMsgLists = new ArrayList<MsgEntity>();
        myTextAdapter = new MyTextAdapter(this, myMsgLists);
        listView.setAdapter(myTextAdapter);

        myResponseListener = new MyResponseListener();
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
                clearMyMessage(); // 点击按钮需要清空输入框

                //　不能发送空消息
                if (msg.equals("") || msg.trim().equals("")){
                    Toast.makeText(getBaseContext(), "不要发送空消息", Toast.LENGTH_SHORT).show();
                    return;
                }

                robot.setInfo(msg);
                myMsgLists.add(new MsgEntity(MsgEntity.FLAGS.SENDER, msg));
                myTextAdapter.notifyDataSetChanged();


                // 添加到数据库
                db = helper.getWritableDatabase();
                // 注意下面字符串的前后需要添加单引号
                index = DBManager.getCount(db, Constant.TABLE_NAME);
                String sql = "insert into " + Constant.TABLE_NAME + " values(" + (++index) + ", '" + "我" + "', '" + msg + "')";
                db.execSQL(sql);
                db.close();

                //-------------------------------------------------------------
                // 这里添加测试 volley 的代码
                RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());
                // 封装请求参数
                Map<String, String> map = new HashMap<>();
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
     * JsonObjectRequest 的返回OK 结果的解析器
     * 这里把返回json格式的结果封装成 TuLingResult 对象，并把它的有用信息保存到数据库中
     */
    private class MyResponseListener implements Response.Listener<JSONObject>{

        @Override
        public void onResponse(JSONObject jsonObject) {
            TuLingResult tuLingResult = TuLingResult.getUniqueInstance();
            tuLingResult.parseJSONObject(jsonObject);

            myMsgLists.add(new MsgEntity(MsgEntity.FLAGS.RECEIVER, tuLingResult.getText()));
            myTextAdapter.notifyDataSetChanged();

            // 添加到数据库
            db = helper.getWritableDatabase();
            // 更新当前记录的索引号
            index = DBManager.getCount(db, Constant.TABLE_NAME);
            // 注意下面字符串的前后需要添加单引号
            String sql = "insert into " + Constant.TABLE_NAME + " values(" + (++index) + ", '" + robot.getName() + "', '" + tuLingResult.toString() + "')";
            db.execSQL(sql);
            db.close();
        }
    }

    /**
     * 清空输入框中用户输入的内容
     */
    private void clearMyMessage(){
        editText.setText("");
    }


}
