package lyq.com.magicvideorecord.camera.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import lyq.com.magicvideorecord.camera.fliter.AbstractFilter;
import lyq.com.magicvideorecord.camera.fliter.CameraFilter;
import lyq.com.magicvideorecord.camera.fliter.ShowFilter;
import lyq.com.magicvideorecord.camera.gpufilter.base.SlideGpuFilterGroup;
import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterItem;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.BeautyFilter;
import lyq.com.magicvideorecord.utils.MatrixUtils;
import lyq.com.magicvideorecord.utils.OpenGLUtils;

/**
 * @author sunshiny
 * @date 2018/12/14.
 * @description 主要用于管理各种滤镜，画面旋转，视频编码录制等
 * <p>
 * 绘制到fbo当中去，最后再通过fbo绘制到屏幕中去
 */
public class CameraRender implements GLSurfaceView.Renderer {

    private static final String TAG = "CameraRender";
    //写入到fbo中去
    private AbstractFilter cameraFilter;
    //没有无滤镜，默认情况下的,显示到屏幕上去
    private AbstractFilter showFilter;
    //滑动滤镜，滑动切换滤镜
    private SlideGpuFilterGroup mSlideGpuFilterGroup;
    private BeautyFilter mBeautyFilter;

    private Context context;
    private SurfaceTexture mSurfaceTxure;//获取摄像头数据传递过来的帧数据内容

    private float[] OM;
    private float[] mtx = new float[16]; // 变换矩阵
    private int textureID;
    /**
     * 预览数据的宽高
     */
    private int mPreviewWidth = 0, mPreviewHeight = 0;
    /**
     * 控件的宽高
     */
    private int width = 0, height = 0;

    private boolean recordingEnabled;
    private int recordingStatus;
    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;
    private static final int RECORDING_PAUSE = 3;
    private static final int RECORDING_RESUME = 4;
    private static final int RECORDING_PAUSED = 5;

    private float mSpeed;
    private int id;

    //创建fbo
    private int[] mFrameBuffers = new int[1];
    //fbo纹理
    private int[] mFrameBufferTextures = new int[1];

    public CameraRender(Context context) {
        this.context = context;
        mSlideGpuFilterGroup = new SlideGpuFilterGroup();

        //必须传入上下翻转的矩阵
        OM = MatrixUtils.getOriginalMatrix();
        MatrixUtils.flip(OM, false, true);//矩阵上下翻转
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        //在OpenGLThread中创建，不然那会出错
        cameraFilter = new CameraFilter(context);
        showFilter = new ShowFilter(context);

        mSlideGpuFilterGroup.init();
        //创建纹理id
        textureID = OpenGLUtils.createTextureID();
        mSurfaceTxure = new SurfaceTexture(textureID);
        cameraFilter.setTextureId(textureID);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;

        //清除遗留的
        GLES20.glDeleteFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);
        GLES20.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);
        /**创建fbo对象*/
        GLES20.glGenTextures(mFrameBuffers.length, mFrameBuffers, 0);
        //创建纹理并配置
        OpenGLUtils.glGenTextures(mFrameBufferTextures);
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        //创建2d的图像
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mPreviewWidth, mPreviewHeight,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        cameraFilter.setSize(mPreviewWidth,mPreviewHeight);
        mSlideGpuFilterGroup.onSizeChanged(mPreviewWidth,mPreviewHeight);

        //用来显示的矩阵,显示到屏幕上去
        MatrixUtils.getShowMatrix(mtx, mPreviewWidth, mPreviewHeight, width, height);
        showFilter.setMatrix(mtx);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        mSurfaceTxure.updateTexImage();
        OpenGLUtils.glBindFrameTexture(mFrameBuffers[0],mFrameBufferTextures[0]);
        GLES20.glViewport(0, 0, mPreviewWidth, mPreviewHeight);
        //先将SurfaceTexure中的YUV数据绘制到FBO中
        cameraFilter.draw();
        OpenGLUtils.glUnbindFrameBuffer();
        mSlideGpuFilterGroup.onDrawFrame(mFrameBufferTextures[0]);

        //显示到屏幕上去,需要重新给个输出的宽高
        GLES20.glViewport(0,0,width,height);

        //将FBO中的纹理绘制到屏幕中
        showFilter.setTextureId(mSlideGpuFilterGroup.getOutputTexture());
        showFilter.draw();

        // TODO: 2018/12/26 录制视频 
        if (recordingEnabled) {


        }
    }

    /**
     * 设置预览数据的size
     *
     * @param width
     * @param height
     */
    public void setPreviewSize(int width, int height) {
        if (mPreviewHeight != width || mPreviewHeight != height) {
            mPreviewWidth = width;
            mPreviewHeight = height;
        }
    }

    public SurfaceTexture getSurfaceTxure() {
        return mSurfaceTxure;
    }

    public void onResume(boolean auto) {
        //视频录制完成之后。将界面恢复成开始录制
        if (auto) {
            if (recordingStatus == RECORDING_PAUSED) {
                recordingStatus = RECORDING_RESUME;
            }
            return;
        }

        if (recordingStatus == RECORDING_PAUSED) {
            recordingStatus = RECORDING_RESUME;
        }
    }

    public void onPause(boolean auto) {
        if (auto) {
            // TODO: 2018/12/26  停止录制
            if (recordingStatus == RECORDING_ON) {
                recordingStatus = RECORDING_PAUSED;
            }
            return;
        }
        if (recordingStatus == RECORDING_ON) {
            recordingStatus = RECORDING_PAUSE;
        }

    }

    //开始录制
    public void startRecord() {
        recordingEnabled = true;
    }
    //触屏事件的传递
    public void onTouch(MotionEvent event){
        mSlideGpuFilterGroup.onTouchEvent(event);
    }
    //设置滤镜切换的监听
    public void setOnFilterChangeListener(SlideGpuFilterGroup.OnFilterChangeListener listener){
        mSlideGpuFilterGroup.setOnFilterChangeListener(listener);
    }
    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public void stopRecord() {
        recordingEnabled = false;
    }

    public void setCameraId(int cameraId) {
        cameraFilter.setFlag(cameraId);
    }

    //设置当前选择的滤镜
    public void setFilterSelect(FilterItem item) {
//        groupFilters.setFilter(FilterFactory.initFilters(item.filterType));
    }

    //美白磨皮
    public void changeBeautyLevel(int level) {

    }
}