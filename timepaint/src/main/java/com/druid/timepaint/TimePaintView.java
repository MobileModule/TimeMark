package com.druid.timepaint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.TextureView;

import com.druid.timepaint.bean.TimeSlot;
import com.druid.timepaint.utils.DPUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class TimePaintView extends TextureView {
    //上下两条线
    protected Paint upAndDownLinePaint = new Paint();//刻度画笔
    protected int upAndDownLineWidth = DPUtils.dip2px(1.5f);//上下两条线的宽度
    protected int upAndDownLineColor = 0xffffffff;

    //刻度配置
    protected Paint smallRulerPaint = new Paint();//小刻度画笔
    protected int rulerColor = 0xff979797;//刻度的颜色
    protected int rulerWidthSmall = DPUtils.dip2px(0.5f);//小刻度的宽度
    protected int rulerHeightSmall = DPUtils.dip2px(7);//小刻度的高度 10
    protected static final int DEFAULT_RULER_SPACE = DPUtils.dip2px(12);//设置item默认间隔
    protected int rulerSpace = DEFAULT_RULER_SPACE;//刻度间的间隔
    protected static final int MAX_SCALE = DPUtils.dip2px(39);//最大缩放值
    protected static final int MIN_SCALE = DPUtils.dip2px(3);//最小缩放值

    protected Paint largeRulerPaint = new Paint();//大刻度画笔
    protected int rulerWidthBig = DPUtils.dip2px(0.5f);//大刻度的宽度
    protected int rulerHeightBig = DPUtils.dip2px(15);//大刻度的高度

    //图表刻度画笔
    protected TextPaint yValuePaint = new TextPaint();
    protected int yValueColor = 0xffCCCCCC;
    protected int yValueTextSize = DPUtils.dip2px(8);

    //文本画笔
    protected TextPaint keyTickTextPaint = new TextPaint();
    protected int textColor = 0xffCCCCCC;
    protected int textSize = DPUtils.dip2px(12);

    protected TextPaint tagTextPaint = new TextPaint();
    protected int tagTextColor = 0xffCCCCCC;
    protected final float tagMarginTopSize = DPUtils.dip2px(2f);

    //中轴线画笔
    protected Paint centerLinePaint = new Paint();
    protected int centerLineColor = 0xff979797;
    protected int centerLineWidth = DPUtils.dip2px(1.5f);
    protected final int TRIANGLE_LENGTH = DPUtils.dip2px(10);

    // 视频区域画笔
    protected Paint tagAreaPaint = new Paint();
    protected int tagOddNumColor = 0x336e9fff;//视频背景颜色
    protected int tagEvenNumColor = 0x336e9fff;//视频背景颜色
    protected RectF tagAreaRect = new RectF();
    protected final float tagAreaMarginTop = DPUtils.dip2px(3.5f);

    //选择时间配置
    protected Paint selectAreaBorderPaint = new Paint();//选择时间边框
    protected int selectTimeBorderColor = 0xffffffff;//边框颜色
    protected final float selectTimeBorderCornerSize = DPUtils.dip2px(4);
    protected final float selectTimeBorderPaddingLeftRightSize = DPUtils.dip2px(15);//8
    protected final float selectTimeBorderPaddingTopBottomSize = DPUtils.dip2px(2);

    protected Paint dragAreaPaint=new Paint();
    protected int dragAreaColor=0xff808000;

    protected Paint selectAreaDragPaint = new Paint();
    protected int selectTimeDragColor = 0xffcccccc;
    protected final float selectTimeDragWidth = DPUtils.dip2px(2);

    protected Paint dashSelectAreaPaint=new Paint();
    protected final float dashSelectAreaWith=DPUtils.dip2px(0.5f);
    protected int dashSelectAreaColor=0x7dffffff;
    protected Path dashSelectAreaPath=new Path();

    protected Paint selectAreaCenterPaint = new Paint();//已选时间
    protected RectF selectAreaCenterRect = new RectF();
    protected int selectTimeCenterColor = 0xffff6b44;//已选时间颜色
    protected final float selectTimeHorizontalStrokeWidth = DPUtils.dip2px(4);
//    protected final float selectTimeVerticalStrokeWidth = DPUtils.dip2px(8);
    protected final float defaultSelectAreaTimeSecond = 3f;//默认选择的时间
    protected float selectBottomMargin= DPUtils.dip2px(3);

    //暂停按钮
    protected final int STOP_BUTTON_HEIGHT = DPUtils.dip2px(36);//控件大小
    protected final int STOP_BUTTON_LEFT_MARGIN = DPUtils.dip2px(12f);
    protected final int STOP_BUTTON_RIGHT_MARGIN = DPUtils.dip2px(8f);

    protected int VIEW_HEIGHT = DPUtils.dip2px(166);
    protected int view_height = VIEW_HEIGHT;//view的高度

    //折线轴
    protected int chartTagThemeColor = 0x7569BECB;//视频背景颜色
    protected int chartTagBlueColor = 0x75315AFB;//视频背景颜色

    protected final int xAxisHeight = DPUtils.dip2px(35);
    protected int xAxisLineLColor = 0xffffffff;
    protected int xAxisRulerColor = 0xff979797;
    protected int xAxisTextColor = 0xffcccccc;

    protected Paint yAxisPaint=new Paint();
    protected int yAxisTextColor = 0x7affffff;
    protected float yAxisValueMax = 4000;//3000
    protected float yAxisValueMin = -4000;
    protected int yAxisValueDensity = 500;//y轴颗粒度

    protected Paint xAxisGridPaint = new Paint();
    protected int xAxisGridWidth = DPUtils.dip2px(0.5f);
    protected int xAxisGridColor = 0x26000000;
    protected Paint xAxisGridCenterPaint = new Paint();
    protected int xAxisCenterGridColor = 0xffffffff;

    protected int accLineWidth = DPUtils.dip2px(1.0f);
    protected Paint xAccPaint = new Paint();
    protected int xAccColor = 0xff315AFB;
    protected Paint yAccPaint = new Paint();
    protected int yAccColor = 0xff5FEAB3;
    protected Paint zAccPaint = new Paint();
    protected int zAccColor = 0xffFFED44;
    protected Path xLinePath;//x曲线路径
    protected Path yLinePath;//y曲线路径
    protected Path zLinePath;//z曲线路径
    protected boolean isBezierLine = false;//是否支持贝塞尔曲线
    protected int accSamplingFrequency = 25;//加速度默认采样频率

    protected Paint videoPlayPaint=new Paint();//视频绘制区域

    protected int viewBackgroundColor = Color.WHITE;

    protected boolean canTouchEvent=true;//是否触摸

    //上下文对象
    protected Context mContext;

    /**
     * 移动偏移量
     */
    protected float lastPix = 0;//移动/滑动的距离，绘制的时候需要根据这个值来确定位置

    protected float itemWithUnit = 0;//s/px

    //是否自动移动时间轴
    protected  boolean isMoving = false;

    //是否显示选择区域
    protected  boolean isSelectTimeArea = false;

    //当前日期的开始时间毫秒值
    protected long startTimeSecondMillis = 0;

    //结束时间秒级别
    protected long endTimeSecondMillis = 0;

    //当前时间秒数（中轴线时间）
    protected float currentCenterSecond = 0;

    //标注时间段集合
    protected List<TimeSlot> timeSlotArray = new ArrayList<>();

    //选择区域
    protected float selectTimeAreaDistanceLeft = -1;//往左边选择的距离
    protected float selectTimeAreaDistanceRight = -1;//往右边选择的距离

    public TimePaintView(@NonNull Context context) {
        super(context);
    }

    public TimePaintView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePaintView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePaintView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract void initAttr(AttributeSet attrs);

    public abstract void initPaint();
}
