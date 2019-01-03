package lyq.com.magicvideorecord.camera.gpufilter.base;

import android.opengl.GLES20;

import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterFactory;
import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterType;
import lyq.com.magicvideorecord.utils.OpenGLUtils;

/**
 * @author sunshiny
 * @date 2019/1/3.
 * @desc
 */
public class BaseGroupFilters extends GPUImageFilter {

    protected static int[] frameBuffers = new int[1];
    protected static int[] frameBufferTextures = new int[1];
    private int frameWidth = -1;
    private int frameHeight = -1;
    protected GPUImageFilter filter;

    public BaseGroupFilters() {
        filter = FilterFactory.initFilters(FilterType.ANTIQUE);
    }

    public void setFilter(GPUImageFilter filter) {
        this.filter = filter;
    }

    @Override
    public void onDestroy() {
        if (filter != null) {
            filter.destory();
            destroyFramebuffers();
        }
    }

    @Override
    public void init() {
        if (filter != null) {
            filter.init();
        }
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);

        if (frameBuffers != null && (frameWidth != width || frameHeight != height)) {
            destroyFramebuffers();
            frameWidth = width;
            frameHeight = height;
        }
            GLES20.glGenFramebuffers(1, frameBuffers, 0);
            OpenGLUtils.genTexturesWithParameter(1, frameBufferTextures, 0, GLES20.GL_RGBA
                    , width, height);
    }

    @Override
    public int onDrawFrame(final int textureId) {
        if (frameBuffers == null || frameBufferTextures == null) {
            return OpenGLUtils.NOT_INIT;
        }

        OpenGLUtils.glBindFrameTexture(frameBuffers[0],frameBufferTextures[0]);
        GLES20.glViewport(0, 0, mInputWidth, mInputHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glClearColor(0, 0, 0, 0);
        filter.onDrawFrame(textureId);
        OpenGLUtils.glUnbindFrameBuffer();

        return OpenGLUtils.ON_DRAWN;
    }

    private void destroyFramebuffers() {
        if (frameBufferTextures != null) {
            GLES20.glDeleteTextures(frameBufferTextures.length, frameBufferTextures, 0);
            frameBufferTextures = null;
        }
        if (frameBuffers != null) {
            GLES20.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
            frameBuffers = null;
        }
    }

    public int getOutputTexture() {
        return frameBuffers[0];
    }

}
