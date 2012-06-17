package com.trinea.common.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * 能够响应各个方向CompoundDrawables点击操作的TextView
 * <ul>
 * 替代TextView使用，使用方法如下
 * <li>xml中配置同TextView</li>
 * <li>设置{@link CompoundDrawablesTextView#setDrawableClickListener(DrawableClickListener)}，实现各个方向图片点击的响应</li>
 * <li>可以设置{@link CompoundDrawablesTextView#setLazy(int, int)}分别表示x和y方向允许的误差，正数表示点击范围向外扩展，负数表示点击范围向内收缩</li>
 * <li>可以设置{@link CompoundDrawablesTextView#setConsumeEvent(boolean)}，true表示在相应图片有效范围内处理后就不再向下传递事件，false表示继续传递</li>
 * </ul>
 * <ul>
 * <strong>注意</strong>
 * <li>若点击的位置同时在多个图片的有效范围内，响应顺序为左上右下，设置{@link CompoundDrawablesTextView#setConsumeEvent(boolean)}为true后就按顺序响应第一个</li>
 * </ul>
 * 
 * @author Trinea 2012-5-3 下午04:47:39
 */
public class CompoundDrawablesTextView extends TextView {

    private Drawable              leftDrawable;
    private Drawable              topDrawable;
    private Drawable              rightDrawable;
    private Drawable              bottomDrawable;

    private boolean               isLeftTouched   = false;
    private boolean               isToptTouched   = false;
    private boolean               isRightTouched  = false;
    private boolean               isBottomTouched = false;

    // 是否消费事件，若为true，表示自己消费，否则向下传递
    private boolean               isConsumeEvent  = true;
    // x(y)方向扩展范围，表示图片x(y)方向的此范围内的点击都被接受
    private int                   lazyX           = 0, lazyY = 0;

    // 图片点击的监听器
    private DrawableClickListener clickListener;

    private boolean               isMove          = false;

    public CompoundDrawablesTextView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    public CompoundDrawablesTextView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public CompoundDrawablesTextView(Context context){
        super(context);
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        leftDrawable = left;
        topDrawable = top;
        rightDrawable = right;
        bottomDrawable = bottom;
        super.setCompoundDrawables(left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.e("CompoundDrawablesTextView",
              event.getAction() == 0 ? "ACTION_DOWN" : (event.getAction() == 1 ? "ACTION_UP" : "" + event.getAction()));
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                isMove = true;
                break;
            case MotionEvent.ACTION_UP:
                if (!isMove && clickListener != null) {
                    boolean finish = clickLeftDrawable(event) && clickTopDrawable(event) && clickRightDrawable(event)
                                     && clickBottomDrawable(event);
                }
            default:
                isMove = false;
        }
        return super.onTouchEvent(event);
    }

    public void setOnClickListener(OnClickListener l) {
        if (l == null) {
            
        }
        super.setOnClickListener(l);
        mOnClickListener = l;
        new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("CompoundDrawablesTextView", "click");

            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        rightDrawable = null;
        bottomDrawable = null;
        leftDrawable = null;
        topDrawable = null;
        super.finalize();
    }

    /**
     * 点击左边的Drawable
     * 
     * @param event
     * @param leftDrawable
     * @return
     */
    private boolean clickLeftDrawable(MotionEvent event) {
        if (leftDrawable != null) {
            // 计算图片点击可响应的范围
            int drawHeight = leftDrawable.getIntrinsicHeight();
            int drawWidth = leftDrawable.getIntrinsicWidth();
            int topBottomDis = (topDrawable == null ? 0 : topDrawable.getIntrinsicHeight())
                               - (bottomDrawable == null ? 0 : bottomDrawable.getIntrinsicHeight());
            double imageCenterY = 0.5 * (this.getHeight() + topBottomDis);
            Rect imageBounds = new Rect(this.getCompoundDrawablePadding() - lazyX, (int)(imageCenterY - 0.5
                                                                                         * drawHeight - lazyY),
                                        this.getCompoundDrawablePadding() + drawWidth + lazyX,
                                        (int)(imageCenterY + 0.5 * drawHeight + lazyY));
            if (imageBounds.contains((int)event.getX(), (int)event.getY())) {
                clickListener.onClick(DrawableClickListener.DrawablePosition.LEFT);
                if (isConsumeEvent) {
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 点击上边的Drawable
     * 
     * @param event
     * @param leftDrawable
     * @return
     */
    private boolean clickTopDrawable(MotionEvent event) {
        if (topDrawable != null) {
            int drawHeight = topDrawable.getIntrinsicHeight();
            int drawWidth = topDrawable.getIntrinsicWidth();
            int leftRightDis = (leftDrawable == null ? 0 : leftDrawable.getIntrinsicWidth())
                               - (rightDrawable == null ? 0 : rightDrawable.getIntrinsicWidth());
            double imageCenterX = 0.5 * (this.getWidth() + leftRightDis);
            Rect imageBounds = new Rect((int)(imageCenterX - 0.5 * drawWidth - lazyX),
                                        this.getCompoundDrawablePadding() - lazyY,
                                        (int)(imageCenterX + 0.5 * drawWidth + lazyX),
                                        this.getCompoundDrawablePadding() + drawHeight + lazyY);
            if (imageBounds.contains((int)event.getX(), (int)event.getY())) {
                clickListener.onClick(DrawableClickListener.DrawablePosition.TOP);
                if (isConsumeEvent) {
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 点击右边的Drawable
     * 
     * @param event
     * @param leftDrawable
     * @return
     */
    private boolean clickRightDrawable(MotionEvent event) {
        if (rightDrawable != null) {
            int drawHeight = rightDrawable.getIntrinsicHeight();
            int drawWidth = rightDrawable.getIntrinsicWidth();
            int topBottomDis = (topDrawable == null ? 0 : topDrawable.getIntrinsicHeight())
                               - (bottomDrawable == null ? 0 : bottomDrawable.getIntrinsicHeight());
            double imageCenterY = 0.5 * (this.getHeight() + topBottomDis);
            Rect imageBounds = new Rect(this.getWidth() - this.getCompoundDrawablePadding() - drawWidth - lazyX,
                                        (int)(imageCenterY - 0.5 * drawHeight - lazyY),
                                        this.getWidth() - this.getCompoundDrawablePadding() + lazyX,
                                        (int)(imageCenterY + 0.5 * drawHeight + lazyY));
            if (imageBounds.contains((int)event.getX(), (int)event.getY())) {
                clickListener.onClick(DrawableClickListener.DrawablePosition.RIGHT);
                if (isConsumeEvent) {
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 点击下边的Drawable
     * 
     * @param event
     * @param leftDrawable
     * @return
     */
    private boolean clickBottomDrawable(MotionEvent event) {
        if (bottomDrawable != null) {
            int drawHeight = bottomDrawable.getIntrinsicHeight();
            int drawWidth = bottomDrawable.getIntrinsicWidth();
            int leftRightDis = (leftDrawable == null ? 0 : leftDrawable.getIntrinsicWidth())
                               - (rightDrawable == null ? 0 : rightDrawable.getIntrinsicWidth());
            double imageCenterX = 0.5 * (this.getWidth() + leftRightDis);
            Rect imageBounds = new Rect((int)(imageCenterX - 0.5 * drawWidth - lazyX),
                                        this.getHeight() - this.getCompoundDrawablePadding() - drawHeight - lazyY,
                                        (int)(imageCenterX + 0.5 * drawWidth + lazyX),
                                        this.getHeight() - this.getCompoundDrawablePadding() + lazyY);
            if (imageBounds.contains((int)event.getX(), (int)event.getY())) {
                clickListener.onClick(DrawableClickListener.DrawablePosition.BOTTOM);
                if (isConsumeEvent) {
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 图片点击的监听器
     * 
     * @author Trinea 2012-5-3 下午11:45:41
     */
    public interface DrawableClickListener {

        public static enum DrawablePosition {
            TOP, BOTTOM, LEFT, RIGHT
        };

        /**
         * 点击相应位置的响应函数
         * 
         * @param position
         */
        public void onClick(DrawablePosition position);
    }

    /**
     * 得到isConsumeEvent
     * 
     * @return the isConsumeEvent
     */
    public boolean isConsumeEvent() {
        return isConsumeEvent;
    }

    /**
     * 设置isConsumeEvent
     * 
     * @param isConsumeEvent
     */
    public void setConsumeEvent(boolean isConsumeEvent) {
        this.isConsumeEvent = isConsumeEvent;
    }

    /**
     * 得到lazyX
     * 
     * @return the lazyX
     */
    public int getLazyX() {
        return lazyX;
    }

    /**
     * 设置lazyX
     * 
     * @param lazyX
     */
    public void setLazyX(int lazyX) {
        this.lazyX = lazyX;
    }

    /**
     * 得到lazyY
     * 
     * @return the lazyY
     */
    public int getLazyY() {
        return lazyY;
    }

    /**
     * 设置lazyY
     * 
     * @param lazyY
     */
    public void setLazyY(int lazyY) {
        this.lazyY = lazyY;
    }

    /**
     * 设置lazyX、lazyY
     * 
     * @param lazyX
     * @param lazyY
     */
    public void setLazy(int lazyX, int lazyY) {
        this.lazyX = lazyX;
        this.lazyY = lazyY;
    }

    /**
     * 设置图片点击的监听器
     * 
     * @param listener
     */
    public void setDrawableClickListener(DrawableClickListener listener) {
        this.clickListener = listener;
    }
}
