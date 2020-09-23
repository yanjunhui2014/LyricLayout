package com.milo.lyriclayout.ui;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2020/7/20
 */
public class DemoActivity extends AppCompatActivity {

    public static Intent createIntent(Context context){
        return new Intent(context, DemoActivity.class);
    }

}
