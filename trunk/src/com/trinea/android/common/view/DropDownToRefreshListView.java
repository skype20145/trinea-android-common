package com.trinea.android.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trinea.android.common.R;

/**
 * 下拉刷新的listView
 * <ul>
 * 替代ListView使用，使用方法如下
 * <li>xml中配置同ListView</li>
 * <li>设置{@link #setOnRefreshListener(OnRefreshListener)}，刷新时执行onRefresh函数</li>
 * <li>刷新结束时调用{@link #onRefreshComplete()}表示刷新结束，恢复View状态</li>
 * </ul>
 * <ul>
 * 其他设置见http://trinea.iteye.com/blog/1560986
 * </ul>
 * <ul>
 * 实现原理见http://trinea.iteye.com/blog/1562281
 * </ul>
 * 
 * @author Trinea 2012-5-20
 */
public class DropDownToRefreshListView extends ListView implements OnScrollListener {

    /** 是否是下拉刷新样式 **/
    private boolean isDropDownToRefreshStyle = true;
    /** 是否是加载更多样式 **/
    private boolean isLoadMoreStyle          = true;
    /** 是否自动加载更多 **/
    private boolean isAutoLoadMore           = false;
    /** 还有更多标识 **/
    private boolean hasMore                  = true;

    /**
     * 刷新的状态
     */
    public enum RefreshStatusEnum {
        /** 点击刷新状态，为初始状态 **/
        CLICK_TO_REFRESH,
        /** 当刷新layout高度低于一定范围时，下拉再释放即可刷新 **/
        DROP_DOWN_TO_REFRESH,
        /** 当刷新layout高度高于一定范围时，释放即可刷新 **/
        RELEASE_TO_REFRESH,
        /** 刷新中 **/
        REFRESHING
    }

    /** 下拉时下拉距离和header top变化的比例 **/
    private static final float HEADER_PADDING_RATE       = 1.5f;
    /** header height变化的上界 **/
    private static final int   HEADER_HEIGHT_UPPER_LEVEL = 10;

    /** 刷新事件 **/
    private OnRefreshListener  onRefreshListener;
    private OnScrollListener   onScrollListener;

    /** 需要的View **/
    private RelativeLayout     refreshViewLayout;
    private TextView           refreshViewTipsText;
    private ImageView          refreshViewImage;
    private ProgressBar        refreshViewProgress;
    private TextView           refreshViewLastUpdatedText;
    private RelativeLayout     loadMoreLayout;
    private ProgressBar        loadMoreProgress;
    private Button             loadMoreButton;

    /** whether show loading progress when loading more **/
    private boolean            isShowLoadMoreProgress    = false;

    /** 当前的滚动状态 **/
    private int                currentScrollState;
    /** 当前的刷新状态 **/
    private RefreshStatusEnum  currentRefreshState;

    /** 正向翻转的animation **/
    private RotateAnimation    flipAnimation;
    /** 反向翻转的animation **/
    private RotateAnimation    reverseFlipAnimation;

    /** header(刷新View layout)的初始高度 **/
    private int                headerOriginalHeight;
    /** header(刷新View layout)的初始top padding **/
    private int                headerOriginalTopPadding;
    /** 用户手指刚接触屏幕时touch的点y坐标 **/
    private float              actionDownPointY;
    /** 是否反弹，滑动到顶部则标记为true **/
    private boolean            isBounceHack;

    private boolean            isNeedLoadMore;

    public DropDownToRefreshListView(Context context){
        super(context);
        init(context);
    }

    public DropDownToRefreshListView(Context context, AttributeSet attrs){
        super(context, attrs);
        getAttrs(context, attrs);
        init(context);
    }

