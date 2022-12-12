package com.druid.timebar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;


import com.druid.timepaint.event.OnTimeBarListener;
import com.druid.timepaint.event.TimeBarEvent;
import com.druid.timepaint.select.OnTimeBarSelectedListener;
import com.druid.timepaint.event.ScaleMode;
import com.druid.timepaint.scroller.ScrollingListener;
import com.druid.timepaint.scroller.TimeScaleZoomScroller;
import com.druid.timepaint.bean.TimeSlot;
import com.druid.timepaint.select.TimeSlotSelectEvent;
import com.druid.timepaint.timer.TimeBarTimer;
import com.druid.timepaint.timer.TimerProcessListener;
import com.druid.timepaint.utils.ArraysDeepCopyUtils;
import com.druid.timepaint.utils.DPUtils;
import com.druid.timepaint.utils.VibratorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class VideoTimeBarView extends TimeBarPaintView implements TextureView.SurfaceTextureListener {
    public static final String TAG = VideoTimeBarView.class.getName();

    public VideoTimeBarView(@NonNull Context context) {
        super(context);
        initView();
    }

    public VideoTimeBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public VideoTimeBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public VideoTimeBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        setSurfaceTextureListener(this);
        setCurrentTimeSecondMillis(0);
        initTimeBarEvent();
        initTimeSlotSelectEvent();
        initBarTimer();
        initScaleZoomScroller();
    }

    private static final int WHAT_MOVING = 447;
    private static final int WHAT_SCROLL_FINISHED = 448;
    private static final int WHAT_SCALE_MAX = 480;
    private static final int WHAT_SCALE_MIN = 944;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_MOVING:
                    if (timeBarEvent != null) {
                        timeBarEvent.onBarAutoMoving(getCurrentTimeMillis());
                    }
                    break;
                case WHAT_SCROLL_FINISHED:
                    if (timeBarEvent != null) {
                        timeBarEvent.onBarMoveFinished(getCurrentTimeMillis());
                    }
                    break;
                case WHAT_SCALE_MAX:
                    if (timeBarEvent != null) {
                        timeBarEvent.onScrollExceedMaxScale();
                    }
                    break;
                case WHAT_SCALE_MIN:
                    if (timeBarEvent != null) {
                        timeBarEvent.onScrollExceedMinScale();
                    }
                    break;
            }
        }
    };

    private boolean canClickVideoRecordsEditor = false;

    public void setCanClickVideoRecordsEditor(boolean canClick) {
        this.canClickVideoRecordsEditor = canClick;
    }

    public boolean isSelectTimeArea() {
        return isSelectTimeArea;
    }

    private String selectTimeSlotTitle = "";

    public String getSelectTimeSlotTitle() {
        return selectTimeSlotTitle;
    }

    public void setSelectTimeSlotTitle(String text) {
        this.selectTimeSlotTitle = text;
        setCurrentTimeSecondMillisNoDelayed(getCurrentTimeMillis());
    }

    private TimeSlot selectTimeSlot = null;

    //开始编辑或者新增选择区域
    public void openSelectTimeArea(TimeSlot timeSlot) {
        this.selectTimeSlot = timeSlot;
        this.isSelectTimeArea = true;
        setSelectTimeArea(true);
        if (slotSelectEvent != null) {
            if (timeSlot != null) {
                slotSelectEvent.onEditorTimeSlotStart(timeSlot);//开始编辑
            } else {
                long centerSecondMillis = getCurrentCenterSecondMillis();
                long diffSecondMillis = Math.abs(centerSecondMillis - (long) (defaultSelectAreaTimeSecond * 1000));
                long startTime = 0;
                long endTime = 0;
                if (diffSecondMillis < 0) {
                    startTime = 0;
                    endTime = 2 * (long) (defaultSelectAreaTimeSecond * 1000);
                } else {
                    startTime = centerSecondMillis - (long) (defaultSelectAreaTimeSecond * 1000);
                    endTime = centerSecondMillis + (long) (defaultSelectAreaTimeSecond * 1000);
                }
                slotSelectEvent.onAddTimeSlot(startTime, endTime);//新增标注
            }
        }

    }

    //保存标签并退出
    public void closeSaveSelectTimeArea() {
        this.isSelectTimeArea = false;
        TimeSlot timeSlot = getSelectTimeSlot();
        sortTimeSlotList(timeSlot);
        if (slotSelectEvent != null) {
            slotSelectEvent.onEditorTimeSlotEnd(timeSlot);
        }
        setSelectTimeArea(false);
    }

    //强制退出标签编辑
    public void forceDeleteSelectTimeArea() {
        this.selectTimeSlot = null;
        if (slotSelectEvent != null) {
            slotSelectEvent.onDeleteSelectedTimeSlot();
        }
        setSelectTimeArea(false);
    }

    private void sortTimeSlotList(TimeSlot selectTimeSlot) {
        timeSlotArray.add(selectTimeSlot);
        TimeSlot[] timeSlots = new TimeSlot[timeSlotArray.size()];
        timeSlotArray.toArray(timeSlots);
        //冒泡排序
        TimeSlot timeSlotTemp = null;
        for (int i = 0; i < timeSlots.length - 1; i++) {
            for (int j = 0; j < timeSlots.length - 1 - i; j++) {
                if (timeSlots[j].getStartTimeSecond() > timeSlots[j + 1].getStartTimeSecond()) {
                    timeSlotTemp = timeSlots[j];
                    timeSlots[j] = timeSlots[j + 1];
                    timeSlots[j + 1] = timeSlotTemp;
                }
            }
        }
        //时间裁剪
        int putPosition = -1;
        for (int i = 0; i < timeSlots.length; i++) {
            TimeSlot timeSlot = timeSlots[i];
            if (timeSlot == selectTimeSlot) {
                putPosition = i;
                break;
            }
        }

        TimeSlot splitRightTimeSlot = null;
        if (putPosition != -1) {
            for (int i = 0; i < timeSlots.length; i++) {
                if (putPosition != i) {
                    TimeSlot timeSlot = timeSlots[i];
                    if (timeSlot != null) {
                        //select完成包含
                        if (selectTimeSlot.getStartTimeSecond() <= timeSlot.getStartTimeSecond() &&
                                selectTimeSlot.getEndTimeSecond() >= timeSlot.getEndTimeSecond()) {
                            timeSlots[i] = null;
                            continue;
                        }
                        //完成包含select
                        if (selectTimeSlot.getStartTimeSecond() > timeSlot.getStartTimeSecond() &&
                                selectTimeSlot.getEndTimeSecond() < timeSlot.getEndTimeSecond()) {
                            //切割为两个
                            splitRightTimeSlot = new TimeSlot(timeSlot.getText(), null);
                            splitRightTimeSlot.setStartTimeSecondMillis(selectTimeSlot.getEndTimeSecondMillis());
                            splitRightTimeSlot.setEndTimeSecondMillis(timeSlot.getEndTimeSecondMillis());
                            //left
                            timeSlot.setEndTimeSecondMillis(selectTimeSlot.getStartTimeSecondMillis());
                            continue;
                        }
                        //右边有交集
                        if (selectTimeSlot.getStartTimeSecond() < timeSlot.getEndTimeSecond() &&
                                selectTimeSlot.getEndTimeSecond() >= timeSlot.getEndTimeSecond()) {
                            timeSlot.setEndTimeSecondMillis(selectTimeSlot.getStartTimeSecondMillis());
                            continue;
                        }
                        //左边有交集
                        if (selectTimeSlot.getEndTimeSecond() > timeSlot.getStartTimeSecond() &&
                                selectTimeSlot.getEndTimeSecond() <= timeSlot.getEndTimeSecond()) {
                            timeSlot.setStartTimeSecondMillis(selectTimeSlot.getEndTimeSecondMillis());
                            continue;
                        }
                    }
                }
            }
        }

        timeSlotArray.clear();
        for (int i = 0; i < timeSlots.length; i++) {
            TimeSlot timeSlot = timeSlots[i];
            if (timeSlot != null) {
                timeSlotArray.add(timeSlot);
            }
        }
        if (splitRightTimeSlot != null) {
            if (putPosition != -1) {
                timeSlotArray.add(putPosition + 1, splitRightTimeSlot);
            }
        }
        //赋值颜色
        for (int i = 0; i < timeSlotArray.size(); i++) {
            TimeSlot timeSlot = timeSlotArray.get(i);
            if (i % 2 == 0) {
                timeSlot.templateColor = tagEvenNumColor;
            } else {
                timeSlot.templateColor = tagOddNumColor;
            }
        }
    }

    //设置是否选择时间区域
    private void setSelectTimeArea(boolean selectTimeArea) {
        if (selectTimeArea) {//选择的时候需要停止选择
            if (scaleMode == ScaleMode.KEY_HOUR || scaleMode == ScaleMode.KEY_MINUTE) {
                scaleMode = ScaleMode.KEY_SECOND;//要恢复到分钟模式，否则刻度精度太高无法选择
                rulerSpace = DEFAULT_RULER_SPACE;
            }
            if (isMoving) {
                if (timeBarEvent != null) {
                    timeBarEvent.onBarMoveStop();
                }
            }
            isMoving = false;
        }
        long centerTime = getCurrentTimeMillis();
        if (selectTimeSlot != null) {
            selectTimeSlotTitle = selectTimeSlot.getText();
            selectTimeAreaDistanceLeft = (selectTimeSlot.getStartTimeSecond()) / itemWithUnit + lastPix;
            selectTimeAreaDistanceLeft -= selectTimeBorderPaddingLeftRightSize;
            selectTimeAreaDistanceRight = (selectTimeSlot.getEndTimeSecond()) / itemWithUnit + lastPix;
            selectTimeAreaDistanceRight += selectTimeBorderPaddingLeftRightSize;
        } else {
            selectTimeSlotTitle = "";
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

    public long getPreviewCurrentTimeMillis(float distance) {
        long timestamp = (long) (getControlWidth() * itemWithUnit * getCenterMode(true) * 1000L);
        if (distance < 0) {
            timestamp = timestamp + (long) (Math.abs(distance * itemWithUnit) * 1000L);
        } else {
            timestamp = timestamp - (long) (distance * itemWithUnit * 1000L);
        }
        return timestamp;
    }

    public void setOnSelectedTimeListener(String tag, OnTimeBarSelectedListener onSelectedTimeListener) {
        if (onSelectedTimeListener == null) {
            if (slotSelectEvent != null) {
                slotSelectEvent.unregisterEvent(tag);
            }
        } else {
            registerTimeSlotSelectEvent(tag, onSelectedTimeListener);
        }
    }

    public void setViewHeightForDp(int view_height) {
        this.view_height = DPUtils.dip2px(view_height);
        refreshCanvas();
    }

    /**
     * 设置一个100ms的延迟，避免过快设置导致短时间内重复绘制
     */
    public void setCurrentTimeSecondMillis(long currentTimeSecondMillis) {
//        currentTimeSecondMillis = currentTimeSecondMillis / 1000L;
        final long timeSecondMillis = currentTimeSecondMillis;// * 1000L;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                lastPix = -((timeSecondMillis - startTimeSecondMillis) / 1000f
                        - getControlWidth() * getCenterMode(true) * itemWithUnit) / itemWithUnit;
                refreshCanvas();
            }
        }, 100);
    }

    /**
     * 设置当前时间的毫秒值.(没有延迟)
     * 设置一个100ms的延迟，避免过快设置导致短时间内重复绘制
     */
    private void setCurrentTimeSecondMillisNoDelayed(long currentTimeSecondMillis) {
        int itemWidth = getItemWith();//单个view的宽度
        setItemWithUnit(itemWidth);
        lastPix = -((currentTimeSecondMillis - startTimeSecondMillis) / 1000f -
                getControlWidth() * getCenterMode(true) * itemWithUnit) / itemWithUnit;
        refreshCanvas();
    }

    private TimerProcessListener processListener = new TimerProcessListener() {
        @Override
        public void onTimerProcess() {
            timerProcess();
        }
    };

    private void timerProcess() {
        if (isMoving) {
            if (endTimeSecondMillis != 0) {
                if (getCurrentCenterSecondMillis() >= endTimeSecondMillis) {
                    isMoving = false;
                    if (timeBarEvent != null) {
                        timeBarEvent.onBarMoveStop();
                    }
                    return;
                }
            }
            //前进1个刻度
            if (scaleMode == ScaleMode.KEY_SECOND) {//1000/5=200;
                lastPix -= (float) (getItemWith() / TimeBarTimer.getTimSecond());
            }
            if (scaleMode == ScaleMode.KEY_MINUTE) {
                lastPix -= getItemWith() / 60.0;
            }
            if (scaleMode == ScaleMode.KEY_HOUR) {
                lastPix -= getItemWith() / (10 * 60.0);
            }
            refreshCanvas();
            if (timeBarEvent != null) {
                if (getCurrentTimeMillis() >= startTimeSecondMillis + 24 * 60 * 60 * 1000) {
                    timeBarEvent.onBarMoveFinished(getCurrentTimeMillis());
                    setMoving(false);
                } else {
                    mHandler.sendEmptyMessage(WHAT_MOVING);
                }
            }
        }
    }

    public boolean isMoving() {
        return isMoving;
    }

    private void setMoving(boolean moving) {
        isMoving = moving;
    }

    public void openMove() {
        scaleMode = ScaleMode.KEY_SECOND;
        reserveCenter();
        setMoving(true);
    }

    //纠偏复位右移
    private void reserveCenter() {
        float yu = Math.abs(lastPix % getItemWith());
        if (yu != 0) {
            yu = getItemWith() - yu;
            if (lastPix < 0) {
                lastPix -= yu;
            } else {
                lastPix += yu;
            }
        }
    }

    public void closeMove() {
        setMoving(false);
    }

    private void refreshCanvas() {
        Canvas canvas = lockCanvas();
        if (canvas != null) {
            canvas.drawColor(viewBackgroundColor);//画背景、由于TextureView不支持直接设置背景颜色，只能按这种方式
            drawStopButton(canvas);//画暂停按钮
            drawUpAndDownLine(canvas);//画上下两条线
            drawTextAndRuler(canvas);//画文本和刻度
            drawRecordArea(canvas);//画有效视频标注区域
            drawSelectTimeArea(canvas);//画视频选择区域
            drawCenterCursor(canvas);//画中间标线
        }
        unlockCanvasAndPost(canvas);
    }

    private boolean changeFlag = false;

    private float getDragPaddingLeftRightTimeSecond() {
        return selectTimeBorderPaddingLeftRightSize / itemWithUnit;
    }

    private RectF leftDragRect = null;
    private RectF rightDragRect = null;

    private void drawSelectTimeArea(Canvas canvas) {
        if (isSelectTimeArea) {
            if (selectTimeAreaDistanceLeft == -1) {
                selectTimeAreaDistanceLeft = (getCurrentTimeMillis() - startTimeSecondMillis) / 1000f
                        / itemWithUnit + lastPix - defaultSelectAreaTimeSecond / itemWithUnit;
                selectTimeAreaDistanceLeft -= selectTimeBorderPaddingLeftRightSize;
            }
            if (selectTimeAreaDistanceRight == -1) {
                selectTimeAreaDistanceRight = (getCurrentTimeMillis() - startTimeSecondMillis) / 1000f
                        / itemWithUnit + lastPix + defaultSelectAreaTimeSecond / itemWithUnit;
                selectTimeAreaDistanceRight += selectTimeBorderPaddingLeftRightSize;
            }
            final float startY = rulerHeightBig + keyTextHeight + tagAreaMarginTop;
            final float stopY = view_height - upAndDownLineWidth;
            //border
            selectAreaBorderPaint.setStrokeWidth(selectTimeHorizontalStrokeWidth);
            RectF rectF = new RectF(selectTimeAreaDistanceLeft, startY,
                    selectTimeAreaDistanceRight, stopY);
            canvas.drawRoundRect(rectF, selectTimeBorderCornerSize, selectTimeBorderCornerSize, selectAreaBorderPaint);
            //
            leftDragRect = new RectF(selectTimeAreaDistanceLeft, startY,
                    selectTimeAreaDistanceLeft + selectTimeBorderPaddingLeftRightSize, stopY);
//            canvas.drawRect(leftDragRect, dragAreaPaint);
            rightDragRect = new RectF(selectTimeAreaDistanceRight - selectTimeBorderPaddingLeftRightSize, startY,
                    selectTimeAreaDistanceRight, stopY);
//            canvas.drawRect(rightDragRect, dragAreaPaint);
            //dash line
            dashSelectAreaPath.reset();
            if (dragStatus == -1) {//左
                dashSelectAreaPath.moveTo(selectTimeAreaDistanceLeft + selectTimeBorderPaddingLeftRightSize,
                        view_height);
                dashSelectAreaPath.lineTo(selectTimeAreaDistanceLeft + selectTimeBorderPaddingLeftRightSize,
                        0);
            }

            if (dragStatus == 1) {//右
                dashSelectAreaPath.moveTo(selectTimeAreaDistanceRight - selectTimeBorderPaddingLeftRightSize,
                        view_height);
                dashSelectAreaPath.lineTo(selectTimeAreaDistanceRight - selectTimeBorderPaddingLeftRightSize,
                        0);
            }
            if (!dashSelectAreaPath.isEmpty()) {
                canvas.drawPath(dashSelectAreaPath, dashSelectAreaPaint);
            }
            //drag
            float paddingTopBottom = (stopY - startY) / 3f;
            canvas.drawLine(selectTimeAreaDistanceLeft + selectTimeBorderPaddingLeftRightSize / 2f,
                    startY + paddingTopBottom,
                    selectTimeAreaDistanceLeft + selectTimeBorderPaddingLeftRightSize / 2f,
                    stopY - paddingTopBottom, selectAreaDragPaint);

            canvas.drawLine(selectTimeAreaDistanceRight - selectTimeBorderPaddingLeftRightSize / 2f,
                    startY + paddingTopBottom,
                    selectTimeAreaDistanceRight - selectTimeBorderPaddingLeftRightSize / 2f,
                    stopY - paddingTopBottom, selectAreaDragPaint);
            //画带透明色的选择区域
            if (selectTimeSlot == null) {
                selectAreaCenterPaint.setColor(selectTimeCenterColor);
            } else {
                if (selectTimeSlot.templateColor != 0) {
                    selectAreaCenterPaint.setColor(selectTimeSlot.templateColor);
                } else {
                    selectAreaCenterPaint.setColor(selectTimeCenterColor);
                }
            }
            selectAreaCenterRect.set(selectTimeAreaDistanceLeft + selectTimeBorderPaddingLeftRightSize,
                    startY + selectTimeBorderPaddingTopBottomSize,
                    selectTimeAreaDistanceRight - selectTimeBorderPaddingLeftRightSize,
                    stopY - selectTimeBorderPaddingTopBottomSize);
            canvas.drawRect(selectAreaCenterRect, selectAreaCenterPaint);
            //文字
            if (!TextUtils.isEmpty(selectTimeSlotTitle)) {
                Rect rect = new Rect();
                keyTickTextPaint.getTextBounds(selectTimeSlotTitle, 0, selectTimeSlotTitle.length(), rect);
                keyTextHeight = rect.height();
                tagTextPaint.getTextBounds(selectTimeSlotTitle, 0, selectTimeSlotTitle.length(), rect);
                float tagWidth = tagTextPaint.measureText(selectTimeSlotTitle);
                float tagHeight = rect.height();
                float visibleWidth = selectAreaCenterRect.width();
                if (visibleWidth >= tagWidth) {
                    float tagCenterPointY = (view_height - (upAndDownLineWidth * 2 + rulerHeightBig +
                            keyTextHeight + tagAreaMarginTop + selectTimeBorderPaddingTopBottomSize * 2)) / 2f;
                    tagCenterPointY += upAndDownLineWidth + rulerHeightBig + keyTextHeight +
                            tagAreaMarginTop + tagHeight / 2f;
                    canvas.drawText(selectTimeSlotTitle, selectTimeAreaDistanceLeft + selectTimeBorderPaddingLeftRightSize +
                            tagMarginTopSize, tagCenterPointY, tagTextPaint);
                }
            }
        }
    }

    public long getSelectEndTimeSecondMillis() {
        if (selectTimeAreaDistanceRight == -1) {
            return startTimeSecondMillis +
                    (long) (currentCenterSecond * 1000) + (long) (getDragPaddingLeftRightTimeSecond() * 1000);
        } else {
            return startTimeSecondMillis +
                    (long) ((selectTimeAreaDistanceRight - lastPix - selectTimeBorderPaddingLeftRightSize)
                            * itemWithUnit * 1000);
        }
    }

    public long getPreviewSelectEndTimeSecondMillis(float distanceRight) {
        if (selectTimeAreaDistanceRight == -1) {
            return startTimeSecondMillis +
                    (long) (currentCenterSecond * 1000) + (long) (getDragPaddingLeftRightTimeSecond() * 1000);
        } else {
            return startTimeSecondMillis +
                    (long) ((distanceRight - lastPix - selectTimeBorderPaddingLeftRightSize)
                            * itemWithUnit * 1000);
        }
    }

    public long getSelectStartTimeSecondMillis() {
        if (selectTimeAreaDistanceLeft == -1) {
            return startTimeSecondMillis + (long) (currentCenterSecond * 1000) -
                    (long) (getDragPaddingLeftRightTimeSecond() * 1000);
        } else {
            return startTimeSecondMillis +
                    (long) ((selectTimeAreaDistanceLeft - lastPix + selectTimeBorderPaddingLeftRightSize) *
                            itemWithUnit * 1000);
        }
    }

    public long getPreviewSelectStartTimeSecondMillis(float distanceLeft) {
        if (selectTimeAreaDistanceLeft == -1) {
            return startTimeSecondMillis + (long) (currentCenterSecond * 1000) -
                    (long) (getDragPaddingLeftRightTimeSecond() * 1000);
        } else {
            return startTimeSecondMillis +
                    (long) ((distanceLeft - lastPix + selectTimeBorderPaddingLeftRightSize) *
                            itemWithUnit * 1000);
        }
    }

    private float getSelectMoveTimeSecond(float pointX) {
        float timeSecond = (pointX - lastPix + selectTimeBorderPaddingLeftRightSize) * itemWithUnit;
        return timeSecond;
    }

    private List<RectF> cacheRecordsAreaRects = new ArrayList<>();
    private List<TimeSlot> cacheRecordsTimeSlot = new ArrayList<>();

    private void drawRecordArea(Canvas canvas) {
        cacheRecordsAreaRects.clear();
        cacheRecordsTimeSlot.clear();
        for (TimeSlot timeSlot : timeSlotArray) {
            freshRecordArea(canvas, timeSlot);
        }
        if (videoTimeSlot != null) {
            freshVideoTimeSlot();
            freshRecordArea(canvas, videoTimeSlot);
        }
    }

    private void freshRecordArea(Canvas canvas, TimeSlot timeSlot) {
//        if (isEvenNumber) {
        tagAreaPaint.setColor(timeSlot.templateColor);
//            timeSlot.templateColor = tagEvenNumColor;
//        } else {
//            tagAreaPaint.setColor(tagOddNumColor);
//            timeSlot.templateColor = tagOddNumColor;
//        }
//        isEvenNumber = !isEvenNumber;
        //判断是否在可见范围内
        if (timeSlot.getEndTimeSecond() > getScreenLeftTimeSecond() && timeSlot.getStartTimeSecond() < getScreenRightTimeSecond()) {
            Rect rect = new Rect();
            //时间文字高度
            keyTickTextPaint.getTextBounds(keyText, 0, keyText.length(), rect);
            keyTextHeight = rect.height();
            //标注字宽度
            tagTextPaint.getTextBounds(timeSlot.getText(), 0, timeSlot.getText().length(), rect);
            float tagWidth = tagTextPaint.measureText(timeSlot.getText());
            float tagHeight = rect.height();
            //画可见标注区域内
            float startX = getRightXByTimeSecond(timeSlot.getStartTimeSecond()) + lastPix;
            startX = startX < 0 ? 0 : startX;//左边超出屏幕（<0）的就不画
            float endX = getRightXByTimeSecond(timeSlot.getEndTimeSecond()) + lastPix;
            endX = endX > getControlWidth() ? getControlWidth() : endX;//右边超出屏幕（>getControlWidth）的就不画
            tagAreaRect.set(startX, rulerHeightBig + keyTextHeight + tagAreaMarginTop, endX,
                    view_height - upAndDownLineWidth);
            cacheRecordsAreaRects.add(new RectF(startX, rulerHeightBig + keyTextHeight + tagAreaMarginTop, endX,
                    view_height - upAndDownLineWidth));
            cacheRecordsTimeSlot.add(timeSlot);
            canvas.drawRect(tagAreaRect, tagAreaPaint);
            //画可见标注文字
            float visibleWidth = tagAreaRect.width();
            if (visibleWidth >= tagWidth) {
                float tagCenterPointY = (view_height - (upAndDownLineWidth * 2 + rulerHeightBig +
                        keyTextHeight + tagAreaMarginTop)) / 2f;
                tagCenterPointY += upAndDownLineWidth + rulerHeightBig + keyTextHeight +
                        tagAreaMarginTop + tagHeight / 2f;
                canvas.drawText(timeSlot.getText(), startX + tagMarginTopSize,
                        tagCenterPointY, tagTextPaint);
            }
        }
    }

    public List<TimeSlot> getTimeSlotArray() {
        return timeSlotArray;
    }

    public TimeSlot getSelectTimeSlot() {
        TimeSlot timeSlot = null;
        if (selectTimeSlot != null) {
            timeSlot = selectTimeSlot;
            timeSlot.setText(selectTimeSlotTitle);
        } else {
            timeSlot = new TimeSlot(selectTimeSlotTitle, null);
        }
        timeSlot.setStartTimeSecondMillis(getSelectStartTimeSecondMillis());
        timeSlot.setEndTimeSecondMillis(getSelectEndTimeSecondMillis());
        return timeSlot;
    }

    private TimeSlot videoTimeSlot = null;

    public void startVideoTimeSlot(String text, Object tag, long startTime) {
        boolean canRestart = true;
        if (videoTimeSlot != null) {
            if (videoTimeSlot.getText().equals(text)) {
                canRestart = false;
            }
        }
        if (canRestart) {
            if (videoTimeSlot != null) {
                timeSlotArray.add(videoTimeSlot);
            }
            videoTimeSlot = new TimeSlot(text, tag);
            int size = timeSlotArray.size();
            if (size % 2 == 0) {
                videoTimeSlot.templateColor = tagEvenNumColor;
            } else {
                videoTimeSlot.templateColor = tagOddNumColor;
            }
            videoTimeSlot.setStartTimeSecondMillis(startTime);
            refreshCanvas();
        }
    }

    public void endVideoTimeSlot() {
        if (videoTimeSlot != null) {
            //videoTimeSlot.setEndTime(endTime);
            if (videoTimeSlot != null) {
                timeSlotArray.add(videoTimeSlot);
            }
            videoTimeSlot = null;
            refreshCanvas();
        }
    }

    public void freshVideoTimeSlot() {
        if (videoTimeSlot != null) {
            videoTimeSlot.setEndTimeSecondMillis(getCurrentCenterSecondMillis());
            //refreshCanvas();
        }
    }

    public void addVideoTimeSlot(ArrayList<TimeSlot> timeSlots) {
        this.timeSlotArray.clear();
//        this.timeSlotArray.addAll(timeSlots);
        this.timeSlotArray = new ArraysDeepCopyUtils<TimeSlot>().deepCopy(timeSlots);
        //赋值颜色
        for (int i = 0; i < timeSlotArray.size(); i++) {
            TimeSlot timeSlot = timeSlotArray.get(i);
            if (i % 2 == 0) {
                timeSlot.templateColor = tagEvenNumColor;
            } else {
                timeSlot.templateColor = tagOddNumColor;
            }
        }
        refreshCanvas();
    }

    /**
     * 通过时间获取开始x坐标
     */
    private float getRightXByTimeSecond(float timeSecond) {
        return timeSecond / itemWithUnit;
    }

    /**
     * 获取屏幕最左边时间.
     * 用来判断是否需要画视频区域(不可见的就不画)
     */
    private float getScreenLeftTimeSecond() {
        return getCurrentCenterSecond() - getControlWidth() * getCenterMode(true) * itemWithUnit;
    }

    /**
     * 获取屏幕最右边时间
     * 用来判断是否需要画视频区域(不可见的就不画)
     */
    private float getScreenRightTimeSecond() {
        return getCurrentCenterSecond() + getControlWidth() * getCenterMode(false) * itemWithUnit;
    }

    public void showStopButton(boolean visible) {
        this.showStopButton = visible;
        setCurrentTimeSecondMillis(getCurrentCenterSecondMillis());
        //  refreshCanvas();
    }

    /**
     * 获取当前时间的秒数
     */
    public float getCurrentCenterSecond() {
        return currentCenterSecond;
    }

    public long getCurrentCenterSecondMillis() {
        return (long) currentCenterSecond * 1000L;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {//布局里面设置大小，其他都是默认值
            view_height = heightSize;
        }
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    private final int AUTO_SCROLL_SECOND = 3;//自动触发滑动的秒数
    private final float SELECT_AREA_MIN_SECOND = 0.5f;//选择区域的最小值
    private int dragStatus = 0;//-1:左边拖动，0：未拖动 平移，1：右边拖动
    private float lastSelectMoveX = 0;
    private int lastSelectMoveDistanceX = 0;

    private long selectAreaEditorKeyDownTime = 0;//编辑状态-选择区域点击事件
    private long unselectAreaEditorKeyDownTime = 0;//编辑状态-未选择区域点击事件
    private long selectAreaKeyDownTime = 0;//未编辑状态-选择区域点击事件
    private final long CLICK_TIME = 200;//100ms 默认为点击

    private final int ATTACH_TIME_SECOND_MILLIS = 500;//吸附效果的时间

    private boolean isSelectAreaEditorClick() {
        long time_diff = Math.abs(selectAreaEditorKeyDownTime - System.currentTimeMillis());
        if (time_diff <= CLICK_TIME) {
            return true;
        }
        return false;
    }

    private boolean isUnSelectAreaEditorClick() {
        long time_diff = Math.abs(unselectAreaEditorKeyDownTime - System.currentTimeMillis());
        if (time_diff <= CLICK_TIME) {
            return true;
        }
        return false;
    }

    private boolean isSelectAreaClick() {
        long time_diff = Math.abs(selectAreaKeyDownTime - System.currentTimeMillis());
        if (time_diff <= CLICK_TIME) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (showStopButton) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                int width = getWidth();
                int topPadding = (view_height - STOP_BUTTON_HEIGHT) / 2;
                Rect dstRect = new Rect(width - STOP_BUTTON_HEIGHT - STOP_BUTTON_RIGHT_MARGIN,
                        topPadding, width - STOP_BUTTON_RIGHT_MARGIN, topPadding + STOP_BUTTON_HEIGHT);
                if (dstRect.contains((int) x, (int) y)) {
                    if (timeBarEvent != null) {
                        timeBarEvent.onClickStopButton();
                    }
                }
            }
        }

        if (canTouchEvent) {
            if (isSelectTimeArea) {//选择视频的时候 下面的时间刻度是禁止滑动的
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        selectAreaEditorKeyDownTime = System.currentTimeMillis();
                        float downX = event.getX();
                        float downY = event.getY();
                        this.lastSelectMoveX = downX;
                        if (leftDragRect != null) {
                            if (leftDragRect.contains(downX, downY)) {
                                dragStatus = -1;
                                VibratorUtils.longClickFeedBack(this);
                            } else {
                                if (rightDragRect != null) {
                                    if (rightDragRect.contains(downX, downY)) {
                                        dragStatus = 1;
                                        VibratorUtils.longClickFeedBack(this);
                                    } else {
                                        dragStatus = 0;
                                    }
                                } else {
                                    dragStatus = 0;
                                }
                            }
                        } else {
                            dragStatus = 0;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        selectAreaEditorKeyDownTime = 0;
//                        selectAreaKeyDownTime = System.currentTimeMillis();
                        float moveX = event.getX();
                        if (dragStatus == -1) {//左边
                            float currentInterval = (selectTimeAreaDistanceRight - selectTimeAreaDistanceLeft - 2 * selectTimeBorderPaddingLeftRightSize) * itemWithUnit;//当前时间间隔
                            float currentLeftTimeSecond = getSelectMoveTimeSecond(moveX);
                            float currentRightTimeSecond = getSelectEndTimeSecondMillis() / 1000f;
                            boolean validMove = false;
                            if (currentRightTimeSecond > currentLeftTimeSecond) {
                                if (currentInterval >= SELECT_AREA_MIN_SECOND) {
                                    validMove = true;
                                } else {
                                    if (moveX < selectTimeAreaDistanceLeft) {
                                        validMove = true;
                                    }
                                }
                            }
                            if (validMove) {
                                float timeSecondStart = getPreviewSelectStartTimeSecondMillis(moveX) / 1000f;
                                float timeSecondCenter = getCurrentCenterSecond();
                                float timeSecondDiff = Math.abs(timeSecondCenter - timeSecondStart);
                                float distanceDiff = timeSecondDiff / itemWithUnit;
                                if (timeSecondStart > timeSecondCenter) {
                                    float curDistance = lastPix - distanceDiff;
                                    if (!scrollValid(curDistance)) {
                                        validMove = false;
                                    }
                                } else {
                                    float curDistance = lastPix + distanceDiff;
                                    if (!scrollValid(curDistance)) {
                                        validMove = false;
                                    }
                                }
                            }
                            if (validMove) {
                                if (currentLeftTimeSecond >= 0) {
                                    selectTimeAreaDistanceLeft = moveX;
                                    //右边自动滑动 3s触发
                                    if ((getSelectStartTimeSecondMillis() + getItemWith() * itemWithUnit * AUTO_SCROLL_SECOND * 1000) >=
                                            (getScreenRightTimeSecond() * 1000)) {
                                        float curDistance = lastPix - getItemWith();
                                        if (scrollValid(curDistance)) {
                                            lastPix -= getItemWith();
                                            selectTimeAreaDistanceRight -= getItemWith();
                                        }
                                    }
                                    //左边自动滑动 3s触发
                                    if ((getSelectStartTimeSecondMillis()) <= (getScreenLeftTimeSecond() * 1000 +
                                            getItemWith() * itemWithUnit * AUTO_SCROLL_SECOND * 1000)) {
                                        float curDistance = lastPix + getItemWith();
                                        if (scrollValid(curDistance)) {
                                            lastPix += getItemWith();
                                            selectTimeAreaDistanceRight += getItemWith();
                                        }
                                    }
                                    if (slotSelectEvent != null) {
                                        slotSelectEvent.onEditorTimeSlotDragging(getSelectStartTimeSecondMillis(), getSelectEndTimeSecondMillis());
                                    }
                                    //时间中线移动
                                    if (timeBarEvent != null) {
                                        timeBarEvent.onDragBarMoving(true, getSelectStartTimeSecondMillis());
                                    }
                                    //
                                    showFeedBack(true);
                                    refreshCanvas();//重绘
                                } else {
                                    //todo
                                }
                            }
                        }
                        if (dragStatus == 1) {//右边
                            float currentInterval = (selectTimeAreaDistanceRight - selectTimeAreaDistanceLeft - 2 * selectTimeBorderPaddingLeftRightSize) * itemWithUnit;//当前时间间隔
                            float currentRightTimeSecond = getSelectMoveTimeSecond(moveX);
                            float currentLeftTimeSecond = getSelectStartTimeSecondMillis() / 1000f;
                            boolean validMove = false;
                            if (currentRightTimeSecond > currentLeftTimeSecond) {
                                if (currentInterval >= SELECT_AREA_MIN_SECOND) {
                                    validMove = true;
                                } else {
                                    if (moveX > selectTimeAreaDistanceRight) {
                                        validMove = true;
                                    }
                                }
                            }
                            if (validMove) {
                                float timeSecondEnd = getPreviewSelectEndTimeSecondMillis(moveX) / 1000f;
                                float timeSecondCenter = getCurrentCenterSecond();
                                float timeSecondDiff = Math.abs(timeSecondCenter - timeSecondEnd);
                                float distanceDiff = timeSecondDiff / itemWithUnit;
                                if (timeSecondEnd > timeSecondCenter) {
                                    float curDistance = lastPix - distanceDiff;
                                    if (!scrollValid(curDistance)) {
                                        validMove = false;
                                    }
                                } else {
                                    float curDistance = lastPix + distanceDiff;
                                    if (!scrollValid(curDistance)) {
                                        validMove = false;
                                    }
                                }
                            }
                            if (validMove) {
                                selectTimeAreaDistanceRight = moveX;
                                //右边自动滑动 3s触发
                                if ((getSelectEndTimeSecondMillis() + getItemWith() * itemWithUnit * AUTO_SCROLL_SECOND * 1000) >= (getScreenRightTimeSecond() * 1000)) {
                                    float curDistance = lastPix - getItemWith();
                                    if (scrollValid(curDistance)) {
                                        lastPix -= getItemWith();
                                        selectTimeAreaDistanceLeft -= getItemWith();
                                    }
                                }
                                //左边自动滑动 3s触发
                                if ((getSelectEndTimeSecondMillis()) <= (getScreenLeftTimeSecond() * 1000 + getItemWith() * itemWithUnit * AUTO_SCROLL_SECOND * 1000)) {
                                    float curDistance = lastPix + getItemWith();
                                    if (scrollValid(curDistance)) {
                                        lastPix += getItemWith();
                                        selectTimeAreaDistanceLeft += getItemWith();
                                    }
                                }
                                if (slotSelectEvent != null) {
                                    slotSelectEvent.onEditorTimeSlotDragging(getSelectStartTimeSecondMillis(), getSelectEndTimeSecondMillis());
                                }
                                //
                                if (timeBarEvent != null) {
                                    timeBarEvent.onDragBarMoving(false, getSelectEndTimeSecondMillis());
                                }
                                //
                                showFeedBack(false);
                                refreshCanvas();
                            }
                        }
                        if (dragStatus == 0) {
                            if (event.getPointerCount() == 1) {
                                int distanceX = (int) (event.getX() - lastSelectMoveX);
                                if (distanceX != 0) {
                                    float curDistance = lastPix + distanceX;
                                    if (scrollValid(curDistance)) {
                                        if (Math.abs(Math.abs(distanceX) - Math.abs(lastSelectMoveDistanceX)) < 150) {//防止快速滑动导致数据跳远过大
                                            lastPix += distanceX;
                                            selectTimeAreaDistanceLeft += distanceX;
                                            selectTimeAreaDistanceRight += distanceX;
                                            lastSelectMoveX = (int) event.getX();
                                            lastSelectMoveDistanceX = distanceX;
                                            if (timeBarEvent != null) {
                                                timeBarEvent.onDragBarMoving(false, getCurrentTimeMillis());
                                            }
                                            refreshCanvas();
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float upX = event.getX();
                        if (dragStatus == -1) {//左边
                            float currentLeftTimeSecond = getSelectMoveTimeSecond(upX);
                            float currentRightTimeSecond = getSelectEndTimeSecondMillis() / 1000f;
                            boolean validUp = false;
                            if (currentRightTimeSecond > currentLeftTimeSecond) {
                                if ((currentRightTimeSecond - currentLeftTimeSecond) >= SELECT_AREA_MIN_SECOND) {
                                    validUp = true;
                                }
                            }
                            if (validUp) {
                                float timeSecondStart = getSelectStartTimeSecondMillis() / 1000f;
                                float timeSecondCenter = getCurrentCenterSecond();
                                float timeSecondDiff = Math.abs(timeSecondCenter - timeSecondStart);
                                float distanceDiff = timeSecondDiff / itemWithUnit;
                                if (timeSecondStart > timeSecondCenter) {
                                    lastPix -= distanceDiff;
                                    selectTimeAreaDistanceLeft -= distanceDiff;
                                    selectTimeAreaDistanceRight -= distanceDiff;
                                } else {
                                    lastPix += distanceDiff;
                                    selectTimeAreaDistanceLeft += distanceDiff;
                                    selectTimeAreaDistanceRight += distanceDiff;
                                }
                                if (timeBarEvent != null) {
                                    timeBarEvent.onDragBarMoving(false, getPreviewCurrentTimeMillis(lastPix));
                                }
                                refreshCanvas();
                            }
                        }
                        if (dragStatus == 1) {//右边
                            float currentRightTimeSecond = getSelectMoveTimeSecond(upX);
                            float currentLeftTimeSecond = getSelectStartTimeSecondMillis() / 1000f;
                            boolean validUp = false;
                            if (currentRightTimeSecond > currentLeftTimeSecond) {
                                if ((currentRightTimeSecond - currentLeftTimeSecond) >= SELECT_AREA_MIN_SECOND) {
                                    validUp = true;
                                }
                            }
                            if (validUp) {
                                float timeSecondEnd = getSelectEndTimeSecondMillis() / 1000f;
                                float timeSecondCenter = getCurrentCenterSecond();
                                float timeSecondDiff = Math.abs(timeSecondCenter - timeSecondEnd);
                                float distanceDiff = timeSecondDiff / itemWithUnit;
                                if (timeSecondEnd > timeSecondCenter) {
                                    lastPix -= distanceDiff;
                                    selectTimeAreaDistanceLeft -= distanceDiff;
                                    selectTimeAreaDistanceRight -= distanceDiff;
                                } else {
                                    lastPix += distanceDiff;
                                    selectTimeAreaDistanceLeft += distanceDiff;
                                    selectTimeAreaDistanceRight += distanceDiff;
                                }
                                if (timeBarEvent != null) {
                                    timeBarEvent.onDragBarMoving(false, getPreviewCurrentTimeMillis(lastPix));
                                }
                                refreshCanvas();
                            }
                        }
                        if (dragStatus == 0) {
                            if (isSelectAreaEditorClick()) {
                                if (TextUtils.isEmpty(selectTimeSlotTitle)) {
                                    if (slotSelectEvent != null) {
                                        slotSelectEvent.onEditorTimeSlotEndTitleError(getSelectTimeSlot());
                                    }
                                } else {
                                    closeSaveSelectTimeArea();
                                    float y = event.getY();
                                    clickVideoRecordsEditor(upX, y);
                                }
                            }
                        }
                        break;
                }
            } else {
                if (canClickVideoRecordsEditor) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        unselectAreaEditorKeyDownTime = System.currentTimeMillis();
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        unselectAreaEditorKeyDownTime = 0;
//                        unselectAreaEditorKeyDownTime = System.currentTimeMillis();
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (isUnSelectAreaEditorClick()) {
                            float x = event.getX();
                            float y = event.getY();
                            clickVideoRecordsEditor(x, y);
                        }
                    }
                } else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        selectAreaKeyDownTime =System.currentTimeMillis();
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                     //   selectAreaKeyDownTime = System.currentTimeMillis();//0;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (isSelectAreaClick()) {
                            float x = event.getX();
                            float y = event.getY();
                            clickVideoRecords(x, y);
                        }
                    }
                }
                dragStatus = 0;
                mScroller.onTouchEvent(event, TAG, false);
            }
        }
        return true;
    }

    private long attachTime = 0;

    private void showFeedBack(boolean isLeft) {
        long timestamp = 0;
        if (isLeft) {
            timestamp = getSelectStartTimeSecondMillis();
        } else {
            timestamp = getSelectEndTimeSecondMillis();
        }
        long centerTimeSecondMillis = getCurrentCenterSecondMillis();
        long time_diff = Math.abs(timestamp - centerTimeSecondMillis);
        Log.i("DIFF", attachTime + "");
        if (time_diff <= ATTACH_TIME_SECOND_MILLIS && time_diff != 0) {
            if (attachTime == 0) {
                attachTime = time_diff;
                return;
            } else {
                if (attachTime > time_diff) {
                    if (isLeft) {
                        if (timestamp > centerTimeSecondMillis) {
                            selectTimeAreaDistanceLeft -= (time_diff / 1000f / itemWithUnit);
                        } else {
                            selectTimeAreaDistanceLeft += (time_diff / 1000f / itemWithUnit);
                        }
                    } else {
                        if (timestamp > centerTimeSecondMillis) {
                            selectTimeAreaDistanceRight -= (time_diff / 1000f / itemWithUnit);
                        } else {
                            selectTimeAreaDistanceRight += (time_diff / 1000f / itemWithUnit);
                        }
                    }
                    VibratorUtils.longClickFeedBack(this);
                }
                attachTime = time_diff;
            }
        }
    }

    private void clickVideoRecordsEditor(float x, float y) {
        int timeSecondMillis = (int) ((x - lastPix) * itemWithUnit * 1000);
        int clickVideoRecordPosition = -1;
        if (cacheRecordsAreaRects.size() == cacheRecordsTimeSlot.size()) {
            for (int i = 0; i < cacheRecordsAreaRects.size(); i++) {
                RectF dstRect = cacheRecordsAreaRects.get(i);
                if (dstRect.contains(x, y)) {
                    clickVideoRecordPosition = i;
                    VibratorUtils.longClickFeedBack(this);
                    break;
                }
            }
            if (clickVideoRecordPosition != -1) {
                TimeSlot clickTimeSlot = cacheRecordsTimeSlot.get(clickVideoRecordPosition);
                int realPosition = -1;
                for (int i = 0; i < timeSlotArray.size(); i++) {
                    TimeSlot timeSlot = timeSlotArray.get(i);
                    if (clickTimeSlot.getText().equals(timeSlot.getText()) &&
                            clickTimeSlot.getStartTimeSecondMillis() == timeSlot.getStartTimeSecondMillis() &&
                            clickTimeSlot.getEndTimeSecondMillis() == timeSlot.getEndTimeSecondMillis()) {
                        realPosition = i;
                        break;
                    }
                }
                if (realPosition != -1) {
                    TimeSlot timeSlot = timeSlotArray.remove(realPosition);
                    int timeSecond = (int) ((timeSlot.getEndTimeSecond() - timeSlot.getStartTimeSecond()) / 2f + timeSlot.getStartTimeSecond());
                    setCurrentTimeSecondMillisNoDelayed(timeSecond * 1000);
                    if (slotSelectEvent != null) {
                     //   slotSelectEvent.onSelectedTimeSlotEditor(timeSecondMillis, timeSlot);//选择编辑
                    }
                    openSelectTimeArea(timeSlot);
                }
            }
        }
    }

    private void clickVideoRecords(float x, float y) {
        int timeSecondMillis = (int) ((x - lastPix) * itemWithUnit * 1000);
        int clickVideoRecordPosition = -1;
        if (cacheRecordsAreaRects.size() == cacheRecordsTimeSlot.size()) {
            for (int i = 0; i < cacheRecordsAreaRects.size(); i++) {
                RectF dstRect = cacheRecordsAreaRects.get(i);
                if (dstRect.contains(x, y)) {
                    clickVideoRecordPosition = i;
                    VibratorUtils.longClickFeedBack(this);
                    break;
                }
            }
            if (clickVideoRecordPosition != -1) {
                TimeSlot clickTimeSlot = cacheRecordsTimeSlot.get(clickVideoRecordPosition);
                int realPosition = -1;
                for (int i = 0; i < timeSlotArray.size(); i++) {
                    TimeSlot timeSlot = timeSlotArray.get(i);
                    if (clickTimeSlot.getText().equals(timeSlot.getText()) &&
                            clickTimeSlot.getStartTimeSecondMillis() == timeSlot.getStartTimeSecondMillis() &&
                            clickTimeSlot.getEndTimeSecondMillis() == timeSlot.getEndTimeSecondMillis()) {
                        realPosition = i;
                        break;
                    }
                }
                if (realPosition != -1) {
                    TimeSlot timeSlot = timeSlotArray.get(realPosition);
                    int timeSecond = (int) ((timeSlot.getEndTimeSecond() - timeSlot.getStartTimeSecond()) / 2f + timeSlot.getStartTimeSecond());
                    setCurrentTimeSecondMillisNoDelayed(timeSecond * 1000);
                    if (slotSelectEvent != null) {
                        slotSelectEvent.onClickTimeSlot(timeSecond * 1000, timeSlot);
                    }
                }
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        refreshCanvas();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private ScrollingListener scrollingListener = new ScrollingListener() {
        @Override
        public void onScrollMoving(int distance) {
            float curDistance = lastPix + distance;
            if (scrollValid(curDistance)) {
                if (timeBarEvent != null) {
                    timeBarEvent.onDragBarMoving(distance > 0, getCurrentTimeMillis());
                }
                lastPix += distance;
                refreshCanvas();
            }
        }

        @Override
        public void onScrollMovingFinished() {
            if (startTimeSecondMillis <= getCurrentTimeMillis()) {
                if (scrollTimer != null) {
                    scrollTimer.cancel();
                }
                scrollTimer = new Timer();
                scrollTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (timeBarEvent != null) {
                            mHandler.sendEmptyMessage(WHAT_SCROLL_FINISHED);
                        }
                    }
                }, 500);
            } else if (startTimeSecondMillis >= getCurrentTimeMillis()) {
                setCurrentTimeSecondMillis(startTimeSecondMillis);
                if (timeBarEvent != null) {
                    timeBarEvent.onBarMoveExceedStartTime();
                }
            } else if (getCurrentTimeMillis() >= startTimeSecondMillis) {
                setCurrentTimeSecondMillis(startTimeSecondMillis);
                if (timeBarEvent != null) {
                    timeBarEvent.onBarMoveExceedEndTime();
                }
            }
        }

        @Override
        public void onScrollZoom(float mScale, double time) {
            //控制缩放比例
            if (mScale > 1) {
                if (rulerSpace < MAX_SCALE) {
                    rulerSpace += DPUtils.dip2px(1);
                } else {
                    mHandler.sendEmptyMessage(WHAT_SCALE_MAX);
                }
            } else {
                if (rulerSpace > MIN_SCALE) {
                    rulerSpace -= DPUtils.dip2px(1);
                } else {
                    mHandler.sendEmptyMessage(WHAT_SCALE_MIN);
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

    private boolean scrollValid(float distance) {
        long timestamp_now = getPreviewCurrentTimeMillis(distance);
        Log.i("SCROLL_TIME", timestamp_now + "");
        if (timestamp_now <= startTimeSecondMillis || timestamp_now >= endTimeSecondMillis) {
            return false;
        } else {
            return true;
        }
    }

    private Timer scrollTimer;

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {

    }

    private TimeBarEvent timeBarEvent;

    private void initTimeBarEvent() {
        timeBarEvent = new TimeBarEvent(getContext());
    }

    public void registerTimeBarEvent(String tag, OnTimeBarListener listener) {
        if (timeBarEvent != null) {
            timeBarEvent.registerEvent(tag, listener);
        }
    }

    //
    private TimeSlotSelectEvent slotSelectEvent;

    private void initTimeSlotSelectEvent() {
        slotSelectEvent = new TimeSlotSelectEvent(getContext());
    }

    public void registerTimeSlotSelectEvent(String tag, OnTimeBarSelectedListener listener) {
        if (slotSelectEvent != null) {
            slotSelectEvent.registerEvent(tag, listener);
        }
    }

    //
    private TimeScaleZoomScroller mScroller;

    private void initScaleZoomScroller() {
        mScroller = new TimeScaleZoomScroller(getContext());
        registerScrollEvent(TAG, scrollingListener);
    }

    public void registerScrollEvent(String tag, ScrollingListener listener) {
        if (mScroller != null) {
            mScroller.registerEvent(tag, listener);
        }
    }

    //
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (barTimer != null) {
            barTimer.unregisterEvent(TAG);
            barTimer.stopTimer();
            barTimer.destroy();
        }
    }
}
