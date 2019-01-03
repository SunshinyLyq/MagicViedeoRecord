package lyq.com.magicvideorecord.camera.gpufilter.fliter;

import android.opengl.GLES20;

import lyq.com.magicvideorecord.R;
import lyq.com.magicvideorecord.camera.gpufilter.base.GPUImageFilter;

/**
 * @author sunshiny
 * @date 2019/1/3.
 * @desc
 */
public class BeautyFilter extends GPUImageFilter {

    private int mIntensity;
    private int mWidth;
    private int mHeight;
    private int mLevel;

    public BeautyFilter() {
        super(R.raw.gpu_base_vertex, R.raw.beauty_frag);
    }

    @Override
    protected void onInit() {
        super.onInit();
        mIntensity = GLES20.glGetUniformLocation(getProgramId(), "intensity");
        mWidth = GLES20.glGetUniformLocation(getProgramId(), "width");
        mHeight = GLES20.glGetUniformLocation(getProgramId(), "height");
        setBeautyLevel(50);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        
        setInteger(mWidth,width);
        setInteger(mHeight,height);
    }

    private void setBeautyLevel(int level) {
        mLevel = level;
    }

    public int getBeautyLeve(){
        return mLevel;
    }
}


