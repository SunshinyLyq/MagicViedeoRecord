package lyq.com.magicvideorecord.camera.fliter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import lyq.com.magicvideorecord.R;

/**
 * @author sunshiny
 * @date 2018/12/29.
 * @desc 获取到基本的filter，也就是需要显示到屏幕上去的filter
 * 从Camera的SurfaceTexture中进行采样，使用额外扩展的采样器
 */
public class OesFilter extends AbstractFilter {

    public OesFilter(Context context) {
        super(context, R.raw.oes_base_vertex, R.raw.oes_base_frag);
    }

    @Override
    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + getTextureType());
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId());
        GLES20.glUniform1i(mVTexture, getTextureType());
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
