package com.mosect.draglayout.example;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mosect.draglayout.entity.ListItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ItemHolder> {

    private List<ListItem> data = new ArrayList<>();

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_list, viewGroup, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        ListItem itemEntity = data.get(position);
        holder.tvTitle.setText(itemEntity.getTitle());
        holder.tvInfo.setText(itemEntity.getInfo());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addAll(Collection<ListItem> itemEntities) {
        if (null != itemEntities) {
            data.addAll(itemEntities);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void initWith(@NonNull final RecyclerView view) {
        // 列表布局方式
        LinearLayoutManager llm = new LinearLayoutManager(
                view.getContext(), LinearLayoutManager.VERTICAL, false);
        view.setLayoutManager(llm);
        // 分割线
        DividerItemDecoration divider = new DividerItemDecoration(
                view.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(new ColorDrawable(Color.parseColor("#cccccc")) {
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                    view.getResources().getDisplayMetrics());

            @Override
            public int getIntrinsicHeight() {
                return height;
            }
        });
        view.addItemDecoration(divider);
        // 设置适配器
        view.setAdapter(this);
    }
}
