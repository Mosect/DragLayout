package com.mosect.draglayout.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_drag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpActivity(DragLayoutActivity.class);
            }
        });
        findViewById(R.id.btn_flow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpActivity(FlowLayoutActivity.class);
            }
        });
        findViewById(R.id.btn_flow2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FlowLayoutActivity.class);
                intent.putExtra("type", 2);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_drefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpActivity(DragRefreshLayoutActivity.class);
            }
        });
        findViewById(R.id.btn_vrefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpActivity(VerticalRefreshLayoutActivity.class);
            }
        });
        findViewById(R.id.btn_hdli).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpActivity(HorizontalDragListItemActivity.class);
            }
        });
    }

    private void jumpActivity(Class<? extends Activity> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }
}
