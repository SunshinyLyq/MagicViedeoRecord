package lyq.com.magicvideorecord.config;

import android.app.Application;
import android.content.Context;

/**
 * @author sunshiny
 * @date 2018/12/22.
 */
public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        Constants.init(this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static Context getContext(){
        return mContext;
    }

}
