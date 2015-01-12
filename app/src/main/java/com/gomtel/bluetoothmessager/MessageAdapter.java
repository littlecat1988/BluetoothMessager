package com.gomtel.bluetoothmessager;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lixiang on 15-1-7.
 */
public class MessageAdapter extends BaseAdapter {
    private int layoutID;
    private ArrayList<HashMap<String, Object>> list;
    private LayoutInflater mInflater;
    private String[] mFrom;
    private int[] mTo;

    public MessageAdapter(Context context, ArrayList<HashMap<String, Object>> list,
                          int layoutID, String [] from, int[] to){
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        this.layoutID = layoutID;
        this.mFrom = from;
        this.mTo = to;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        convertView = mInflater.inflate(layoutID, null);
        View v;
        if(convertView == null){
            v = mInflater.inflate(layoutID,parent,false);
        }else{
            v = convertView;
        }
        bindView(position,v);
        return v;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void bindView(int position, View view) {
        final Map dataSet = list.get(position);
        if(dataSet == null){
            return;
        }
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;

        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
                    if (v instanceof TextView) {
                        // Note: keep the instanceof TextView check at the bottom of these
                        // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                        ((TextView) v).setText((String)list.get(position).get(from[1]));
                    } else if (v instanceof ImageView) {
                        ((ImageView) v).setBackground((Drawable) list.get(position).get(from[0]));
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }

}
