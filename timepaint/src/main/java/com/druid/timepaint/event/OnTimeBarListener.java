package com.druid.timepaint.event;

//暴露接口
public interface OnTimeBarListener {
    /**
     * 点击停止按钮
     */
    void onClickStopButton();

    /**
     * 停止移动
     */
    void onBarMoveStop();

    /**
     * 当拖动的时候回调
     */
    void onDragBarMoving(boolean isLeftDrag, long currentTime);

    /**
     * 当时间轴自动移动的时候回调
     */
    void onBarAutoMoving(long currentTime);

    /**
     * 当拖动完成时回调
     */
    void onBarMoveFinished(long currentTime);

    /**
     * 移动超过开始时间
     */
    void onBarMoveExceedStartTime();

    /**
     * 移动超过结束时间
     */
    void onBarMoveExceedEndTime();

    /**
     * 超过最大缩放值
     */
    void onScrollExceedMaxScale();

    /**
     * 超过最小缩放值
     */
    void onScrollExceedMinScale();
}
