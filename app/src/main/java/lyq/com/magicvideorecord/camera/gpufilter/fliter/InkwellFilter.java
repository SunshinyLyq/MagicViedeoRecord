package lyq.com.magicvideorecord.camera.gpufilter.fliter;

import android.opengl.GLES20;

import lyq.com.magicvideorecord.R;
import lyq.com.magicvideorecord.camera.gpufilter.base.GPUImageFilter;
import lyq.com.magicvideorecord.config.MyApplication;
import lyq.com.magicvideorecord.utils.OpenGLUtils;

/**
 * @author sunshiny
 * @date 2018/12/30.
 * @desc
 */
public class InkwellFilter extends GPUImageFilter {

    private int[] vTexture = {-1};
    private int[] vTextureLocation = {-1};

    public InkwellFilter() {
        super(R.raw.gpu_base_vertex,R.raw.inkwell);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(vTexture.length, vTexture, 0);
        for (int i = 0; i < vTexture.length; i++) {
            vTexture[i] = -1;
        }
    }

    @Override
    protected void onDrawArraysPre() {
        for (int i = 0; i < vTexture.length && vTexture[i] != OpenGLUtils.NO_TEXTURE; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,vTexture[i]);
            GLES20.glUniform1i(vTextureLocation[i],(i+3));
        }
    }

    @Override
    protected void onDrawArraysAfter() {
        for (int i = 0; i < vTexture.length && vTexture[i] != OpenGLUtils.NO_TEXTURE; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }

    }

    @Override
    protected void onInit() {
        super.onInit();
        for (int i = 0; i < vTextureLocation.length; i++) {
            vTextureLocation[i] = GLES20.glGetUniformLocation(getProgramId(), "vTexture" + (2 + i));
        }
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                vTexture[0] = OpenGLUtils.loadTexture(MyApplication.getContext(), "filter/inkwellmap.png");
            }
        });
    }
}
