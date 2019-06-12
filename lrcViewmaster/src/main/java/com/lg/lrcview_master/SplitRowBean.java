package com.lg.lrcview_master;

import android.graphics.Paint;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 为了解决LrcView,播放歌词的时候，实现对单词的自动换行功能，定义该结构体
 */
public class SplitRowBean {
    /**
     * 没有播放时，对应一行歌词进行的分割，实现多行展示
     */
    private List<String> mNormalList = new ArrayList<>();
    /**
     * 正在播放时，对应一行歌词进行的分割，实现多行展示
     */
    private List<String> mPlayingList = new ArrayList<>();
    /**
     * 原始的一行歌词
     */
    private String mRawStr;
    private Paint mNormalPaint;
    private Paint mPlayingPaint;
    private int mWidth;

    public SplitRowBean(String rawStr, Paint normalPaint, Paint playingPaint, int viewWidth) {
        this.mRawStr = rawStr;
        this.mNormalPaint = normalPaint;
        this.mPlayingPaint = playingPaint;
        this.mWidth = viewWidth;
    }

    public void splitText() {
        splitText(mNormalList, mRawStr, mNormalPaint);
        splitText(mPlayingList, mRawStr, mPlayingPaint);
    }

    public List<String> getNormalList() {
        return mNormalList;
    }

    public List<String> getPlayingList() {
        return mPlayingList;
    }


    private void splitText(List<String> splitList, String rawLineStr, Paint paint) {
        String rawStr = rawLineStr;
        int splitTextLength = 0;
        while (splitTextLength < rawLineStr.length()) {
            int index = binSearch(rawStr, paint);
            if (index < 0) {
                return;
            }
            splitList.add(rawStr.substring(0, index + 1));
            splitTextLength += index + 1;
            if (splitTextLength < rawLineStr.length()) {
                rawStr = rawStr.substring(index + 1);
            }
        }
    }


    // 二分查找普通循环实现
    private int binSearch(String rawStr, Paint paint) {
        if (TextUtils.isEmpty(rawStr)) {
            return -1;
        }
        float totalLength = paint.measureText(rawStr);
        if (totalLength <= mWidth) {
            return rawStr.length() - 1;
        }

        int mid = rawStr.length() / 2;

        float midLength = paint.measureText(rawStr.substring(0, mid));
        float midMoreLength = paint.measureText(rawStr.substring(0, mid + 1));

        if (midLength <= mWidth && midMoreLength > mWidth) {
            return mid - 1;
        }
        int start = 0;
        int end = rawStr.length() - 1;
        while (start <= end) {
            mid = (end - start) / 2 + start;

            midLength = paint.measureText(rawStr.substring(0, mid));
            midMoreLength = paint.measureText(rawStr.substring(0, mid + 1));

            if (mWidth < midLength) {
                end = mid - 1;
            } else if (mWidth > midMoreLength) {
                start = mid + 1;
            } else {
                return mid - 1;
            }
        }
        return -1;
    }
}
