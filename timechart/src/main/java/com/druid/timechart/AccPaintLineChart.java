package com.druid.timechart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.druid.timepaint.bean.AccChartData;
import com.druid.timepaint.bean.TimeSlot;
import com.druid.timepaint.select.TimeSlotSelectEvent;
import com.druid.timepaint.utils.AccDataQueryUtils;
import com.druid.timepaint.utils.ArraysDeepCopyUtils;
import com.druid.timepaint.utils.DateUtils;
import com.druid.timepaint.utils.FitCurveLineUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class AccPaintLineChart extends BaseLineChart {
    public AccPaintLineChart(@NonNull Context context) {
        super(context);
    }

    public AccPaintLineChart(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AccPaintLineChart(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AccPaintLineChart(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private List<AccChartData> accChartDataList = new ArrayList<>();

    public void setAccChartDataList(List<AccChartData> source) {
        accChartDataList.clear();
        accChartDataList.addAll(source);
    }

    int videoWidth = 0;
    int videoHeight = 0;

    //标注时间段集合
    List<TimeSlot> timeSlotArray = new ArrayList<>();

    TimeSlot selectTimeSlot = null;

    float yAxisMaxWidth = 0;

    void drawGridLine(Canvas canvas) {
        canvas.translate(0, getBottomPintY() / 2);//坐标平移
        setHeightAccUnit();
        float maxValue = Math.abs(yAxisValueMin) > Math.abs(yAxisValueMax) ? Math.abs(yAxisValueMin) : Math.abs(yAxisValueMax);
        String text = String.valueOf(yAxisValueMin).length() > String.valueOf(yAxisValueMax).length() ?
                String.valueOf(yAxisValueMin) : String.valueOf(yAxisValueMax);
        Rect rect = new Rect();
        yValuePaint.getTextBounds(text, 0, text.length(), rect);
        yAxisMaxWidth = yValuePaint.measureText(text);
        float startX = yAxisMaxWidth;
        float stopX = getControlWidth();
        float textHeight = rect.height();
        float textHeightHalf = textHeight / 2;
        int yAxisValueDensityCount = (int) (maxValue / yAxisValueDensity);

        for (int i = 0; i < yAxisValueDensityCount; i++) {
            int yValueMg = i * yAxisValueDensity;
            float yValueHeight = yValueMg / heightAccUnit;
            if (i == 0) {
                canvas.drawLine(startX, yValueHeight, stopX, yValueHeight, xAxisGridCenterPaint);
                //   drawYAxisValueText(canvas, yValueHeight + textHeightHalf, String.valueOf(yValueMg));
            } else {
                //向下
                canvas.drawLine(startX, yValueHeight, stopX, yValueHeight, xAxisGridPaint);
                // drawYAxisValueText(canvas, yValueHeight + textHeightHalf, String.valueOf(-yValueMg));
                //向上
                canvas.drawLine(startX, -yValueHeight, stopX, -yValueHeight, xAxisGridPaint);
                //drawYAxisValueText(canvas, -yValueHeight + textHeightHalf, String.valueOf(yValueMg));
            }
        }
    }

    void drawYAxisValueText(Canvas canvas, float pointY, String text) {
        canvas.drawText(text, 0, pointY, yValuePaint);
    }

    boolean isPortrait = true;
    static long lastConfigChangedTime;//横竖屏切换时的时间--->让横竖屏时间一致
    static long lastPortraitTime = 0;
    static long lastLandscapeTime = 0;

    void drawXAxisTimeBar(Canvas canvas) {
        if (paintXAxisTimeBar) {
            drawXAxisLine(canvas);
        }
        int viewWidth = getControlWidth();
        int itemWidth = getItemWith();
        setItemWithUnit(itemWidth);
        int count = viewWidth / itemWidth;
        if (lastPix < 0) {//<0表示往左边移动-->右滑
            count += -(lastPix / itemWidth);
        }
        int leftCount = 0;
        keyTickTextPaint.setColor(xAxisTextColor);
        //从屏幕左边开始画刻度和文本
        for (int index = leftCount; index < count; index++) {
            float rightX = index * itemWidth + lastPix;//右边方向x坐标
            if (index == 0) {//根据最左边的时刻算最中间的时刻，左边时刻(rightX*每秒多少像素)+中间时刻（view的宽度/2*每秒多少像素）
                if (rightX < 0) {//15分钟之后的移动
                    currentCenterSecond = viewWidth * itemWithUnit / 2f + Math.abs(rightX * itemWithUnit);
                } else {//15分钟之前的移动
                    currentCenterSecond = viewWidth * itemWithUnit / 2f - rightX * itemWithUnit;
                }
            }
            if (isPortrait) {
                lastPortraitTime = getCurrentTimeMillis();
            } else {
                lastLandscapeTime = getCurrentTimeMillis();
            }
            int divisor = 5;//除数、刻度精度

            if (paintXAxisTimeBar) {
                if (rightX <= viewWidth)
                    if(rulerSpace<=(DEFAULT_RULER_SPACE/2)){
                        divisor=20;
                    }
                    if (index % divisor == 0) {//大刻度
                        //上边
                        canvas.drawLine(rightX, 0 + upAndDownLineWidth / 2f + getBottomPintY(), rightX,
                                rulerHeightBig - upAndDownLineWidth / 2f + getBottomPintY(), largeRulerPaint);
                        //文本
                        drawXAxisValueText(canvas, index * 1, rightX);
                    } else {//小刻度
                        //上面
                        canvas.drawLine(rightX, 0 + upAndDownLineWidth / 2f + getBottomPintY(), rightX,
                                rulerHeightSmall - upAndDownLineWidth / 2f + getBottomPintY(), smallRulerPaint);
                    }
            }
        }
    }

    void drawXAxisLine(Canvas canvas) {
        int viewWidth = getWidth();
        canvas.drawLine(0, getBottomPintY(), viewWidth, getBottomPintY(), upAndDownLinePaint);
    }

    String keyText = "";//文本
    float keyTextX = 0;//文本的x轴坐标
    float keyTextWidth = 0;//文本的宽度
    float keyTextHeight = 0;//文本的高度

    //画时间文本
    void drawXAxisValueText(Canvas canvas, int time, float x) {
        if (time < 0) {
            time = -time;
        }
        keyText = DateUtils.getTimemmssByCurrentSecond(time);
        Rect rect = new Rect();
        keyTickTextPaint.getTextBounds(keyText, 0, keyText.length(), rect);
        keyTextWidth = keyTickTextPaint.measureText(keyText);//rect.width();//舍弃，该方法含有padding
        keyTextHeight = rect.height();
        keyTextX = x - keyTextWidth / 2f;
        float pointY = rulerHeightBig + keyTextHeight + getBottomPintY();
        canvas.drawText(keyText, keyTextX, pointY, keyTickTextPaint);
    }

    void drawPolyLine(Canvas canvas) {
        float timeSecondStart = getScreenLeftTimeSecond();
        if (timeSecondStart <= 0) {
            timeSecondStart = 0;
        }
        float timeSecondEnd = getScreenRightTimeSecond();
        if (timeSecondEnd > (endTimeSecondMillis / 1000)) {
            if (accChartDataList.size() > 0)
                timeSecondEnd = accChartDataList.get(accChartDataList.size() - 1).timeSecondMillis / 1000f;
            //endTimeSecondMillis/1000f;
        }
        drawPolyLineTime(canvas, timeSecondStart, timeSecondEnd);
    }

    void drawPolyLineTime(Canvas canvas, float timeSecondStart, float timeSecondEnd) {
        ArrayList<AccChartData> datas = AccDataQueryUtils.quickQueryAccChartTime(accChartDataList, accSamplingFrequency,
                timeSecondStart, timeSecondEnd);
        if (datas.size() > 0) {
            Log.i("CNM", "[start-->" + timeSecondStart + ",end-->" + timeSecondEnd + "]" + "-->filter" +
                    "[" + datas.get(0).timeSecondMillis + "," + datas.get(datas.size() - 1).timeSecondMillis + "]" + "-->value" +
                    "[" + datas.get(0).xAcc + "," + datas.get(datas.size() - 1).xAcc + "]");
            setLinePath(datas);
            //todo
//            Path dst = new Path();
//            PathMeasure measure = new PathMeasure(xLinePath, false);
//            measure.getSegment(0,  measure.getLength(), dst, true);
//            canvas.drawPath(dst, xAccPaint);
            canvas.drawPath(xLinePath, xAccPaint);
            canvas.drawPath(yLinePath, yAccPaint);
            canvas.drawPath(zLinePath, zAccPaint);
        }
    }

    private void setLinePath(ArrayList<AccChartData> datas) {
        ArrayList<AccChartData> fitDatas = FitCurveLineUtils.fitLineChartData(datas);
        setXLinePath(fitDatas);
        setYLinePath(fitDatas);
        setZLinePath(fitDatas);
    }

    private void setXLinePath(ArrayList<AccChartData> datas) {
        int itemWidth = getItemWith();
        setItemWithUnit(itemWidth);
        if (datas.size() <= 0) {
            return;
        }
        xLinePath.reset();
        AccChartData preAccData = datas.get(0);
        AccChartData accData0 = preAccData;
        Point preXPoint = new Point();
        preXPoint.x = (int) (accData0.timeSecondMillis / 1000 / itemWithUnit + lastPix);
        preXPoint.y = (int) (-accData0.xAcc / heightAccUnit);
        xLinePath.moveTo(preXPoint.x, preXPoint.y);

        for (int i = 1; i < datas.size(); i++) {
            AccChartData accData = datas.get(i);
            Point nextXPoint = new Point();
            nextXPoint.x = (int) (accData.timeSecondMillis / 1000 / itemWithUnit + lastPix);
            nextXPoint.y = (int) (-accData.xAcc / heightAccUnit);
            if (isBezierLine) {
                int cW = preXPoint.x;
                Point p1 = new Point();//控制点1
                p1.set(cW, preXPoint.y);
                Point p2 = new Point();//控制点2
                p2.set(cW, nextXPoint.y);
                xLinePath.cubicTo(p1.x, p1.y, p2.x, p2.y, nextXPoint.x, nextXPoint.y);//创建三阶贝塞尔曲线
            } else {
                xLinePath.lineTo(nextXPoint.x, nextXPoint.y);
            }
            preXPoint = nextXPoint;
        }
    }

    private void setYLinePath(ArrayList<AccChartData> datas) {
        int itemWidth = getItemWith();
        setItemWithUnit(itemWidth);
        if (datas.size() <= 0) {
            return;
        }
        yLinePath.reset();
        AccChartData accData0 = datas.get(0);
        Point preYPoint = new Point();
        preYPoint.x = (int) (accData0.timeSecondMillis / 1000 / itemWithUnit + lastPix);
        preYPoint.y = (int) (-accData0.yAcc / heightAccUnit);
        yLinePath.moveTo(preYPoint.x, preYPoint.y);

        for (int i = 1; i < datas.size(); i++) {
            AccChartData accData = datas.get(i);
            Point nextYPoint = new Point();
            nextYPoint.x = (int) (accData.timeSecondMillis / 1000 / itemWithUnit + lastPix);
            nextYPoint.y = (int) (-accData.yAcc / heightAccUnit);
            if (isBezierLine) {
                int cW = preYPoint.x;
                Point p1 = new Point();//控制点1
                p1.set(cW, preYPoint.y);
                Point p2 = new Point();//控制点2
                p2.set(cW, nextYPoint.y);
                yLinePath.cubicTo(p1.x, p1.y, p2.x, p2.y, nextYPoint.x, nextYPoint.y);//创建三阶贝塞尔曲线
            } else {
                yLinePath.lineTo(nextYPoint.x, nextYPoint.y);
            }
            preYPoint = nextYPoint;
        }
    }

    private void setZLinePath(ArrayList<AccChartData> datas) {
        int itemWidth = getItemWith();
        setItemWithUnit(itemWidth);
        if (datas.size() <= 0) {
            return;
        }
        zLinePath.reset();
        AccChartData accData0 = datas.get(0);
        Point preZPoint = new Point();
        preZPoint.x = (int) (accData0.timeSecondMillis / 1000 / itemWithUnit + lastPix);
        preZPoint.y = (int) (-accData0.zAcc / heightAccUnit);
        zLinePath.moveTo(preZPoint.x, preZPoint.y);

        for (int i = 1; i < datas.size(); i++) {
            AccChartData accData = datas.get(i);
            Point nextZPoint = new Point();
            nextZPoint.x = (int) (accData.timeSecondMillis / 1000 / itemWithUnit + lastPix);
            nextZPoint.y = (int) (-accData.zAcc / heightAccUnit);
            if (isBezierLine) {
                int cW = preZPoint.x;
                Point p1 = new Point();//控制点1
                p1.set(cW, preZPoint.y);
                Point p2 = new Point();//控制点2
                p2.set(cW, nextZPoint.y);
                zLinePath.cubicTo(p1.x, p1.y, p2.x, p2.y, nextZPoint.x, nextZPoint.y);//创建三阶贝塞尔曲线
            } else {
                zLinePath.lineTo(nextZPoint.x, nextZPoint.y);
            }
            preZPoint = nextZPoint;
        }
    }

    void drawYAxis(Canvas canvas) {
        setHeightAccUnit();
        float maxValue = Math.abs(yAxisValueMin) > Math.abs(yAxisValueMax) ? Math.abs(yAxisValueMin) : Math.abs(yAxisValueMax);
        String text = String.valueOf(yAxisValueMin).length() > String.valueOf(yAxisValueMax).length() ?
                String.valueOf(yAxisValueMin) : String.valueOf(yAxisValueMax);
        Rect rect = new Rect();
        yValuePaint.getTextBounds(text, 0, text.length(), rect);
        yAxisMaxWidth = yValuePaint.measureText(text);
        float startX = yAxisMaxWidth;
        float stopX = getControlWidth();
        float textHeight = rect.height();
        float textHeightHalf = textHeight / 2;
        int yAxisValueDensityCount = (int) (maxValue / yAxisValueDensity);

        RectF rectBg = new RectF(0, -getBottomPintY() / 2, yAxisMaxWidth, getBottomPintY() / 2);
        canvas.drawRect(rectBg, yAxisPaint);

        for (int i = 0; i < yAxisValueDensityCount; i++) {
            int yValueMg = i * yAxisValueDensity;
            float yValueHeight = yValueMg / heightAccUnit;
            if (i == 0) {
                drawYAxisValueText(canvas, yValueHeight + textHeightHalf, String.valueOf(yValueMg));
            } else {
                //向下
                drawYAxisValueText(canvas, yValueHeight + textHeightHalf, String.valueOf(-yValueMg));
                //向上
                drawYAxisValueText(canvas, -yValueHeight + textHeightHalf, String.valueOf(yValueMg));
            }
        }
    }

    void drawRecordArea(Canvas canvas) {
        for (int i = 0; i < timeSlotArray.size(); i++) {
            TimeSlot timeSlot = timeSlotArray.get(i);
            freshRecordArea(canvas, timeSlot, i);
        }
    }

    private void freshRecordArea(Canvas canvas, TimeSlot timeSlot, int index) {
        tagAreaPaint.setColor(timeSlot.templateColor);
        //判断是否在可见范围内
        if (timeSlot.getEndTimeSecond() > getScreenLeftTimeSecond() && timeSlot.getStartTimeSecond() < getScreenRightTimeSecond()) {
            //画可见标注区域内
            float startX = getRightXByTimeSecond(timeSlot.getStartTimeSecond()) + lastPix;
            startX = startX < 0 ? 0 : startX;//左边超出屏幕（<0）的就不画
            float endX = getRightXByTimeSecond(timeSlot.getEndTimeSecond()) + lastPix;
            endX = endX > getControlWidth() ? getControlWidth() : endX;//右边超出屏幕（>getControlWidth）的就不画
            tagAreaRect.set(startX, -getBottomPintY() / 2, endX,
                    getBottomPintY() / 2);
            canvas.drawRect(tagAreaRect, tagAreaPaint);
        }
    }

    void drawVideoPlayArea(Canvas canvas) {
        if (videoWidth != 0 && videoHeight != 0) {
            int allWidth = getWidth();
            float emptyWidthHalf = (allWidth - videoWidth) / 2f;
            int allHeight = getBottomPintY();
            float emptyHeightHalf = (allHeight - videoHeight) / 2f;
            RectF rectF = new RectF(emptyWidthHalf, emptyHeightHalf, emptyWidthHalf + videoWidth, emptyHeightHalf + videoHeight);
            canvas.drawRect(rectF, videoPlayPaint);
        }
    }

     void drawSelectTimeArea(Canvas canvas) {
        if (isSelectTimeArea) {
            if (selectTimeAreaDistanceLeft == -1) {
                selectTimeAreaDistanceLeft = (getCurrentTimeMillis() - startTimeSecondMillis) / 1000f
                        / itemWithUnit + lastPix - defaultSelectAreaTimeSecond / itemWithUnit;
            }
            if (selectTimeAreaDistanceRight == -1) {
                selectTimeAreaDistanceRight = (getCurrentTimeMillis() - startTimeSecondMillis) / 1000f
                        / itemWithUnit + lastPix + defaultSelectAreaTimeSecond / itemWithUnit;
            }
            final float startY = rulerHeightBig + keyTextHeight + tagAreaMarginTop;
            final float stopY = view_height - upAndDownLineWidth;
            //border
            selectAreaBorderPaint.setStrokeWidth(selectTimeHorizontalStrokeWidth);
            RectF rectF = new RectF(selectTimeAreaDistanceLeft, startY,
                    selectTimeAreaDistanceRight, stopY);
            canvas.drawRoundRect(rectF, selectTimeBorderCornerSize, selectTimeBorderCornerSize, selectAreaBorderPaint);
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
            selectAreaCenterRect.set(selectTimeAreaDistanceLeft ,
                    startY + selectTimeBorderPaddingTopBottomSize,
                    selectTimeAreaDistanceRight ,
                    stopY - selectTimeBorderPaddingTopBottomSize);
            canvas.drawRect(selectAreaCenterRect, selectAreaCenterPaint);
        }
    }

    /**
     * 通过时间获取开始x坐标
     */
    private float getRightXByTimeSecond(float timeSecond) {
        return timeSecond / itemWithUnit;
    }

    private float getScreenLeftTimeSecond() {
        return getCurrentCenterSecond() - getControlWidth() * getCenterMode(true) * itemWithUnit;
    }

    private float getScreenRightTimeSecond() {
        return getCurrentCenterSecond() + getControlWidth() * getCenterMode(false) * itemWithUnit;
    }

    public float getCurrentCenterSecond() {
        return currentCenterSecond;
    }

    float heightAccUnit = 0;//每1px有多少mg

    public void setHeightAccUnit() {
        float totalAccY = Math.abs(yAxisValueMin) + Math.abs(yAxisValueMax);
        heightAccUnit = totalAccY / getBottomPintY();
    }

    int getBottomPintY() {
        int viewHeight = getHeight();
        if (paintXAxisTimeBar) {
            viewHeight -= xAxisHeight;
        }
        return viewHeight;
    }
}
