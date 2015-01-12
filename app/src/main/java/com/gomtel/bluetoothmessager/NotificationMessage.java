package com.gomtel.bluetoothmessager;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by lixiang on 14-12-30.
 */
public class NotificationMessage {

    private String mSender = null;
    private int mTimestamp = 0;
    private String mContent = null;
    private String id = null;
    private String icon;
    private String title;
    private String tickerText;
    private String number;
    private String category;
    private String subtype;

    String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        this.mSender = sender;
    }

    int getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(int timestamp) {
        this.mTimestamp = timestamp;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public void setAppId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return id;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTickerText(String tickerText) {
        this.tickerText = tickerText;
    }

    public String getTickerText() {
        return tickerText;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }


    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory(){
        return category;
    }

    public void setSubType(String subtype) {
        this.subtype = subtype;
    }

    public String getSubType(){
        return subtype;
    }
}