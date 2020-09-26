package com.milo.liblyric.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.milo.liblyric.LibLyricLog;
import com.milo.liblyric.R;
import com.milo.liblyric.WeakHandler;


/**
 * Title：矩形渐变TextView
 * Describe：
 * Remark：支持多行渐变的TextView
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2020/6/22
 */
public class GradientRectTextView extends AppCompatTextView {
    private static final String TAG        = "GradientRectTextView";
    private static final int    MSG_UPDATE = 1;

    public long mTotalCount;
    public long mStartCount = -1;
    public long mEndCount   = -1;
    public long mCurrentCount;

    public final long mIntervalCount = 16L;

    private WeakHandler    mWeakHandler;
    private LinearGradient mLinearGradient;

    @ColorRes
    private int mNormalColor   = R.color.lib_lyric_white;//默认字体色
    @ColorRes
    private int mGradientColor = R.color.lib_lyric_c1;//歌词覆盖时字体色

    public GradientRectTextView(@NonNull Context context) {
        super(context);
    }

    public GradientRectTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mWeakHandler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE:
                        mCurrentCount += mIntervalCount;
                        if (mCurrentCount >= mTotalCount) {
                            mCurrentCount = mTotalCount;
                            stopCount();
                        } else {
                            mWeakHandler.sendEmptyMessageDelayed(MSG_UPDATE, mIntervalCount);
                            updateCount(mCurrentCount);
                        }
                        break;
                }
                return false;
            }
        });
    }

    public GradientRectTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setColor(@ColorRes int normalColor, @ColorRes int gradientCorlor) {
        mNormalColor = normalColor;
        mGradientColor = gradientCorlor;
        setTextColor(ContextCompat.getColor(getContext(), mNormalColor));

        if (mLinearGradient == null) {
            return;
        }
        mLinearGradient = new LinearGradient(0, getTop(), getMeasuredWidth(), getTop(), new int[]{ContextCompat.getColor(getContext(), mGradientColor), ContextCompat.getColor(getContext(), mNormalColor)},
                new float[]{1.0f, 0.0f}, Shader.TileMode.CLAMP);
        getPaint().setShader(mLinearGradient);

        Matrix matrix = getMatrix();
        matrix.setScale(0.0f, 1.0f);
        mLinearGradient.setLocalMatrix(matrix);
    }

    private void initGradient() {
        setTextColor(ContextCompat.getColor(getContext(), mNormalColor));
        if (mLinearGradient == null) {

            //这里x0取值非常关键，它不是根据屏幕来的，而是根据当前组件来的，不然会导致渐变控制错误
            mLinearGradient = new LinearGradient(0, getTop(), getMeasuredWidth(), getTop(), new int[]{ContextCompat.getColor(getContext(), mGradientColor), ContextCompat.getColor(getContext(), mNormalColor)},
                    new float[]{1.0f, 0.0f}, Shader.TileMode.CLAMP);
            getPaint().setShader(mLinearGradient);

            Matrix matrix = getMatrix();
            matrix.setScale(0.0f, 1.0f);
            mLinearGradient.setLocalMatrix(matrix);
        }
    }

    public void setStartAndEndCount(int startCount, int endCount) {
        if (startCount >= endCount) {
            throw new IllegalArgumentException("startCount >= endCount");
        }
        this.mStartCount = startCount;
        this.mEndCount = endCount;
        this.mTotalCount = endCount - startCount;
    }

    public void setTotalCount(long totalCount) {
        this.mTotalCount = totalCount;
    }

    public void startCount() {
        if (mWeakHandler.hasMessages(MSG_UPDATE)) {
            LibLyricLog.d(TAG, "startcount未执行 -> 已有指令 msg_update");
            return;
        }

        mWeakHandler.sendEmptyMessage(MSG_UPDATE);
    }

    public void stopCount() {
        if (mWeakHandler != null)
            mWeakHandler.removeCallbacksAndMessages(null);
    }

    public void updateCount(long count) {
        initGradient();

        mCurrentCount = Math.min(mTotalCount, count);
        if (mCurrentCount != 0) {
            float scalex = mCurrentCount * 1.0f / mTotalCount * 1.0f;

            Matrix matrix = getMatrix();
            matrix.setScale(scalex, 1.0f);
            mLinearGradient.setLocalMatrix(matrix);
        }
        postInvalidate();
    }

    public void resetCount() {
        stopCount();
        if (mLinearGradient != null) {
            mCurrentCount = 0;
            Matrix matrix = getMatrix();
            matrix.setScale(0.0f, 1.0f);
            mLinearGradient.setLocalMatrix(matrix);

            invalidate();
        }
    }

}
