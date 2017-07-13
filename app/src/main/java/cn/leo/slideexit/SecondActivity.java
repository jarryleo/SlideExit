package cn.leo.slideexit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Window;

public class SecondActivity extends Activity {

    private ViewPager mVp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SlideExit.bind(this, SlideExit.SLIDE_RIGHT_EXIT );
        init();

    }

    private void init() {
        mVp = (ViewPager) findViewById(R.id.vp);
        mVp.setAdapter(new MyVpAdapter());
    }
}
