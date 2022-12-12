package com.druid.timebar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.druid.timepaint.event.ScaleMode;
import com.druid.timepaint.utils.DPUtils;
import com.druid.timepaint.utils.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class TimeBarPaintView extends BaseTimeBarView {
    public TimeBarPaintView(@NonNull Context context) {
        super(context);
    }

    public TimeBarPaintView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeBarPaintView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimeBarPaintView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 根据缩放精度获取每格表示是多少分钟
     */
    void setItemWithUnit(int itemWidth) {
        if (scaleMode == ScaleMode.KEY_SECOND) {
            itemWithUnit = 1f / itemWidth;
        }
        if (scaleMode == ScaleMode.KEY_MINUTE) {
            itemWithUnit = 60f / itemWidth;
        }
        if (scaleMode == ScaleMode.KEY_HOUR) {
            itemWithUnit = 10 * 60f / itemWidth;
        }
    }

    //绘制暂停按钮
    void drawStopButton(Canvas canvas) {
        if (showStopButton) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mark_tag_end);
            Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            int width = getWidth();
            int revWidth = (bitmap.getWidth() / bitmap.getHeight()) * STOP_BUTTON_HEIGHT;
            int topPadding = (view_height - STOP_BUTTON_HEIGHT) / 2;
            Rect dstRect = new Rect(width - revWidth - STOP_BUTTON_RIGHT_MARGIN,
                    topPadding, width - STOP_BUTTON_RIGHT_MARGIN, topPadding + STOP_BUTTON_HEIGHT);
            canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
        }
    }

    //画上下两条线
    void drawUpAndDownLine(Canvas canvas) {
        int viewWidth = getWidth();
        canvas.drawLine(0, upAndDownLineWidth / 2, viewWidth, rulerWidthSmall / 2, upAndDownLinePaint);
        canvas.drawLine(0, view_height, viewWidth, view_height, upAndDownLinePaint);
    }

    boolean isPortrait = true;
    static long lastConfigChangedTime;//横竖屏切换时的时间--->让横竖屏时间一致
    static long lastPortraitTime = 0;
    static long lastLandscapeTime = 0;

    //画文本和刻度
    void drawTextAndRuler(Canvas canvas) {
        int viewWidth = getControlWidth();
        int itemWidth = getItemWith();
        setItemWithUnit(itemWidth);
        int count = viewWidth / itemWidth;
        if (lastPix < 0) {//<0表示往左边移动-->右滑
            count += -(lastPix / itemWidth);
        }
        int leftCount = 0;
        if (scaleMode == ScaleMode.KEY_SECOND) {

        }
        if (scaleMode == ScaleMode.KEY_MINUTE) {
            if (getCurrentTimeMillis() < startTimeSecondMillis + 15 * 60 * 1000) {
                leftCount = -60;
            }
        }
        if (scaleMode == ScaleMode.KEY_HOUR) {
            if (getCurrentTimeMillis() < startTimeSecondMillis + 6 * 60 * 60 * 1000) {
                leftCount = -60;
            }
        }
        //从屏幕左边开始画刻度和文本
        for (int index = leftCount; index < count; index++) {
            float rightX = index * itemWidth + lastPix;//右边方向x坐标
            if (index == 0) {//根据最左边的时刻算最中间的时刻，左边时刻(rightX*每秒多少像素)+中间时刻（view的宽度/2*每秒多少像素）
                if (rightX < 0) {
                    currentCenterSecond = viewWidth * itemWithUnit * getCenterMode(true) +
                            Math.abs(rightX * itemWithUnit);
                } else {
                    currentCenterSecond = viewWidth * itemWithUnit  * getCenterMode(true) -
                            rightX * itemWithUnit;
                }
            }
            if (isPortrait) {
                lastPortraitTime = getCurrentTimeMillis();
            } else {
                lastLandscapeTime = getCurrentTimeMillis();
            }
            int divisor;//除数、刻度精度
            switch (scaleMode) {
                case ScaleMode.KEY_HOUR:
                case ScaleMode.KEY_MINUTE:
                    divisor = 10;
                    break;
                case ScaleMode.KEY_SECOND:
                default:
                    divisor = 5;//10
            }
            if (rightX <= viewWidth)
                if(rulerSpace<=(DEFAULT_RULER_SPACE/2)){
                    divisor=20;
                }
                if (index % divisor == 0) {//大刻度
                    //上边
                    canvas.drawLine(rightX, 0 + upAndDownLineWidth / 2f, rightX, rulerHeightBig - upAndDownLineWidth / 2f, largeRulerPaint);
                    //下边
                    //canvas.drawLine(rightX, view_height - textSize * 1.2f, rightX, view_height - rulerHeightBig - textSize * 1.2f, largeRulerPaint);
                    //文本
                    if (scaleMode == ScaleMode.KEY_SECOND) {
                        draText(canvas, index * 1, rightX);
                    } else {
                        draText(canvas, index * 60, rightX);
                    }
                } else {//小刻度
                    //上面
                    canvas.drawLine(rightX, 0 + upAndDownLineWidth / 2f, rightX, rulerHeightSmall - upAndDownLineWidth / 2f, smallRulerPaint);
                    //下面
                    // canvas.drawLine(rightX, view_height - textSize * 1.2f, rightX, view_height - rulerHeightSmall - textSize * 1.2f, smallRulerPaint);
                }
        }
    }

    String keyText = "";//文本
    float keyTextX = 0;//文本的x轴坐标
    float keyTextWidth = 0;//文本的宽度
    float keyTextHeight = 0;//文本的高度

    //画时间文本
    void draText(Canvas canvas, int time, float x) {
        if (scaleMode == ScaleMode.KEY_SECOND) {
            if (time < 0) {
                time=-time;
            }
            keyText = DateUtils.getTimemmssByCurrentSecond(time);
        } else {
            if (time < 0) {
                keyText = DateUtils.getTimeHHmmssByCurrentSecond(24 * 60 * 60 + time);
            } else {
                keyText = DateUtils.getTimeHHmmssByCurrentSecond(time);
            }
        }
        Rect rect = new Rect();
        keyTickTextPaint.getTextBounds(keyText, 0, keyText.length(), rect);
        keyTextWidth = keyTickTextPaint.measureText(keyText);//rect.width();//舍弃，该方法含有padding
        keyTextHeight = rect.height();
        keyTextX = x - keyTextWidth / 2f;
        float bottomY = view_height - DPUtils.dip2px(3);
        float pointY = rulerHeightBig + keyTextHeight;
        canvas.drawText(keyText, keyTextX, pointY, keyTickTextPaint);
    }

    final static float CENTER_CURSOR_HALF = 0.5f;
    final static float CENTER_CURSOR_SHOW_BUTTON = 4 / 5f;

    float getCenterMode(boolean isLeft) {
        float unit = CENTER_CURSOR_HALF;
        if (showStopButton) {
            if (isLeft) {
                unit = CENTER_CURSOR_SHOW_BUTTON;
            } else {
                unit = CENTER_CURSOR_SHOW_BUTTON;
            }
        }
        return unit;
    }

    //画中间线
    void drawCenterCursor(Canvas canvas) {
        float diffY = 0.3f;
        float pointX = getControlWidth() * CENTER_CURSOR_HALF;
        float startY = upAndDownLineWidth - diffY;
        float endY = view_height - upAndDownLineWidth + diffY;
        if (showStopButton) {
            pointX = getControlWidth() * CENTER_CURSOR_SHOW_BUTTON;
            //triangle
            Path path = new Path();
            path.moveTo(pointX - TRIANGLE_LENGTH / 2f, startY);
            path.lineTo(pointX + TRIANGLE_LENGTH / 2f, startY);
            float triangle_height = (float) Math.sqrt(TRIANGLE_LENGTH * TRIANGLE_LENGTH - (TRIANGLE_LENGTH / 2) * (TRIANGLE_LENGTH / 2));
            path.lineTo(pointX, startY + triangle_height);
            path.lineTo(pointX - TRIANGLE_LENGTH / 2f, startY);
            canvas.drawPath(path, centerLinePaint);
        }
        //line
        canvas.drawLine(pointX, startY, pointX, endY, centerLinePaint);
    }
}
