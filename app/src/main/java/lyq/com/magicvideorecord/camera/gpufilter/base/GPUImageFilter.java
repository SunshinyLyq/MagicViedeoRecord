package lyq.com.magicvideorecord.camera.gpufilter.base;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import lyq.com.magicvideorecord.R;
import lyq.com.magicvideorecord.utils.OpenGLUtils;
import lyq.com.magicvideorecord.utils.RatationUtils;

/**
 * @author sunshiny
 * @date 2018/12/30.
 * @desc
 */
public class GPUImageFilter {
    private final LinkedList<Runnable> mRunOnDraw;
    private final String mVertexShader;
    private final String mFragmentShader;

    protected int mGLProgramId;
    protected int mVPosition;
    protected int mVCoord;
    protected int mVTexture;

    protected int mInputWidth;
    protected int mInputHeight;
    protected boolean mIsInitialzed;

    protected FloatBuffer mGLVertexBuffer; //顶点坐标buffer
    protected FloatBuffer mGLFragmentBuffer; //纹理坐标buffer

    protected int mOutputWidth, mOutputHeight;


    public GPUImageFilter() {
        this(R.raw.gpu_base_vertex,R.raw.gpu_base_frag);
    }
    public GPUImageFilter(int vertexShader, int fragmentShader) {
        this.mRunOnDraw = new LinkedList<>();
        mVertexShader = OpenGLUtils.readRawTextFile(vertexShader);
        mFragmentShader = OpenGLUtils.readRawTextFile(fragmentShader);

        mGLVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLVertexBuffer.put(RatationUtils.VERTEX).position(0);
        mGLFragmentBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLFragmentBuffer.put(RatationUtils.TEXTURE_NO_ROTATION).position(0);
    }

    public void init(){
        onInit();
        onInitialized();
    }

    protected void onInit() {
        mGLProgramId = OpenGLUtils.loadProgram(mVertexShader,mFragmentShader);
        mVPosition = GLES20.glGetAttribLocation(mGLProgramId,"vPosition");
        mVCoord = GLES20.glGetAttribLocation(mGLProgramId,"vCoord");
        mVTexture =GLES20.glGetUniformLocation(mGLProgramId,"vTexture");
        mIsInitialzed=true;
    }

    protected void onInitialized(){

    }

    public int getProgramId() {
        return mGLProgramId;
    }

    public final void destory(){
        mIsInitialzed = false;
        GLES20.glDeleteProgram(mGLProgramId);
        onDestroy();
    }

    protected void onDestroy() {
    }

    public void onInputSizeChanged(int width,int height){
        mInputWidth = width;
        mInputHeight = height;
    }

    public int onDrawFrame(int textureId){
        return this.onDrawFrame(textureId,mGLVertexBuffer,mGLFragmentBuffer);
    }

    private int onDrawFrame(int textureId, FloatBuffer vertexBuffer,
                             FloatBuffer textureBuffer) {
        GLES20.glUseProgram(mGLProgramId);
        runPendingOnDrawTasks();
        if (!mIsInitialzed){
            return OpenGLUtils.NOT_INIT;
        }
        vertexBuffer.position(0);
        //传值的过程
        GLES20.glVertexAttribPointer(mVPosition,2,GLES20.GL_FLOAT,false,0,vertexBuffer);
        GLES20.glEnableVertexAttribArray(mVPosition);
        GLES20.glVertexAttribPointer(mVCoord,2,GLES20.GL_FLOAT,false,0,textureBuffer);
        GLES20.glEnableVertexAttribArray(mVCoord);

        if (textureId != OpenGLUtils.NO_TEXTURE){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
            GLES20.glUniform1i(mVTexture,0);
        }

        onDrawArraysPre();

        //绘画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        //解绑
        GLES20.glDisableVertexAttribArray(mVPosition);
        GLES20.glDisableVertexAttribArray(mVCoord);
        onDrawArraysAfter();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        return OpenGLUtils.ON_DRAWN;
    }

    protected void onDrawArraysPre() {
    }
    protected void onDrawArraysAfter() {
    }


    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()){
            mRunOnDraw.removeFirst().run();
        }
    }

    public boolean isInitialzed(){
        return mIsInitialzed;
    }

    protected void runOnDraw(final Runnable runnable){
        synchronized (mRunOnDraw){
            mRunOnDraw.addLast(runnable);
        }
    }

    public void onDisplaySizeChanged(int width,int height){
        mOutputWidth = width;
        mOutputHeight = height;
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }


}
