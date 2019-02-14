package cn.leo.swipe_back_lib;

import android.animation.IntEvaluator;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;

/**
 * @author : Jarry Leo
 * @date : 2019/2/13 9:51
 */
@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class SwipeBack extends FrameLayout implements Application.ActivityLifecycleCallbacks {
    private LinkedList<Activity> mActivities = new LinkedList<>();
    /**
     * 当前展示的activity的内容页面
     */
    private View mContentView;
    /**
     * 底下activity的view作为当前activity的背景
     */
    private Bitmap mBackViewBitmap;
    private Paint mPaint;
    private Paint mBitmapPaint;
    private ViewDragHelper mDragHelper;
    private IntEvaluator mEvaluator;
    private boolean mIsSwipeBack;
    /**
     * 是否边缘滑动
     */
    private boolean mEdge;

    private SwipeBack(@NonNull Context context, boolean edge) {
        super(context);
        mEdge = edge;
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mBitmapPaint = new Paint();
        mEvaluator = new IntEvaluator();
        mDragHelper = ViewDragHelper.create(this, 1.0f, mCallback);
        if (mEdge) {
            mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        }
        setBackgroundColor(Color.WHITE);
    }


    public static void init(Application application, boolean edge) {
        SwipeBack swipeBack = new SwipeBack(application, edge);
        application.registerActivityLifecycleCallbacks(swipeBack);
    }

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (mEdge) {
                return false;
            }
            boolean capture = child == mContentView && mActivities.size() > 1;
            if (capture && mBackViewBitmap == null) {
                getBackViewBitmap();
            }
            return capture;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //滑动边界1/3即关闭Activity
            int xDistance = getMeasuredWidth() / 3;
            if (mContentView == null) {
                return;
            }
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

            //刷新动画
            ViewCompat.postInvalidateOnAnimation(SwipeBack.this);
        }


        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left <= 0) {
                return 0;
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //绘制阴影刷新显示
            ViewCompat.postInvalidateOnAnimation(SwipeBack.this);
        }


        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mDragHelper.captureChildView(mContentView, pointerId);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            if (mEdge) {
                return 0;
            }
            return 100;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            if (mEdge) {
                return 0;
            }
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
            int left = mContentView.getLeft();
            if (Math.abs(left) == getMeasuredWidth()) {
                Activity last = mActivities.getLast();
                mIsSwipeBack = true;
                last.finish();
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
        int left = mContentView.getLeft();
        if (left > 0) {
            //绘制背景图
            if (mBackViewBitmap != null && !mBackViewBitmap.isRecycled()) {
                int width = mContentView.getWidth();
                int i = (width - left) / 4;
                Rect src = new Rect(i, 0, i + left, mBackViewBitmap.getHeight());
                Rect dst = new Rect(0, 0, left, mBackViewBitmap.getHeight());
                canvas.drawBitmap(mBackViewBitmap, src, dst, mBitmapPaint);
            } else {
                getBackViewBitmap();
            }
            //绘制阴影
            Integer evaluate = mEvaluator.evaluate(
                    mContentView.getLeft() * 1.0f / mContentView.getMeasuredWidth(),
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //左边阴影
            canvas.drawRect(0, 0, mContentView.getLeft(), getMeasuredHeight(), mPaint);
        }

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        mActivities.addLast(activity);
        recycleBitmap();
        if (!checkIgnore(activity)) {
            getViewToNewActivity();
        }
        activity.overridePendingTransition(R.anim.swipe_back_in_right, R.anim.swipe_back_out_left);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (mIsSwipeBack) {
            activity.overridePendingTransition(R.anim.swipe_back_in_left, R.anim.swipe_back_out_right);
            mIsSwipeBack = false;
        } else {
            activity.overridePendingTransition(R.anim.swipe_back_in_left_normal, R.anim.swipe_back_out_right_normal);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        boolean isTop = activity == mActivities.getLast();
        Activity secondLastActivity = getSecondActivity();
        boolean isSecond = activity == secondLastActivity;
        mActivities.remove(activity);
        if (isTop) {
            //从销毁的页面移除自身
            mContentView = null;
            recycleBitmap();
            ViewGroup decorView = getDecorView(activity);
            decorView.removeAllViews();
            if (checkIgnore(secondLastActivity)) {
                return;
            }
            //处理底下漏出的新页面
            resetViewToSecondActivity();
        } else if (isSecond) {
            recycleBitmap();
        }
    }

    /**
     * 把底部的页面还原回去
     * 并且把它下面的view拿上来
     */
    private void resetViewToSecondActivity() {
        if (mActivities.size() == 0) {
            recycleBitmap();
            return;
        }
        this.removeAllViews();
        Activity lastActivity = mActivities.getLast();
        ViewGroup decorView = getDecorView(lastActivity);
        if (mActivities.size() < 2) {
            recycleBitmap();
            return;
        }
        mContentView = getContentView(lastActivity);
        decorView.removeAllViews();
        this.addView(mContentView);
        //把本容器添加到Activity的父容器
        decorView.addView(this);
    }

    private Activity getSecondActivity() {
        if (mActivities.size() < 2) {
            return null;
        }
        return mActivities.get(mActivities.size() - 2);
    }

    /**
     * 把底部页面的布局添加到当前展示的activity的底下；
     */
    private void getViewToNewActivity() {
        if (mActivities.size() < 2) {
            return;
        }
        Activity lastActivity = mActivities.getLast();
        Activity secondLastActivity = getSecondActivity();
        //处理之前的activity
        this.removeAllViews();
        if (mContentView != null && secondLastActivity != null) {
            ViewGroup preDecorView = getDecorView(secondLastActivity);
            preDecorView.removeAllViews();
            preDecorView.addView(mContentView);
        }
        //处理现在的activity
        ViewGroup decorView = getDecorView(lastActivity);
        if (decorView.getChildCount() > 0) {
            //拿到Activity的contentView
            mContentView = getContentView(lastActivity);
            //把contentView添加到本容器
            decorView.removeAllViews();
            this.addView(mContentView);
            //把本容器添加到Activity的父容器
            decorView.addView(this);
        }
    }

    private ViewGroup getDecorView(Activity activity) {
        Window window = activity.getWindow();
        return (ViewGroup) window.getDecorView();
    }

    private View getContentView(Activity activity) {
        ViewGroup decorView = getDecorView(activity);
        return decorView.getChildAt(0);
    }

    private void getBackViewBitmap() {
        Activity activity = getSecondActivity();
        if (activity == null) {
            return;
        }
        View v = getContentView(activity);
        recycleBitmap();
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        v.draw(canvas);
        mBackViewBitmap = bmp;
    }

    private void recycleBitmap() {
        if (mBackViewBitmap != null) {
            mBackViewBitmap.recycle();
            mBackViewBitmap = null;
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnoreSwipeBack {
        // 有些自定义view在解绑时会跟本工具冲突(onPause后view空白)
        // 可以在activity上打上此注解关闭当前页面的滑动退出
    }

    private boolean checkIgnore(Activity activity) {
        if (activity == null) {
            return false;
        }
        Class<? extends Activity> a = activity.getClass();
        return a.isAnnotationPresent(IgnoreSwipeBack.class);
    }
}
