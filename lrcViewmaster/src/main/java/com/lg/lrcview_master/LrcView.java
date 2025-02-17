package com.lg.lrcview_master;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/***
 *
 * 须知：
 * 在ViewGroup里面 scrollTo，scrollBy方法移动的是子View
 * 在View里面scrollTo，scrollBy方法移动的是View里面绘制的内容
 * 要点：
 * 1:歌词的上下平移用什么实现？
 *    用Scroller实现，Scroller只是一个工具而已，
 *    真正实现滚动效果的还是View的scrollTo方法
 * 2：歌词的水平滚动怎么实现？
 *    通过属性动画ValueAnimator控制高亮歌词绘制的x轴起始坐标
 *
 * @author Ligang  2014/8/19
 *
 */
public class LrcView extends View implements ILrcView {
    private List<List<String>> mNormalRowList = new ArrayList<>();
    private List<List<String>> mPlayingRowList = new ArrayList<>();
    /**
     * 所有的歌词
     ***/
    private List<LrcRow> mLrcRows;
    /**
     * 无歌词数据的时候 显示的默认文字
     **/
    private static final String DEFAULT_TEXT = "酷狗音乐，传播好音乐";
    /**
     * 默认文字的字体大小
     **/
    private static final float SIZE_FOR_DEFAULT_TEXT = 35;

    /**
     * 画高亮歌词的画笔
     ***/
    private Paint mPaintForHighLightLrc;
    /**
     * 高亮歌词的默认字体大小
     ***/
    private static final float DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC = 100;
//    private static final float DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC = 35;
    /**
     * 高亮歌词当前的字体大小
     ***/
    private float mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC;
    /**
     * 高亮歌词的默认字体颜色
     **/
    private static final int DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC = 0xffEE4000;
    /**
     * 高亮歌词当前的字体颜色
     **/
    private int mCurColorForHightLightLrc = DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC;

    /**
     * 画其他歌词的画笔
     ***/
    private Paint mPaintForOtherLrc;
    /**
     * 其他歌词的默认字体大小
     ***/
    private static final float DEFAULT_SIZE_FOR_OTHER_LRC = 85;
//    private static final float DEFAULT_SIZE_FOR_OTHER_LRC = 30;
    /**
     * 其他歌词当前的字体大小
     ***/
    private float mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC;
    /**
     * 高亮歌词的默认字体颜色
     **/
    private static final int DEFAULT_COLOR_FOR_OTHER_LRC = Color.WHITE;
    /**
     * 高亮歌词当前的字体颜色
     **/
    private int mCurColorForOtherLrc = DEFAULT_COLOR_FOR_OTHER_LRC;


    /**
     * 画时间线的画笔
     ***/
    private Paint mPaintForTimeLine;
    /***时间线的颜色**/
    private static final int COLOR_FOR_TIME_LINE = 0xffD02090;
    /**
     * 时间文字大小
     **/
    private static final int SIZE_FOR_TIME = 18;
    /**
     * 是否画时间线
     **/
    private boolean mIsDrawTimeLine = false;

    /**
     * 歌词间默认的行距
     **/
    private static final float DEFAULT_PADDING = 20;
    /**
     * 歌词当前的行距
     **/
    private float mCurPadding = DEFAULT_PADDING;

    /**
     * 歌词的最大缩放比例
     **/
    public static final float MAX_SCALING_FACTOR = 1.5f;
    /**
     * 歌词的最小缩放比例
     **/
    public static final float MIN_SCALING_FACTOR = 0.5f;
    /**
     * 默认缩放比例
     **/
    private static final float DEFAULT_SCALING_FACTOR = 1.0f;
    /**
     * 歌词的当前缩放比例
     **/
    private float mCurScalingFactor = DEFAULT_SCALING_FACTOR;

    /**
     * 实现歌词竖直方向平滑滚动的辅助对象
     **/
    private Scroller mScroller;
    /***移动一句歌词的持续时间**/
    private static final int DURATION_FOR_LRC_SCROLL = 1500;
    /***停止触摸时 如果View需要滚动 时的持续时间**/
    private static final int DURATION_FOR_ACTION_UP = 400;

    /**
     * 控制文字缩放的因子
     **/
    private float mCurFraction = 0;
    private int mTouchSlop;