    public DropDownToRefreshListView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        getAttrs(context, attrs);
        init(context);
    }

    private void init(Context context) {
        if (isDropDownToRefreshStyle) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            initDropDownToRefresh(context, inflater);
        }
        if (isLoadMoreStyle) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            initLoadMore(context, inflater);
        }
        isNeedLoadMore = false;
        super.setOnScrollListener(this);
    }

    /**
     * 加载更多样式初始化
     * 
     * @param context
     * @param inflater
     */
    private void initLoadMore(Context context, LayoutInflater inflater) {
        loadMoreLayout = (RelativeLayout)inflater.inflate(R.layout.drop_down_to_refresh_list_bottom,
                                                          this, false);
        loadMoreButton = (Button)loadMoreLayout.findViewById(R.id.drop_down_to_refresh_list_more);
        loadMoreButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        loadMoreButton.setDrawingCacheBackgroundColor(0);

        loadMoreProgress = (ProgressBar)loadMoreLayout.findViewById(R.id.drop_down_to_refresh_list_more_progress);
        addFooterView(loadMoreLayout);
    }

    /**
     * 得到加载更多的Button
     * 
     * @return
     */
    public Button getLoadMoreButton() {
        return loadMoreButton;
    }

    /**
     * 下拉刷新样式初始化
     * 
     * @param context
     * @param inflater
     */
    private void initDropDownToRefresh(Context context, LayoutInflater inflater) {
        flipAnimation = new RotateAnimation(0, 180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        flipAnimation.setInterpolator(new LinearInterpolator());
        flipAnimation.setDuration(250);
        flipAnimation.setFillAfter(true);
        reverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                                   RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseFlipAnimation.setInterpolator(new LinearInterpolator());
        reverseFlipAnimation.setDuration(250);
        reverseFlipAnimation.setFillAfter(true);

        refreshViewLayout = (RelativeLayout)inflater.inflate(R.layout.drop_down_to_refresh_list_header,
                                                             this, false);
        refreshViewTipsText = (TextView)refreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_text);
        refreshViewImage = (ImageView)refreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_image);
        refreshViewProgress = (ProgressBar)refreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_progress);
        refreshViewLastUpdatedText = (TextView)refreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_last_updated_text);
        refreshViewImage.setMinimumHeight(50);
        refreshViewLayout.setOnClickListener(new OnClickRefreshListener());
        refreshViewTipsText.setText(R.string.drop_down_to_refresh_list_refresh_view_tips);
        addHeaderView(refreshViewLayout);

        // 设置OnScrollListener为当前的listener
        super.setOnScrollListener(this);

        measureView(refreshViewLayout);
        headerOriginalHeight = refreshViewLayout.getMeasuredHeight();
        headerOriginalTopPadding = refreshViewLayout.getPaddingTop();
        currentRefreshState = RefreshStatusEnum.CLICK_TO_REFRESH;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if (isDropDownToRefreshStyle) {
            setSecondPositionVisible();
        }
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener listener) {
        onScrollListener = listener;
    }

    /**
     * 设置刷新事件
     * 
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    /**
     * 设置加载更多事件
     * 
     * @param onLoadMoreListener
     */
    public void setOnLoadMoreListener(OnClickListener onLoadMoreListener) {
        if (isLoadMoreStyle) {
            loadMoreButton.setOnClickListener(onLoadMoreListener);
        }
    }

    /**
     * get whether show loading progress when loading more
     * 
     * @return
     */
    public boolean isShowLoadMoreProgress() {
        return isShowLoadMoreProgress;
    }

    /**
     * set whether show loading progress when loading more
     * 
     * @param isShowLoadMoreProgress
     */
    public void setShowLoadMoreProgress(boolean isShowLoadMoreProgress) {
        this.isShowLoadMoreProgress = isShowLoadMoreProgress;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDropDownToRefreshStyle) {
            isBounceHack = false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    actionDownPointY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    adjustHeaderPadding(event);
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isVerticalScrollBarEnabled()) {
                        setVerticalScrollBarEnabled(true);
                    }
                    if (getFirstVisiblePosition() == 0
                        && currentRefreshState != RefreshStatusEnum.REFRESHING) {
                        switch (currentRefreshState) {
                            case CLICK_TO_REFRESH:
                                setStatusClickToRefresh();
                                break;
                            case RELEASE_TO_REFRESH:
                                onRefresh();
                                break;
                            case DROP_DOWN_TO_REFRESH:
                                setStatusClickToRefresh();
                                setSecondPositionVisible();
                                break;
                            default:
                                break;
                        }
                    }
                    break;
            }
        }

        // 当为加载更多样式并且设置了自动加载更多且含有更多元素时到达屏幕底时自动加载更多
        if (isLoadMoreStyle && isAutoLoadMore && hasMore && isNeedLoadMore) {
            switch (event.getAction()) {
                // case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    loadMoreButton.performClick();
                    isNeedLoadMore = false;
                    break;
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        if (isDropDownToRefreshStyle) {
            /**
             * ListView为SCROLL_STATE_TOUCH_SCROLL状态(按着不放滚动中)并且刷新状态不为REFRESHING a.
             * 刷新对应的item可见时，若刷新layout高度超出范围，则置刷新状态为RELEASE_TO_REFRESH； 若刷新layout高度低于高度范围，则置刷新状态为DROP_DOWN_TO_REFRESH b.
             * 刷新对应的item不可见，重置header ListView为SCROLL_STATE_FLING状态(松手滚动中) a.
             * 若刷新对应的item可见并且刷新状态不为REFRESHING，设置position为1的(即第二个)item可见 b. 若反弹回来，设置position为1的(即第二个)item可见
             */
            if (currentScrollState == SCROLL_STATE_TOUCH_SCROLL
                && currentRefreshState != RefreshStatusEnum.REFRESHING) {
                if (firstVisibleItem == 0) {
                    refreshViewImage.setVisibility(View.VISIBLE);
                    if (refreshViewLayout.getBottom() >= headerOriginalHeight
                                                         + HEADER_HEIGHT_UPPER_LEVEL
                        || refreshViewLayout.getTop() >= 0) {
                        setStatusReleaseToRefresh();
                    } else if (refreshViewLayout.getBottom() < headerOriginalHeight
                                                               + HEADER_HEIGHT_UPPER_LEVEL) {
                        setStatusDropDownToRefresh();
                    }
                } else {
                    setStatusClickToRefresh();
                }
            } else if (currentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0
                       && currentRefreshState != RefreshStatusEnum.REFRESHING) {
                setSecondPositionVisible();
                isBounceHack = true;
            } else if (currentScrollState == SCROLL_STATE_FLING && isBounceHack) {
                setSecondPositionVisible();
            }
        }

        // 当为加载更多样式并且设置了自动加载更多且含有更多元素时到达屏幕底时自动加载更多
        if (isLoadMoreStyle && isAutoLoadMore && hasMore) {
            if (firstVisibleItem > 0 && totalItemCount > 0
                && (firstVisibleItem + visibleItemCount == totalItemCount)) {
                isNeedLoadMore = true;
            }
        } else {
            isNeedLoadMore = false;
        }
        if (onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isDropDownToRefreshStyle) {
            currentScrollState = scrollState;

            if (currentScrollState == SCROLL_STATE_IDLE) {
                isBounceHack = false;
            }
        }

        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * 准备刷新
     */
    public void onRefreshBegin() {
        if (isDropDownToRefreshStyle) {
            setStatusRefreshing();
        }
    }

    /**
     * 准备加载更多
     */
    public void onLoadMoreBegin() {
        if (isLoadMoreStyle) {
            if (isShowLoadMoreProgress) {
                loadMoreProgress.setVisibility(View.VISIBLE);
            }
            loadMoreButton.setText(R.string.drop_down_to_refresh_list_loading_more_tips);
            loadMoreButton.setEnabled(false);
        }
    }

    /**
     * 加载更多结束
     */
    public void onLoadMoreComplete() {
        if (isLoadMoreStyle) {
            if (isShowLoadMoreProgress) {
                loadMoreProgress.setVisibility(View.GONE);
            }
            loadMoreButton.setEnabled(true);
            if (!hasMore) {
                loadMoreButton.setText(R.string.drop_down_to_refresh_list_no_more_tips);
            } else {
                loadMoreButton.setText(R.string.drop_down_to_refresh_list_more_tips);
            }
        }
    }

    /**
     * 刷新
     */
    public void onRefresh() {
        if (isDropDownToRefreshStyle && onRefreshListener != null) {
            onRefreshBegin();
            onRefreshListener.onRefresh();
        }
    }

    /**
     * 刷新结束
     * 
     * @param lastUpdatedText 上次更新信息，若为null，不显示
     */
    public void onRefreshComplete(CharSequence lastUpdatedText) {
        if (isDropDownToRefreshStyle) {
            setLastUpdatedText(lastUpdatedText);
            onRefreshComplete();
        }
    }

    /**
     * 刷新结束，恢复View状态
     */
    public void onRefreshComplete() {
        if (isDropDownToRefreshStyle) {
            setStatusClickToRefresh();

            if (refreshViewLayout.getBottom() > 0) {
                invalidateViews();
                setSecondPositionVisible();
            }
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

    /**
     * 如果第一个可见的item position为0(即为刷新View)，设置position为1的(即第二个)item可见
     */
    public void setSecondPositionVisible() {
        if (getAdapter() != null && getAdapter().getCount() > 0 && getFirstVisiblePosition() == 0) {
            setSelection(1);
        }
    }

    /**
     * 设置上次更新信息
     * 
     * @param lastUpdatedText 上次更新信息，若为null，不显示
     */
    public void setLastUpdatedText(CharSequence lastUpdatedText) {
        if (isDropDownToRefreshStyle) {
            if (lastUpdatedText == null) {
                refreshViewLastUpdatedText.setVisibility(View.GONE);
            } else {
                refreshViewLastUpdatedText.setVisibility(View.VISIBLE);
                refreshViewLastUpdatedText.setText(lastUpdatedText);
            }
        }
    }

    /**
     * 得到是否是下拉刷新样式
     * 
     * @return
     */
    public boolean isDropDownToRefreshStyle() {
        return isDropDownToRefreshStyle;
    }

    /**
     * 得到是否是加载更多样式
     * 
     * @return
     */
    public boolean isLoadMoreStyle() {
        return isLoadMoreStyle;
    }

    /**
     * 得到是否自动加载更多
     * 
     * @return
     */
    public boolean isAutoLoadMore() {
        return isAutoLoadMore;
    }

    /**
     * 设置是否自动加载更多
     * 
     * @param isAutoLoadMore
     */
    public void setAutoLoadMore(boolean isAutoLoadMore) {
        this.isAutoLoadMore = isAutoLoadMore;
    }

    /**
     * 设置还有更多
     * 
     * @param hasMore
     */
    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    /**
     * 得到是否有更多
     * 
     * @return
     */
    public boolean isHasMore() {
        return hasMore;
    }

    /**
     * 设置为CLICK_TO_REFRESH状态
     */
    private void setStatusClickToRefresh() {
        if (currentRefreshState != RefreshStatusEnum.CLICK_TO_REFRESH) {
            resetHeaderPadding();

            refreshViewImage.clearAnimation();
            refreshViewImage.setImageResource(R.drawable.drop_down_to_refresh_list_arrow);
            refreshViewImage.setVisibility(View.GONE);
            refreshViewProgress.setVisibility(View.GONE);
            refreshViewTipsText.setText(R.string.drop_down_to_refresh_list_refresh_view_tips);

            currentRefreshState = RefreshStatusEnum.CLICK_TO_REFRESH;
        }
    }

    /**
     * 设置为DROP_DOWN_TO_REFRESH状态
     */
    private void setStatusDropDownToRefresh() {
        if (currentRefreshState != RefreshStatusEnum.DROP_DOWN_TO_REFRESH) {
            refreshViewImage.setVisibility(View.VISIBLE);
            // CLICK_TO_REFRESH不需要启动动画
            if (currentRefreshState != RefreshStatusEnum.CLICK_TO_REFRESH) {
                refreshViewImage.clearAnimation();
                refreshViewImage.startAnimation(reverseFlipAnimation);
            }
            refreshViewProgress.setVisibility(View.GONE);
            refreshViewTipsText.setText(R.string.drop_down_to_refresh_list_pull_tips);

            if (isVerticalFadingEdgeEnabled()) {
                setVerticalScrollBarEnabled(false);
            }

            currentRefreshState = RefreshStatusEnum.DROP_DOWN_TO_REFRESH;
        }
    }

    /**
     * 设置为RELEASE_TO_REFRESH状态
     */
    private void setStatusReleaseToRefresh() {
        if (currentRefreshState != RefreshStatusEnum.RELEASE_TO_REFRESH) {
            refreshViewImage.setVisibility(View.VISIBLE);
            refreshViewImage.clearAnimation();
            refreshViewImage.startAnimation(flipAnimation);
            refreshViewProgress.setVisibility(View.GONE);
            refreshViewTipsText.setText(R.string.drop_down_to_refresh_list_release_tips);

            currentRefreshState = RefreshStatusEnum.RELEASE_TO_REFRESH;
        }
    }

    /**
     * 设置为REFRESHING状态
     */
    private void setStatusRefreshing() {
        if (currentRefreshState != RefreshStatusEnum.REFRESHING) {
            resetHeaderPadding();

            refreshViewImage.setVisibility(View.GONE);
            refreshViewImage.setImageDrawable(null);
            refreshViewProgress.setVisibility(View.VISIBLE);
            refreshViewTipsText.setText(R.string.drop_down_to_refresh_list_refreshing_tips);

            currentRefreshState = RefreshStatusEnum.REFRESHING;
            setSelection(0);
        }
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
                refreshViewLayout.setPadding(refreshViewLayout.getPaddingLeft(),
                                             (int)(((ev.getHistoricalY(i) - actionDownPointY) - headerOriginalHeight) / HEADER_PADDING_RATE),
                                             refreshViewLayout.getPaddingRight(),
                                             refreshViewLayout.getPaddingBottom());
            }
        }
    }

    /**
     * 重置header的padding
     */
    private void resetHeaderPadding() {
        refreshViewLayout.setPadding(refreshViewLayout.getPaddingLeft(), headerOriginalTopPadding,
                                     refreshViewLayout.getPaddingRight(),
                                     refreshViewLayout.getPaddingBottom());
    }

    /**
     * 测量View的宽度和高度
     * 
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
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

    /**
     * 得到属性
     * 
     * @param context
     * @param attrs
     */
    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs,
                                                       R.styleable.drop_down_to_refresh_list_attr);
        isDropDownToRefreshStyle = ta.getBoolean(R.styleable.drop_down_to_refresh_list_attr_is_drop_down_to_refresh_style,
                                                 true);
        isLoadMoreStyle = ta.getBoolean(R.styleable.drop_down_to_refresh_list_attr_is_load_more_style,
                                        true);
        isAutoLoadMore = ta.getBoolean(R.styleable.drop_down_to_refresh_list_attr_is_auto_load_more,
                                       false);
        ta.recycle();
    }
}
