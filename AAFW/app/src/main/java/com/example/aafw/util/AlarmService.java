package com.example.aafw.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.aafw.AlarmActivity;
import com.example.aafw.IntroActivity;
import com.example.aafw.R;

public class AlarmService extends Service {

    public AlarmService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int alarmNo = intent.getIntExtra("alarm_no", 1);                // 알람번호
        String alarmName = intent.getStringExtra("alarm_name");         // 알람이름

        // 알림을 클릭하면 IntroActivity 호출
        Intent appIntent = new Intent(this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, alarmNo, appIntent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.notification_channel_id_default);

        // Oreo(26) 이전버전에 사용할 사운드
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 알림 Builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_alarm_24_white)
                .setContentTitle(alarmName)
                .setContentText("알람 시간입니다.")
                .setAutoCancel(true)
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)      // 소리 + 팝업(헤드 업)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo(26) 버전 이후 버전부터는 channel 이 필요함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            if (channel == null) {
                // 기본 채널 생성 (IMPORTANCE_HIGH : 소리 + 팝업(헤드 업))
                channel = new NotificationChannel(getString(R.string.notification_channel_id_default),
                        getString(R.string.notification_channel_name_default), NotificationManager.IMPORTANCE_HIGH);

                notificationManager.createNotificationChannel(channel);
            }

            // 알림 표시
            startForeground(alarmNo, notificationBuilder.build());
            stopForeground(false);      // false 해야지 알림 선택시 알림이 지워짐 (시간이 지나면 자동으로 알림이 삭제됨)
        } else {
            // 알림 표시
            notificationManager.notify(alarmNo, notificationBuilder.build());
        }

        // 알람창 호출
        Intent intent1 = new Intent(this, AlarmActivity.class);
        intent1.putExtra("alarm_name", alarmName);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent1);

        // 시스템에 의해 강제 종료되어도 Service 가 재시작 하지 않음
        return START_NOT_STICKY;
    }
}
