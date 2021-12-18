package com.example.aafw;

import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    private SoundPool soundPool;        // 사운드
    private int soundId;                // 사운드 로드 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alarm);

        // 알람 정보
        Intent intent = getIntent();
        String alarmName = intent.getStringExtra("alarm_name");     // 알람이름

        ((TextView) findViewById(R.id.txtAlarmName)).setText(alarmName);

        findViewById(R.id.imgClose).setOnClickListener(view -> {
            // 닫기
            finish();
        });

        // 사운드
        this.soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .build();

        this.soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            // 소리 무한반복(-1)
            soundPool.play(soundId, 0.5f, 0.5f, 1, -1, 1.0f);
        });

        // 사운드 로드
        this.soundId = this.soundPool.load(this, R.raw.alarm, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.soundPool != null) {
            this.soundPool.release();
            this.soundPool = null;
        }
    }
}
