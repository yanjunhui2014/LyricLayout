package com.milo.liblyric.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import com.milo.liblyric.MultiLyricData;

import java.util.ArrayList;
import java.util.List;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2020/9/25
 */
public class MultiLyricLayout extends LinearLayout {

    private List<MultiLyricData> mLyricList;

    private List<LyricLayout> mLyricLayoutList = new ArrayList<>();

    private int         mCurLyricIndex = -1;
    private LyricLayout mCurLyricLayout;

    private int mNormalColor  = -1;
    private int mCheckedColor = -1;

    public MultiLyricLayout(Context context) {
        super(context);
    }

    public MultiLyricLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiLyricLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLyricData(List<MultiLyricData> lyricList, int measureHeight) {
        mLyricList = lyricList;
        removeAllViews();
        mLyricLayoutList.clear();

        for (int i = 0; i < lyricList.size(); i++) {
            MultiLyricData lyricData = lyricList.get(i);
            LyricLayout lyricLayout = new LyricLayout(getContext());
            lyricLayout.setContent(lyricData.lyric, lyricData.endTime - lyricData.beginTime, measureHeight);
            if(mNormalColor !=- 1 && mCheckedColor != -1){
                lyricLayout.setTextColor(mNormalColor, mCheckedColor);
            }
            addView(lyricLayout);
            mLyricLayoutList.add(lyricLayout);
        }
    }

    public void updateLyricByTime(int seekTime) {
        updateLyricByTime(seekTime, false);
    }

    public void setTextColor(@ColorRes int normalColor, @ColorRes int checkedColor) {
        this.mNormalColor = normalColor;
        this.mCheckedColor = checkedColor;

        if (mLyricLayoutList != null && mLyricLayoutList.size() > 0) {
            for(LyricLayout lyricLayout : mLyricLayoutList){
                lyricLayout.setTextColor(normalColor, checkedColor);
            }
        }
    }

    /**
     * @param seekTime
     * @param sycnAll  - 是否同步更新所有子view，开启后更消耗性能
     */
    public void updateLyricByTime(int seekTime, boolean sycnAll) {
        for (int i = 0; i < mLyricList.size(); i++) {
            mCurLyricIndex = i;
            mCurLyricLayout = mLyricLayoutList.get(i);

            MultiLyricData lyric = mLyricList.get(i);
            if (seekTime >= lyric.beginTime && seekTime <= lyric.endTime) {
                int updateTime = seekTime - lyric.beginTime;
                mCurLyricLayout.updateTime(updateTime);
            }

            if (sycnAll) {
                if (seekTime <= lyric.beginTime) {
                    mLyricLayoutList.get(i).reset();
                } else if (seekTime >= lyric.endTime) {
                    mCurLyricLayout.updateTime(mLyricList.get(i).endTime - mLyricList.get(i).beginTime, true);
                }
            }
        }
    }

    public void reset() {
        updateLyricByTime(0, true);
    }

    public int getCurLyricIndex() {
        return mCurLyricIndex;
    }

    public LyricLayout getCurLyricLayout() {
        return mCurLyricLayout;
    }

}
