package com.madt.calculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter for displaying calculation history items.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    private final List<HistoryItem> historyList;
    private final OnItemClickListener listener;

    public HistoryAdapter(List<HistoryItem> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.tvExpression.setText(item.getExpression());
        holder.tvResult.setText(item.getResult());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvExpression;
        final TextView tvResult;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpression = itemView.findViewById(R.id.tvHistoryExpression);
            tvResult = itemView.findViewById(R.id.tvHistoryResult);
        }
    }
}
