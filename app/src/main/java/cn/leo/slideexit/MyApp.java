package cn.leo.slideexit;

import android.app.Application;

/**
 * @author : Jarry Leo
 * @date : 2019/2/13 11:21
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SwipeBack.init(this);
    }
}
