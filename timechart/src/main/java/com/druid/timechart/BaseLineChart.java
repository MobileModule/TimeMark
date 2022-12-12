package com.druid.timechart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import com.druid.timepaint.TimePaintView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseLineChart extends TimePaintView {
    public static final String TAG = BaseLineChart.class.getName();

    public BaseLineChart(@NonNull Context context) {
        super(context);
        mContext = context;
        initPaint();
    }

    public BaseLineChart(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initAttr(attrs);
        initPaint();
    }

    public BaseLineChart(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttr(attrs);
        initPaint();
    }

    public BaseLineChart(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initAttr(attrs);
        initPaint();
    }

    boolean paintXAxisTimeBar = false;//是否绘制x轴时间线
    boolean bindTimeBarTimer = false;//是否绑定TimeBar的timer
    boolean bindTimeBarScroll=false;//是否绑定TimerBar的scroll
    boolean bindTimeBarSelectEvent=false;//是否绑定TimerBar
    boolean bindTimeBarStatusEvent=false;//是否绑定TimerBar

    @Override
    public void initAttr(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimeLineChartView);
        int attrCount = a.getIndexCount();
        for (int index = 0; index < attrCount; index++) {
            int attr = a.getIndex(index);
            if (attr == R.styleable.TimeLineChartView_viewBackgroundColor) {
                viewBackgroundColor = a.getColor(attr, viewBackgroundColor);
            }
            if (attr == R.styleable.TimeLineChartView_bindTimeBarTimer) {
                bindTimeBarTimer = a.getBoolean(attr, bindTimeBarTimer);
            }
            if (attr == R.styleable.TimeLineChartView_bindTimeBarScroll) {
                bindTimeBarScroll = a.getBoolean(attr, bindTimeBarScroll);
            }
            if (attr == R.styleable.TimeLineChartView_bindTimeBarSelectEvent) {
                bindTimeBarSelectEvent = a.getBoolean(attr, bindTimeBarSelectEvent);
            }
            if (attr == R.styleable.TimeLineChartView_bindTimeBarStatusEvent) {
                bindTimeBarStatusEvent = a.getBoolean(attr, bindTimeBarStatusEvent);
            }
            if (attr == R.styleable.TimeLineChartView_paintXAxisTimeBar) {
                paintXAxisTimeBar = a.getBoolean(attr, paintXAxisTimeBar);
            }
            if (attr == R.styleable.TimeLineChartView_xAxisLineLColor) {
                xAxisLineLColor = a.getColor(attr, xAxisLineLColor);
            }
            if (attr == R.styleable.TimeLineChartView_xAxisRulerColor) {
                xAxisRulerColor = a.getColor(attr, xAxisRulerColor);
            }
            if (attr == R.styleable.TimeLineChartView_xAxisTextColor) {
                yValueColor= xAxisTextColor = a.getColor(attr, xAxisTextColor);
            }
            if (attr == R.styleable.TimeLineChartView_yAxisTextColor) {
                yAxisTextColor = a.getColor(attr, yAxisTextColor);
            }
            if (attr == R.styleable.TimeLineChartView_xAxisGridColor) {
                xAxisGridColor = a.getColor(attr, xAxisGridColor);
            }
            if (attr == R.styleable.TimeLineChartView_xAxisCenterGridColor) {
                xAxisCenterGridColor = a.getColor(attr, xAxisCenterGridColor);
            }

        }
        a.recycle();
    }

    @Override
    public void initPaint() {
        smallRulerPaint.setAntiAlias(true);
        smallRulerPaint.setColor(xAxisRulerColor);
        smallRulerPaint.setStrokeWidth(rulerWidthSmall);

        largeRulerPaint.setAntiAlias(true);
        largeRulerPaint.setColor(xAxisRulerColor);
        largeRulerPaint.setStrokeWidth(rulerWidthBig);

        keyTickTextPaint.setAntiAlias(true);
        keyTickTextPaint.setColor(xAxisTextColor);
        keyTickTextPaint.setTextSize(textSize);

        yValuePaint.setAntiAlias(true);
        yValuePaint.setColor(yValueColor);
        yValuePaint.setTextSize(yValueTextSize);

        upAndDownLinePaint.setAntiAlias(true);
        upAndDownLinePaint.setColor(xAxisLineLColor);
        upAndDownLinePaint.setStyle(Paint.Style.STROKE);
        upAndDownLinePaint.setStrokeWidth(upAndDownLineWidth);

        yAxisPaint.setAntiAlias(true);
        yAxisPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        yAxisPaint.setColor(viewBackgroundColor);

        xAxisGridPaint.setAntiAlias(true);
        xAxisGridPaint.setColor(xAxisGridColor);
        xAxisGridPaint.setStrokeWidth(xAxisGridWidth);

        xAxisGridCenterPaint.setAntiAlias(true);
        xAxisGridCenterPaint.setColor(xAxisCenterGridColor);
        xAxisGridCenterPaint.setStrokeWidth(xAxisGridWidth);

        xAccPaint.setAntiAlias(true);
        xAccPaint.setStyle(Paint.Style.STROKE);
        xAccPaint.setColor(xAccColor);
        xAccPaint.setStrokeWidth(accLineWidth);
//        xAccPaint.setStrokeJoin(Paint.Join.ROUND);

        yAccPaint.setAntiAlias(true);
        yAccPaint.setStyle(Paint.Style.STROKE);
        yAccPaint.setColor(yAccColor);
        yAccPaint.setStrokeWidth(accLineWidth);
//        yAccPaint.setStrokeJoin(Paint.Join.ROUND);

        zAccPaint.setAntiAlias(true);
        zAccPaint.setStyle(Paint.Style.STROKE);
        zAccPaint.setColor(zAccColor);
        zAccPaint.setStrokeWidth(accLineWidth);
//        zAccPaint.setStrokeJoin(Paint.Join.ROUND);

        xLinePath = new Path();
        yLinePath = new Path();
        zLinePath = new Path();

        videoPlayPaint.setAntiAlias(true);
        videoPlayPaint.setStyle(Paint.Style.FILL);
        videoPlayPaint.setColor(Color.argb(0, 255, 255, 255));
        videoPlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    int getControlWidth() {
        int width = getWidth();
        return width;
    }

    int getControlHeight() {
        int height = getHeight();
        return height;
    }

    final static float CENTER_CURSOR_HALF = 0.5f;

    public float getCenterMode(boolean isLeft) {
        float unit = CENTER_CURSOR_HALF;
        return unit;
    }

    /**
     * 根据缩放精度获取每格表示是多少分钟
     */
    public void setItemWithUnit(int itemWidth) {
        itemWithUnit = 1f / itemWidth;
    }

    int getItemWith() {
        return rulerWidthSmall + rulerSpace;
    }

    /**
     * 设置结束时间
     */
    public void setEndTimeSecondMillis(long endTimeSecondMillis) {
        endTimeSecondMillis = endTimeSecondMillis / 1000;//去掉消暑
        endTimeSecondMillis = endTimeSecondMillis * 1000;
        this.endTimeSecondMillis = endTimeSecondMillis;
    }

    public long getCurrentTimeMillis() {
        return startTimeSecondMillis + (long) currentCenterSecond * 1000L;
    }
}
