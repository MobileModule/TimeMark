package com.druid.timepaint.utils;

import com.druid.timepaint.bean.AccChartData;

import java.util.ArrayList;
import java.util.List;

//25hz的采样频率需要做一个拟合曲线 寻找1s内的最大点
//否则1s内的点全部压缩到一个点上
public class FitCurveLineUtils {
    public static final int SECOND=1*1000;

    public static ArrayList<AccChartData> fitLineChartData(ArrayList<AccChartData> source){
        ArrayList<AccChartData> fitSource=new ArrayList<>();
        AccChartData preAcc=null;
        int preIndex=0;
        for(int i=0;i<source.size();i++){
            AccChartData acc=source.get(i);
            if(i==0){
                preAcc=acc;
            }else {
                if(Math.abs(acc.timeSecondMillis-preAcc.timeSecondMillis)>SECOND){
                    List<AccChartData> sortAcc=source.subList(preIndex,i+1);
                    int timeHalf=Math.abs(sortAcc.get(0).timeSecondMillis-
                            sortAcc.get(sortAcc.size()-1).timeSecondMillis)/2;
                    //0.5s 拟合最小值 存在问题【不知道先显示最小值还是最大值】
                    int xMin=AccDataQueryUtils.searchMinValue(sortAcc,0);
                    int yMin=AccDataQueryUtils.searchMinValue(sortAcc,1);
                    int zMin=AccDataQueryUtils.searchMinValue(sortAcc,2);
                    AccChartData minAcc=new AccChartData(xMin,yMin,zMin,
                            preAcc.timeSecondMillis+timeHalf,"");
                    //1s 拟合最大值
                    int xMax=AccDataQueryUtils.searchMaxValue(sortAcc,0);
                    int yMax=AccDataQueryUtils.searchMaxValue(sortAcc,1);
                    int zMax=AccDataQueryUtils.searchMaxValue(sortAcc,2);
                    AccChartData maxAcc=new AccChartData(xMax,yMax,zMax,preAcc.timeSecondMillis,"");
                    fitSource.add(maxAcc);
                    //
                    preAcc=acc;
                    preIndex=i;
                }else {
                    if(i==source.size()-1){
                        List<AccChartData> sortAcc=source.subList(preIndex,i);
                        int xMax=AccDataQueryUtils.searchMaxValue(sortAcc,0);
                        int yMax=AccDataQueryUtils.searchMaxValue(sortAcc,1);
                        int zMax=AccDataQueryUtils.searchMaxValue(sortAcc,2);
                        AccChartData maxAcc=new AccChartData(xMax,yMax,zMax,preAcc.timeSecondMillis,"");
                        fitSource.add(maxAcc);
                        preAcc=acc;
                        preIndex=i;
                    }
                }
            }
        }
        return fitSource;
    }
}
