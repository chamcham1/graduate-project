package com.example.aafw.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aafw.AlarmAddActivity;
import com.example.aafw.AlarmEditActivity;
import com.example.aafw.R;
import com.example.aafw.adapter.AlarmAdapter;
import com.example.aafw.entity.Alarm;
import com.example.aafw.listener.OnItemClickListener;
import com.example.aafw.util.AlarmReceiver;
import com.example.aafw.util.Constants;
import com.example.aafw.util.DBHelper;

import java.util.ArrayList;

import static android.content.Context.ALARM_SERVICE;

public class AlarmFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlarmAdapter adapter;

    private ArrayList<Alarm> items;

    private LinearLayout layNoData;         // 데이터 없을때 표시할 레이아웃

    private int selectedPosition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        // 리사이클러뷰
        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        this.layNoData = view.findViewById(R.id.layNoData);

        view.findViewById(R.id.fabAdd).setOnClickListener(view1 -> {
            // 추가
            Intent intent = new Intent(getContext(), AlarmAddActivity.class);
            this.alarmActivityLauncher.launch(intent);
        });

        view.post(() -> {
            // 알람 리스트 보기
            listAlarm();
        });

        return view;
    }

    /* 알람 리스트 보기 */
    private void listAlarm() {
        // 내역 얻기
        this.items = getItems();

        this.adapter = new AlarmAdapter(mItemClickListener, this.items);
        this.recyclerView.setAdapter(this.adapter);

        if (this.items.size() == 0) {
            // 내역이 없으면
            this.layNoData.setVisibility(View.VISIBLE);
        }
    }

    /* 알람 리스트 얻기 */
    private ArrayList<Alarm> getItems() {
        ArrayList<Alarm> alarms = new ArrayList<>();

        SQLiteDatabase db = DBHelper.getInstance(getContext()).getReadableDatabase();

        // select 쿼리문 (최근순으로 정렬)
        String sql = "SELECT alarmNo, alarmDateTime, alarmName FROM " +
                Constants.DataBaseTableName.ALARM + " ORDER BY alarmDateTime DESC";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            // 알람 데이터
            Alarm alarm = new Alarm(cursor.getInt(0), cursor.getLong(1), cursor.getString(2));
            alarms.add(alarm);
        }
        cursor.close();

        db.close();
        return alarms;
    }

    /* 알람 삭제 */
    private void delete(int position) {
        SQLiteDatabase db = DBHelper.getInstance(getContext()).getReadableDatabase();

        try {
            // 알람 삭제
            String sql = "DELETE FROM " + Constants.DataBaseTableName.ALARM + " WHERE alarmNo = ?";
            Object[] args = { this.items.get(position).alarmNo };
            db.execSQL(sql, args);

            int alarmNo = this.items.get(position).alarmNo;
            if (this.items.get(position).dateTime > System.currentTimeMillis()) {
                // 알람취소
                cancelAlarm(alarmNo);
            }

            // 리스트에서 삭제
            this.adapter.remove(position);

            if (this.items.size() == 0) {
                this.layNoData.setVisibility(View.VISIBLE);
            }
        } catch (SQLException ignored) {
            // 오류
            Toast.makeText(getContext(), R.string.msg_error, Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    /* 알람 취소 */
    private void cancelAlarm(int alarmNo) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), alarmNo, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    private final OnItemClickListener mItemClickListener = (view, position) -> {
        // 선택 및 삭제
        this.selectedPosition = position;

        if (view.getId() == R.id.imgDelete) {
            // 삭제
            new AlertDialog.Builder(getContext())
                    .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                        // 삭제
                        delete(position);
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .setCancelable(false)
                    .setTitle(R.string.dialog_title_delete)
                    .setMessage(R.string.dialog_msg_delete)
                    .show();
        } else {
            // 선택 (알람)
            if (this.items.get(position).dateTime > System.currentTimeMillis()) {
                // 알람이 아직 지나지 않았으면
                Intent intent = new Intent(getContext(), AlarmEditActivity.class);
                intent.putExtra("alarm", this.items.get(position));
                this.alarmActivityLauncher.launch(intent);
            }
        }
    };

    /* 알람 추가 ActivityForResult */
    private final ActivityResultLauncher<Intent> alarmActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // 알람 추가 및 편집후 리스트에 적용
                    Intent data = result.getData();
                    if (data != null) {
                        Alarm alarm = data.getParcelableExtra("alarm");
                        if (data.getIntExtra("type", 0) == 1) {
                            // 등록
                            this.layNoData.setVisibility(View.GONE);

                            // 최 상단에 추가
                            this.adapter.add(alarm, 0);
                            this.recyclerView.scrollToPosition(0);
                        } else if (data.getIntExtra("type", 0) == 2) {
                            // 수정
                            this.items.set(this.selectedPosition, alarm);
                            this.adapter.notifyItemChanged(this.selectedPosition);
                        }
                    }
                }
            });
}
