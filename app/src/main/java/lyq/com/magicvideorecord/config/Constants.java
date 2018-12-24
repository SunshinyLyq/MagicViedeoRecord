package lyq.com.magicvideorecord.config;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * @author sunshiny
 * @date 2018/12/22.
 */
public class Constants {

    /**
     * 屏幕宽高
     */
    public static int screenWidth;
    public static int screenHeight;


    public static void init(Context context) {
        DisplayMetrics mDisplayMetrics = context.getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
    }
}
