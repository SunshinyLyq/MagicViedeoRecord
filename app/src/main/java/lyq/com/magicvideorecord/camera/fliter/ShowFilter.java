package lyq.com.magicvideorecord.camera.fliter;

import android.content.Context;
import android.opengl.GLES20;

import lyq.com.magicvideorecord.R;

/**
 * @author sunshiny
 * @date 2018/12/29.
 * @desc  camerafilter 是往fbo中绘制，
 * 也就是往OpenGL中绘制，不需要采用额外的采样器，直接使用sampler2D就可以
 */
public class ShowFilter extends AbstractFilter {

    public ShowFilter(Context context) {
        super(context, R.raw.base_vertex,R.raw.base_frag);
    }


    @Override
    protected void onClear() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
