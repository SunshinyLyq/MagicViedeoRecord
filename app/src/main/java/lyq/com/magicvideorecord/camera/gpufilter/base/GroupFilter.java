package lyq.com.magicvideorecord.camera.gpufilter.base;

import android.opengl.GLES20;

import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterFactory;
import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterItem;
import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterType;
import lyq.com.magicvideorecord.utils.OpenGLUtils;

/**
 * @author sunshiny
 * @date 2019/1/29.
 * @desc
 */
public class GroupFilter {


    private int[] mFrameBuffers = new int[1];
    private int[] mFrameTextureBuffers = new int[1];
    private GPUImageFilter filter;

    public GroupFilter() {
        filter  = getFilter(FilterType.NONE);
    }

    public void setItem(FilterItem item) {
        filter = getFilter(item.filterType);
        init();
    }

    public void init() {
        filter.init();
    }

    private GPUImageFilter getFilter(FilterType type) {
        GPUImageFilter filter = FilterFactory.initFilters(type);

        if (filter == null) {
            filter = new GPUImageFilter();
        }
        return filter;
    }


    public void onSizeChanged(int width, int height) {

        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        OpenGLUtils.genTexturesWithParameter(1, mFrameTextureBuffers, 0, GLES20.GL_RGBA
                , width, height);
        onFilterSizeChanged(width, height);
    }

    private void onFilterSizeChanged(int width, int height) {
        filter.onInputSizeChanged(width, height);

        filter.onDisplaySizeChanged(width, height);
    }


    public void onDrawFrame(int textureId) {
        OpenGLUtils.glBindFrameTexture(mFrameBuffers[0], mFrameTextureBuffers[0]);
        filter.onDrawFrame(textureId);
        OpenGLUtils.glUnbindFrameBuffer();
    }

    public int getOutputTexture() {
        return mFrameTextureBuffers[0];
    }
}
