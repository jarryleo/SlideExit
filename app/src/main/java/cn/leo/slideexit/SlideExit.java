package cn.leo.slideexit;

import android.animation.IntEvaluator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

/**
 * Created by JarryLeo on 2017/5/6.
 */

public class SlideExit extends FrameLayout {
    //支持往四边滑动关闭Activity

    public static final int SLIDE_LEFT_EXIT = 1 << 0;
    public static final int SLIDE_RIGHT_EXIT = 1 << 1;
    public static final int SLIDE_UP_EXIT = 1 << 2;
    public static final int SLIDE_DOWN_EXIT = 1 << 3;
    private ViewDragHelper mDragHelper;
    private View mContentView;
    private int mSide;
    private Paint mPaint;
    private Activity mActivity;
    private IntEvaluator mEvaluator;

    public SlideExit(Context context) {
        this(context, null);
    }

    public SlideExit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideExit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mEvaluator = new IntEvaluator();
        mDragHelper = ViewDragHelper.create(this, mCallback);
    }

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {


        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //捕获子容器
            return child == mContentView;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //滑动边界1/4即关闭Activity
            float xDistance = getMeasuredWidth() / 3;
            float yDistance = getMeasuredHeight() / 4;
            //左右超过边界处理
            if (mContentView.getLeft() != 0) {
                if (mContentView.getLeft() < -xDistance) {
                    mDragHelper.smoothSlideViewTo(mContentView, -getMeasuredWidth(), 0);
                } else if (mContentView.getLeft() > xDistance) {
                    mDragHelper.smoothSlideViewTo(mContentView, getMeasuredWidth(), 0);
                } else {
                    //未超过则回弹
                    mDragHelper.smoothSlideViewTo(mContentView, 0, 0);
                }
            }
            //上下超过边界
            if (mContentView.getTop() != 0) {
                if (mContentView.getTop() < -yDistance) {
                    mDragHelper.smoothSlideViewTo(mContentView, 0, -getMeasuredHeight());
                } else if (mContentView.getTop() > yDistance) {
                    mDragHelper.smoothSlideViewTo(mContentView, 0, getMeasuredHeight());
                } else {
                    //未超过则回弹
                    mDragHelper.smoothSlideViewTo(mContentView, 0, 0);
                }
            }
            //刷新动画
            ViewCompat.postInvalidateOnAnimation(SlideExit.this);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //上滑
            if ((mSide & SLIDE_UP_EXIT) == SLIDE_UP_EXIT && top < 0) {
                return top;
            }
            //下滑
            if ((mSide & SLIDE_DOWN_EXIT) == SLIDE_DOWN_EXIT && top > 0) {
                return top;
            }
            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //左滑
            if ((mSide & SLIDE_LEFT_EXIT) == SLIDE_LEFT_EXIT && left < 0) {
                return left;
            }
            //右滑
            if ((mSide & SLIDE_RIGHT_EXIT) == SLIDE_RIGHT_EXIT && left > 0) {
                return left;
            }
            return 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //绘制阴影刷新显示
            ViewCompat.postInvalidateOnAnimation(SlideExit.this);
        }


        @Override
        public int getViewHorizontalDragRange(View child) {
            return 100;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 100;
        }
    };

    @Override
    public void computeScroll() {
        //滑动动画处理
        if (mDragHelper.continueSettling(true)) {
            //刷新显示
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            //滑动结束,关闭Activity
            if (Math.abs(mContentView.getLeft()) == getMeasuredWidth()
                    || Math.abs(mContentView.getTop()) == getMeasuredHeight()) {
                mActivity.finish();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //交给ViewDragHelper处理拦截事件
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //交给ViewDragHelper处理滑动事件
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        //绘制阴影
        if (mContentView.getTop() > 0) {
            Integer evaluate = mEvaluator.evaluate(mContentView.getTop() / mContentView.getMeasuredHeight() * 1.0f,
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //上边阴影
            canvas.drawRect(0, 0, mContentView.getMeasuredWidth(),
                    mContentView.getTop(), mPaint);
        } else if (mContentView.getTop() < 0) {
            Integer evaluate = mEvaluator.evaluate(-mContentView.getTop() / mContentView.getMeasuredHeight() * 1.0f,
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //下边阴影
            canvas.drawRect(0, mContentView.getMeasuredHeight() + mContentView.getTop(),
                    getMeasuredWidth(), getMeasuredHeight(), mPaint);
        }

        if (mContentView.getLeft() > 0) {
            Integer evaluate = mEvaluator.evaluate(mContentView.getLeft() * 1.0f / mContentView.getMeasuredWidth(),
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //左边阴影
            canvas.drawRect(0, 0, mContentView.getLeft(),
                    getMeasuredHeight(), mPaint);
        } else if (mContentView.getLeft() < 0) {
            Integer evaluate = mEvaluator.evaluate(-mContentView.getLeft() / mContentView.getMeasuredWidth() * 1.0f,
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //右边阴影
            canvas.drawRect(mContentView.getLeft() + getMeasuredWidth(), 0,
                    getMeasuredWidth(), getMeasuredHeight(), mPaint);
        }
    }

    public static void bind(Activity activity, int slide_side) {
        //创建本类对象并绑定Activity
        new SlideExit(activity).attach(activity, slide_side);
    }


    private void attach(Activity activity, int slide_side) {
        //滑动关闭方向
        mActivity = activity;
        mSide = slide_side;
        //获取Activity布局的父容器
        Window window = activity.getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();

        if (decorView.getChildCount() > 0) {
            //拿到Activity的contentView
            mContentView = decorView.getChildAt(0);
            decorView.removeAllViews();
            //把contentView添加到本容器
            this.removeAllViews();
            this.addView(mContentView);
            //把本容器添加到Activity的父容器
            decorView.addView(this);
        }
    }
}
