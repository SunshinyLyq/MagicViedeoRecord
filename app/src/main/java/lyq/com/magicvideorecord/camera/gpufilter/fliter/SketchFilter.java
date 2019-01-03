package lyq.com.magicvideorecord.camera.gpufilter.fliter;

import android.opengl.GLES20;

import lyq.com.magicvideorecord.R;
import lyq.com.magicvideorecord.camera.gpufilter.base.GPUImageFilter;

/**
 * @author sunshiny
 * @date 2019/1/3.
 * @desc 素描滤镜
 */
public class SketchFilter extends GPUImageFilter {
    private int mSingleStepOffsetLocation;
    //0.0 - 1.0
    private int mStrengthLocation;

    public SketchFilter(){
        super(R.raw.gpu_base_vertex, R.raw.sketch);
    }

    protected void onInit() {
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgramId(), "singleStepOffset");
        mStrengthLocation = GLES20.glGetUniformLocation(getProgramId(), "strength");
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {1.0f / w, 1.0f / h});
    }

    protected void onInitialized(){
        super.onInitialized();
        setFloat(mStrengthLocation, 0.5f);
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }

}
