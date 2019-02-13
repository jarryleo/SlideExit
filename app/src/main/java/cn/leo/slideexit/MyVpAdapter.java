package cn.leo.slideexit;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by JarryLeo on 2017/5/6.
 */

public class MyVpAdapter extends PagerAdapter {
    String[] title = new String[]{"第一页", "第二页", "第三页"};

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TextView tv = new TextView(container.getContext());
        tv.setText(title[position]);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.YELLOW);
        container.addView(tv);
        return tv;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