    public LrcView(Context context) {
        super(context);
        init();
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    /**
     * 初始化画笔等
     */
    @Override
    public void init() {
        mScroller = new Scroller(getContext());
        mPaintForHighLightLrc = new Paint();
        mPaintForHighLightLrc.setColor(mCurColorForHightLightLrc);
        mPaintForHighLightLrc.setTextSize(mCurSizeForHightLightLrc);

        mPaintForOtherLrc = new Paint();
        mPaintForOtherLrc.setColor(mCurColorForOtherLrc);
        mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);

        mPaintForTimeLine = new Paint();
        mPaintForTimeLine.setColor(COLOR_FOR_TIME_LINE);
        mPaintForTimeLine.setTextSize(SIZE_FOR_TIME);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private int mTotleDrawRow;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLrcRows == null || mLrcRows.size() == 0) {
            //画默认的显示文字
            mPaintForOtherLrc.setTextSize(mCurSizeForHightLightLrc);
            float textWidth = mPaintForOtherLrc.measureText(DEFAULT_TEXT);
            float textX = (getWidth() - textWidth) / 2;
            canvas.drawText(DEFAULT_TEXT, textX, getHeight() / 2, mPaintForOtherLrc);
            return;
        }
        if (mTotleDrawRow == 0) {
            //初始化将要绘制的歌词行数
            mTotleDrawRow = (int) (getHeight() / (mCurSizeForOtherLrc + mCurPadding));
        }
        //因为不需要将所有歌词画出来
        int minRaw = mCurRow - getRowCount(mCurRow, true);
        int maxRaw = mCurRow + getRowCount(mCurRow, false);

