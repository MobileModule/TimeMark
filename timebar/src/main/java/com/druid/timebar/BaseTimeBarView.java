package com.druid.timebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.druid.timepaint.event.OnTimeBarListener;
import com.druid.timepaint.event.ScaleMode;
import com.druid.timepaint.TimePaintView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BaseTimeBarView extends TimePaintView {

    boolean showStopButton = false;

    int scaleMode = ScaleMode.KEY_SECOND;

    public BaseTimeBarView(@NonNull Context context) {
        super(context);
        mContext = context;
        initPaint();
    }

    public BaseTimeBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initAttr(attrs);
        initPaint();
    }

    public BaseTimeBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttr(attrs);
        initPaint();
    }

    public BaseTimeBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initAttr(attrs);
        initPaint();
    }

    @Override
    public void initAttr(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VideoTimeBarView);
        int attrCount = a.getIndexCount();
        for (int index = 0; index < attrCount; index++) {
            int attr = a.getIndex(index);
            if (attr == R.styleable.VideoTimeBarView_viewHeight) {
                view_height = (int) a.getDimension(attr, VIEW_HEIGHT);
            }
            if (attr == R.styleable.VideoTimeBarView_centerLineColor) {//中轴线颜色
                centerLineColor = a.getColor(attr, centerLineColor);
            }
            if (attr == R.styleable.VideoTimeBarView_centerLineSize) {//中轴线宽度
                centerLineWidth = (int) a.getDimension(attr, centerLineWidth);
            }
            if (attr == R.styleable.VideoTimeBarView_tagOddNumAreaColor) {// 标注奇数区域颜色
                tagOddNumColor = a.getColor(attr, tagOddNumColor);
            }
            if (attr == R.styleable.VideoTimeBarView_tagEvenNumAreaColor) {// 标注偶数区域颜色
                tagEvenNumColor = a.getColor(attr, tagEvenNumColor);
            }
            if (attr == R.styleable.VideoTimeBarView_tagTextColor) {// 标注字体颜色
                tagTextColor = a.getColor(attr, tagTextColor);
            }
            if (attr == R.styleable.VideoTimeBarView_rulerTextColor) {// 刻度文本颜色
                textColor = a.getColor(attr, textColor);
            }
            if (attr == R.styleable.VideoTimeBarView_viewBackgroundColor) {// 刻度文本颜色
                viewBackgroundColor = a.getColor(attr, viewBackgroundColor);
            }
            if (attr == R.styleable.VideoTimeBarView_rulerTextSize) {// 刻度文本大小
                textSize = (int) a.getDimension(attr, textSize);
            }
            if (attr == R.styleable.VideoTimeBarView_rulerLineColor) {// 刻度线颜色
                rulerColor = a.getColor(attr, rulerColor);
            }
            if (attr == R.styleable.VideoTimeBarView_horizontalColor) {
                upAndDownLineColor = a.getColor(attr, upAndDownLineColor);
            }
            if (attr == R.styleable.VideoTimeBarView_selectTimeBorderColor) {// 选择时间的边框颜色
                selectTimeBorderColor = a.getColor(attr, selectTimeBorderColor);
            }
            if (attr == R.styleable.VideoTimeBarView_selectTimeAreaColor) {// 已选时间区域颜色
                selectTimeCenterColor = a.getColor(attr, selectTimeCenterColor);
            }
            if (attr == R.styleable.VideoTimeBarView_smallRulerLineWidth) {// 小刻度宽度
                rulerWidthSmall = (int) a.getDimension(attr, rulerWidthSmall);
            }
            if (attr == R.styleable.VideoTimeBarView_smallRulerLineHeight) {// 小刻度高度
                rulerHeightSmall = (int) a.getDimension(attr, rulerHeightSmall);
            }
            if (attr == R.styleable.VideoTimeBarView_largeRulerLineWidth) {// 大刻度宽度
                rulerWidthBig = (int) a.getDimension(attr, rulerWidthBig);
            }
            if (attr == R.styleable.VideoTimeBarView_largeRulerLineHeight) {// 大刻度高度
                rulerHeightBig = (int) a.getDimension(attr, rulerHeightBig);
            }
            if (attr == R.styleable.VideoTimeBarView_showStopButton) {
                showStopButton = a.getBoolean(attr, showStopButton);
            }
            if (attr == R.styleable.VideoTimeBarView_canTouchEvent) {
                canTouchEvent = a.getBoolean(attr, canTouchEvent);
            }
        }
        a.recycle();
    }

    @Override
    public void initPaint() {
        smallRulerPaint.setAntiAlias(true);
        smallRulerPaint.setColor(rulerColor);
        smallRulerPaint.setStrokeWidth(rulerWidthSmall);

        largeRulerPaint.setAntiAlias(true);
        largeRulerPaint.setColor(rulerColor);
        largeRulerPaint.setStrokeWidth(rulerWidthBig);

        keyTickTextPaint.setAntiAlias(true);
        keyTickTextPaint.setColor(textColor);
        keyTickTextPaint.setTextSize(textSize);

        tagTextPaint.setAntiAlias(true);
        tagTextPaint.setColor(tagTextColor);
        tagTextPaint.setTextSize(textSize);

        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setStrokeWidth(centerLineWidth);
        centerLinePaint.setColor(centerLineColor);

        tagAreaPaint.setAntiAlias(true);
        tagAreaPaint.setColor(tagOddNumColor);

        upAndDownLinePaint.setAntiAlias(true);
        upAndDownLinePaint.setColor(upAndDownLineColor);
        upAndDownLinePaint.setStrokeWidth(upAndDownLineWidth);

        selectAreaBorderPaint.setColor(selectTimeBorderColor);
        selectAreaBorderPaint.setAntiAlias(true);
//        selectAreaBorderPaint.setStrokeCap(Paint.Cap.ROUND);
//        selectAreaBorderPaint.setStyle(Paint.Style.STROKE);
        selectAreaBorderPaint.setStrokeWidth(selectTimeBorderPaddingLeftRightSize);

        selectAreaDragPaint.setColor(selectTimeDragColor);
        selectAreaDragPaint.setAntiAlias(true);
        selectAreaDragPaint.setStrokeCap(Paint.Cap.ROUND);
        selectAreaDragPaint.setStyle(Paint.Style.STROKE);
        selectAreaDragPaint.setStrokeWidth(selectTimeDragWidth);

        selectAreaCenterPaint.setColor(selectTimeCenterColor);
        selectAreaCenterPaint.setAntiAlias(true);

        dashSelectAreaPaint.setAntiAlias(true);
        dashSelectAreaPaint.setColor(dashSelectAreaColor);
        dashSelectAreaPaint.setStyle(Paint.Style.STROKE);
        dashSelectAreaPaint.setStrokeWidth(dashSelectAreaWith);
        dashSelectAreaPaint.setPathEffect(new DashPathEffect(new float[]{8,4},0));

        dragAreaPaint.setAntiAlias(true);
        dragAreaPaint.setColor(dragAreaColor);

    }

    int getControlWidth() {
        int width = getWidth();
        if (showStopButton) {
            width = width - STOP_BUTTON_HEIGHT - STOP_BUTTON_LEFT_MARGIN - STOP_BUTTON_RIGHT_MARGIN;
        }
        return width;
    }

    int getItemWith() {
        return rulerWidthSmall + rulerSpace;
    }

    /**
     * ---------------------------------------------
     * 从这里开始以下都是属性的设置
     * ---------------------------------------------
     */
    public void setRulerColor(int rulerColor) {
        this.rulerColor = rulerColor;
    }

    public void setRulerWidthSmall(int rulerWidthSmall) {
        this.rulerWidthSmall = rulerWidthSmall;
    }

    public void setRulerHeightSmall(int rulerHeightSmall) {
        this.rulerHeightSmall = rulerHeightSmall;
    }

    public void setRulerWidthBig(int rulerWidthBig) {
        this.rulerWidthBig = rulerWidthBig;
    }

    public void setRulerHeightBig(int rulerHeightBig) {
        this.rulerHeightBig = rulerHeightBig;
    }

    public void setUpAndDownLineWidth(int upAndDownLineWidth) {
        this.upAndDownLineWidth = upAndDownLineWidth;
    }

    public void setUpAndDownLineColor(int upAndDownLineColor) {
        this.upAndDownLineColor = upAndDownLineColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setCenterLineColor(int centerLineColor) {
        this.centerLineColor = centerLineColor;
    }

    public void setCenterLineWidth(int centerLineWidth) {
        this.centerLineWidth = centerLineWidth;
    }

    public void setTagEvenNumColor(int color) {
        this.tagEvenNumColor = color;
    }

    public void setTagOddNumColor(int color) {
        this.tagOddNumColor = color;
    }

    public void setSelectTimeBorderColor(int selectTimeBorderColor) {
        this.selectTimeBorderColor = selectTimeBorderColor;
    }

    public void setSelectTimeCenterColor(int selectTimeCenterColor) {
        this.selectTimeCenterColor = selectTimeCenterColor;
    }

    public void setViewBackgroundColor(int viewBackgroundColor) {
        this.viewBackgroundColor = viewBackgroundColor;
    }

    /**
     * 设置结束时间
     */
    public void setEndTimeSecondMillis(long endTimeSecondMillis) {
//        endTimeSecondMillis = endTimeSecondMillis / 1000;//去掉消暑
//        endTimeSecondMillis = endTimeSecondMillis * 1000;
        this.endTimeSecondMillis = endTimeSecondMillis;
    }

    public long getCurrentTimeMillis() {
        return startTimeSecondMillis + (long) currentCenterSecond * 1000L;
    }
}
