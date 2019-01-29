package lyq.com.magicvideorecord.camera.gpufilter.Beauty;

import android.opengl.GLES20;

import lyq.com.magicvideorecord.R;
import lyq.com.magicvideorecord.camera.gpufilter.base.GPUImageFilter;

/**
 * @author sunshiny
 * @date 2019/1/29.
 * @desc
 */
public class BeautyFilter extends GPUImageFilter {

    private int mWidth;
    private int mHeight;

    private int mIntensityHandle; // 磨皮程度 0.0 ~ 1.0
    private float mIntensity;

    public BeautyFilter() {
        super(R.raw.gpu_base_vertex,R.raw.beauty_frag);
    }

    @Override
    protected void onInit() {
        super.onInit();

        mWidth = GLES20.glGetUniformLocation(mGLProgramId,"width");
        mHeight = GLES20.glGetUniformLocation(mGLProgramId,"height");
        mIntensityHandle = GLES20.glGetUniformLocation(mGLProgramId,"intensity");
        mIntensity = 0.5f;
    }


    @Override
    protected void onInitialized() {
        super.onInitialized();
        setFloat(mIntensityHandle,mIntensity);
        setInteger(mWidth,mOutputWidth);
        setInteger(mHeight,mOutputHeight);
    }

    //设置磨皮程度
    public void setSkinBeautyIntensity(float intensity){
        mIntensity = intensity;
    }

    public void onSizeChanged(int width, int height) {
        onInputSizeChanged(width,height);
        initFrameBuffer(width,height);
        onDisplaySizeChanged(width,height);

    }


}
