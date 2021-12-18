package com.example.aafw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.example.aafw.fragment.AlarmFragment;
import com.example.aafw.fragment.NewsFragment;
import com.example.aafw.util.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BackPressHandler backPressHandler;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 제목을 알람으로 설정
        setTitle(R.string.menu_alarm);

        // 종료 핸들러
        this.backPressHandler = new BackPressHandler(this);

        // 네비게이션 뷰 (하단에 표시되는 메뉴)
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnItemSelectedListener(mItemSelectedListener);

        // Fragment 메니저를 이용해서 layContent 레이아웃에 Fragment 넣기
        this.fragment = new AlarmFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layContent, this.fragment).commit();
    }

    @Override
    public void onBackPressed() {
        this.backPressHandler.onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    private final NavigationBarView.OnItemSelectedListener mItemSelectedListener = item -> {

        switch (item.getItemId()) {
            case R.id.menu_button_alarm:
                // 알람
                setTitle(R.string.menu_alarm);
                this.fragment = new AlarmFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.layContent, this.fragment).commit();
                return true;
            case R.id.menu_button_news:
                // 뉴스
                setTitle(R.string.menu_news);
                this.fragment = new NewsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.layContent, this.fragment).commit();
                return true;
        }

        return false;
    };

    /* Back Press Class */
    private class BackPressHandler {
        private final Context context;
        private Toast toast;

        private long backPressedTime = 0;

        public BackPressHandler(Context context) {
            this.context = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > this.backPressedTime + (Constants.LoadingDelay.LONG * 2)) {
                this.backPressedTime = System.currentTimeMillis();

                this.toast = Toast.makeText(this.context, R.string.msg_back_press_end, Toast.LENGTH_SHORT);
                this.toast.show();
                return;
            }

            if (System.currentTimeMillis() <= this.backPressedTime + (Constants.LoadingDelay.LONG * 2)) {
                // 종료
                moveTaskToBack(true);
                finish();
                this.toast.cancel();
            }
        }
    }
}