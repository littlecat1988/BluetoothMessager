package com.gomtel.bluetoothmessager;

import android.util.Log;
import android.util.Xml;

import com.gomtel.bluetoothmessager.util.LLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiang on 14-12-30.
 */
public class ParseNotification {
    private static final String CHARSET = "UTF-8";
    private static final String TAG = "PARSE";
    private List<NotificationMessage> NotificationMsgs = null;
    private NotificationMessage msg = null;

    public static final String CATEGORY = "category";
    public static final String SUBTYPE = "subType";
    public static final String BODY = "body";
    public static final String SENDER = "sender";
    public static final String APPID = "appId";
    public static final String TIEMSTAMP = "timestamp";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String TICKER_TEXT = "ticker_text";
    public static final String ICON = "icon";
    public static final String NUMBER = "number";
    public static final String MISSED_CALL_COUNT = "missed_call_count";
    private boolean isEnd = false;
    public static final String NOTI = "text";
    public static final String SMS = "sms";
    public static final String MISSCALL = "missed_call";
    private int bodyType = 0;
    public static final int NOTI_TYPE = 1;
    public static final int SMS_TYPE = 2;
    public static final int MISSCALL_TYPE = 3;

    public List<NotificationMessage> parse(byte[] bytes) throws XmlPullParserException, IOException {
        String str = new String(bytes);
        Charset cs = Charset.forName(CHARSET);
        cs.encode(str);
        StringReader stringReader = new StringReader(str);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stringReader);
        int eventType = parser.getEventType();
        LLog.e(TAG, "lixiang---eventType= " + eventType);
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    NotificationMsgs = new ArrayList<NotificationMessage>();
                    LLog.e(TAG,"lixiang---03");
                    break;
                case XmlPullParser.START_TAG:
                    LLog.e(TAG,"lixiang---04= "+parser.getName());
                    if (parser.getName().equals("event_report")) {
                        msg = new NotificationMessage();
                    } else if (parser.getName().equals(CATEGORY)) {
                        eventType = parser.next();
                        msg.setCategory(parser.getText());
                        LLog.e(TAG, "lixiang---CATEGORY= " + parser.getText());
                    }else if (parser.getName().equals(SUBTYPE)) {
                        eventType = parser.next();
                        msg.setSubType(parser.getText());
                        if(parser.getText().equals(NOTI)){
                            bodyType = NOTI_TYPE;
                        }else if(parser.getText().equals(SMS)){
                            bodyType = SMS_TYPE;
                        }else if(parser.getText().equals(MISSCALL)){
                            bodyType = MISSCALL_TYPE;
                        }
                        LLog.e(TAG, "lixiang---SUBTYPE= " + parser.getText());
                    }else if(bodyType == NOTI_TYPE) {
                        if (parser.getName().equals(TICKER_TEXT)) {
                            eventType = parser.next();
                            String text = parser.getText().substring(1,parser.getText().length()-1);
                            msg.setTickerText(text);
                        } else if (parser.getName().equals(ICON)) {
                            eventType = parser.next();
                            msg.setIcon(parser.getText());
                        } else if (parser.getName().equals(SUBTYPE)) {
                            eventType = parser.next();
                            msg.setSubType(parser.getText());
                        }
                    }else if(bodyType == SMS_TYPE) {
                        if (parser.getName().equals(SENDER)) {
                            eventType = parser.next();
                            msg.setSender(parser.getText());
                        } else if (parser.getName().equals(NUMBER)) {
                            eventType = parser.next();
                            msg.setNumber(parser.getText());
                        } else if (parser.getName().equals(CONTENT)) {
                            eventType = parser.next();
                            msg.setContent(parser.getText());
                        }
                    }else if(bodyType == MISSCALL_TYPE) {
                        if (parser.getName().equals(SENDER)) {
                            eventType = parser.next();
                            msg.setSender(parser.getText());
                        } else if (parser.getName().equals(NUMBER)) {
                            eventType = parser.next();
                            msg.setNumber(parser.getText());
                        } else if (parser.getName().equals(CONTENT)) {
                            eventType = parser.next();
                            msg.setContent(parser.getText());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    LLog.e(TAG,"lixiang---END_TAG= "+parser.getName());
                    if (parser.getName().equals("event_report")) {
                        LLog.e(TAG,"lixiang---END_TAG MSG= "+msg);
                        NotificationMsgs.add(msg);
                        msg = null;
                        isEnd = true;
                    }
                    break;
            }
            if(isEnd)
                break;
            eventType = parser.next();
        }
        return NotificationMsgs;
    }

}
