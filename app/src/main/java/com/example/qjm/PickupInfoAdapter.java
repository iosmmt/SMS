package com.example.qjm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class PickupInfoAdapter extends RecyclerView.Adapter<PickupInfoAdapter.ViewHolder> {
    private static final String TAG = "PickupInfoAdapter";
    private List<PickupInfo> pickupInfoList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onMarkCollectedClick(int position);
        void onDeleteClick(int position);
        void onItemClick(int position); // 添加项点击事件
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PickupInfoAdapter(List<PickupInfo> pickupInfoList) {
        this.pickupInfoList = pickupInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pickup_info_item, parent, false);
            return new ViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "创建ViewHolder时发生异常", e);
            // 返回一个默认的ViewHolder以防止崩溃
            View view = new View(parent.getContext());
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (position < 0 || position >= pickupInfoList.size()) {
                return;
            }
            
            PickupInfo pickupInfo = pickupInfoList.get(position);
            if (pickupInfo == null) {
                return;
            }
            
            // 安全设置文本内容
            if (holder.codeTextView != null) {
                holder.codeTextView.setText("取件码: " + (pickupInfo.getCode() != null ? pickupInfo.getCode() : ""));
            }
            
            if (holder.companyTextView != null) {
                holder.companyTextView.setText("快递公司: " + (pickupInfo.getCompany() != null ? pickupInfo.getCompany() : "未知"));
            }
            
            if (holder.addressTextView != null) {
                holder.addressTextView.setText("地址: " + (pickupInfo.getAddress() != null ? pickupInfo.getAddress() : "未知"));
            }
            
            if (holder.stationTextView != null) {
                holder.stationTextView.setText("驿站: " + (pickupInfo.getStation() != null ? pickupInfo.getStation() : "未知"));
            }
            
            if (holder.timeTextView != null) {
                holder.timeTextView.setText("时间: " + (pickupInfo.getFormattedTime() != null ? pickupInfo.getFormattedTime() : ""));
            }
            
            // 根据状态设置卡片边框颜色和背景色
            Context context = holder.itemView.getContext();
            if (pickupInfo.getStatus() == PickupInfo.STATUS_UNCOLLECTED) {
                // 未取件使用绿色边框和浅绿色背景
                if (holder.cardView != null) {
                    holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.colorPending));
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorLightPending));
                }
                if (holder.codeTextView != null) {
                    holder.codeTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPending));
                }
            } else {
                // 已取件使用灰色边框和浅灰色背景
                if (holder.cardView != null) {
                    holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.colorCollected));
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorLightCollected));
                }
                if (holder.codeTextView != null) {
                    holder.codeTextView.setTextColor(ContextCompat.getColor(context, R.color.colorCollected));
                }
            }
            
            // 根据状态设置按钮文本
            if (holder.markCollectedButton != null) {
                if (pickupInfo.getStatus() == PickupInfo.STATUS_UNCOLLECTED) {
                    holder.markCollectedButton.setText("标记已取件");
                } else {
                    holder.markCollectedButton.setText("标记未取件");
                }
            }

            // 设置按钮点击事件
            if (holder.markCollectedButton != null) {
                holder.markCollectedButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMarkCollectedClick(position);
                    }
                });
            }

            if (holder.deleteButton != null) {
                holder.deleteButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(position);
                    }
                });
            }
            
            // 一键复制取件码
            if (holder.copyCodeButton != null) {
                holder.copyCodeButton.setOnClickListener(v -> {
                    copyCodeToClipboard(holder.itemView.getContext(), pickupInfo.getCode());
                });
            }
            
            // 设置整个卡片的点击事件
            if (holder.cardView != null) {
                holder.cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "绑定ViewHolder时发生异常", e);
        }
    }

    @Override
    public int getItemCount() {
        try {
            return pickupInfoList != null ? pickupInfoList.size() : 0;
        } catch (Exception e) {
            Log.e(TAG, "获取item数量时发生异常", e);
            return 0;
        }
    }

    public void updateList(List<PickupInfo> newList) {
        try {
            if (pickupInfoList != null) {
                pickupInfoList.clear();
                if (newList != null) {
                    pickupInfoList.addAll(newList);
                }
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "更新列表时发生异常", e);
        }
    }

    // 获取指定位置的取件信息
    public PickupInfo getItem(int position) {
        try {
            if (pickupInfoList != null && position >= 0 && position < pickupInfoList.size()) {
                return pickupInfoList.get(position);
            }
        } catch (Exception e) {
            Log.e(TAG, "获取指定位置的取件信息时发生异常", e);
        }
        return null;
    }

    // 复制取件码到剪贴板
    private void copyCodeToClipboard(Context context, String code) {
        try {
            if (context == null || code == null) {
                return;
            }
            
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("取件码", code);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "取件码已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "复制取件码到剪贴板时发生异常", e);
            if (context != null) {
                Toast.makeText(context, "复制失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView codeTextView;
        TextView companyTextView;
        TextView addressTextView;
        TextView stationTextView;
        TextView timeTextView;
        Button markCollectedButton;
        Button deleteButton;
        Button copyCodeButton;

        ViewHolder(View itemView) {
            super(itemView);
            try {
                cardView = itemView.findViewById(R.id.cardView);
                codeTextView = itemView.findViewById(R.id.codeTextView);
                companyTextView = itemView.findViewById(R.id.companyTextView);
                addressTextView = itemView.findViewById(R.id.addressTextView);
                stationTextView = itemView.findViewById(R.id.stationTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                markCollectedButton = itemView.findViewById(R.id.markCollectedButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
                copyCodeButton = itemView.findViewById(R.id.copyCodeButton);
            } catch (Exception e) {
                Log.e("PickupInfoAdapter", "初始化ViewHolder时发生异常", e);
            }
        }
    }
}