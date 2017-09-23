package com.lintex9527.android.aimeimei;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * 适配器，加载用户头像，显示对话内容
 * Created by LinTeX9527 on 2017/9/23.
 */

public class MyTextAdapter extends BaseAdapter {

    private Context context;

    private List<MsgEntity> lists;

    private RelativeLayout layout;

    public MyTextAdapter(Context context, List<MsgEntity> lists) {
        this.context = context;
        this.lists = lists;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        // 判断是发送方还是接收方，分别加载不同的视图
        if (lists.get(position).getFlag() == MsgEntity.FLAGS.SENDER){
            layout = (RelativeLayout) inflater.inflate(R.layout.right_item, null);
        } else if (lists.get(position).getFlag() == MsgEntity.FLAGS.RECEIVER) {
            layout = (RelativeLayout) inflater.inflate(R.layout.left_item, null);
        }

        TextView tv = (TextView) layout.findViewById(R.id.tvMsg);
        tv.setText(lists.get(position).getMsg().toString());
        return layout;
    }
}
