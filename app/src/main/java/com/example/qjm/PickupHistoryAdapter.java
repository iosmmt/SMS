package com.example.qjm;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class PickupHistoryAdapter extends RecyclerView.Adapter<PickupHistoryAdapter.ViewHolder> {
    private List<PickupInfo> historyList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onCopyCodeClick(int position);
        void onCallCourierClick(int position);
    }

    public PickupHistoryAdapter(List<PickupInfo> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pickup_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PickupInfo pickupInfo = historyList.get(position);
        
        holder.codeTextView.setText("取件码: " + pickupInfo.getCode());
        holder.companyTextView.setText("快递公司: " + pickupInfo.getCompany());
        holder.addressTextView.setText("地址: " + pickupInfo.getAddress());
        holder.stationTextView.setText("驿站: " + pickupInfo.getStation());
        holder.timeTextView.setText("时间: " + pickupInfo.getFormattedTime());
        holder.phoneTextView.setText("快递员电话: " + (pickupInfo.getCourierPhone() != null ? pickupInfo.getCourierPhone() : "未知"));
        
        // 根据状态设置卡片边框颜色和背景色
        Context context = holder.itemView.getContext();
        if (pickupInfo.getStatus() == PickupInfo.STATUS_UNCOLLECTED) {
            // 未取件使用绿色边框和浅绿色背景
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.colorPending));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorLightPending));
            holder.codeTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPending));
        } else {
            // 已取件使用灰色边框和浅灰色背景
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.colorCollected));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorLightCollected));
            holder.codeTextView.setTextColor(ContextCompat.getColor(context, R.color.colorCollected));
        }

        holder.copyCodeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCopyCodeClick(position);
            }
        });

        holder.callCourierButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallCourierClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView codeTextView;
        TextView companyTextView;
        TextView addressTextView;
        TextView stationTextView;
        TextView timeTextView;
        TextView phoneTextView;
        Button copyCodeButton;
        Button callCourierButton;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            codeTextView = itemView.findViewById(R.id.codeTextView);
            companyTextView = itemView.findViewById(R.id.companyTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            stationTextView = itemView.findViewById(R.id.stationTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            copyCodeButton = itemView.findViewById(R.id.copyCodeButton);
            callCourierButton = itemView.findViewById(R.id.callCourierButton);
        }
    }
}