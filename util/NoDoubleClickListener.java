package com.fala.profession.professionnet.listener;

import android.view.View;

import com.fala.profession.professionnet.utils.LogManager;

import java.util.Calendar;

/**
 * Created by SoMustYY on 2018/1/31.
 * 防止手滑多次点击
 */

public abstract class NoDoubleClickListener implements View.OnClickListener {
    private static final String TAG = "NoDoubleClickListener";
    private static long lastClickTime = 0;
    private static final int MIN_CLICK_DELAY_TIME = 500;
    public abstract void onNoDoubleClick(View view);

    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {   //如果点击事件间隔相差“MIN_CLICK_DELAY_TIME”毫秒
            lastClickTime = currentTime;
            onNoDoubleClick(v);
        }else{
            LogManager.e(TAG,"防止手滑多次点击");
        }
    }
}
