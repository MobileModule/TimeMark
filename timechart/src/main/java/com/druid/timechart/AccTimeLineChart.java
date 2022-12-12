package com.druid.timechart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

import com.druid.timepaint.bean.TimeSlot;
import com.druid.timepaint.event.OnTimeBarListener;
import com.druid.timepaint.event.TimeBarEvent;
import com.druid.timepaint.scroller.ScrollingListener;
import com.druid.timepaint.scroller.TimeScaleZoomScroller;
import com.druid.timepaint.select.OnTimeBarSelectedListener;
import com.druid.timepaint.select.TimeSlotSelectEvent;
import com.druid.timepaint.timer.TimeBarTimer;
import com.druid.timepaint.timer.TimerProcessListener;
import com.druid.timepaint.utils.ArraysDeepCopyUtils;
import com.druid.timepaint.utils.DPUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AccTimeLineChart extends AccPaintLineChart implements TextureView.SurfaceTextureListener,
        TimerProcessListener {
    public static final String TAG = AccTimeLineChart.class.getName();

    public AccTimeLineChart(@NonNull Context context) {
        super(context);
        initView();
    }

    public AccTimeLineChart(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AccTimeLineChart(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public AccTimeLineChart(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        setSurfaceTextureListener(this);
        setCurrentTimeSecondMillis(0);
        if (!bindTimeBarTimer) {
            initBarTimer();
        }

        if (!bindTimeBarScroll) {
            initScaleZoomScroller();
            registerScrollEvent(TAG, scrollingListener);
        }
        if (!bindTimeBarSelectEvent) {

        }
        if (!bindTimeBarStatusEvent) {

        }
    }

    //事件刻度注册
    private TimeBarTimer barTimer;

    private void initBarTimer() {
        barTimer = new TimeBarTimer();
        registerTimerEvent(TAG, processListener);
        barTimer.startTimer();
    }

    public void registerTimerEvent(String tag, TimerProcessListener listener) {
        if (barTimer != null) {
            barTimer.registerEvent(tag, listener);
        }
    }

    private TimerProcessListener processListener = new TimerProcessListener() {
        @Override
        public void onTimerProcess() {
            timerProcess();
        }
    };

    public TimerProcessListener getProcessListener() {
        return processListener;
    }

    private void timerProcess() {
        if (isMoving) {
            if (endTimeSecondMillis != 0) {
                if (getCurrentCenterSecondMillis() >= endTimeSecondMillis) {
                    isMoving = false;
                    return;
                }
            }
            //前进1个刻度
            lastPix -= getItemWith() / 1;
            refreshCanvas();
        }
    }

    //timebar 事件
    private TimeBarEvent timeBarEvent;

    private void initTimeBarEvent() {
        timeBarEvent = new TimeBarEvent(getContext());
    }

    public void registerTimeStatusEvent(String tag, OnTimeBarListener listener) {
        if (timeBarEvent != null) {
            timeBarEvent.registerEvent(tag, listener);
        }
    }

    private OnTimeBarListener timeBarListener = new OnTimeBarListener() {
        @Override
        public void onClickStopButton() {

        }

        @Override
        public void onBarMoveStop() {

        }

        @Override
        public void onDragBarMoving(boolean isLeftDrag, long currentTime) {
            setCurrentTimeSecondMillisNoDelayed(currentTime);
        }

        @Override
        public void onBarAutoMoving(long currentTime) {

        }

        @Override
        public void onBarMoveFinished(long currentTime) {

        }

        @Override
        public void onBarMoveExceedStartTime() {

        }

        @Override
        public void onBarMoveExceedEndTime() {

        }

        @Override
        public void onScrollExceedMaxScale() {

        }

        @Override
        public void onScrollExceedMinScale() {

        }
    };

    public OnTimeBarListener getTimeBarListener() {
        return timeBarListener;
    }

    //滚动事件注册
    private TimeScaleZoomScroller mScroller;

    private void initScaleZoomScroller() {
        mScroller = new TimeScaleZoomScroller(getContext());
    }

    private void registerScrollEvent(String tag, ScrollingListener listener) {
        if (mScroller != null) {
            mScroller.registerEvent(tag, listener);
        }
    }

    private ScrollingListener scrollingListener = new ScrollingListener() {
        @Override
        public void onScrollMoving(int distance) {
            float curDistance = lastPix + distance;
            if (scrollValid(curDistance)) {
                lastPix += distance;
                refreshCanvas();
            }
        }

        @Override
        public void onScrollMovingFinished() {

        }

        @Override
        public void onScrollZoom(float mScale, double time) {
            //控制缩放比例
            if (mScale > 1) {
                if (rulerSpace < MAX_SCALE) {
                    rulerSpace += DPUtils.dip2px(1);
                } else {
//                mHandler.sendEmptyMessage(WHAT_SCALE_MAX);
                }
            } else {
                if (rulerSpace > MIN_SCALE) {
                    rulerSpace -= DPUtils.dip2px(1);
                } else {
//                mHandler.sendEmptyMessage(WHAT_SCALE_MIN);
                }
            }
            //改变刻度文字精度
//        if (rulerSpace <= DPUtils.dip2px(10)) {
//            scaleMode = ScaleMode.KEY_MINUTE;//小时
//        } else {
//            scaleMode = ScaleMode.KEY_SECOND;//分钟
//        }
            setCurrentTimeSecondMillisNoDelayed(getCurrentTimeMillis());//实时设置到当前时间
        }

        @Override
        public void onScrollZoomFinished() {

        }
    };

    public ScrollingListener getScrollingListener() {
        return scrollingListener;
    }

    //视频标注注册
    private TimeSlotSelectEvent slotSelectEvent;

    private void initTimeSlotSelectEvent() {
        slotSelectEvent = new TimeSlotSelectEvent(getContext());
    }

    public void registerTimeSlotSelectEvent(String tag, OnTimeBarSelectedListener listener) {
        if (slotSelectEvent != null) {
            slotSelectEvent.registerEvent(tag, listener);
        }
    }

    private OnTimeBarSelectedListener timeBarSelectedListener = new OnTimeBarSelectedListener() {
        @Override
        public void onAddTimeSlot(long startTime, long endTime) {
            setSelectTimeArea(true);
        }

        @Override
        public void onClickTimeSlot(int timeSecondMillis, TimeSlot timeSlot) {

        }

        @Override
        public void onEditorTimeSlotStart(TimeSlot timeSlot) {
            selectTimeSlot = timeSlot;
            setSelectTimeArea(true);
        }

        @Override
        public void onEditorTimeSlotEnd(TimeSlot timeSlot) {
            isSelectTimeArea = false;
        }

        @Override
        public void onDeleteSelectedTimeSlot() {

        }

        @Override
        public void onEditorTimeSlotEndTitleError(TimeSlot timeSlot) {

        }

        @Override
        public void onEditorTimeSlotDragging(long startTime, long endTime) {
            selectTimeAreaDistanceLeft = startTime / 1000 / itemWithUnit + lastPix;
            selectTimeAreaDistanceRight = endTime / 1000 / itemWithUnit + lastPix;
            refreshCanvas();
        }
    };

    //设置是否选择时间区域
    private void setSelectTimeArea(boolean selectTimeArea) {
        long centerTime = getCurrentTimeMillis();
        if (selectTimeSlot != null) {
            selectTimeAreaDistanceLeft = (selectTimeSlot.getStartTimeSecond()) / itemWithUnit + lastPix;
            selectTimeAreaDistanceLeft -= selectTimeBorderPaddingLeftRightSize;
            selectTimeAreaDistanceRight = (selectTimeSlot.getEndTimeSecond()) / itemWithUnit + lastPix;
            selectTimeAreaDistanceRight += selectTimeBorderPaddingLeftRightSize;
        } else {
            selectTimeAreaDistanceLeft = -1;//需要复位
            selectTimeAreaDistanceRight = -1;//需要复位
            //
            if (getCurrentTimeMillis() < (defaultSelectAreaTimeSecond * 1000L)) {
                centerTime = (long) defaultSelectAreaTimeSecond * 1000L;
            }
        }
        isSelectTimeArea = selectTimeArea;
        setCurrentTimeSecondMillisNoDelayed(centerTime);
    }

    private OnTimeBarSelectedListener getTimeBarSelectedListener() {
        return timeBarSelectedListener;
    }

    public boolean isMoving() {
        return isMoving;
    }

    private void setMoving(boolean moving) {
        isMoving = moving;
    }

    public void openMove() {
        setMoving(true);
    }

    public void closeMove() {
        setMoving(false);
    }

    public float getCurrentCenterSecond() {
        return currentCenterSecond;
    }

    public long getCurrentCenterSecondMillis() {
        return (long) currentCenterSecond * 1000L;
    }

    public void setCurrentTimeSecondMillis(long currentTimeSecondMillis) {
        final long timeSecondMillis = currentTimeSecondMillis;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                lastPix = -((timeSecondMillis - startTimeSecondMillis) / 1000f
                        - getControlWidth() * getCenterMode(true) * itemWithUnit) / itemWithUnit;
                refreshCanvas();
            }
        }, 100);
    }

    private void setCurrentTimeSecondMillisNoDelayed(long currentTimeSecondMillis) {
        int itemWidth = getItemWith();//单个view的宽度
        setItemWithUnit(itemWidth);
        setHeightAccUnit();
        lastPix = -((currentTimeSecondMillis - startTimeSecondMillis) / 1000f -
                getControlWidth() * getCenterMode(true) * itemWithUnit) / itemWithUnit;
        refreshCanvas();
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        refreshCanvas();
    }

    private void refreshCanvas() {
        Canvas canvas = lockCanvas();
        if (canvas != null) {
            if (viewBackgroundColor == 0) {
                canvas.drawColor(viewBackgroundColor, PorterDuff.Mode.CLEAR);
            } else {
                canvas.drawColor(viewBackgroundColor);
            }
            if (paintVideoArea) {
                drawVideoPlayArea(canvas);
            }
            drawXAxisTimeBar(canvas);
            drawGridLine(canvas);
            drawPolyLine(canvas);
            drawRecordArea(canvas);
            //  drawSelectTimeArea(canvas);
            drawYAxis(canvas);
        }
        unlockCanvasAndPost(canvas);
    }

    public void addVideoTimeSlot(ArrayList<TimeSlot> timeSlots) {
        this.timeSlotArray.clear();
        this.timeSlotArray = new ArraysDeepCopyUtils<TimeSlot>().deepCopy(timeSlots);
        for (int i = 0; i < timeSlotArray.size(); i++) {
            TimeSlot timeSlot = timeSlotArray.get(i);
            if (i % 2 == 0) {
                timeSlot.templateColor = chartTagThemeColor;
            } else {
                timeSlot.templateColor = chartTagBlueColor;
            }
        }
        refreshCanvas();
    }

    private boolean paintVideoArea = false;

    public void setPaintVideoArea(boolean paint) {
        this.paintVideoArea = paint;
        refreshCanvas();
    }

    public void setVideoPlayArea(int width, int height, boolean paint) {
        this.videoWidth = width;
        this.videoHeight = height;
        this.paintVideoArea = paint;
        refreshCanvas();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    @Override
    public void onTimerProcess() {

    }

    public long getPreviewCurrentTimeMillis(float distance) {
        long timestamp = (long) (getControlWidth() * itemWithUnit * getCenterMode(true) * 1000L);
        if (distance < 0) {
            timestamp = timestamp + (long) (Math.abs(distance * itemWithUnit) * 1000L);
        } else {
            timestamp = timestamp - (long) (distance * itemWithUnit * 1000L);
        }
        return timestamp;
    }


    private boolean scrollValid(float distance) {
        long timestamp_now = getPreviewCurrentTimeMillis(distance);
        if (timestamp_now <= startTimeSecondMillis || timestamp_now >= endTimeSecondMillis) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (!bindTimeBarScroll) {
        if (mScroller != null)
            mScroller.onTouchEvent(event, TAG, false);
//        }
        return true;
    }
}
