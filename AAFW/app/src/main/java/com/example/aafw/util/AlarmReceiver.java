package com.example.aafw.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("alarm_no", intent.getIntExtra("alarm_no", 1));
        serviceIntent.putExtra("alarm_name", intent.getStringExtra("alarm_name"));

        // Oreo 버전 이후부터는 Background 에서 실행을 금지하기 때문에 Foreground 에서 실행해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}