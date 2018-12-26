package lyq.com.magicvideorecord.camera.render;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.icu.text.UFormat;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import lyq.com.magicvideorecord.camera.fliter.ScreenFilter;
import lyq.com.magicvideorecord.utils.OpenGLUtils;

/**
 * @author sunshiny
 * @date 2018/12/14.
 * @description 主要用于管理各种滤镜，画面旋转，视频编码录制等
 */
public class CameraRender implements GLSurfaceView.Renderer {


    private Context context;
    private SurfaceTexture mSurfaceTxure;//获取摄像头数据传递过来的帧数据内容

    private float[] OM;

    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];
    private float[] mtx = new float[16]; // 变换矩阵

    /**
     * 显示画面的filter
     */
    private ScreenFilter screenFilter;
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


    public CameraRender(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //创建纹理id
        textureID = OpenGLUtils.createTextureID();
        mSurfaceTxure = new SurfaceTexture(textureID);

        screenFilter = new ScreenFilter(context);
        screenFilter.setTextureId(textureID);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;

        //清除遗留的
//        GLES20.glDeleteFramebuffers(fFrame.length,fFrame,0);
//        GLES20.glDeleteTextures(fTexture.length,fTexture,0);

        /**创建一个帧缓冲区对象*/
//        GLES20.glGenTextures(fFrame.length,fFrame,0);
//        GLES20.glGenTextures(fTexture.length,fTexture,0);

//        OpenGLUtils.getShowMatrix(mtx,mPreviewWidth,mPreviewHeight,width,height);
//        screenFilter.setMatrix(mtx);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mSurfaceTxure.updateTexImage();
        mSurfaceTxure.getTransformMatrix(mtx);
        GLES20.glViewport(0, 0, width, height);
        screenFilter.setMatrix(mtx);
        screenFilter.draw();

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

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public void stopRecord() {
        recordingEnabled = false;
    }

}