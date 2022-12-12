package com.druid.timepaint.timer;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TimeBarTimer {
    private Timer timer;

    public TimeBarTimer() {
        if (timer == null) {
            timer = new Timer();
        }
    }

    public static final long SECONDS_MILLIS = 1000L;

    public static float getTimSecond(){
        return (float) (SECONDS_MILLIS/1000f);
    }

    public void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerProcess();
            }
        }, 0, SECONDS_MILLIS);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void destroy() {
        eventMap.clear();
    }

    private void timerProcess() {
        Iterator<Map.Entry<String, TimerProcessListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, TimerProcessListener> entry = it.next();
            if (entry.getValue() != null) {
                entry.getValue().onTimerProcess();
            }
        }
    }

    private final Map<String, TimerProcessListener> eventMap = new ConcurrentHashMap<>();

    public void registerEvent(String tag, TimerProcessListener listener) {
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


}
