package lyq.com.magicvideorecord.camera.gpufilter.base;

import android.opengl.GLES20;
import android.opengl.GLES30;

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
 * @desc 基础滤镜类
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
    protected boolean mIsInitialized;

    protected FloatBuffer mGLVertexBuffer; //顶点坐标buffer
    protected FloatBuffer mGLFragmentBuffer; //纹理坐标buffer

    protected int mOutputWidth, mOutputHeight;

    // FBO的宽高，可能跟输入的纹理大小不一致
    protected int mFrameWidth = -1;
    protected int mFrameHeight = -1;

    // FBO
    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;

    //滤镜是否可用
    private boolean mFilterEnable = true;


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
        mIsInitialized=true;
    }

    protected void onInitialized(){

    }

    public int getProgramId() {
        return mGLProgramId;
    }

    public final void destory(){
        mIsInitialized = false;
        GLES20.glDeleteProgram(mGLProgramId);
        onDestroy();
    }

    protected void onDestroy() {
    }

    public void onInputSizeChanged(int width,int height){
        mInputWidth = width;
        mInputHeight = height;
    }

    protected int onDrawFrame(int textureId){
        return this.onDrawFrame(textureId,mGLVertexBuffer,mGLFragmentBuffer);
    }

    public int onDrawFrameBuffer(int textureId){
        return this.onDrawFrameBuffer(textureId,mGLVertexBuffer,mGLFragmentBuffer);
    }

    public int onDrawFrame(int textureId, FloatBuffer vertexBuffer,
                           FloatBuffer textureBuffer) {
        //使用着色器
        GLES20.glUseProgram(mGLProgramId);
        //延迟绘画
        runPendingOnDrawTasks();
        if (!mIsInitialized){
            return OpenGLUtils.NOT_INIT;
        }

        //绘制纹理
        onDrawTexture(textureId,vertexBuffer,textureBuffer);

        return OpenGLUtils.ON_DRAWN;
    }

    public int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer,
                                 FloatBuffer textureBuffer){

        if (textureId == OpenGLUtils.NO_TEXTURE || mFrameBuffers == null
                || !mIsInitialized || !mFilterEnable) {

            return textureId;
        }

        // 绑定FBO
        GLES30.glViewport(0, 0, mFrameWidth, mFrameHeight);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers[0]);
        // 使用当前的program
        GLES30.glUseProgram(mGLProgramId);
        // 运行延时任务，这个要放在glUseProgram之后，要不然某些设置项会不生效
        runPendingOnDrawTasks();

        // 绘制纹理
        onDrawTexture(textureId, vertexBuffer, textureBuffer);

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];
    }


    /**
     * 创建FBO
     * @param width
     * @param height
     */
    public void initFrameBuffer(int width, int height) {
        if (!isInitialized()) {
            return;
        }
        if (mFrameBuffers != null && (mFrameWidth != width || mFrameHeight != height)) {
            destroyFrameBuffer();
        }
        if (mFrameBuffers == null) {
            mFrameWidth = width;
            mFrameHeight = height;
            mFrameBuffers = new int[1];
            mFrameBufferTextures = new int[1];
            OpenGLUtils.createFrameBuffer(mFrameBuffers, mFrameBufferTextures, width, height);
        }
    }

    /**
     * 销毁纹理
     */
    public void destroyFrameBuffer() {
        if (!mIsInitialized) {
            return;
        }
        if (mFrameBufferTextures != null) {
            GLES30.glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }

        if (mFrameBuffers != null) {
            GLES30.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
        mFrameWidth = -1;
        mFrameWidth = -1;
    }


    protected void onDrawTexture(int textureId, FloatBuffer vertexBuffer,
                                 FloatBuffer textureBuffer){
        //设置顶点缓冲区的起始位置
        vertexBuffer.position(0);
        //传值的过程
        GLES20.glVertexAttribPointer(mVPosition,2,GLES20.GL_FLOAT,false,0,vertexBuffer);
        GLES20.glEnableVertexAttribArray(mVPosition);
        GLES20.glVertexAttribPointer(mVCoord,2,GLES20.GL_FLOAT,false,0,textureBuffer);
        GLES20.glEnableVertexAttribArray(mVCoord);

        if (textureId != OpenGLUtils.NO_TEXTURE){
            //选择活动纹理单元
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

    public boolean isInitialized(){
        return mIsInitialized;
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

    protected void setInteger(final int location, final int intValue){
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location,intValue);
            }
        });
    }

    /**
     * 设置滤镜是否可用
     * @param enable
     */
    public void setFilterEnable(boolean enable) {
        mFilterEnable = enable;
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mIsInitialized) {
            GLES30.glDeleteProgram(mGLProgramId);
        }
        destroyFrameBuffer();
    }

}
