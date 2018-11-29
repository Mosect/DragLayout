package com.mosect.draglayout.example;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ItemHolder extends RecyclerView.ViewHolder {

    public TextView tvTitle;
    public TextView tvInfo;

    public ItemHolder(@NonNull View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tv_title);
        tvInfo = itemView.findViewById(R.id.tv_info);
    }
}
