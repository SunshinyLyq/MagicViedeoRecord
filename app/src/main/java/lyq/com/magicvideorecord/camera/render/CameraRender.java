package lyq.com.magicvideorecord.camera.render;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
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
 * @description 
 * 主要用于管理各种滤镜，画面旋转，视频编码录制等
 */
public class CameraRender implements GLSurfaceView.Renderer{


    private Context context;
    private SurfaceTexture mSurfaceTxure;//获取摄像头数据传递过来的帧数据内容

    private int[] fFrame = new int[1];
    private int[] fTexture =new int[1];
    private float[] mtx = new float[16]; // 变换矩阵

    /**显示画面的filter*/
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


    public CameraRender(Context context) {
        this.context =context;
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
        GLES20.glDeleteFramebuffers(fFrame.length,fFrame,0);
        GLES20.glDeleteTextures(fTexture.length,fTexture,0);

        /**创建一个帧缓冲区对象*/
        GLES20.glGenTextures(fFrame.length,fFrame,0);
        GLES20.glGenTextures(fTexture.length,fTexture,0);

        OpenGLUtils.getShowMatrix(mtx,mPreviewWidth,mPreviewHeight,width,height);
        screenFilter.setMatrix(mtx);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        mSurfaceTxure.updateTexImage();
        mSurfaceTxure.getTransformMatrix(mtx);
        GLES20.glViewport(0,0,width,height);
        screenFilter.draw();
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

}