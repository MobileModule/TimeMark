package com.druid.timepaint.bean;


import java.io.Serializable;

public class TimeSlot implements Serializable {

    private float startTime;

    private float endTime;

    private Object obj;

    private String text;

    public TimeSlot(String text,Object obj) {
        this.text=text;
        this.obj = obj;
    }

    public void setStartTimeSecondMillis(float startTime){
        this.startTime=startTime;
    }

    public void setEndTimeSecondMillis(float endTime){
        this.endTime=endTime;
    }

    public float getStartTimeSecondMillis() {
        return (startTime) ;
    }

    public float getEndTimeSecondMillis() {
        return (endTime);
    }

    public float getStartTimeSecond() {
        return (startTime) / 1000f;
    }

    public float getEndTimeSecond() {
        return (endTime) / 1000f;
    }

    public long getStartTimeSecondMillisInt(){
        return  (long)(startTime);
    }

    public long getEndTimeSecondMillisInt() {
        return (long)(endTime);
    }

    public String getText(){
        return text;
    }

    public void setText(String text){
        this.text=text;
    }

    public Object getObject(){
        return obj;
    }

    public int templateColor=0;

    @Override
    public String toString() {
        return "TimeSlot{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
