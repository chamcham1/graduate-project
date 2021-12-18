package com.example.aafw;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aafw.util.Constants;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);

        // 인트로 화면을 1초동안 보여주고 메인으로 이동
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 메인으로 이동
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }, Constants.LoadingDelay.LONG);
    }

    @Override
    public void onBackPressed() {
        // 백키 눌려도 종료 안되게 하기 위함
        //super.onBackPressed();
    }
}