        minRaw = Math.max(minRaw, 0); //处理上边界
        maxRaw = Math.min(maxRaw, mLrcRows.size() - 1); //处理下边界
        //实现渐变的最大歌词行数
        int count = Math.max(maxRaw - mCurRow, mCurRow - minRaw);
        //两行歌词间字体颜色变化的透明度
        int alpha = (0xFF - 0x11) / count;
        //画出来的第一行歌词的y坐标
        float rowY = getHeight() / 2 + minRaw * (mCurSizeForOtherLrc + mCurPadding);
        //TODO.
        for (int i = minRaw; i <= maxRaw; i++) {

            if (i == mCurRow) {//画高亮歌词
                //因为有缩放效果，所有需要动态设置歌词的字体大小
                float textSize = mCurSizeForOtherLrc + (mCurSizeForHightLightLrc - mCurSizeForOtherLrc) * mCurFraction;
                mPaintForHighLightLrc.setTextSize(textSize);

                for (String shownRow : mPlayingRowList.get(i)) {
                    float textWidth = mPaintForHighLightLrc.measureText(shownRow);
                    float rowX = (getWidth() - textWidth) / 2;
                    canvas.drawText(shownRow, rowX, rowY, mPaintForHighLightLrc);
                    rowY += mCurSizeForHightLightLrc + mCurPadding;
                }
                rowY -= (mCurSizeForHightLightLrc + mCurPadding);


            } else {
//                if (i == mLastRow) {//画高亮歌词的上一句
//                    //因为有缩放效果，所有需要动态设置歌词的字体大小
//                    float textSize = mCurSizeForHightLightLrc - (mCurSizeForHightLightLrc - mCurSizeForOtherLrc) * mCurFraction;
//                    mPaintForOtherLrc.setTextSize(textSize);
//                } else {//画其他的歌词
                mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);
//                }

                for (String text : mNormalRowList.get(i)) {
                    float textWidth = mPaintForOtherLrc.measureText(text);
                    float textX = (getWidth() - textWidth) / 2;
                    //实现颜色渐变  从0xFFFFFFFF 逐渐变为 0x11FFFFFF(颜色还是白色，只是透明度变化)
                    int curAlpha = 255 - (Math.abs(i - mCurRow) - 1) * alpha; //求出当前歌词颜色的透明度
                    mPaintForOtherLrc.setColor(0x1000000 * curAlpha + 0xffffff);
                    canvas.drawText(text, textX, rowY, mPaintForOtherLrc);
                    rowY += mCurSizeForOtherLrc + mCurPadding;
                }
                rowY -= (mCurSizeForOtherLrc + mCurPadding);


            }
            //计算出下一行歌词绘制的y坐标
            rowY += mCurSizeForOtherLrc + mCurPadding;
        }


        //画时间线和时间
        if (mIsDrawTimeLine) {
            float y = getHeight() / 2 + getScrollY();
            canvas.drawText(mLrcRows.get(mCurRow).getTimeStr(), 0, y - 5, mPaintForTimeLine);
            canvas.drawLine(0, y, getWidth(), y, mPaintForTimeLine);
        }

    }

    /**
     * 是否可拖动歌词
     **/
    private boolean canDrag = false;
    /**
     * 事件的第一次的y坐标
     **/
    private float firstY;
    /**
     * 事件的上一次的y坐标
     **/
    private float lastY;
    private float lastX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstY = event.getRawY();
                lastX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canDrag) {
                    if (Math.abs(event.getRawY() - firstY) > mTouchSlop && Math.abs(event.getRawY() - firstY) > Math.abs(event.getRawX() - lastX)) {
                        canDrag = true;
                        mIsDrawTimeLine = true;
                        mScroller.forceFinished(true);
                        stopScrollLrc();
                        mCurFraction = 1;
                    }
                    lastY = event.getRawY();
                }

                if (canDrag) {
                    float offset = event.getRawY() - lastY;//偏移量
                    if (getScrollY() - offset < 0) {
                        if (offset > 0) {
                            offset = offset / 3;
                        }
                    } else if (getScrollY() - offset > mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding) {
                        if (offset < 0) {
                            offset = offset / 3;
                        }
                    }
                    scrollBy(getScrollX(), -(int) offset);
                    lastY = event.getRawY();
                    int currentRow = (int) (getScrollY() / (mCurSizeForOtherLrc + mCurPadding));
                    currentRow = Math.min(currentRow, mLrcRows.size() - 1);
                    currentRow = Math.max(currentRow, 0);
                    seekTo(mLrcRows.get(currentRow).getTime(), false, false);
                    return true;
                }
                lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!canDrag) {
                    if (onLrcClickListener != null) {
                        onLrcClickListener.onClick();
                    }
                } else {
                    if (onSeekToListener != null && mCurRow != -1) {
                        onSeekToListener.onSeekTo(mLrcRows.get(mCurRow).getTime());
                    }
                    if (getScrollY() < 0) {
                        smoothScrollTo(0, DURATION_FOR_ACTION_UP);
                    } else if (getScrollY() > mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding) {
                        smoothScrollTo((int) (mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding), DURATION_FOR_ACTION_UP);
                    }

                    canDrag = false;
                    mIsDrawTimeLine = false;
                    invalidate();
                }
                break;
        }
        return true;
    }

    /**
     * 为LrcView设置歌词List集合数据
     */
    @Override
    public void setLrcRows(List<LrcRow> lrcRows) {
        reset();
        this.mLrcRows = lrcRows;
        invalidate();
        spliteRows();

    }

    /**
     * 当前高亮歌词的行号
     **/
    private int mCurRow = -1;
    /**
     * 上一次的高亮歌词的行号
     **/
    private int mLastRow = -1;

    @Override
    public void seekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }
        //如果是由seekbar的进度改变触发 并且这时候处于拖动状态，则返回
        if (fromSeekBar && canDrag) {
            return;
        }
        for (int i = mLrcRows.size() - 1; i >= 0; i--) {

            if (progress >= mLrcRows.get(i).getTime()) {
                if (mCurRow != i) {
                    mLastRow = mCurRow;
                    mCurRow = i;
                    log("mCurRow=i=" + mCurRow);
                    if (fromSeekBarByUser) {
                        if (!mScroller.isFinished()) {
                            mScroller.forceFinished(true);
                        }
                        scrollTo(getScrollX(), (int) (mCurRow * (mCurSizeForOtherLrc + mCurPadding)));
                    } else {
                        smoothScrollTo((int) (mCurRow * (mCurSizeForOtherLrc + mCurPadding)), DURATION_FOR_LRC_SCROLL);
                    }
                    //如果高亮歌词的宽度大于View的宽，就需要开启属性动画，让它水平滚动
                    float textWidth = mPaintForHighLightLrc.measureText(mLrcRows.get(mCurRow).getContent());
                    log("textWidth=" + textWidth + "getWidth()=" + getWidth());
                    if (textWidth > getWidth()) {
                        if (fromSeekBarByUser) {
                            mScroller.forceFinished(true);
                        }
                        log("开始水平滚动歌词:" + mLrcRows.get(mCurRow).getContent());
                        startScrollLrc(getWidth() - textWidth, (long) (mLrcRows.get(mCurRow).getTotalTime() * 0.6));
                    }
                    invalidate();
                }
                break;
            }
        }

    }

    /**
     * 控制歌词水平滚动的属性动画
     ***/
    private ValueAnimator mAnimator;

    /**
     * 开始水平滚动歌词
     *
     * @param endX     歌词第一个字的最终的x坐标
     * @param duration 滚动的持续时间
     */
    private void startScrollLrc(float endX, long duration) {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0, endX);
            mAnimator.addUpdateListener(updateListener);
        } else {
            mCurTextXForHighLightLrc = 0;
            mAnimator.cancel();
            mAnimator.setFloatValues(0, endX);
        }
        mAnimator.setDuration(duration);
        mAnimator.setStartDelay((long) (duration * 0.3)); //延迟执行属性动画
        mAnimator.start();
    }

    /**
     * 停止歌词的滚动
     */
    private void stopScrollLrc() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mCurTextXForHighLightLrc = 0;
    }

    /**
     * 高亮歌词当前的其实x轴绘制坐标
     **/
    private float mCurTextXForHighLightLrc;
    /***
     * 监听属性动画的数值值的改变
     */
    AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //TODO
            mCurTextXForHighLightLrc = (Float) animation.getAnimatedValue();
            log("mCurTextXForHighLightLrc=" + mCurTextXForHighLightLrc);
            invalidate();
        }
    };

    /**
     * 设置歌词的缩放比例
     */
    @Override
    public void setLrcScalingFactor(float scalingFactor) {
        mCurScalingFactor = scalingFactor;
        mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC * mCurScalingFactor;
        mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC * mCurScalingFactor;
        mCurPadding = DEFAULT_PADDING * mCurScalingFactor;
        mTotleDrawRow = (int) (getHeight() / (mCurSizeForOtherLrc + mCurPadding)) + 3;
        log("mRowTotal=" + mTotleDrawRow);
        scrollTo(getScrollX(), (int) (mCurRow * (mCurSizeForOtherLrc + mCurPadding)));
        mScroller.forceFinished(true);
        spliteRows();
        invalidate();
    }

    /**
     * 重置
     */
    @Override
    public void reset() {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        mLrcRows = null;
        scrollTo(getScrollX(), 0);
        invalidate();
    }


    /**
     * 平滑的移动到某处
     *
     * @param dstY
     */
    private void smoothScrollTo(int dstY, int duration) {
        int oldScrollY = getScrollY();
        int offset = dstY - oldScrollY;
        mScroller.startScroll(getScrollX(), oldScrollY, getScrollX(), offset, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                int oldY = getScrollY();
                int y = mScroller.getCurrY();
                if (oldY != y && !canDrag) {
                    scrollTo(getScrollX(), y);
                }
                mCurFraction = mScroller.timePassed() * 3f / DURATION_FOR_LRC_SCROLL;
                mCurFraction = Math.min(mCurFraction, 1F);
                invalidate();
            }
        }
    }

    /**
     * 返回当前的歌词缩放比例
     *
     * @return
     */
    public float getmCurScalingFactor() {
        return mCurScalingFactor;
    }

    private OnSeekToListener onSeekToListener;

    public void setOnSeekToListener(OnSeekToListener onSeekToListener) {
        this.onSeekToListener = onSeekToListener;
    }

    public interface OnSeekToListener {
        void onSeekTo(int progress);
    }

    private OnLrcClickListener onLrcClickListener;

    public void setOnLrcClickListener(OnLrcClickListener onLrcClickListener) {
        this.onLrcClickListener = onLrcClickListener;
    }

    public interface OnLrcClickListener {
        void onClick();
    }

    public void log(Object o) {
        Log.d("LrcView", o + "");
    }

    /**
     * 分割字符串的调用地方
     */
    private void spliteRows() {

        if (null == mLrcRows || mLrcRows.size() == 0) {
            return;
        }
        Paint mNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNormalPaint.setTextAlign(Paint.Align.LEFT);
        mNormalPaint.setTextSize(mCurSizeForOtherLrc);
        Paint mPlayingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPlayingPaint.setTextAlign(Paint.Align.LEFT);
        mPlayingPaint.setTextSize(mCurSizeForHightLightLrc);

        List<String> rawStrList = new ArrayList<>();
        for (LrcRow lrcRow : mLrcRows) {
            rawStrList.add(lrcRow.getContent());
        }
        SplitRowUtil splitRowUtil = new SplitRowUtil(rawStrList, mNormalPaint, mPlayingPaint, getWidth());
        splitRowUtil.splitAllRow();
        mNormalRowList.clear();
        mPlayingRowList.clear();
        mNormalRowList.addAll(splitRowUtil.getNormalRowList());
        mPlayingRowList.addAll(splitRowUtil.getPlayingRowList());
    }

    /**
     * 需要重新修改可以展示的row范围
     *
     * @param curRow
     */
    private int getRowCount(int curRow, boolean isForward) {
        int totalRow = (int) (getHeight() / (mCurSizeForOtherLrc + mCurPadding)) / 2;
        int rowNum = curRow;
        int rowCount = 0;
        int pureRowCount = 0; //真正向前或向后移动的行数
        if (isForward) {
            rowNum--;
        } else {
            rowNum++;
        }
        while (rowNum >= 0 && rowNum < mNormalRowList.size() && rowCount < totalRow) {

            rowCount += mNormalRowList.get(rowNum).size();
            pureRowCount++;
            if (isForward) {
                rowNum--;
            } else {
                rowNum++;
            }

        }
        return pureRowCount - 1;

    }
}
