package com.mosect.draglayout.example;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mosect.draglayout.lib.DragLayout;

public class ListAdapter2 extends ListAdapter {

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_list2, viewGroup, false);
        ItemHolder holder = new ItemHolder(view);
        DragLayout dragLayout = holder.itemView.findViewById(R.id.ly_drag);
        final View right = holder.itemView.findViewById(R.id.ly_right);
        final Button btnRight = holder.itemView.findViewById(R.id.btn_right);
        final Button btnLeft = holder.itemView.findViewById(R.id.btn_left);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(btnLeft.getContext(), "点击了左边按钮", Toast.LENGTH_SHORT).show();
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(btnRight.getContext(), "点击了右边按钮", Toast.LENGTH_SHORT).show();
            }
        });

        // 将right和btnRight的宽度设置成一样
        dragLayout.postAfterLayout(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams lp = right.getLayoutParams();
                lp.width = btnRight.getWidth();
                right.setLayoutParams(lp);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        DragLayout dragLayout = holder.itemView.findViewById(R.id.ly_drag);
        dragLayout.closeEdge(false); // 关闭周边
    }
}
