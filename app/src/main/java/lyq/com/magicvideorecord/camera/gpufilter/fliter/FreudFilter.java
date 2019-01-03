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
public class FreudFilter extends GPUImageFilter {

    private int mTextureHeightUniformLocation;
    private int mTextureWidthUniformLocation;
    private int[] inputTextureHandles = {-1};
    private int[] inputTextureUniformLocations = {-1};
    private int mGLStrengthLocation;

    public FreudFilter() {
        super(R.raw.gpu_base_vertex, R.raw.freud);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(1, inputTextureHandles, 0);
        for (int i = 0; i < inputTextureHandles.length; i++) {
            inputTextureHandles[i] = -1;
        }
    }

    @Override
    protected void onDrawArraysPre() {
        for (int i = 0; i < inputTextureHandles.length
                 && inputTextureHandles[i] != OpenGLUtils.NO_TEXTURE; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,inputTextureHandles[i]);
            GLES20.glUniform1i(inputTextureUniformLocations[i],(i+3));
        }
    }

    @Override
    protected void onDrawArraysAfter() {
        for (int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGLUtils.NO_TEXTURE; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }
    }

    @Override
    protected void onInit() {
        super.onInit();
        inputTextureUniformLocations[0] = GLES20.glGetUniformLocation(getProgramId(),"vTexture2");
        mTextureHeightUniformLocation = GLES20.glGetUniformLocation(getProgramId(),"inputImageTextureHeight");
        mTextureWidthUniformLocation=GLES20.glGetUniformLocation(getProgramId(),"inputImageTextureWidth");
        mGLStrengthLocation = GLES20.glGetUniformLocation(getProgramId(),"strength");
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        setFloat(mGLStrengthLocation,1.0f);

        runOnDraw(new Runnable() {
            @Override
            public void run() {
                inputTextureHandles[0] = OpenGLUtils.loadTexture(MyApplication.getContext(),"filter/freud_rand.png");
            }
        });
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);

        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(mTextureWidthUniformLocation,(float) width);
                GLES20.glUniform1f(mTextureHeightUniformLocation,(float)height);
            }
        });
    }
}
