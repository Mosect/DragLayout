package com.mosect.draglayout.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mosect.draglayout.entity.ListItem;

import java.util.ArrayList;
import java.util.List;

public class HorizontalDragListItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hdli);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView rvContent = findViewById(R.id.rv_content);
        ListAdapter2 adapter2 = new ListAdapter2();
        adapter2.initWith(rvContent);

        List<ListItem> items = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            ListItem item = new ListItem();
            item.setTitle(getString(R.string.title) + (i + 1));
            item.setInfo(getString(R.string.info));
            items.add(item);
        }
        adapter2.addAll(items);
    }
}
