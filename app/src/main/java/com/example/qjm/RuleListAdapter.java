package com.example.qjm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class RuleListAdapter extends RecyclerView.Adapter<RuleListAdapter.ViewHolder> {
    private List<Rule> ruleList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onToggleEnabledClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public RuleListAdapter(List<Rule> ruleList) {
        this.ruleList = ruleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rule_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rule rule = ruleList.get(position);
        
        // 设置规则信息
        holder.nameTextView.setText(rule.getName());
        holder.prefixTextView.setText("前缀: " + rule.getPrefix());
        holder.suffixTextView.setText("后缀: " + rule.getSuffix());
        holder.typeTextView.setText("类型: " + rule.getInfoType());
        
        // 设置启用状态
        holder.enabledSwitch.setChecked(rule.isEnabled());
        
        // 设置按钮点击事件
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(position);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
        
        holder.enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleEnabledClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ruleList != null ? ruleList.size() : 0;
    }

    public void updateList(List<Rule> newList) {
        if (ruleList != null) {
            ruleList.clear();
            if (newList != null) {
                ruleList.addAll(newList);
            }
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView nameTextView;
        TextView prefixTextView;
        TextView suffixTextView;
        TextView typeTextView;
        Switch enabledSwitch;
        Button editButton;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.ruleCardView);
            nameTextView = itemView.findViewById(R.id.ruleNameTextView);
            prefixTextView = itemView.findViewById(R.id.rulePrefixTextView);
            suffixTextView = itemView.findViewById(R.id.ruleSuffixTextView);
            typeTextView = itemView.findViewById(R.id.ruleTypeTextView);
            enabledSwitch = itemView.findViewById(R.id.ruleEnabledSwitch);
            editButton = itemView.findViewById(R.id.editRuleButton);
            deleteButton = itemView.findViewById(R.id.deleteRuleButton);
        }
    }
}