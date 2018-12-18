package lyq.com.magicvideorecord.camera.fliter;

import android.content.Context;

import lyq.com.magicvideorecord.R;

/**
 * @author sunshiny
 * @date 2018/12/18.
 */
public class ScreenFilter extends AbstractFilter{


    public ScreenFilter(Context context) {
        super(context, R.raw.base_vertex,R.raw.base_frag);
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
