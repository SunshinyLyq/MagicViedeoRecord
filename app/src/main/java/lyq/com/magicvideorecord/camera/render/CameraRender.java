package lyq.com.magicvideorecord.camera.render;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author liyuqing
 * @date 2018/12/14.
 * @description 
 * 主要用于管理各种滤镜，画面旋转，视频编码录制等
 */
public class CameraRender implements GLSurfaceView.Renderer{


    private SurfaceTexture mSurfaceTxure;//获取摄像头数据传递过来的帧数据内容

    private int[] mTextures = new int[1];
    private int[] fFrame = new int[1];
    private int[] fTexture =new int[1];
    private float[] mtx = new float[16]; // 变换矩阵

    /**
     * 预览数据的宽高
     */
    private int mPreviewWidth = 0, mPreviewHeight = 0;
    /**
     * 控件的宽高
     */
    private int width = 0, height = 0;


    public CameraRender(Resources resources) {
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //创建纹理id
        createTextureID();
        mSurfaceTxure = new SurfaceTexture(mTextures[0]);
    }

    private void createTextureID() {
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures[0]);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
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


    }

    @Override
    public void onDrawFrame(GL10 gl) {

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