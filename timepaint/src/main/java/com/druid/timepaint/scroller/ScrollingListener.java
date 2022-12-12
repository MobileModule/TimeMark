package com.druid.timepaint.scroller;

//内部实现方法
public interface ScrollingListener {
    /**
     * 滑动时
     */
    void onScrollMoving(int distance);

    /**
     * 滑动结束
     */
    void onScrollMovingFinished();

    /**
     * 缩放时
     */
    void onScrollZoom(float mScale, double time);

    /**
     * 缩放结束
     */
    void onScrollZoomFinished();
}
