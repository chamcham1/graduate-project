package com.example.aafw.adapter;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aafw.R;
import com.example.aafw.entity.News;
import com.example.aafw.listener.OnItemClickListener;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private ArrayList<News> items;

    public NewsAdapter(OnItemClickListener listener, ArrayList<News> items) {
        this.listener = listener;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        // ViewHolder 생성
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtTitle.setText(Html.fromHtml(this.items.get(position).title)); // 뉴스 제목 (html 적용)
        holder.txtDate.setText(this.items.get(position).date);                  // 네이버에 제공된 시간
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtTitle, txtDate;

        ViewHolder(View view) {
            super(view);

            this.txtTitle = view.findViewById(R.id.txtTitle);
            this.txtDate = view.findViewById(R.id.txtDate);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // 리스트 선택
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(view, position);
            }
        }
    }
}
