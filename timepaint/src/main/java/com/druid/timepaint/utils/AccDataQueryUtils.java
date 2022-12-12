package com.druid.timepaint.utils;

import com.druid.timepaint.bean.AccChartData;

import java.util.ArrayList;
import java.util.List;

public class AccDataQueryUtils {

    //快速查找点 按照25HZ的采样频率实现
    //二分法查找
    //遍历
    public static ArrayList<AccChartData> quickQueryAccChartTime(List<AccChartData> accChartDataList,
                                                                 int accSamplingFrequency,
                                                                 float timeSecondStart, float timeSecondEnd) {
        ArrayList<AccChartData> queryAccData = new ArrayList<>();
        int timeSecondMillisStart = (int) (timeSecondStart * 1000);
        int timeSecondMillisEnd = (int) (timeSecondEnd * 1000);

        int indexRealStart = -1;
        int indexRealEnd = -1;
        int indexFuzzyStart = (int) (timeSecondStart * (accSamplingFrequency - 1));//模糊定位
        int indexFuzzyEnd = (int) (timeSecondEnd * (accSamplingFrequency - 1));//模糊定位
        if (false) {
            if (accChartDataList.size() > indexFuzzyStart) {
                AccChartData fuzzyAccData = accChartDataList.get(indexFuzzyStart);
                //寻找到大位置
                if (fuzzyAccData.timeSecondMillis > timeSecondMillisStart) {
                    for (int i = indexFuzzyStart - 1; i <= 0; i--) {
                        if (accChartDataList.get(i).timeSecondMillis <= timeSecondMillisStart) {
                            indexRealStart = i;
                            break;
                        }
                    }
                }
                //寻找到小位置
                if (fuzzyAccData.timeSecondMillis < timeSecondMillisStart) {
                    for (int i = indexFuzzyStart + 1; i < accChartDataList.size(); i++) {
                        if (accChartDataList.get(i).timeSecondMillis >= timeSecondMillisStart) {
                            indexRealStart = i;
                            break;
                        }
                    }
                }
                //寻找到相同位置
                if (fuzzyAccData.timeSecondMillis == timeSecondMillisStart) {

                }
            }

            if (accChartDataList.size() > indexFuzzyEnd) {
                AccChartData fuzzyAccData = accChartDataList.get(indexFuzzyEnd);
                //寻找到大位置
                if (fuzzyAccData.timeSecondMillis > timeSecondMillisEnd) {
                    for (int i = indexFuzzyEnd - 1; i <= 0; i--) {
                        if (accChartDataList.get(i).timeSecondMillis <= timeSecondMillisEnd) {
                            indexRealEnd = i;
                            break;
                        }
                    }
                }
                //寻找到小位置
                if (fuzzyAccData.timeSecondMillis < timeSecondMillisEnd) {
                    for (int i = indexFuzzyEnd + 1; i < accChartDataList.size(); i++) {
                        if (accChartDataList.get(i).timeSecondMillis >= timeSecondMillisEnd) {
                            indexRealEnd = i;
                            break;
                        }
                    }
                }

                //寻找到相同位置
                if (fuzzyAccData.timeSecondMillis == timeSecondMillisEnd) {

                }
            }
        }

        if (indexRealStart == -1 || indexRealEnd == -1) {
            AccChartData preAccData = null;
            for (int i = 0; i < accChartDataList.size(); i++) {
                AccChartData accData = accChartDataList.get(i);
                if (i == 0) {
                    preAccData = accData;
                }
                if (indexRealStart == -1) {
                    if (preAccData.timeSecondMillis <= timeSecondMillisStart &&
                            timeSecondMillisStart <= accData.timeSecondMillis) {
                        indexRealStart = i;
                    }
                }
                if (indexRealEnd == -1) {
                    if (preAccData.timeSecondMillis <= timeSecondMillisEnd &&
                            timeSecondMillisEnd <= accData.timeSecondMillis) {
                        indexRealEnd = i;
                    }
                }
            }
        }
        if (indexRealStart != -1 && indexRealEnd != -1) {
            queryAccData.addAll(accChartDataList.subList(indexRealStart, indexRealEnd));
        }
        return queryAccData;
    }

    //二分法查找数据
    public static int binarySearchAccChartTime(List<AccChartData> arr, int left, int right, int findVal) {
        int mid = (left + right) / 2;
        //先进行判断，如果要找的元素findVal小于数组的最小值或者大于数组的最大值，则不能找到findVal，返回-1
        //如果左边下标大于右边下标也不能找到该元素findVal,返回-1
        if (findVal < arr.get(left).timeSecondMillis || findVal > arr.get(right).timeSecondMillis
                || left > right) {
            return -1;
        }
        while (left < right) {
            mid = (left + right) / 2;
            //如果，要找的值findVal 大于arr[mid],在数组的右边，左下标定义为left=mid+1
            if (findVal > arr.get(mid).timeSecondMillis) {
                left = mid + 1;
            } else if (findVal < arr.get(mid).timeSecondMillis) {
                //如果要找的元素findVal小于arr[mid]，说明在数组的左边，右下边冲洗定义为right=mid-1
                right = mid - 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    private static int compare(int a, int b) {
        return a > b ? a : b;
    }

    //accAxis 0:x,1:y,2:z
    public static int searchMaxValue(List<AccChartData> arr, int accAxis) {
        int maxValue = 0;
        for (int i = 0; i < arr.size(); i++) {
            AccChartData acc = arr.get(i);
            if (i == 0) {
                if (accAxis == 0) {
                    maxValue = acc.xAcc;
                }
                if (accAxis == 1) {
                    maxValue = acc.yAcc;
                }
                if (accAxis == 2) {
                    maxValue = acc.zAcc;
                }
            } else {
                if (accAxis == 0) {
                    if (acc.xAcc > maxValue) {
                        maxValue = acc.xAcc;
                    }
                }
                if (accAxis == 1) {
                    if (acc.yAcc > maxValue) {
                        maxValue = acc.yAcc;
                    }
                }
                if (accAxis == 2) {
                    if (acc.zAcc > maxValue) {
                        maxValue = acc.zAcc;
                    }
                }
            }
        }
        return maxValue;
    }

    //accAxis 0:x,1:y,2:z
    public static int searchMinValue(List<AccChartData> arr, int accAxis) {
        int minValue = 0;
        for (int i = 0; i < arr.size(); i++) {
            AccChartData acc = arr.get(i);
            if (i == 0) {
                if (accAxis == 0) {
                    minValue = acc.xAcc;
                }
                if (accAxis == 1) {
                    minValue = acc.yAcc;
                }
                if (accAxis == 2) {
                    minValue = acc.zAcc;
                }
            } else {
                if (accAxis == 0) {
                    if (acc.xAcc < minValue) {
                        minValue = acc.xAcc;
                    }
                }
                if (accAxis == 1) {
                    if (acc.yAcc < minValue) {
                        minValue = acc.yAcc;
                    }
                }
                if (accAxis == 2) {
                    if (acc.zAcc < minValue) {
                        minValue = acc.zAcc;
                    }
                }
            }
        }
        return minValue;
    }
}
