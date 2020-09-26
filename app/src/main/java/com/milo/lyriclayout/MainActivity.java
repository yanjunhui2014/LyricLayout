package com.milo.lyriclayout;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.milo.liblyric.LibLyricLog;
import com.milo.liblyric.MultiLyricData;
import com.milo.liblyric.WeakHandler;
import com.milo.liblyric.widget.LyricLayout;
import com.milo.liblyric.widget.MultiLyricLayout;
import com.milo.lyriclayout.util.LyricDataFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private static final int MSG_UPDATE_MULTI_SEEK  = 1;
    private static final int MSG_UPDATE_SINGLE_SEEK = 2;

    private SeekBar mSeekBar;
    private Button  mBtnStart;
    private Button  mBtnStop;
    private Button  mBtnReset;

    private LyricLayout      mLyricLayout;
    private MultiLyricLayout mMultiLyricLayout;
    private RadioButton      mRaBtnSingle;
    private RadioButton      mRaBtnMulti;

    private static final int UPDATE_INTERVAL = 16;
    private static final int DURATION_SINGLE = 3000;

    private int                 mSingleSeek          = 0;
    private int                 mMultiSeek           = 0;
    private int                 mMaxMultiSeek        = 0;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private WeakHandler mWeakHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_MULTI_SEEK:
                    mMultiSeek += UPDATE_INTERVAL;
                    LibLyricLog.d(TAG, "mMultiSeek == " + mMultiSeek);

                    if (mMultiSeek > mMaxMultiSeek) {
                        mMultiSeek = mMaxMultiSeek;
                        mWeakHandler.removeMessages(MSG_UPDATE_MULTI_SEEK);
                    } else {
                        mWeakHandler.sendEmptyMessageDelayed(MSG_UPDATE_MULTI_SEEK, UPDATE_INTERVAL);
                    }

                    mMultiLyricLayout.updateLyricByTime(mMultiSeek);
                    break;
                case MSG_UPDATE_SINGLE_SEEK:
                    mSingleSeek += UPDATE_INTERVAL;
                    LibLyricLog.d(TAG, "mSingleSeek == " + mMultiSeek);

                    if (mSingleSeek > DURATION_SINGLE) {
                        mSingleSeek = DURATION_SINGLE;
                        mWeakHandler.removeMessages(MSG_UPDATE_SINGLE_SEEK);
                    } else {
                        mWeakHandler.sendEmptyMessageDelayed(MSG_UPDATE_SINGLE_SEEK, UPDATE_INTERVAL);
                    }

                    mLyricLayout.updateTime(mSingleSeek);
                    break;
            }
            return false;
        }
    }) {
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLyricLayout.getLayoutParams();
        mLyricLayout.setContent("风到这里就是粘粘住过客的思念", DURATION_SINGLE, getScreenWidth(this) - params.leftMargin - params.rightMargin);

        LinearLayout.LayoutParams multiPs = (LinearLayout.LayoutParams) mMultiLyricLayout.getLayoutParams();
        mMultiLyricLayout.setLyricData(getLyricDataList(), getScreenWidth(this) - multiPs.leftMargin - multiPs.rightMargin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
        mCompositeDisposable = new CompositeDisposable();

        mWeakHandler.removeCallbacksAndMessages(null);
        mWeakHandler = null;
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnStart) {
            if (mRaBtnSingle.isChecked()) {
                if (mSingleSeek == DURATION_SINGLE) {
                    mSingleSeek = 0;
                    mLyricLayout.reset();
                }
                mWeakHandler.sendEmptyMessage(MSG_UPDATE_SINGLE_SEEK);
            } else if (mRaBtnMulti.isChecked()) {
                if (mMultiSeek == mMaxMultiSeek) {
                    mMultiSeek = 0;
                    mMultiLyricLayout.reset();
                }
                mWeakHandler.sendEmptyMessage(MSG_UPDATE_MULTI_SEEK);
            }
        } else if (v == mBtnStop) {
            mWeakHandler.removeMessages(MSG_UPDATE_MULTI_SEEK);
            mWeakHandler.removeMessages(MSG_UPDATE_SINGLE_SEEK);
        } else if (v == mBtnReset) {
            mMultiSeek = 0;
            mLyricLayout.reset();
            mMultiLyricLayout.reset();
        }
    }

    private void initView() {
        mSeekBar = findViewById(R.id.mSeekBar);
        mBtnStart = findViewById(R.id.mBtnStart);
        mBtnStart.setOnClickListener(this);

        mBtnStop = findViewById(R.id.mBtnStop);
        mBtnStop.setOnClickListener(this);

        mBtnReset = findViewById(R.id.mBtnReset);
        mBtnReset.setOnClickListener(this);

        mLyricLayout = findViewById(R.id.mLyricLayout);
        mMultiLyricLayout = findViewById(R.id.mMultiLyricLayout);

        mRaBtnSingle = findViewById(R.id.mRaBtnSingle);
        mRaBtnMulti = findViewById(R.id.mRaBtnMulti);
//        mMultiLyricLayout.setTextColor(R.color.c1, R.color.c2);

        mSeekBar.setMax(10000);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mRaBtnSingle.isChecked()) {
                    mSingleSeek = (int) (progress * 1.0f / 10000 * 1.0f * DURATION_SINGLE);
                    mLyricLayout.updateTime(mSingleSeek);
                }

                if (mRaBtnMulti.isChecked()) {
                    mMultiSeek = (int) (progress * 1.0f / 10000 * 1.0f * mMaxMultiSeek);
                    mMultiLyricLayout.updateLyricByTime(mMultiSeek, true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    private List<MultiLyricData> getLyricDataList() {
        MultiLyricData data0 = LyricDataFactory.createLyricData("风到这里就是粘", 100);
        MultiLyricData data1 = LyricDataFactory.createLyricData("粘住过客的思念", data0.endTime + 1000);
        MultiLyricData data2 = LyricDataFactory.createLyricData("雨到了这里缠成线", data1.endTime + 1000);
        MultiLyricData data3 = LyricDataFactory.createLyricData("缠着我们留恋人世间", data2.endTime + 1000);
        MultiLyricData data4 = LyricDataFactory.createLyricData("你在身边就是缘", data3.endTime + 1000);
        MultiLyricData data5 = LyricDataFactory.createLyricData("缘分写在三圣石上面", data4.endTime + 1000);
        MultiLyricData data6 = LyricDataFactory.createLyricData("爱有万分之一甜", data5.endTime + 1000);
        MultiLyricData data7 = LyricDataFactory.createLyricData("宁愿我就葬在这一点", data6.endTime + 1000);
        mMaxMultiSeek = data7.endTime;

        List<MultiLyricData> list = new ArrayList<>();
        list.add(data0);
        list.add(data1);
        list.add(data2);
        list.add(data3);
        list.add(data4);
        list.add(data5);
        list.add(data6);
        list.add(data7);
        return list;
    }

}
