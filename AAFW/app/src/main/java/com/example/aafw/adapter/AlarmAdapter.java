package com.example.aafw.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aafw.R;
import com.example.aafw.entity.Alarm;
import com.example.aafw.listener.OnItemClickListener;
import com.example.aafw.util.Utils;

import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private ArrayList<Alarm> items;

    public AlarmAdapter(OnItemClickListener listener, ArrayList<Alarm> items) {
        this.listener = listener;
        this.items = items;
    }

    /* 추가 */
    public void add(Alarm data, int position) {
        position = position == -1 ? getItemCount()  : position;
        // 알람 추가
        this.items.add(position, data);
        // 추가된 알람을 리스트에 적용하기 위함
        notifyItemInserted(position);
    }

    /* 삭제 */
    public Alarm remove(int position){
        Alarm data = null;

        if (position < getItemCount()) {
            data = this.items.get(position);
            // 알람 삭제
            this.items.remove(position);
            // 삭제된 알람을 리스트에 적용하기 위함
            notifyItemRemoved(position);
        }

        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        // ViewHolder 생성
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtDateTime.setText(Utils.getDate("yyyy-MM-dd E HH:mm", this.items.get(position).dateTime));    // 알람일시
        holder.txtName.setText(this.items.get(position).name);          // 알람이름

        if (this.items.get(position).dateTime > System.currentTimeMillis()) {
            holder.layBody.setBackgroundResource(R.drawable.list_item_selector);
            //holder.imgDelete.setVisibility(View.VISIBLE);
        } else {
            holder.layBody.setBackgroundResource(R.color.disable_background_color);
            //holder.imgDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout layBody;
        TextView txtDateTime, txtName;
        ImageView imgDelete;

        ViewHolder(View view) {
            super(view);

            this.layBody = view.findViewById(R.id.layBody);
            this.txtDateTime = view.findViewById(R.id.txtDateTime);
            this.txtName = view.findViewById(R.id.txtName);
            this.imgDelete = view.findViewById(R.id.imgDelete);

            this.imgDelete.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // 리스트 선택 및 삭제
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(view, position);
            }
        }
    }
}
