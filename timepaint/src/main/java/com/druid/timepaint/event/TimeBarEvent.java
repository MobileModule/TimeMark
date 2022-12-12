package com.druid.timepaint.event;

import android.content.Context;


import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeBarEvent implements OnTimeBarListener{
    private Context context;

    public TimeBarEvent(Context context) {
        this.context = context;
        init();
    }

    private void init() {

    }

    public void destroyEvent() {
        eventMap.clear();
    }

    private final Map<String, OnTimeBarListener> eventMap = new ConcurrentHashMap<>();

    public void registerEvent(String tag, OnTimeBarListener listener) {
        if (eventMap.containsKey(tag)) {
            eventMap.remove(tag);
        }
        eventMap.put(tag, listener);
    }

    public void unregisterEvent(String tag) {
        if (eventMap.containsKey(tag)) {
            eventMap.remove(tag);
        }
    }

    @Override
    public void onClickStopButton() {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onClickStopButton();
            }
        }
    }

    @Override
    public void onBarMoveStop() {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onBarMoveStop();
            }
        }
    }

    @Override
    public void onDragBarMoving(boolean isLeftDrag, long currentTime) {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onDragBarMoving(isLeftDrag, currentTime);
            }
        }
    }

    @Override
    public void onBarAutoMoving(long currentTime) {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onBarAutoMoving(currentTime);
            }
        }
    }

    @Override
    public void onBarMoveFinished(long currentTime) {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onBarMoveFinished(currentTime);
            }
        }
    }

    @Override
    public void onBarMoveExceedStartTime() {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onBarMoveExceedStartTime();
            }
        }
    }

    @Override
    public void onBarMoveExceedEndTime() {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onBarMoveExceedEndTime();
            }
        }
    }

    @Override
    public void onScrollExceedMaxScale() {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onScrollExceedMaxScale();
            }
        }
    }

    @Override
    public void onScrollExceedMinScale() {
        Iterator<Map.Entry<String, OnTimeBarListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onScrollExceedMinScale();
            }
        }
    }
}
