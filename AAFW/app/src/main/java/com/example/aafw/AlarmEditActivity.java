package com.example.aafw;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aafw.entity.Alarm;
import com.example.aafw.util.AlarmReceiver;
import com.example.aafw.util.Constants;
import com.example.aafw.util.DBHelper;
import com.example.aafw.util.SharedPreferencesUtils;
import com.example.aafw.util.Utils;

import java.util.Calendar;
import java.util.Objects;

public class AlarmEditActivity extends AppCompatActivity {

    private Calendar calendar;                  // 알람일시

    private TimePicker timePicker;
    private TextView txtDate;
    private EditText editAlarmName;

    private InputMethodManager imm;             // 키보드를 숨기기 위해 필요함

    private Alarm alarm;                        // 알람정보

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_form);

        // 알람정보
        Intent intent = getIntent();
        this.alarm = intent.getParcelableExtra("alarm");

        // 제목 표시
        setTitle(getString(R.string.title_alarm_edit));

        // 홈버튼(<-) 표시
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.timePicker = findViewById(R.id.timePicker);
        this.txtDate = findViewById(R.id.txtDate);
        this.editAlarmName = findViewById(R.id.editAlarmName);

        this.txtDate.setOnClickListener(mClickListener);
        findViewById(R.id.imgCalendar).setOnClickListener(mClickListener);
        findViewById(R.id.btnSave).setOnClickListener(mClickListener);

        this.calendar = Calendar.getInstance();
        this.txtDate.setText(Utils.getDate("yyyy-MM-dd E", this.calendar.getTimeInMillis()));    // 현재일

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 알람정보 표시
        infoAlarm();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 알람정보 표시 */
    private void infoAlarm() {
        this.calendar.setTimeInMillis(this.alarm.dateTime);

        // 시간표시
        this.timePicker.setHour(this.calendar.get(Calendar.HOUR));
        this.timePicker.setMinute(this.calendar.get(Calendar.MINUTE));

        this.txtDate.setText(Utils.getDate("yyyy-MM-dd E", this.calendar.getTimeInMillis()));

        this.editAlarmName.setText(this.alarm.name);        // 알람이름
    }

    /* DatePickerDialog 호출 */
    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this, (datePicker, year, monthOfYear, dayOfMonth) -> {
            this.calendar.set(Calendar.YEAR, year);
            this.calendar.set(Calendar.MONTH, monthOfYear);
            this.calendar.set(Calendar.DATE, dayOfMonth);

            this.txtDate.setText(Utils.getDate("yyyy-MM-dd E", this.calendar.getTimeInMillis()));   // 설정일
        }, this.calendar.get(Calendar.YEAR), this.calendar.get(Calendar.MONTH), this.calendar.get(Calendar.DATE));
        dialog.show();
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        Calendar calNow = Calendar.getInstance();

        // 알람 시간 설정
        this.calendar.set(Calendar.HOUR_OF_DAY, this.timePicker.getHour());
        this.calendar.set(Calendar.MINUTE, this.timePicker.getMinute());
        this.calendar.set(Calendar.SECOND, 0);

        // 현재일시보다 이전이면
        if (this.calendar.before(calNow)) {
            Toast.makeText(this, R.string.msg_alarm_time_check_wrong, Toast.LENGTH_SHORT).show();
            return false;
        }

        // 알람이름 입력 체크
        String alarmName = this.editAlarmName.getText().toString();
        if (TextUtils.isEmpty(alarmName)) {
            Toast.makeText(this, R.string.msg_alarm_name_check_empty, Toast.LENGTH_SHORT).show();
            this.editAlarmName.requestFocus();
            return false;
        }

        // 키보드 숨기기
        this.imm.hideSoftInputFromWindow(this.editAlarmName.getWindowToken(), 0);

        return true;
    }

    /* 저장 (변경된 내용 저장) */
    private void save() {
        SQLiteDatabase db = DBHelper.getInstance(this).getReadableDatabase();

        this.alarm.dateTime = this.calendar.getTimeInMillis();
        this.alarm.name = this.editAlarmName.getText().toString();

        try {
            // 알람 내역 수정
            String sql = "UPDATE " + Constants.DataBaseTableName.ALARM + " SET alarmDateTime=?, alarmName=? WHERE alarmNo=?";
            Object[] args = { this.alarm.dateTime, this.alarm.name, this.alarm.alarmNo };
            db.execSQL(sql, args);

            // 먼저 기존 알람 취소
            cancelAlarm(this.alarm.alarmNo);

            // 알람 설정
            setAlarm(this.alarm);

            // Fragment 에 전달 후 닫기
            Intent intent = new Intent();
            intent.putExtra("type", 2);
            intent.putExtra("alarm", this.alarm);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } catch (SQLException ignored) {
            // 오류
            Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    /* 알람 설정 */
    private void setAlarm(Alarm alarm) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("alarm_no", alarm.alarmNo);        // 알람번호
        alarmIntent.putExtra("alarm_name", alarm.name);         // 알람이름

        // alarmNo : requestCode
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, alarm.alarmNo, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.dateTime, alarmPendingIntent);
    }

    /* 알람 취소 */
    private void cancelAlarm(int alarmNo) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmNo, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.txtDate:
                case R.id.imgCalendar:
                    // DatePickerDialog 호출
                    showDatePicker();
                    break;
                case R.id.btnSave:
                    // 저장 (변경된 내용 저장)
                    // 입력 체크
                    if (checkData()) {
                        // 저장 (변경된 내용 저장)
                        save();
                    }
                    break;
            }
        }
    };
}
