package cn.leo.slideexit;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by JarryLeo on 2017/5/6.
 */

public class MyViewPager extends ViewPager {

    private float mDownX;

    public MyViewPager(Context context) {
        this(context, null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int item = getCurrentItem();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                mDownX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                if ((x > mDownX && item == 0) ||
                        x < mDownX && item == getAdapter().getCount() - 1) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
