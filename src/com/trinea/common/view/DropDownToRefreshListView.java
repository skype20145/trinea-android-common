package com.trinea.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trinea.common.R;
import com.trinea.java.common.StringUtils;

/**
 * 下拉刷新的listView
 * <ul>
 * 替代ListView使用，使用方法如下
 * <li>xml中配置同ListView</li>
 * </ul>
 * http://johannilsson.com/2011/03/13/android-pull-to-refresh-update.html
 * https://github.com/wdx700/Android-Pull-To-Refresh/blob/master/PullToRefresh/src/com/dmobile/pulltorefresh/
 * PullRefreshContainerView.java
 * http://blog.csdn.net/aomandeshangxiao/article/details/7325383
 * 类DropDownToRefreshListView.java的实现描述：TODO 类实现描述
 * 
 * @author Trinea 2012-5-20 上午12:36:33
 */
public class DropDownToRefreshListView extends ListView implements OnScrollListener {

    /**
     * 刷新的状态
     */
    public enum RefreshStatusEnum {
        TAP_TO_REFRESH, PULL_TO_REFRESH, RELEASE_TO_REFRESH, REFRESHING
    }

    // 下拉时下拉距离和header top变化的比例
    private static final float  HEADER_PADDING_RATE       = 1.5f;
    // 向下拉动时的默认提示
    private static final String DEFAULT_PULL_TIPS         = "Pull to refresh...";
    // 拉动释放时的默认提示
    private static final String DEFAULT_RELEASE_TIPS      = "Release to refresh...";
    // 刷新中的默认提示
    private static final String DEFAULT_REFRESHING_TIPS   = "Loading...";
    // 刷新View的默认text
    private static final String DEFAULT_REFRESH_VIEW_TIPS = "Tap to refresh...";

    // 刷新事件
    private OnRefreshListener   onRefreshListener;
    // 向下拉动时的提示
    private CharSequence        pullTips;
    // 拉动释放时的提示
    private CharSequence        releaseTips;
    // 刷新中的提示
    private CharSequence        refreshingTips;
    // 刷新View的提示
    private CharSequence        refreshViewTips;

    private OnScrollListener    onScrollListener;

    /**
     * 需要的View
     */
    private RelativeLayout      refreshViewLayout;
    private TextView            refreshViewTipsText;
    private ImageView           refreshViewImage;
    private ProgressBar         refreshViewProgress;
    private TextView            refreshViewLastUpdatedText;

    // 当前的滚动状态
    private int                 currentScrollState;
    // 当前的刷新状态
    private RefreshStatusEnum   currentRefreshState;

    // 正向翻转的animation
    private RotateAnimation     mFlipAnimation;
    // 反向翻转的animation
    private RotateAnimation     mReverseFlipAnimation;

    // header(刷新View layout)的初始高度
    private int                 headerOriginalHeight;
    // header(刷新View layout)的初始top padding
    private int                 headerOriginalTopPadding;
    // 最后touch的点y坐标
    private float               lastTouchY;
    // 是否反弹，滑动到顶部则标记为true
    private boolean             isBounceHack;

    public DropDownToRefreshListView(Context context){
        super(context);
        init(context);
    }

