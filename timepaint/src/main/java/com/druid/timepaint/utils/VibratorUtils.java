package com.druid.timepaint.utils;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

public class VibratorUtils {
    public static void vibratorSelected(Context context){
        Vibrator vib = (Vibrator)context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(1);//只震动一秒，一次
    }

    public static void longClickFeedBack(View view){
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        );
    }
}
