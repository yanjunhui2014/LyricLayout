package com.milo.lyriclayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.milo.lyriclayout.ui.DemoActivity;
import com.milo.lyriclayout.ui.VideoDemoActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnDemo;
    private Button mBtnVideoDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnDemo) {
            startActivity(DemoActivity.createIntent(this));
        } else if (v == mBtnVideoDemo) {
            startActivity(VideoDemoActivity.createIntent(this));
        }
    }

    private void initView() {
        mBtnDemo = findViewById(R.id.mBtnDemo);
        mBtnDemo.setOnClickListener(this);

        mBtnVideoDemo = findViewById(R.id.mBtnVideoDemo);
        mBtnVideoDemo.setOnClickListener(this);
    }


}
