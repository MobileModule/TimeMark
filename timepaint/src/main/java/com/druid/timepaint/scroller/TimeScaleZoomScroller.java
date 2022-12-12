package com.druid.timepaint.scroller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Scroller;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 辅助滑动监听类
 */
public class TimeScaleZoomScroller {

    private Context context;
    private GestureDetector gestureDetector; //滑动手势
    private Scroller scroller; //滑动辅助类
    private static int lastX;
    private final int ON_FLING = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean isFinished = scroller.computeScrollOffset();
            int curX = scroller.getCurrX();
            lastX = curX;
            if (isFinished)
                handler.sendEmptyMessage(ON_FLING);
            else
                onScrollFinished("",false);
        }
    };

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isDouble == false) {
                final int minX = -0x7fffffff;
                final int maxX = 0x7fffffff;
                lastX = 0;
                scroller.fling(0, 0, (int) -velocityX, 0, minX, maxX, 0, 0);
                handler.sendEmptyMessage(ON_FLING);
            }
            return true;
        }
    };

    public TimeScaleZoomScroller(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
        gestureDetector.setIsLongpressEnabled(false);
        scroller = new Scroller(context);
    }

    private float beforeLength, afterLenght, mScale;
    private boolean isDouble = false;
    private double time;
    private float lastDistanceX;
    private boolean isCanScroll = true;//是否可以拖动--->刚缩放完成1秒内不能拖动（防止时间抖动）


    //由外部传入event事件
    public boolean onTouchEvent(MotionEvent event, String tag, boolean excludeSelf) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isDouble = false;
            scroller.forceFinished(true);
            lastX = (int) event.getX();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (event.getPointerCount() == 1 && !isDouble && isCanScroll) {
                int distanceX = (int) (event.getX() - lastX);
                if (distanceX != 0) {
                    if (Math.abs(Math.abs(distanceX) - Math.abs(lastDistanceX)) < 150) {//防止快速滑动导致数据跳远过大
                        onScroll(distanceX, tag, excludeSelf);
                        lastX = (int) event.getX();
                        lastDistanceX = distanceX;
                    }
                }
            } else if (event.getPointerCount() == 2 && isDouble) {
                isCanScroll = false;//不能在拖动
                afterLenght = getDistance(event);// 获取两点的距离
                if (beforeLength == 0) {
                    beforeLength = afterLenght;
                }
                float gapLength = afterLenght - beforeLength;// 变化的长度
                if (Math.abs(gapLength) > 5f) {
                    mScale = afterLenght / beforeLength;// 求的缩放的比例
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            onZoom(mScale, time, tag, excludeSelf);
                            beforeLength = afterLenght;
                        }
                    }, 90);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getPointerCount() == 1 && !isDouble) {
                onScrollFinished(tag, excludeSelf);
            } else if (isDouble) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isCanScroll = true;//1秒之后才能继续拖动
                    }
                }, 300);
                onZoomFinished(tag, excludeSelf);
            }
        } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
            if (event.getPointerCount() == 2) {
                beforeLength = getDistance(event);
                isDouble = true;
            }
        }
        gestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 计算两点的距离
     **/
    private float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    private void onScrollFinished(String tag, boolean excludeSelf) {
        Iterator<Map.Entry<String, ScrollingListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ScrollingListener> entry = it.next();
            boolean canCallEnable = true;
            if (excludeSelf) {
                if (entry.getKey().equals(tag)) {
                    canCallEnable = false;
                }
            }
            if(canCallEnable) {
                if (entry.getValue() != null) {
                    entry.getValue().onScrollMovingFinished();
                }
            }
        }
    }

    private void onScroll(int distanceX, String tag, boolean excludeSelf) {
        Iterator<Map.Entry<String, ScrollingListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ScrollingListener> entry = it.next();
            boolean canCallEnable = true;
            if (excludeSelf) {
                if (entry.getKey().equals(tag)) {
                    canCallEnable = false;
                }
            }
            if(canCallEnable) {
                if (entry.getValue() != null) {
                    entry.getValue().onScrollMoving(distanceX);
                }
            }
        }
    }

    private void onZoom(float mScale, double time, String tag, boolean excludeSelf) {
        Iterator<Map.Entry<String, ScrollingListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ScrollingListener> entry = it.next();
            boolean canCallEnable = true;
            if (excludeSelf) {
                if (entry.getKey().equals(tag)) {
                    canCallEnable = false;
                }
            }
            if(canCallEnable) {
                if (entry.getValue() != null) {
                    entry.getValue().onScrollZoom(mScale, time);
                }
            }
        }
    }

    private void onZoomFinished(String tag, boolean excludeSelf) {
        Iterator<Map.Entry<String, ScrollingListener>> it = eventMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ScrollingListener> entry = it.next();
            boolean canCallEnable = true;
            if (excludeSelf) {
                if (entry.getKey().equals(tag)) {
                    canCallEnable = false;
                }
            }
            if(canCallEnable) {
                if (entry.getValue() != null) {
                    entry.getValue().onScrollZoomFinished();
                }
            }
        }
    }

    public void destroyEvent() {
        eventMap.clear();
    }

    private final Map<String, ScrollingListener> eventMap = new ConcurrentHashMap<>();

    public void registerEvent(String tag, ScrollingListener listener) {
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
