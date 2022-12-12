package com.druid.timepaint.select;

import com.druid.timepaint.bean.TimeSlot;

public interface OnTimeBarSelectedListener {
    /**
     * 新增标签
     */
    void onAddTimeSlot(long startTime, long endTime);

    /**
     * 点击了某个标签
     */
    void onClickTimeSlot(int timeSecondMillis, TimeSlot timeSlot);

    /**
     * 开始编辑标签
     */
    void onEditorTimeSlotStart(TimeSlot timeSlot);

    /**
     * 编辑时拖动标签
     */
    void onEditorTimeSlotDragging(long startTime, long endTime);

    /**
     * 结束编辑标签/新增标签
     */
    void onEditorTimeSlotEnd(TimeSlot timeSlot);

    /**
     * 删除某个标注
     */
    void onDeleteSelectedTimeSlot();

    /**
     * 结束编辑标签错误（标签名空）
     */
    void onEditorTimeSlotEndTitleError(TimeSlot timeSlot);
}
