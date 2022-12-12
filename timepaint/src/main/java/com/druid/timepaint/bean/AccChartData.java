package com.druid.timepaint.bean;

import java.io.Serializable;

public class AccChartData implements Serializable {
    public int xAcc = 0;
    public int yAcc = 0;
    public int zAcc = 0;
    public int timeSecondMillis = 0;
    public String text="";

    public AccChartData(int x, int y, int z, int timeSecondMillis,String text) {
        this.xAcc = x;
        this.yAcc = y;
        this.zAcc = z;
        this.timeSecondMillis = timeSecondMillis;
        this.text=text;
    }
}
