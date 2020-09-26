package com.milo.liblyric.widget;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.milo.liblyric.LibLyricLog;
import com.milo.liblyric.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Title：歌词Layout
 * Describe：支持多行文字渐变
 * Remark：使用时必须指定组件宽度，至少也得是match_parent
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2020/6/23
 */
public class LyricLayout extends LinearLayout {
    private static final String TAG = "LyricLayout";

    @ColorRes
    public int mTextColor = R.color.lib_lyric_white;//默认颜色
    @ColorRes
    public int mTextOverColor = R.color.lib_lyric_c1;//歌词覆盖颜色

    public int    mTotalTime;
    public int    mCurTime;
    public String mTotalText;

    private TextPaint mTextPaint;

    private GradientRectTextView[] mGradientTextViews;

    public LyricLayout(Context context) {
        this(context, null);
    }

    public LyricLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LyricLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param text
     * @param totalTime
     * @param measureWidth - 预计的宽度
     */
    public void setContent(final String text, final int totalTime, final int measureWidth) {
        this.mTotalTime = totalTime;
        this.mTotalText = text;

        removeAllViews();
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> emitter) throws Exception {
                if (getMeasuredWidth() != 0) {
                    emitter.onNext(getMeasuredHeight());
                } else if (measureWidth > 0) {
                    emitter.onNext(measureWidth);
                } else {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            emitter.onNext(getMeasuredWidth());
                        }
                    });
                }
            }
        }).flatMap(new Function<Integer, ObservableSource<ItemData[]>>() {
            @Override
            public ObservableSource<ItemData[]> apply(final Integer width) throws Exception {
                return Observable.unsafeCreate(new ObservableSource<ItemData[]>() {
                    @Override
                    public void subscribe(Observer<? super ItemData[]> observer) {
                        float paintWidth = mTextPaint.measureText(text);

                        //宽度未指定，只能显示一行，否则会直接奔溃
                        if (paintWidth < width || width == 0) {
                            ItemData[] itemData = new ItemData[1];
                            itemData[0] = new ItemData(text, 0, mTotalTime);
                            observer.onNext(itemData);
                        } else {
                            int line = (int) (Math.ceil(paintWidth / width));
                            ItemData[] itemData = new ItemData[line];

                            String transText = text;

                            for (int i = 0; i < line; i++) {
                                String lineText = getLineText(transText, width);
                                transText = transText.replaceFirst(lineText, "");

                                int startTime = (i == 0) ? 0 : itemData[i - 1].endTime;
                                int endTime = (i == line - 1) ? totalTime : measureEndTime(lineText);

                                itemData[i] = new ItemData(lineText, startTime, endTime);
                            }
                            observer.onNext(itemData);
                        }
                    }
                });
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ItemData[]>() {
                    @Override
                    public void accept(ItemData[] datas) throws Exception {
                        mGradientTextViews = new GradientRectTextView[datas.length];
                        for (int i = 0; i < datas.length; i++) {
                            ItemData itemData = datas[i];
                            mGradientTextViews[i] = new GradientRectTextView(getContext());
                            mGradientTextViews[i].setText(itemData.text);
//                            mGradientTextViews[i].setColor(mTextColor, mTextOverColor);
                            mGradientTextViews[i].setTextColor(ContextCompat.getColor(getContext(), R.color.lib_lyric_white));
                            mGradientTextViews[i].setStartAndEndCount(itemData.startTime, itemData.endTime);

                            if (i == datas.length - 1) {
                                LinearLayout.LayoutParams ps = new LinearLayout.LayoutParams((int) mTextPaint.measureText(itemData.text), LayoutParams.WRAP_CONTENT);
                                addView(mGradientTextViews[i], ps);
                            } else {
                                addView(mGradientTextViews[i]);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LibLyricLog.d(TAG, throwable.getMessage());
                        Toast.makeText(getContext(), "LyricLayout setContent error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setTextColor(@ColorRes int normalColor, @ColorRes int checkedColor) {
        this.mTextColor = normalColor;
        this.mTextOverColor = checkedColor;

        if(mGradientTextViews != null){
            for(GradientRectTextView rectTextView : mGradientTextViews){
                rectTextView.setColor(normalColor, checkedColor);
            }
        }
    }

    /**
     * 时间更新
     *
     * @param curTime
     */
    public void updateTime(int curTime) {
        updateTime(curTime, false);
    }

    /**
     * @param curTime
     * @param updateAllChild - 是否同时更新所有子view，适用于时间跳跃更新
     */
    public void updateTime(int curTime, boolean updateAllChild) {
        if (mGradientTextViews == null) {
            return;
        }
        this.mCurTime = curTime;

        if (mGradientTextViews.length == 1) {
            mGradientTextViews[0].updateCount(curTime);
        } else {
            for (int i = 0; i < mGradientTextViews.length; i++) {
                if (updateAllChild) {
                    mGradientTextViews[i].updateCount(curTime - mGradientTextViews[i].mStartCount);
                } else if (curTime >= mGradientTextViews[i].mStartCount && curTime <= mGradientTextViews[i].mEndCount) {
                    mGradientTextViews[i].updateCount(curTime - mGradientTextViews[i].mStartCount);
                    break;
                }
            }
        }
    }

    public void reset() {
        if (mGradientTextViews == null) {
            return;
        } else {
            for (int i = 0; i < mGradientTextViews.length; i++) {
                mGradientTextViews[i].resetCount();
            }
        }
    }

    private void init() {
        mTextColor = getResources().getColor(R.color.lib_lyric_white);
        mTextOverColor = getResources().getColor(R.color.lib_lyric_c1);
        setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mTextPaint = textView.getPaint();
    }

    private String getLineText(String totalText, int designWidth) {
        for (int i = totalText.length(); i > 0; i--) {
            String lineText = totalText.substring(0, i);
            if (designWidth > mTextPaint.measureText(lineText)) {
                return lineText;
            }
        }
        return totalText;
    }

    private int measureEndTime(String lineText) {
        int endIndex = mTotalText.indexOf(lineText) + lineText.length();
        return (int) (mTotalTime * (endIndex * 1.0f / mTotalText.length() * 1.0f));
    }

    private static class ItemData {
        protected String text;
        protected int    startTime;
        protected int    endTime;

        public ItemData(String text, int startTime, int endTime) {
            this.text = text;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

}