    public DropDownToRefreshListView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    public DropDownToRefreshListView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                             RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        refreshViewLayout = (RelativeLayout)inflater.inflate(R.layout.drop_down_to_refresh_list_header, this, false);
        refreshViewTipsText = (TextView)refreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_text);
        refreshViewImage = (ImageView)refreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_image);
        refreshViewProgress = (ProgressBar)refreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_progress);
        refreshViewLastUpdatedText = (TextView)refreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_last_updated_text);
        refreshViewImage.setMinimumHeight(50);
        refreshViewLayout.setOnClickListener(new OnClickRefreshListener());
        refreshViewTipsText.setText(DEFAULT_REFRESH_VIEW_TIPS);
        addHeaderView(refreshViewLayout);

        super.setOnScrollListener(this);

        measureView(refreshViewLayout);
        headerOriginalHeight = refreshViewLayout.getMeasuredHeight();
        headerOriginalTopPadding = refreshViewLayout.getPaddingTop();
        currentRefreshState = RefreshStatusEnum.TAP_TO_REFRESH;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        scrollToTop();
    }

    /**
     * 滚动到刷新View外的第一个item
     */
    public void scrollToTop() {
        if (getAdapter() != null && getAdapter().getCount() > 0) {
            setSelection(1);
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        scrollToTop();
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        onScrollListener = l;
    }

    /**
     * 设置刷新事件器
     * 
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isBounceHack = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                adjustHeaderPadding(event);
                break;
            case MotionEvent.ACTION_UP:
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                if (getFirstVisiblePosition() == 0 && currentRefreshState != RefreshStatusEnum.REFRESHING) {
                    if ((refreshViewLayout.getBottom() >= headerOriginalHeight || refreshViewLayout.getTop() >= 0)
                        && currentRefreshState == RefreshStatusEnum.RELEASE_TO_REFRESH) {
                        // 刷新
                        onRefresh();
                    } else if (refreshViewLayout.getBottom() < headerOriginalHeight || refreshViewLayout.getTop() <= 0) {
                        // 放弃刷新
                        resetHeader();
                        scrollToTop();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 调整header的padding
     * 
     * @param ev
     */
    private void adjustHeaderPadding(MotionEvent ev) {
        /**
         * 通过获取move历史坐标点，不断设置header的padding
         */
        int pointerCount = ev.getHistorySize();
        for (int i = 0; i < pointerCount; i++) {
            if (currentRefreshState == RefreshStatusEnum.RELEASE_TO_REFRESH) {
                if (isVerticalFadingEdgeEnabled()) {
                    setVerticalScrollBarEnabled(false);
                }
                refreshViewLayout.setPadding(refreshViewLayout.getPaddingLeft(),
                                             (int)(((ev.getHistoricalY(i) - lastTouchY) - headerOriginalHeight) / HEADER_PADDING_RATE),
                                             refreshViewLayout.getPaddingRight(), refreshViewLayout.getPaddingBottom());
            }
        }
    }

    /**
     * 重置header的padding
     */
    private void resetHeaderPadding() {
        refreshViewLayout.setPadding(refreshViewLayout.getPaddingLeft(), headerOriginalTopPadding,
                                     refreshViewLayout.getPaddingRight(), refreshViewLayout.getPaddingBottom());
    }

    /**
     * 重置header
     */
    private void resetHeader() {
        if (currentRefreshState != RefreshStatusEnum.TAP_TO_REFRESH) {
            currentRefreshState = RefreshStatusEnum.TAP_TO_REFRESH;

            resetHeaderPadding();

            refreshViewTipsText.setText(StringUtils.isEmpty(refreshViewTips) ? DEFAULT_REFRESH_VIEW_TIPS : refreshViewTips);
            refreshViewImage.clearAnimation();
            refreshViewImage.setImageResource(R.drawable.drop_down_to_refresh_list_arrow);

            refreshViewImage.setVisibility(View.GONE);
            refreshViewProgress.setVisibility(View.GONE);
        }
    }

    /**
     * 测量View的宽度和高度
     * 
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        /**
         * 按着不放滚动中并且非刷新状态REFRESHING
         * a. 刷新对应的item可见，若高度超出范围，则置为RELEASE_TO_REFRESH；若低于高度范围，则置为PULL_TO_REFRESH。
         * b. 刷新对应的item不可见，重置header
         * 松手滚动中
         * a. 若刷新对应的item可见，滚动到第一个item
         * b. 若反弹回来，滚动到第一个item
         */
        if (currentScrollState == SCROLL_STATE_TOUCH_SCROLL && currentRefreshState != RefreshStatusEnum.REFRESHING) {
            if (firstVisibleItem == 0) {
                refreshViewImage.setVisibility(View.VISIBLE);
                if ((refreshViewLayout.getBottom() >= headerOriginalHeight + 20 || refreshViewLayout.getTop() >= 0)
                    && currentRefreshState != RefreshStatusEnum.RELEASE_TO_REFRESH) {
                    refreshViewTipsText.setText(StringUtils.isEmpty(releaseTips) ? DEFAULT_RELEASE_TIPS : releaseTips);
                    refreshViewImage.clearAnimation();
                    refreshViewImage.startAnimation(mFlipAnimation);
                    currentRefreshState = RefreshStatusEnum.RELEASE_TO_REFRESH;
                } else if (refreshViewLayout.getBottom() < headerOriginalHeight + 20
                           && currentRefreshState != RefreshStatusEnum.PULL_TO_REFRESH) {
                    refreshViewTipsText.setText(StringUtils.isEmpty(pullTips) ? DEFAULT_PULL_TIPS : pullTips);
                    if (currentRefreshState == RefreshStatusEnum.RELEASE_TO_REFRESH) {
                        refreshViewImage.clearAnimation();
                        refreshViewImage.startAnimation(mReverseFlipAnimation);
                    }
                    currentRefreshState = RefreshStatusEnum.PULL_TO_REFRESH;
                }
            } else {
                resetHeader();
            }
        } else if (currentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0
                   && currentRefreshState != RefreshStatusEnum.REFRESHING) {
            scrollToTop();
            isBounceHack = true;
        } else if (isBounceHack && currentScrollState == SCROLL_STATE_FLING) {
            scrollToTop();
        }

        if (onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        currentScrollState = scrollState;

        if (currentScrollState == SCROLL_STATE_IDLE) {
            isBounceHack = false;
        }

        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * 准备刷新
     */
    public void prepareForRefresh() {
        resetHeaderPadding();

        refreshViewImage.setVisibility(View.GONE);
        refreshViewImage.setImageDrawable(null);
        refreshViewProgress.setVisibility(View.VISIBLE);
        refreshViewTipsText.setText(StringUtils.isEmpty(refreshingTips) ? DEFAULT_REFRESHING_TIPS : refreshingTips);
    }

    /**
     * 刷新
     */
    public void onRefresh() {
        if (onRefreshListener != null) {
            currentRefreshState = RefreshStatusEnum.REFRESHING;
            prepareForRefresh();
            onRefreshListener.onRefresh();
        }
    }

    /**
     * 刷新结束
     * 
     * @param lastUpdatedText 上次更新信息，若为null，不显示
     */
    public void onRefreshComplete(CharSequence lastUpdatedText) {
        setLastUpdatedText(lastUpdatedText);
        onRefreshComplete();
    }

    /**
     * 刷新结束
     */
    public void onRefreshComplete() {
        resetHeader();

        if (refreshViewLayout.getBottom() > 0) {
            invalidateViews();
            scrollToTop();
        }
    }

    /**
     * 点击刷新View时调用<br/>
     * 主要在list仅有少量items，无法下拉刷新只能手动点击刷新View时调用
     */
    private class OnClickRefreshListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (currentRefreshState != RefreshStatusEnum.REFRESHING) {
                onRefresh();
            }
        }

    }

    /**
     * 在刷新list时调用
     * 
     * @author Trinea 2012-5-31 上午11:15:39
     */
    public interface OnRefreshListener {

        /**
         * 在刷新list时调用
         */
        public void onRefresh();
    }

    public CharSequence getPullTips() {
        return pullTips;
    }

    public void setPullTips(CharSequence pullTips) {
        this.pullTips = pullTips;
    }

    public CharSequence getReleaseTips() {
        return releaseTips;
    }

    public void setReleaseTips(CharSequence releaseTips) {
        this.releaseTips = releaseTips;
    }

    public CharSequence getRefreshingTips() {
        return refreshingTips;
    }

    public void setRefreshingTips(CharSequence refreshingTips) {
        this.refreshingTips = refreshingTips;
    }

    public CharSequence getRefreshViewTips() {
        return refreshViewTips;
    }

    public void setRefreshViewText(CharSequence refreshViewTips) {
        this.refreshViewTips = refreshViewTips;
    }

    /**
     * 设置上次更新信息
     * 
     * @param lastUpdatedText 上次更新信息，若为null，不显示
     */
    public void setLastUpdatedText(CharSequence lastUpdatedText) {
        if (lastUpdatedText == null) {
            refreshViewLastUpdatedText.setVisibility(View.GONE);
        } else {
            refreshViewLastUpdatedText.setVisibility(View.VISIBLE);
            refreshViewLastUpdatedText.setText(lastUpdatedText);
        }
    }
}
