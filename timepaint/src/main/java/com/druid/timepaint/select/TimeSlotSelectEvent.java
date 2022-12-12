package com.druid.timepaint.select;

import android.content.Context;
import android.view.MotionEvent;

import com.druid.timepaint.bean.TimeSlot;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeSlotSelectEvent implements OnTimeBarSelectedListener {
    private Context context;

    public TimeSlotSelectEvent(Context context) {
        this.context = context;
        init();
    }

    private void init() {

    }

    public void destroyEvent() {
        eventMap.clear();
    }

    private final Map<String, OnTimeBarSelectedListener> eventMap = new ConcurrentHashMap<>();

    public void registerEvent(String tag, OnTimeBarSelectedListener listener) {
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

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void onAddTimeSlot(long startTime, long endTime) {
        Iterator<Map.Entry<String, OnTimeBarSelectedListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarSelectedListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onAddTimeSlot(startTime, endTime);
            }
        }
    }

    @Override
    public void onClickTimeSlot(int timeSecondMillis, TimeSlot timeSlot) {
        Iterator<Map.Entry<String, OnTimeBarSelectedListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarSelectedListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onClickTimeSlot(timeSecondMillis, timeSlot);
            }
        }
    }

    @Override
    public void onEditorTimeSlotStart(TimeSlot timeSlot) {
        Iterator<Map.Entry<String, OnTimeBarSelectedListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarSelectedListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onEditorTimeSlotStart(timeSlot);
            }
        }
    }

    @Override
    public void onEditorTimeSlotEnd(TimeSlot timeSlot) {
        Iterator<Map.Entry<String, OnTimeBarSelectedListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarSelectedListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onEditorTimeSlotEnd(timeSlot);
            }
        }
    }

    @Override
    public void onDeleteSelectedTimeSlot() {
        Iterator<Map.Entry<String, OnTimeBarSelectedListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarSelectedListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onDeleteSelectedTimeSlot();
            }
        }
    }

    @Override
    public void onEditorTimeSlotEndTitleError(TimeSlot timeSlot) {
        Iterator<Map.Entry<String, OnTimeBarSelectedListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarSelectedListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onEditorTimeSlotEndTitleError(timeSlot);
            }
        }
    }

    @Override
    public void onEditorTimeSlotDragging(long startTime, long endTime) {
        Iterator<Map.Entry<String, OnTimeBarSelectedListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OnTimeBarSelectedListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onEditorTimeSlotDragging(startTime, endTime);
            }
        }
    }
}
