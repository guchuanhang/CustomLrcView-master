package com.lg.lrcview_master;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

public class SplitRowUtil {
    /**
     * 没有播放时，对应一行歌词进行的分割，实现多行展示
     */
    private List<List<String>> mNormalRowList = new ArrayList<List<String>>();
    /**
     * 正在播放时，对应一行歌词进行的分割，实现多行展示
     */
    private List<List<String>> mPlayingRowList = new ArrayList<List<String>>();
    /**
     * 原始的N行歌词
     */
    private List<String> mRowList;
    private Paint mNormalPaint;
    private Paint mPlayingPaint;
    private int mWidth;

    public SplitRowUtil(List<String> rowList, Paint normalPaint, Paint playingPaint, int viewWidth) {
        this.mRowList = rowList;
        this.mNormalPaint = normalPaint;
        this.mPlayingPaint = playingPaint;
        this.mWidth = viewWidth;
    }

    public void splitAllRow() {
        if (null == mRowList || mRowList.size() == 0) {
            return;
        }
        for (String rowStr : mRowList) {
            SplitRowBean bean = new SplitRowBean(rowStr, mNormalPaint, mPlayingPaint, mWidth);
            bean.splitText();
            List<String> normalRow = bean.getNormalList();
            List<String> playingRow = bean.getPlayingList();
            if (null == normalRow || normalRow.size() == 0 || null == playingRow || playingRow.size() == 0) {
                //should not reach here.
                continue;
            }
            mNormalRowList.add(normalRow);
            mPlayingRowList.add(playingRow);
        }
    }

    public List<List<String>> getNormalRowList() {
        return this.mNormalRowList;
    }

    public List<List<String>> getPlayingRowList() {
        return this.mPlayingRowList;
    }
}
